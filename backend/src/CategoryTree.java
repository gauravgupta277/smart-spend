// Phase 3: Category Budget Binary Tree
// DSA Concept: Binary Tree & Traversals (Preorder, Inorder, Postorder)
// VIVA ANSWER: Budget hierarchical hota hai (Budget -> Needs & Wants -> Rent & Food).
//              Postorder (Left -> Right -> Root) se hum bottom-up total calculate karte hain.

class TreeNode {
    String name;
    double amount;
    TreeNode left;
    TreeNode right;

    public TreeNode(String name, double amount) {
        this.name = name;
        this.amount = amount;
        this.left = null;
        this.right = null;
    }
}

public class CategoryTree {
    TreeNode root;

    public CategoryTree() {
        // Default budget tree build karna
        //              Total Budget (32,000)
        //             /                    \
        //       Needs (23,000)         Wants (9,000)
        //       /           \          /           \
        //  Rent(15,000)  Food(8,000) Dining(4,000) Shopping(5,000)

        root = new TreeNode("Total Budget", 0);
        root.left = new TreeNode("Needs", 0);
        root.right = new TreeNode("Wants", 0);

        root.left.left = new TreeNode("Rent & Utilities", 15000);
        root.left.right = new TreeNode("Groceries & Food", 8000);

        root.right.left = new TreeNode("Dining & Leisure", 4000);
        root.right.right = new TreeNode("Shopping & Gadgets", 5000);

        // Bottom-up total calculate karna
        calculateTotalBottomUp(root);
    }

    // Postorder Traversal se bottom-up total nikalna - O(n) Time
    public double calculateTotalBottomUp(TreeNode node) {
        if (node == null) return 0;
        if (node.left == null && node.right == null) return node.amount;

        double leftSum = calculateTotalBottomUp(node.left);
        double rightSum = calculateTotalBottomUp(node.right);

        node.amount = leftSum + rightSum; // Parent = Left + Right
        return node.amount;
    }

    // Preorder: Root -> Left -> Right
    public void preorder(TreeNode node) {
        if (node == null) return;
        System.out.printf("[%s: Rs. %.0f] -> ", node.name, node.amount);
        preorder(node.left);
        preorder(node.right);
    }

    // Inorder: Left -> Root -> Right
    public void inorder(TreeNode node) {
        if (node == null) return;
        inorder(node.left);
        System.out.printf("[%s: Rs. %.0f] -> ", node.name, node.amount);
        inorder(node.right);
    }

    // Postorder: Left -> Right -> Root
    public void postorder(TreeNode node) {
        if (node == null) return;
        postorder(node.left);
        postorder(node.right);
        System.out.printf("[%s: Rs. %.0f] -> ", node.name, node.amount);
    }
}
