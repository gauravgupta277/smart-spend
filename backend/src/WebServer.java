// Phase 5: Minimal Built-in Java Web Server + MySQL Persistence
// Uses Java standard library (com.sun.net.httpserver) + JDBC Persistence!
// Connects our custom DSA classes directly to the Minimalist Web Dashboard.

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class WebServer {
    private static CustomLinkedList expenseList = new CustomLinkedList();
    private static CustomStack undoStack = new CustomStack();
    private static CustomQueue billQueue = new CustomQueue();
    private static CategoryTree categoryTree = new CategoryTree();
    private static FriendGraph friendGraph = new FriendGraph();
    private static List<Bill> allBillsList = new ArrayList<>();

    private static int nextTransId = 105;
    private static int nextBillId = 205;

    public static void main(String[] args) throws IOException {
        initSampleData();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static Files Handler
        server.createContext("/", new StaticFileHandler());

        // API Endpoints
        server.createContext("/api/state", new StateHandler());
        server.createContext("/api/expenses", new ExpensesHandler());
        server.createContext("/api/undo", new UndoHandler());
        server.createContext("/api/bills/pay", new PayBillHandler());
        server.createContext("/api/bills/add", new AddBillHandler());
        server.createContext("/api/graph/path", new GraphPathHandler());
        server.createContext("/api/sort", new SortHandler());
        server.createContext("/api/greedy", new GreedyHandler());

        server.setExecutor(null); // default executor
        System.out.println("=============================================================");
        System.out.println("  SMART SPEND ROYAL WEB DASHBOARD IS RUNNING!");
        System.out.println("  Database: " + (DatabaseManager.isConnected() ? "MySQL Connected" : "In-Memory Fallback"));
        System.out.println("  Open in Browser: http://localhost:" + port);
        System.out.println("=============================================================");
        server.start();
    }

    private static void initSampleData() {
        // Try loading from MySQL first if connected
        List<Transaction> dbTrans = DatabaseManager.fetchAllTransactions();
        List<Bill> dbBills = DatabaseManager.fetchAllBills();

        if (DatabaseManager.isConnected() && !dbTrans.isEmpty()) {
            for (Transaction t : dbTrans) {
                expenseList.insertAtHead(t);
                if (t.id >= nextTransId) nextTransId = t.id + 1;
            }
        } else {
            // Seed default demo transactions
            Transaction t1 = new Transaction(101, "Grocery Shopping", "Food", 2450.0);
            Transaction t2 = new Transaction(102, "Metro Smart Card", "Travel", 500.0);
            Transaction t3 = new Transaction(103, "Netflix Subscription", "Entertainment", 799.0);
            Transaction t4 = new Transaction(104, "Electricity Bill", "Utilities", 3200.0);

            expenseList.insertAtHead(t1);
            expenseList.insertAtHead(t2);
            expenseList.insertAtHead(t3);
            expenseList.insertAtHead(t4);
            undoStack.push("ADD", t4);

            if (DatabaseManager.isConnected()) {
                DatabaseManager.insertTransaction(t1);
                DatabaseManager.insertTransaction(t2);
                DatabaseManager.insertTransaction(t3);
                DatabaseManager.insertTransaction(t4);
            }
        }

        if (DatabaseManager.isConnected() && !dbBills.isEmpty()) {
            for (Bill b : dbBills) {
                billQueue.enqueue(b);
                allBillsList.add(b);
                if (b.id >= nextBillId) nextBillId = b.id + 1;
            }
        } else {
            // Seed default demo bills
            Bill b1 = new Bill(201, "Apartment Rent", 12000.0, "2026-10-05", 5);
            Bill b2 = new Bill(202, "Wi-Fi Broadband", 999.0, "2026-10-07", 4);
            Bill b3 = new Bill(203, "Mobile Postpaid", 499.0, "2026-10-10", 3);
            Bill b4 = new Bill(204, "Gym Membership", 1500.0, "2026-10-15", 2);

            billQueue.enqueue(b1);
            billQueue.enqueue(b2);
            billQueue.enqueue(b3);
            billQueue.enqueue(b4);

            allBillsList.add(b1);
            allBillsList.add(b2);
            allBillsList.add(b3);
            allBillsList.add(b4);

            if (DatabaseManager.isConnected()) {
                DatabaseManager.insertBill(b1);
                DatabaseManager.insertBill(b2);
                DatabaseManager.insertBill(b3);
                DatabaseManager.insertBill(b4);
            }
        }

        friendGraph.addDebt("Gaurav", "Amit");
        friendGraph.addDebt("Amit", "Rahul");
        friendGraph.addDebt("Rahul", "Priya");
        friendGraph.addDebt("Sneha", "Rahul");
    }

    // --- STATIC FILES HANDLER ---
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = new File("frontend" + path);
            if (!file.exists()) file = new File("../frontend" + path);
            if (!file.exists()) file = new File("web" + path);
            if (!file.exists()) file = new File("../web" + path);
            if (!file.exists()) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            String mime = path.endsWith(".html") ? "text/html" : 
                          path.endsWith(".js") ? "application/javascript" : 
                          path.endsWith(".css") ? "text/css" : "text/plain";

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", mime + "; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // --- API: FULL STATE ---
    static class StateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                sendResponse(exchange, 204, "text/plain", "");
                return;
            }
            StringBuilder json = new StringBuilder("{");
            json.append("\"dbConnected\":").append(DatabaseManager.isConnected()).append(",");
            json.append("\"totalExpense\":").append(expenseList.getTotalExpense()).append(",");
            json.append("\"stackDepth\":").append(undoStack.isEmpty() ? 0 : 1).append(",");

            // Expenses List
            json.append("\"expenses\":[");
            Node curr = expenseList.head;
            boolean first = true;
            while (curr != null) {
                if (!first) json.append(",");
                json.append(String.format(Locale.US, "{\"id\":%d,\"title\":\"%s\",\"category\":\"%s\",\"amount\":%.2f}",
                    curr.data.id, escapeJson(curr.data.title), escapeJson(curr.data.category), curr.data.amount));
                first = false;
                curr = curr.next;
            }
            json.append("],");

            // Bills Queue
            json.append("\"bills\":[");
            QueueNode bCurr = billQueue.front;
            first = true;
            while (bCurr != null) {
                if (!first) json.append(",");
                json.append(String.format(Locale.US, "{\"id\":%d,\"name\":\"%s\",\"amount\":%.2f,\"dueDate\":\"%s\",\"urgency\":%d}",
                    bCurr.bill.id, escapeJson(bCurr.bill.name), bCurr.bill.amount, bCurr.bill.dueDate, bCurr.bill.urgency));
                first = false;
                bCurr = bCurr.next;
            }
            json.append("],");

            // Friend Debts
            json.append("\"debts\":[");
            first = true;
            for (String debtor : friendGraph.adjList.keySet()) {
                for (String creditor : friendGraph.adjList.get(debtor)) {
                    if (!first) json.append(",");
                    json.append(String.format("{\"from\":\"%s\",\"to\":\"%s\"}", escapeJson(debtor), escapeJson(creditor)));
                    first = false;
                }
            }
            json.append("],");

            // Tree Traversals
            List<String> preList = new ArrayList<>();
            collectPreorder(categoryTree.root, preList);
            List<String> inList = new ArrayList<>();
            collectInorder(categoryTree.root, inList);
            List<String> postList = new ArrayList<>();
            collectPostorder(categoryTree.root, postList);

            json.append("\"treeTraversals\":{");
            json.append("\"preorder\":").append(toJsonArray(preList)).append(",");
            json.append("\"inorder\":").append(toJsonArray(inList)).append(",");
            json.append("\"postorder\":").append(toJsonArray(postList));
            json.append("}");

            json.append("}");
            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // --- API: EXPENSES (ADD & DELETE) ---
    static class ExpensesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                sendResponse(exchange, 204, "text/plain", "");
                return;
            }
            String method = exchange.getRequestMethod();
            if (method.equalsIgnoreCase("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String title = extractJsonField(body, "title");
                String cat = extractJsonField(body, "category");
                double amt = Double.parseDouble(extractJsonField(body, "amount"));

                Transaction newT = new Transaction(nextTransId++, title, cat, amt);
                expenseList.insertAtHead(newT);
                DatabaseManager.insertTransaction(newT);
                undoStack.push("ADD", newT);

                sendResponse(exchange, 200, "application/json", "{\"success\":true}");
            } else if (method.equalsIgnoreCase("DELETE")) {
                String query = exchange.getRequestURI().getQuery();
                int id = Integer.parseInt(query.split("=")[1]);

                Transaction delT = expenseList.deleteById(id);
                if (delT != null) {
                    DatabaseManager.deleteTransaction(id);
                    undoStack.push("DELETE", delT);
                    sendResponse(exchange, 200, "application/json", "{\"success\":true}");
                } else {
                    sendResponse(exchange, 404, "application/json", "{\"error\":\"Not found\"}");
                }
            }
        }
    }

    // --- API: UNDO ---
    static class UndoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                sendResponse(exchange, 204, "text/plain", "");
                return;
            }
            if (undoStack.isEmpty()) {
                sendResponse(exchange, 400, "application/json", "{\"message\":\"Nothing to undo!\"}");
                return;
            }

            StackNode lastAction = undoStack.pop();
            String msg = "";
            if (lastAction.actionType.equals("ADD")) {
                expenseList.deleteById(lastAction.data.id);
                DatabaseManager.deleteTransaction(lastAction.data.id);
                msg = "Undid ADD: Removed '" + lastAction.data.title + "' from Linked List & Database.";
            } else if (lastAction.actionType.equals("DELETE")) {
                expenseList.insertAtHead(lastAction.data);
                DatabaseManager.insertTransaction(lastAction.data);
                msg = "Undid DELETE: Restored '" + lastAction.data.title + "' to Linked List & Database.";
            }
            sendResponse(exchange, 200, "application/json", "{\"message\":\"" + escapeJson(msg) + "\"}");
        }
    }

    // --- API: PAY BILL ---
    static class PayBillHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                sendResponse(exchange, 204, "text/plain", "");
                return;
            }
            Bill paid = billQueue.dequeue();
            if (paid != null) {
                DatabaseManager.deleteBill(paid.id);
                sendResponse(exchange, 200, "application/json", 
                    "{\"message\":\"Paid '" + escapeJson(paid.name) + "' (Rs. " + paid.amount + ") from Queue.\"}");
            } else {
                sendResponse(exchange, 400, "application/json", "{\"message\":\"No bills left to pay!\"}");
            }
        }
    }

    // --- API: ADD BILL ---
    static class AddBillHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String name = extractJsonField(body, "name");
            double amount = Double.parseDouble(extractJsonField(body, "amount"));
            String due = extractJsonField(body, "dueDate");
            int urg = 3;
            try {
                String urgStr = extractJsonField(body, "urgency");
                if (!urgStr.isEmpty()) urg = Integer.parseInt(urgStr);
            } catch (Exception ignored) {}

            Bill newB = new Bill(nextBillId++, name, amount, due, urg);
            billQueue.enqueue(newB);
            allBillsList.add(newB);
            DatabaseManager.insertBill(newB);

            sendResponse(exchange, 200, "application/json", "{\"success\":true}");
        }
    }

    // --- API: GRAPH BFS PATH ---
    static class GraphPathHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String from = "";
            String to = "";
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if (pair[0].equals("from")) from = pair[1];
                        if (pair[0].equals("to")) to = pair[1];
                    }
                }
            }

            List<String> path = friendGraph.bfsFindPath(from, to);
            if (path != null) {
                sendResponse(exchange, 200, "application/json", 
                    "{\"found\":true,\"path\":" + toJsonArray(path) + "}");
            } else {
                sendResponse(exchange, 200, "application/json", 
                    "{\"found\":false,\"message\":\"No settlement path exists between " + from + " and " + to + ".\"}");
            }
        }
    }

    // --- API: SORT (MERGE SORT) ---
    static class SortHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            boolean desc = query != null && query.contains("order=desc");

            Transaction[] arr = new Transaction[expenseList.count];
            Node curr = expenseList.head;
            int idx = 0;
            while (curr != null) {
                arr[idx++] = curr.data;
                curr = curr.next;
            }

            SearchSort.mergeSort(arr, 0, arr.length - 1);

            if (desc) {
                for (int i = 0; i < arr.length / 2; i++) {
                    Transaction temp = arr[i];
                    arr[i] = arr[arr.length - 1 - i];
                    arr[arr.length - 1 - i] = temp;
                }
            }

            StringBuilder json = new StringBuilder("{\"sortedExpenses\":[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) json.append(",");
                json.append(String.format(Locale.US, "{\"id\":%d,\"title\":\"%s\",\"category\":\"%s\",\"amount\":%.2f}",
                    arr[i].id, escapeJson(arr[i].title), escapeJson(arr[i].category), arr[i].amount));
            }
            json.append("]}");
            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // --- API: GREEDY BUDGET ALLOCATION ---
    static class GreedyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            double budget = 14000;
            if (query != null && query.contains("budget=")) {
                try {
                    budget = Double.parseDouble(query.split("=")[1]);
                } catch (Exception ignored) {}
            }

            List<Bill> sortedBills = new ArrayList<>(allBillsList);
            sortedBills.sort((b1, b2) -> Integer.compare(b2.urgency, b1.urgency));

            double rem = budget;
            double spent = 0;

            StringBuilder json = new StringBuilder("{");
            json.append("\"paidBills\":[");
            boolean firstPaid = true;
            for (Bill b : sortedBills) {
                if (b.amount <= rem) {
                    if (!firstPaid) json.append(",");
                    json.append(String.format(Locale.US, "{\"name\":\"%s\",\"amount\":%.2f,\"urgency\":%d}",
                        escapeJson(b.name), b.amount, b.urgency));
                    firstPaid = false;
                    rem -= b.amount;
                    spent += b.amount;
                }
            }
            json.append("],");

            json.append("\"unpaidBills\":[");
            boolean firstUnpaid = true;
            for (Bill b : sortedBills) {
                if (b.amount > rem && spent < b.amount) {
                    if (!firstUnpaid) json.append(",");
                    json.append(String.format(Locale.US, "{\"name\":\"%s\",\"amount\":%.2f,\"urgency\":%d}",
                        escapeJson(b.name), b.amount, b.urgency));
                    firstUnpaid = false;
                }
            }
            json.append("],");

            json.append(String.format(Locale.US, "\"totalSpent\":%.2f,", spent));
            json.append("\"unpaidCount\":").append(firstUnpaid ? 0 : 1);
            json.append("}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // --- HELPER UTILITIES ---
    private static void sendResponse(HttpExchange ex, int code, String contentType, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        ex.sendResponseHeaders(code, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        idx += pattern.length();
        while (idx < json.length() && (json.charAt(idx) == ' ' || json.charAt(idx) == '\"')) idx++;
        StringBuilder val = new StringBuilder();
        while (idx < json.length() && json.charAt(idx) != '\"' && json.charAt(idx) != ',' && json.charAt(idx) != '}') {
            val.append(json.charAt(idx));
            idx++;
        }
        return val.toString().trim();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String toJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(list.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static void collectPreorder(TreeNode n, List<String> list) {
        if (n == null) return;
        list.add(String.format("%s (Rs. %.0f)", n.name, n.amount));
        collectPreorder(n.left, list);
        collectPreorder(n.right, list);
    }

    private static void collectInorder(TreeNode n, List<String> list) {
        if (n == null) return;
        collectInorder(n.left, list);
        list.add(String.format("%s (Rs. %.0f)", n.name, n.amount));
        collectInorder(n.right, list);
    }

    private static void collectPostorder(TreeNode n, List<String> list) {
        if (n == null) return;
        collectPostorder(n.left, list);
        collectPostorder(n.right, list);
        list.add(String.format("%s (Rs. %.0f)", n.name, n.amount));
    }
}
