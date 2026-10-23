// Phase 4: Smart Budgeting Module
// DSA Concept: Greedy Algorithm
// VIVA ANSWER: Limited budget me sabse zyada urgent bills (Urgency 5 -> 1)
//              pehle pay karte hain taaki maximum critical bills settle ho sakein.

import java.util.*;

public class GreedyBudget {

    public static void allocateBudget(List<Bill> bills, double budget) {
        System.out.printf("\n--- GREEDY BUDGET ALLOCATION (Available Budget: Rs. %.2f) ---\n", budget);

        // Greedy Step: Urgency ke hisaab se descending sort karna (5 pehle, fir 4, 3...)
        List<Bill> sortedBills = new ArrayList<>(bills);
        sortedBills.sort((b1, b2) -> Integer.compare(b2.urgency, b1.urgency));

        double remainingBudget = budget;
        double totalSpent = 0;

        System.out.println("\n[PAID BILLS - Prioritized by Urgency]:");
        for (Bill b : sortedBills) {
            if (b.amount <= remainingBudget) {
                System.out.printf("  * Paid: %-18s | Rs. %-8.2f (Urgency: %d/5)\n", b.name, b.amount, b.urgency);
                remainingBudget -= b.amount;
                totalSpent += b.amount;
            }
        }

        System.out.println("\n[POSTPONED BILLS - Budget Exceeded]:");
        for (Bill b : sortedBills) {
            if (b.amount > remainingBudget && !isPaid(b, remainingBudget, totalSpent, budget)) {
                // If it wasn't paid
                System.out.printf("  * Postponed: %-18s | Rs. %-8.2f (Urgency: %d/5)\n", b.name, b.amount, b.urgency);
            }
        }

        System.out.printf("\nSummary: Total Spent = Rs. %.2f | Remaining Budget = Rs. %.2f\n", 
            totalSpent, remainingBudget);
    }

    private static boolean isPaid(Bill b, double remaining, double spent, double total) {
        return (spent >= b.amount); // Helper check
    }
}
