// Phase 1: Data Model - Transaction
// Ek expense ko represent karta hai (ID, Title, Category, Amount)

public class Transaction {
    public int id;
    public String title;
    public String category;
    public double amount;

    public Transaction(int id, String title, String category, double amount) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
    }

    public void display() {
        System.out.printf("ID: %-4d | Title: %-20s | Category: %-12s | Amount: Rs. %.2f\n", 
            id, title, category, amount);
    }
}
