// Data Model - Recurring Bill
// Pending bills (Rent, Electricity) ke liye model.
// Urgency: 1 (Low) se 5 (Critical) - Greedy algorithm ke liye use hota hai.

public class Bill {
    public int id;
    public String name;
    public double amount;
    public String dueDate;
    public int urgency; // 1 to 5

    public Bill(int id, String name, double amount, String dueDate, int urgency) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.dueDate = dueDate;
        this.urgency = urgency;
    }

    public void display() {
        System.out.printf("Bill: %-18s | Amount: Rs. %-8.2f | Due: %-10s | Urgency: %d/5\n", 
            name, amount, dueDate, urgency);
    }
}
