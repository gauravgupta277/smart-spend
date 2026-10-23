// Phase 4: Sorting & Searching Module
// DSA Concepts: 
// 1. Merge Sort (Divide & Conquer) - O(n log n)
// 2. Binary Search (Search on Sorted Data) - O(log n)
// VIVA ANSWER: Merge Sort array ko aadha-aadha baant kar sort karta hai.
//              Sort hone ke baad Binary Search O(log n) me transaction dhoond leta hai.

public class SearchSort {

    // Merge Sort (Ascending by Amount)
    public static void mergeSort(Transaction[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            // Aadha divide karo
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);

            // Dono halves ko merge karo
            merge(arr, left, mid, right);
        }
    }

    private static void merge(Transaction[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Transaction[] L = new Transaction[n1];
        Transaction[] R = new Transaction[n2];

        for (int i = 0; i < n1; ++i) L[i] = arr[left + i];
        for (int j = 0; j < n2; ++j) R[j] = arr[mid + 1 + j];

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (L[i].amount <= R[j].amount) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }

        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    // Binary Search on Sorted Array - O(log n) Time
    public static Transaction binarySearch(Transaction[] arr, double targetAmount) {
        int low = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            // Compare amount
            if (Math.abs(arr[mid].amount - targetAmount) < 0.01) {
                return arr[mid];
            }

            if (arr[mid].amount < targetAmount) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return null; // Not found
    }
}
