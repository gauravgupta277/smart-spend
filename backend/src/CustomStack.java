// Stack plate ki tarah hota hai. Sabse aakhiri action top par hota hai.
//              Undo karne par top action pop hota hai aur inverse operation chalta hai.

class StackNode {
    String actionType; // "ADD" ya "DELETE"
    Transaction data;
    StackNode next;

    public StackNode(String actionType, Transaction data) {
        this.actionType = actionType;
        this.data = data;
        this.next = null;
    }
}

public class CustomStack {
    StackNode top; // Sabse upar wala element

    // Naya action push karna - O(1) Time
    public void push(String actionType, Transaction data) {
        StackNode newNode = new StackNode(actionType, data);
        newNode.next = top;
        top = newNode;
    }

    // Top action pop karna - O(1) Time
    public StackNode pop() {
        if (isEmpty()) return null;
        StackNode popped = top;
        top = top.next;
        return popped;
    }

    public boolean isEmpty() {
        return top == null;
    }

    // Undo action ko Linked List par apply karna
    public void undo(CustomLinkedList list) {
        if (isEmpty()) {
            System.out.println("Nothing to undo! (Stack is empty)");
            return;
        }

        StackNode lastAction = pop();
        if (lastAction.actionType.equals("ADD")) {
            // Agar pehle add kiya tha, toh ab use delete karenge
            list.deleteById(lastAction.data.id);
            System.out.printf("Undid ADD: Removed '%s' (Rs. %.2f) from Linked List.\n", 
                lastAction.data.title, lastAction.data.amount);
        } else if (lastAction.actionType.equals("DELETE")) {
            // Agar pehle delete kiya tha, toh wapas head par jod denge
            list.insertAtHead(lastAction.data);
            System.out.printf("Undid DELETE: Restored '%s' (Rs. %.2f) back to Linked List.\n", 
                lastAction.data.title, lastAction.data.amount);
        }
    }
}
