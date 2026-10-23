// Phase 3: Friend Shared-Expense Debt Network
// DSA Concept: Directed Graph (Adjacency List) + BFS (Breadth-First Search)
// VIVA ANSWER: Friends ke beech debt network graph hota hai.
//              BFS Queue use karke shortest debt settlement chain dhoondta hai O(V+E) me.

import java.util.*;

public class FriendGraph {
    // Adjacency List: Har person kiske kiske paise dene hain
    Map<String, List<String>> adjList = new HashMap<>();

    public void addDebt(String debtor, String creditor) {
        adjList.putIfAbsent(debtor, new ArrayList<>());
        adjList.putIfAbsent(creditor, new ArrayList<>());
        adjList.get(debtor).add(creditor);
    }

    // BFS se shortest debt settlement path nikalna
    public List<String> bfsFindPath(String start, String target) {
        if (!adjList.containsKey(start) || !adjList.containsKey(target)) {
            return null;
        }

        // BFS ke liye Queue
        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        // Start person ko path me daal kar queue me push karo
        List<String> startPath = new ArrayList<>();
        startPath.add(start);
        queue.add(startPath);
        visited.add(start);

        while (!queue.isEmpty()) {
            List<String> currentPath = queue.poll();
            String currentPerson = currentPath.get(currentPath.size() - 1);

            // Agar target person mil gaya, path return karo
            if (currentPerson.equals(target)) {
                return currentPath;
            }

            // Agle neighbors ko queue me add karo
            for (String neighbor : adjList.getOrDefault(currentPerson, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }

        return null; // Path nahi mila
    }

    public void displayDebts() {
        System.out.println("\n--- CURRENT FRIEND DEBT NETWORK ---");
        for (String debtor : adjList.keySet()) {
            for (String creditor : adjList.get(debtor)) {
                System.out.printf("  * %s owes money to -> %s\n", debtor, creditor);
            }
        }
    }
}
