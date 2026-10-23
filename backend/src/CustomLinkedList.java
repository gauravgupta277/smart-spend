// Array ka size fix hota hai, lekin Linked List me naya expense shuru me (Head par) O(1) constant time me add ho jata hai.

class Node {
    Transaction data;
    Node next;

    public Node(Transaction data) {
        this.data = data;
        this.next = null;
    }
}

public class CustomLinkedList {
    Node head; // Pehla dibba (pointer)
    int count = 0;

    // Head par naya transaction jodna - O(1) Time
    public void insertAtHead(Transaction t) {
        Node newNode = new Node(t); // made a new node
        newNode.next = head;  // link new node to the current head
        head = newNode;  // update head to point to the new node
        count++; 
    }

    // ID dhoond kar transaction delete karna - O(n) Time
    // Return karta hai deleted transaction taaki Undo Stack me use ho sake
    public Transaction deleteById(int id) {
        if (head == null) return null;

        // Case 1: Agar pehla node (head) hi delete karna ho
        if (head.data.id == id) {
            Transaction deleted = head.data;
            head = head.next;
            count--;
            return deleted;
        }

        // Case 2: Baaki list me search karna
        Node current = head;
        while (current.next != null && current.next.data.id != id) {
            current = current.next;
        }

        if (current.next != null) {
            Transaction deleted = current.next.data;
            current.next = current.next.next; // Node bypass karke delete
            count--;
            return deleted;
        }

        return null; // ID nahi mila
    }

    // Saare transactions print karna (Traversal) - O(n) Time
    public void displayAll() {
        if (head == null) {
            System.out.println("No expenses recorded yet. (Linked List is empty)");
            return;
        }
        System.out.println("\n--- EXPENSE HISTORY (Linked List Traversal: Head -> Next) ---");
        Node current = head;
        while (current != null) {
            current.data.display();
            current = current.next;
        }
        System.out.printf("Total Spent: Rs. %.2f across %d transactions.\n", getTotalExpense(), count);
    }

    // Total expense calculate karna
    public double getTotalExpense() {
        double total = 0;
        Node current = head;
        while (current != null) {
            total += current.data.amount;
            current = current.next;
        }
        return total;
    }

    public boolean isEmpty() {
        return head == null;
    }
}
