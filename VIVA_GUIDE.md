# Smart Spend: Java DSA + MySQL Viva & Presentation Guide 🎓

Yeh guide aapke presentation aur viva ke liye banayi gayi hai taaki aap professor ke kisi bhi technical question ka confident answer de sako!

---

## 1. Intro Speech (Mam ke samne shuru me kya bolna hai)

> *"Good morning, Mam. My project is **Smart Spend: A Personal Finance Manager**.*
> 
> *Is project ka architecture 2-tier hai:*
> 1. *Core Engine: **Pure Custom Data Structures** written from scratch (`Node`-based Singly Linked List, LIFO Stack, FIFO Queue, Binary Tree, Graph with BFS, Merge Sort, aur Greedy Algorithm).*
> 2. *Storage Layer: **MySQL Database** for permanent disk persistence.*
> 
> *Frontend ko modern aur attractive banane ke liye humne **Executive Charts (Chart.js)** aur **Royal Dark & Gold Glassmorphic UI** design kiya hai."*

---

## 2. The #1 Tricky Question: "MySQL bhi hai aur DSA bhi, kyu?"

* **Mam ka Question:** *"Agar MySQL database me tables hain, toh custom Linked List, Stack aur Queue kyu banayi?"*
* **Aapka Answer (Golden Answer):**
  > *"Mam, MySQL sirf permanent disk storage ke liye use hota hai taaki computer band hone par data lost na ho.*
  > 
  > *Lekin runtime par RAM ke andar saari fast operations hamare **Custom Data Structures** karte hain:*
  > * Naya transaction head par judna: **$O(1)$ Linked List**.
  > * Accidental delete ko undo karna: **$O(1)$ LIFO Stack**.
  > * Bills ka first-come-first-served order: **$O(1)$ FIFO Queue**.
  > * Budget totals calculate karna: **Postorder Tree Traversal**.
  > 
  > *Is design pattern ko industry me **In-Memory Caching Architecture** bolte hain (jaise Redis karta hai)."*

---

## 3. Core DSA Modules & Complexities

### 1. `CustomLinkedList.java` (Expense Tracking)
* **Concept:** Singly Linked List (`Node head -> next`)
* **Operations:** Insert at Head $O(1)$, Delete by ID $O(n)$, Traversal $O(n)$.

### 2. `CustomStack.java` (Undo Action)
* **Concept:** Stack (**LIFO - Last In, First Out**)
* **Operations:** Push $O(1)$, Pop $O(1)$. Top action ko reverse karta hai.

### 3. `CustomQueue.java` (Recurring Bills)
* **Concept:** Queue (**FIFO - First In, First Out / FCFS**)
* **Operations:** Enqueue at rear $O(1)$, Dequeue from front $O(1)$.

### 4. `CategoryTree.java` (Category Budget)
* **Concept:** Binary Tree & **Postorder Traversal** (Left $\rightarrow$ Right $\rightarrow$ Root).
* **Viva Answer:** Postorder traversal subcategories (Rent aur Food) ka sum pehle nikalta hai, fir parent category (Needs) ka total banta hai (Bottom-Up calculation).

### 5. `FriendGraph.java` (Debt Network + BFS)
* **Concept:** Directed Graph + **Breadth-First Search (BFS)**
* **Complexity:** $O(V + E)$. Shortest debt settlement chain dhoondta hai (`Gaurav -> Amit -> Priya`).

### 6. `SearchSort.java` (Merge Sort + Binary Search)
* **Concept:** Merge Sort (**Divide and Conquer**) $O(n \log n)$ & Binary Search $O(\log n)$.

### 7. `GreedyBudget.java` (Priority Bill Allocation)
* **Concept:** Greedy Algorithm.
* **Viva Answer:** Bills ko unke **Urgency Score (5 = Critical to 1 = Low)** ke descending order me sort karke sabse zaroori bills pehle settle karta hai.

---

## 4. How to Run the Project in VS Code

### Web Dashboard with Charts & Royal Theme:
```powershell
javac -cp "lib/*;bin" -d bin src/*.java
java -cp "lib/*;bin" WebServer
```
Open in browser: **`http://localhost:8080`**

### MySQL Password Setup (Optional):
`db.properties` file me apna password update kar sakte ho:
```properties
db.password=YOUR_PASSWORD
```
*(Agar password set na bhi ho, toh bhi project bina ruke In-Memory DSA mode me chalega!)*
