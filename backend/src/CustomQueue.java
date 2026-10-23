// Phase 2: Recurring Bill Queue
// DSA Concept: Queue (FIFO - First In, First Out / FCFS)
// VIVA ANSWER: Bills hamesha line me lagte hain (First-Come, First-Served).
//              Enqueue peeche (rear) hota hai, Pay/Dequeue aage (front) se hota hai.

class QueueNode {
    Bill bill;
    QueueNode next;

    public QueueNode(Bill bill) {
        this.bill = bill;
        this.next = null;
    }
}

public class CustomQueue {
    QueueNode front; // Line me sabse aage wala bill
    QueueNode rear;  // Line me sabse aakhiri bill

    // Bill ko queue ke peeche add karna - O(1) Time
    public void enqueue(Bill bill) {
        QueueNode newNode = new QueueNode(bill);
        if (rear == null) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
    }

    // Sabse pehla due bill pay karna - O(1) Time
    public Bill dequeue() {
        if (isEmpty()) return null;
        Bill paid = front.bill;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        return paid;
    }

    public boolean isEmpty() {
        return front == null;
    }

    // Pending bills line dekhna
    public void displayQueue() {
        if (isEmpty()) {
            System.out.println("No pending bills in queue! (Queue is empty)");
            return;
        }
        System.out.println("\n--- PENDING BILLS QUEUE (FIFO: Front -> Rear) ---");
        QueueNode current = front;
        while (current != null) {
            current.bill.display();
            current = current.next;
        }
    }
}
