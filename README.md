# Smart Spend: A Personal Finance Manager (Java DSA + Spring Boot & MySQL)

A complete, full-stack Personal Finance Management system built with **Custom Data Structures & Algorithms** in Java, persistent storage via **MySQL 8.0**, and an interactive **Royal Gold & Dark Dashboard**.

---

## 📂 Project Architecture (Clean Separation)

```text
DSA-project/
│
├── backend/                           # 📁 Complete Backend & DSA Engine
│   ├── src/                           # Pure Java Source Code & DSA modules
│   │   ├── Main.java                  # Interactive Console CLI Runner
│   │   ├── WebServer.java             # Web Server for UI Dashboard (Port 8080)
│   │   ├── DatabaseManager.java       # MySQL JDBC Persistence
│   │   ├── Transaction.java           # Expense Model
│   │   ├── Bill.java                  # Recurring Bill Model
│   │   ├── CustomLinkedList.java      # Singly Linked List (O(1) Head Insert)
│   │   ├── CustomStack.java           # Action Stack for Undo (LIFO: O(1))
│   │   ├── CustomQueue.java           # Bill Queue (FIFO: O(1))
│   │   ├── CategoryTree.java          # Category Binary Tree (Bottom-up Sum)
│   │   ├── FriendGraph.java           # Friend Debt Network with BFS
│   │   ├── SearchSort.java            # Merge Sort & Binary Search
│   │   ├── GreedyBudget.java          # Greedy Priority Bill Allocator
│   │   └── EXPLANATION_FOR_VIVA.txt   # 1-Minute Viva Cheat Sheet
│   │
│   ├── lib/                           # MySQL JDBC Connector Driver
│   │   └── mysql-connector.jar
│   ├── bin/                           # Compiled .class files
│   ├── pom.xml                        # Spring Boot Maven Configuration
│   ├── mvnw & mvnw.cmd                # Maven Wrapper
│   └── db.properties                  # Database connection credentials
│
├── frontend/                          # 📁 Luxury Royal Web Dashboard
│   ├── index.html                     # Tabbed Dark & Gold UI with Chart.js
│   └── app.js                         # Dynamic state, charts, and node visualizers
│
├── db.properties                      # Root DB connection properties
├── README.md                          # Documentation
└── VIVA_GUIDE.md                      # Comprehensive Viva Q&A Guide
```

---

## 🚀 How to Run the Project

### Option 1: Run the Royal Web Dashboard (With MySQL & Charts)
Open your terminal in VS Code root and run:

```powershell
javac -cp "backend/lib/*;backend/bin" -d backend/bin backend/src/*.java
java -cp "backend/lib/*;backend/bin" WebServer
```

Open in your browser:
👉 **`http://localhost:8080`**

---

### Option 2: Run the Terminal CLI Menu
In your VS Code terminal, run:

```powershell
java -cp "backend/lib/*;backend/bin" Main
```
Demonstrates any DSA operation directly in your terminal console!

---

### Option 3: Compile via Maven / Spring Boot (Inside `backend/`)
```powershell
cd backend
.\mvnw.cmd compile
```
