// Phase 5: Main Application & Console Menu + MySQL Persistence
// Pure Java CLI Demo for College Presentation and Viva

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Core DSA Objects
        CustomLinkedList expenseList = new CustomLinkedList();
        CustomStack undoStack = new CustomStack();
        CustomQueue billQueue = new CustomQueue();
        CategoryTree categoryTree = new CategoryTree();
        FriendGraph friendGraph = new FriendGraph();
        List<Bill> allBillsList = new ArrayList<>();

        int nextTransId = 105;
        int nextBillId = 205;

        // -------------------------------------------------------------
        // LOAD FROM MYSQL (Or Fallback to Demo Data)
        // -------------------------------------------------------------
        List<Transaction> dbTrans = DatabaseManager.fetchAllTransactions();
        List<Bill> dbBills = DatabaseManager.fetchAllBills();

        if (DatabaseManager.isConnected() && !dbTrans.isEmpty()) {
            for (Transaction t : dbTrans) {
                expenseList.insertAtHead(t);
                if (t.id >= nextTransId) nextTransId = t.id + 1;
            }
        } else {
            Transaction t1 = new Transaction(101, "Grocery Shopping", "Food", 2450.0);
            Transaction t2 = new Transaction(102, "Metro Smart Card", "Travel", 500.0);
            Transaction t3 = new Transaction(103, "Netflix Subscription", "Entertainment", 799.0);
            Transaction t4 = new Transaction(104, "Electricity Bill", "Utilities", 3200.0);

            expenseList.insertAtHead(t1);
            expenseList.insertAtHead(t2);
            expenseList.insertAtHead(t3);
            expenseList.insertAtHead(t4);
            undoStack.push("ADD", t4); // Preload last action for undo

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

        // -------------------------------------------------------------
        // INTERACTIVE MENU LOOP
        // -------------------------------------------------------------
        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("   SMART SPEND: PERSONAL FINANCE MANAGER (JAVA DSA)   ");
            System.out.println("   Database: " + (DatabaseManager.isConnected() ? "MySQL Connected" : "In-Memory Fallback"));
            System.out.println("=======================================================");
            System.out.println("[1] Add New Expense              --> Singly Linked List (Head Insert)");
            System.out.println("[2] Delete Expense               --> Singly Linked List (Delete by ID)");
            System.out.println("[3] View All Expenses            --> Singly Linked List (Traversal)");
            System.out.println("[4] Undo Last Action             --> Stack (LIFO Undo)");
            System.out.println("[5] Add Recurring Bill           --> Queue (Enqueue at Rear)");
            System.out.println("[6] Pay Next Due Bill            --> Queue (Dequeue from Front)");
            System.out.println("[7] View Category Budget Tree    --> Binary Tree (Traversals)");
            System.out.println("[8] Friend Debt Settlement Path  --> Graph + BFS (Shortest Path)");
            System.out.println("[9] Sort Expenses by Amount      --> Merge Sort O(n log n) & Binary Search");
            System.out.println("[10] Smart Budget Allocation     --> Greedy Algorithm (Priority Bills)");
            System.out.println("[0] Exit");
            System.out.print("Enter choice [0-10]: ");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (choice == 1) {
                System.out.print("Enter Expense Title (e.g., Dinner): ");
                String title = sc.nextLine();
                System.out.print("Enter Category (Food/Travel/etc.): ");
                String cat = sc.nextLine();
                System.out.print("Enter Amount (Rs.): ");
                double amt = Double.parseDouble(sc.nextLine());

                Transaction newT = new Transaction(nextTransId++, title, cat, amt);
                expenseList.insertAtHead(newT);
                DatabaseManager.insertTransaction(newT);
                undoStack.push("ADD", newT);
                System.out.printf("Success! Added '%s' (Rs. %.2f) at head of Linked List & Saved to DB.\n", title, amt);

            } else if (choice == 2) {
                System.out.print("Enter Expense ID to delete: ");
                int delId = Integer.parseInt(sc.nextLine());
                Transaction delT = expenseList.deleteById(delId);
                if (delT != null) {
                    DatabaseManager.deleteTransaction(delId);
                    undoStack.push("DELETE", delT);
                    System.out.printf("Deleted '%s' (Rs. %.2f). Stored in Stack for Undo.\n", delT.title, delT.amount);
                } else {
                    System.out.println("Transaction ID not found.");
                }

            } else if (choice == 3) {
                expenseList.displayAll();

            } else if (choice == 4) {
                undoStack.undo(expenseList);

            } else if (choice == 5) {
                System.out.print("Enter Bill Name (e.g., Water Bill): ");
                String bName = sc.nextLine();
                System.out.print("Enter Amount (Rs.): ");
                double bAmt = Double.parseDouble(sc.nextLine());
                System.out.print("Enter Due Date (YYYY-MM-DD): ");
                String bDue = sc.nextLine();
                System.out.print("Enter Urgency [1 (Low) to 5 (Critical)]: ");
                int bUrg = Integer.parseInt(sc.nextLine());

                Bill newB = new Bill(nextBillId++, bName, bAmt, bDue, bUrg);
                billQueue.enqueue(newB);
                allBillsList.add(newB);
                DatabaseManager.insertBill(newB);
                System.out.printf("Success! Enqueued '%s' at rear of Queue & Saved to DB.\n", bName);

            } else if (choice == 6) {
                Bill paid = billQueue.dequeue();
                if (paid != null) {
                    DatabaseManager.deleteBill(paid.id);
                    System.out.printf("Success! Paid '%s' of Rs. %.2f from front of Queue.\n", paid.name, paid.amount);
                } else {
                    System.out.println("No bills left to pay!");
                }

            } else if (choice == 7) {
                System.out.println("\n--- CATEGORY BUDGET BINARY TREE ---");
                System.out.println("Hierarchical Structure: Total Budget -> Needs & Wants -> Subcategories");
                System.out.print("\nPreorder Traversal (Root -> Left -> Right):\n  ");
                categoryTree.preorder(categoryTree.root);
                System.out.print("\n\nInorder Traversal (Left -> Root -> Right):\n  ");
                categoryTree.inorder(categoryTree.root);
                System.out.print("\n\nPostorder Traversal (Left -> Right -> Root) [Used for bottom-up sums]:\n  ");
                categoryTree.postorder(categoryTree.root);
                System.out.println();

            } else if (choice == 8) {
                friendGraph.displayDebts();
                System.out.print("\nEnter Debtor Friend Name (e.g., Gaurav): ");
                String debtor = sc.nextLine().trim();
                System.out.print("Enter Creditor Friend Name (e.g., Priya): ");
                String creditor = sc.nextLine().trim();

                List<String> path = friendGraph.bfsFindPath(debtor, creditor);
                if (path != null) {
                    System.out.println("\nShortest Debt Settlement Chain (Found via BFS):");
                    System.out.println("  " + String.join(" -> owes -> ", path));
                    System.out.printf("Recommendation: %s can settle directly with %s!\n", debtor, creditor);
                } else {
                    System.out.println("No debt settlement path found between these two friends.");
                }

            } else if (choice == 9) {
                Transaction[] arr = new Transaction[expenseList.count];
                Node curr = expenseList.head;
                int idx = 0;
                while (curr != null) {
                    arr[idx++] = curr.data;
                    curr = curr.next;
                }

                SearchSort.mergeSort(arr, 0, arr.length - 1);
                System.out.println("\n--- EXPENSES SORTED BY AMOUNT (Merge Sort: O(n log n)) ---");
                for (Transaction t : arr) {
                    System.out.printf("  Rs. %-8.2f | %s (%s)\n", t.amount, t.title, t.category);
                }

                System.out.print("\nEnter an exact amount to search (Binary Search O(log n)): ");
                double searchAmt = Double.parseDouble(sc.nextLine());
                Transaction found = SearchSort.binarySearch(arr, searchAmt);
                if (found != null) {
                    System.out.printf("Found in O(log n) time: ID %d | '%s' (Rs. %.2f)\n", 
                        found.id, found.title, found.amount);
                } else {
                    System.out.println("Amount not found in the list.");
                }

            } else if (choice == 10) {
                System.out.print("Enter your available budget for bills (e.g., 14000): ");
                double budget = Double.parseDouble(sc.nextLine());
                GreedyBudget.allocateBudget(allBillsList, budget);

            } else if (choice == 0) {
                System.out.println("Exiting Smart Spend. All the best for your viva!");
                break;
            } else {
                System.out.println("Invalid option. Please choose between 0 and 10.");
            }
        }
        sc.close();
    }
}
