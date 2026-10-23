// Smart Spend Royal Dashboard Controller
const API_BASE = (window.location.port !== "8080") ? "http://localhost:8080" : "";

let currentState = {
  expenses: [],
  bills: [],
  debts: [],
  totalExpense: 0,
  stackDepth: 0,
  dbConnected: false
};
let categoryChart = null;
let billsBarChart = null;

// Initial setup
document.addEventListener("DOMContentLoaded", () => {
  fetchState();
  if (window.lucide) lucide.createIcons();
});

// -------------------------------------------------------------
// TAB SWITCHING (Instant & Robust)
// -------------------------------------------------------------
function switchTab(tabId) {
  // 1. Hide all tab panes
  const panes = document.querySelectorAll(".tab-pane");
  panes.forEach(pane => {
    pane.classList.add("hidden");
  });

  // 2. De-highlight all tab buttons
  const buttons = document.querySelectorAll(".tab-btn");
  buttons.forEach(btn => {
    btn.classList.remove("border-amber-400", "text-amber-300");
    btn.classList.add("border-transparent", "text-slate-400");
  });

  // 3. Show targeted pane
  const activePane = document.getElementById(tabId);
  if (activePane) {
    activePane.classList.remove("hidden");
  }

  // 4. Highlight active button
  const activeBtn = document.getElementById("btn-" + tabId);
  if (activeBtn) {
    activeBtn.classList.remove("border-transparent", "text-slate-400");
    activeBtn.classList.add("border-amber-400", "text-amber-300");
  }

  // 5. Update charts if returning to overview
  if (tabId === "tab-overview" && currentState) {
    updateCharts(currentState.expenses, currentState.bills);
  }

  if (window.lucide) lucide.createIcons();
}

// -------------------------------------------------------------
// FETCH STATE FROM BACKEND
// -------------------------------------------------------------
async function fetchState() {
  try {
    const res = await fetch(`${API_BASE}/api/state`);
    if (!res.ok) throw new Error("Backend response error: " + res.status);
    const data = await res.json();
    currentState = data;

    // Update Nav & KPIs
    const navExpense = document.getElementById("navTotalExpense");
    if (navExpense) navExpense.innerText = `Rs. ${data.totalExpense.toFixed(2)}`;

    const kpiExpense = document.getElementById("kpiTotalExpense");
    if (kpiExpense) kpiExpense.innerText = `Rs. ${data.totalExpense.toFixed(2)}`;

    const kpiTrans = document.getElementById("kpiTransCount");
    if (kpiTrans) kpiTrans.innerText = data.expenses.length;

    const kpiBills = document.getElementById("kpiBillsCount");
    if (kpiBills) kpiBills.innerText = data.bills.length;

    const undoBadge = document.getElementById("undoCountBadge");
    if (undoBadge) undoBadge.innerText = data.stackDepth;

    // MySQL Status Pill
    const dbBadge = document.getElementById("dbBadge");
    const dbBadgeText = document.getElementById("dbBadgeText");
    const kpiStorageMode = document.getElementById("kpiStorageMode");

    if (data.dbConnected) {
      if (dbBadge) dbBadge.className = "hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-mono border border-emerald-500/30 bg-emerald-950/40 text-emerald-300";
      if (dbBadgeText) dbBadgeText.innerText = "MySQL Connected (smartspend)";
      if (kpiStorageMode) {
        kpiStorageMode.innerText = "MySQL Active";
        kpiStorageMode.className = "text-lg font-bold font-mono text-emerald-400 mt-2";
      }
    } else {
      if (dbBadge) dbBadge.className = "hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-mono border border-amber-500/30 bg-amber-950/40 text-amber-300";
      if (dbBadgeText) dbBadgeText.innerText = "In-Memory Engine (MySQL Offline)";
      if (kpiStorageMode) {
        kpiStorageMode.innerText = "In-Memory Active";
        kpiStorageMode.className = "text-lg font-bold font-mono text-amber-400 mt-2";
      }
    }

    // Render Components
    renderLinkedList(data.expenses);
    renderQueue(data.bills);
    renderDebts(data.debts);
    updateCharts(data.expenses, data.bills);
    handleGreedyOptimization();

  } catch (err) {
    console.warn("Backend state fetch warning:", err.message);
    const dbBadgeText = document.getElementById("dbBadgeText");
    if (dbBadgeText) dbBadgeText.innerText = "Server Disconnected";
  } finally {
    if (window.lucide) lucide.createIcons();
  }
}

// -------------------------------------------------------------
// CHARTS (Chart.js Analytics)
// -------------------------------------------------------------
function updateCharts(expenses, bills) {
  if (!window.Chart) return;

  // 1. Group expenses by category
  const categoryTotals = {};
  if (expenses && expenses.length) {
    expenses.forEach(e => {
      categoryTotals[e.category] = (categoryTotals[e.category] || 0) + e.amount;
    });
  }

  const categories = Object.keys(categoryTotals);
  const amounts = Object.values(categoryTotals);

  const chartColors = [
    'rgba(212, 175, 55, 0.85)',
    'rgba(99, 102, 241, 0.85)',
    'rgba(16, 185, 129, 0.85)',
    'rgba(244, 63, 94, 0.85)',
    'rgba(168, 85, 247, 0.85)',
    'rgba(14, 165, 233, 0.85)'
  ];

  // Doughnut Chart
  const doughnutCtx = document.getElementById("categoryChart");
  if (doughnutCtx) {
    if (categoryChart) categoryChart.destroy();
    categoryChart = new Chart(doughnutCtx, {
      type: 'doughnut',
      data: {
        labels: categories.length ? categories : ['No Expenses Recorded'],
        datasets: [{
          data: amounts.length ? amounts : [1],
          backgroundColor: categories.length ? chartColors : ['rgba(255,255,255,0.08)'],
          borderColor: '#0b1120',
          borderWidth: 3,
          hoverOffset: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              color: '#94a3b8',
              font: { family: 'Plus Jakarta Sans', size: 11, weight: '500' },
              padding: 12
            }
          },
          tooltip: {
            backgroundColor: '#070a12',
            titleColor: '#d4af37',
            bodyColor: '#f1f5f9',
            borderColor: 'rgba(212, 175, 55, 0.3)',
            borderWidth: 1,
            callbacks: {
              label: function(ctx) {
                return ` ${ctx.label}: Rs. ${Number(ctx.raw).toFixed(2)}`;
              }
            }
          }
        },
        cutout: '68%'
      }
    });
  }

  // 2. Bills Bar Chart
  const barCtx = document.getElementById("billsBarChart");
  if (barCtx) {
    if (billsBarChart) billsBarChart.destroy();
    const billNames = (bills && bills.length) ? bills.map(b => b.name) : ['No Bills Scheduled'];
    const billAmounts = (bills && bills.length) ? bills.map(b => b.amount) : [0];
    const billUrgencies = (bills && bills.length) ? bills.map(b => b.urgency) : [1];

    const barColors = billUrgencies.map(u => 
      u >= 4 ? 'rgba(212, 175, 55, 0.85)' : 'rgba(99, 102, 241, 0.75)'
    );

    billsBarChart = new Chart(barCtx, {
      type: 'bar',
      data: {
        labels: billNames,
        datasets: [{
          label: 'Amount (Rs.)',
          data: billAmounts,
          backgroundColor: barColors,
          borderRadius: 8,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: {
            grid: { display: false },
            ticks: { color: '#64748b', font: { family: 'Plus Jakarta Sans', size: 10 } }
          },
          y: {
            grid: { color: 'rgba(255, 255, 255, 0.05)' },
            ticks: { color: '#64748b', font: { family: 'JetBrains Mono', size: 10 } }
          }
        },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#070a12',
            titleColor: '#d4af37',
            bodyColor: '#f1f5f9',
            borderColor: 'rgba(212, 175, 55, 0.3)',
            borderWidth: 1,
            callbacks: {
              label: function(ctx) {
                return ` Obligation: Rs. ${Number(ctx.raw).toFixed(2)}`;
              }
            }
          }
        }
      }
    });
  }
}

// -------------------------------------------------------------
// TAB 1: LINKED LIST RENDERING
// -------------------------------------------------------------
function renderLinkedList(expenses) {
  const container = document.getElementById("linkedListVisualChain");
  const tbody = document.getElementById("expensesTableBody");
  const nodeCount = document.getElementById("nodeChainCount");

  if (!container || !tbody) return;

  if (nodeCount) nodeCount.innerText = `${expenses ? expenses.length : 0} Nodes in Memory`;
  container.innerHTML = "";
  tbody.innerHTML = "";

  if (!expenses || expenses.length === 0) {
    container.innerHTML = `<span class="text-xs text-slate-500 italic">Linked list is empty (head = NULL)</span>`;
    tbody.innerHTML = `<tr><td colspan="5" class="py-6 text-center text-slate-500 italic">No transactions recorded.</td></tr>`;
    return;
  }

  // Visual Node Chain
  expenses.forEach((item, index) => {
    const isHead = index === 0;
    const el = document.createElement("div");
    el.className = "node-chain flex-shrink-0 flex items-center";
    el.innerHTML = `
      <div class="px-4 py-2.5 rounded-xl border text-xs shadow-lg transition-all hover:scale-105 ${
        isHead 
          ? 'bg-gradient-to-br from-amber-500/20 to-royal-900 border-amber-400 text-amber-200 gold-glow ring-1 ring-amber-400/50' 
          : 'bg-royal-850/80 border-slate-750 text-slate-200 border-slate-700'
      }">
        <div class="font-bold flex items-center gap-1.5">
          ${isHead ? '<span class="text-[9px] bg-amber-400 text-royal-950 font-bold px-1.5 py-0.2 rounded font-mono">HEAD</span>' : ''}
          <span>${item.title}</span>
        </div>
        <div class="text-[11px] font-mono mt-0.5 ${isHead ? 'text-amber-300 font-semibold' : 'text-slate-400'}">
          Rs. ${item.amount.toFixed(2)} &bull; <span class="text-slate-500">${item.category}</span>
        </div>
      </div>
    `;
    container.appendChild(el);

    // Table Row
    const tr = document.createElement("tr");
    tr.className = "hover:bg-royal-850/40 transition";
    tr.innerHTML = `
      <td class="py-3 px-5 font-mono text-amber-400/70 font-semibold">#${item.id}</td>
      <td class="py-3 px-5 font-medium text-slate-100">${item.title}</td>
      <td class="py-3 px-5"><span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-royal-800 text-indigo-300 border border-indigo-500/20">${item.category}</span></td>
      <td class="py-3 px-5 font-mono font-bold text-amber-400">Rs. ${item.amount.toFixed(2)}</td>
      <td class="py-3 px-5 text-right">
        <button onclick="handleDeleteExpense(${item.id})" class="px-2.5 py-1 text-slate-400 hover:text-rose-400 hover:bg-rose-950/40 rounded border border-transparent hover:border-rose-500/30 text-xs transition">
          Delete
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function handleAddExpense(e) {
  if (e) e.preventDefault();
  const title = document.getElementById("expTitle").value.trim();
  const category = document.getElementById("expCategory").value;
  const amount = parseFloat(document.getElementById("expAmount").value);

  if (!title || isNaN(amount)) return;

  try {
    const res = await fetch(`${API_BASE}/api/expenses`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title, category, amount })
    });

    if (res.ok) {
      document.getElementById("expTitle").value = "";
      document.getElementById("expAmount").value = "";
      showNotification(`Recorded '${title}' to head of Linked List & Database`);
      fetchState();
    }
  } catch (err) {
    showNotification("Failed to add expense: " + err.message, true);
  }
}

async function handleDeleteExpense(id) {
  try {
    const res = await fetch(`${API_BASE}/api/expenses?id=${id}`, { method: "DELETE" });
    if (res.ok) {
      showNotification(`Removed record #${id} (Saved to Undo Stack)`);
      fetchState();
    }
  } catch (err) {
    showNotification("Delete failed: " + err.message, true);
  }
}

// -------------------------------------------------------------
// STACK: UNDO OPERATION
// -------------------------------------------------------------
async function handleUndo() {
  try {
    const res = await fetch(`${API_BASE}/api/undo`, { method: "POST" });
    const data = await res.json();
    if (res.ok) {
      showNotification(data.message);
      fetchState();
    } else {
      showNotification(data.message || "Nothing to undo!", true);
    }
  } catch (err) {
    showNotification("Undo failed: " + err.message, true);
  }
}

// -------------------------------------------------------------
// TAB 2: QUEUE RENDERING
// -------------------------------------------------------------
function renderQueue(bills) {
  const container = document.getElementById("queueVisualPipeline");
  const count = document.getElementById("queueLineCount");
  if (count) count.innerText = `${bills ? bills.length : 0} Bills in Line`;
  if (!container) return;
  container.innerHTML = "";

  if (!bills || bills.length === 0) {
    container.innerHTML = `<span class="text-xs text-slate-500 italic">Queue pipeline is empty (no pending bills)</span>`;
    return;
  }

  bills.forEach((bill, idx) => {
    const isFront = idx === 0;
    const isRear = idx === bills.length - 1;

    const el = document.createElement("div");
    el.className = "flex-shrink-0 flex items-center";
    el.innerHTML = `
      <div class="px-4 py-3 rounded-xl border text-xs shadow-xl transition-all ${
        isFront 
          ? 'bg-gradient-to-br from-emerald-500/20 to-royal-900 border-emerald-400 text-emerald-200 ring-2 ring-emerald-500/40' 
          : 'bg-royal-850/80 border-slate-700 text-slate-200'
      }">
        <div class="flex items-center gap-1.5 font-bold">
          ${isFront ? '<span class="text-[9px] bg-emerald-400 text-royal-950 font-mono px-1.5 rounded">FRONT (Next)</span>' : ''}
          ${isRear && !isFront ? '<span class="text-[9px] bg-slate-700 text-slate-300 font-mono px-1 rounded">REAR</span>' : ''}
          <span>${bill.name}</span>
        </div>
        <div class="text-[11px] font-mono mt-1 ${isFront ? 'text-emerald-300' : 'text-slate-400'}">
          Rs. ${bill.amount.toFixed(2)} &bull; Due: ${bill.dueDate}
        </div>
      </div>
      ${!isRear ? '<span class="mx-2.5 text-amber-400/60 font-bold">➔</span>' : ''}
    `;
    container.appendChild(el);
  });
}

async function handlePayBill() {
  try {
    const res = await fetch(`${API_BASE}/api/bills/pay`, { method: "POST" });
    const data = await res.json();
    if (res.ok) {
      showNotification(data.message);
      fetchState();
    } else {
      showNotification(data.message || "No bills due!", true);
    }
  } catch (err) {
    showNotification("Pay failed: " + err.message, true);
  }
}

async function handleEnqueueBill(e) {
  if (e) e.preventDefault();
  const name = document.getElementById("billName").value.trim();
  const amount = parseFloat(document.getElementById("billAmount").value);
  const dueDate = document.getElementById("billDue").value;

  if (!name || isNaN(amount)) return;

  try {
    const res = await fetch(`${API_BASE}/api/bills/add`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, amount, dueDate, urgency: 3 })
    });

    if (res.ok) {
      document.getElementById("billName").value = "";
      document.getElementById("billAmount").value = "";
      document.getElementById("billDue").value = "";
      showNotification(`Enqueued '${name}' at rear of line`);
      fetchState();
    }
  } catch (err) {
    showNotification("Enqueue failed: " + err.message, true);
  }
}

// -------------------------------------------------------------
// TAB 3: TREE TRAVERSALS
// -------------------------------------------------------------
function showTraversal(type) {
  if (!currentState || !currentState.treeTraversals) return;
  const list = currentState.treeTraversals[type];
  const box = document.getElementById("traversalOutputBox");
  if (box && list) {
    box.innerHTML = `<span class="font-bold gold-gradient-text uppercase mr-2.5">[${type}]:</span> ${list.join(" ➔ ")}`;
  }
}

// -------------------------------------------------------------
// TAB 4: GRAPH & BFS
// -------------------------------------------------------------
function renderDebts(debts) {
  const list = document.getElementById("activeDebtsList");
  if (!list) return;
  list.innerHTML = "";
  if (!debts || !debts.length) {
    list.innerHTML = `<span class="text-xs text-slate-500 italic">No debt connections in network.</span>`;
    return;
  }
  debts.forEach(d => {
    const el = document.createElement("div");
    el.className = "p-3 rounded-xl bg-royal-850/80 border border-slate-800 flex items-center justify-between";
    el.innerHTML = `
      <span class="font-semibold text-slate-200">${d.from}</span>
      <span class="text-amber-400/80 font-mono text-xs flex items-center gap-1 font-bold">owes ➔</span>
      <span class="font-semibold text-amber-300">${d.to}</span>
    `;
    list.appendChild(el);
  });
}

async function handleFindBFSPath() {
  const debtor = document.getElementById("bfsDebtor").value;
  const creditor = document.getElementById("bfsCreditor").value;
  const box = document.getElementById("bfsResultBox");

  try {
    const res = await fetch(`${API_BASE}/api/graph/path?from=${debtor}&to=${creditor}`);
    const data = await res.json();

    if (data.found) {
      box.innerHTML = `<span class="font-bold gold-gradient-text">BFS Optimal Path:</span> <span class="text-slate-200 ml-2 font-semibold">${data.path.join(" ➔ owes ➔ ")}</span>`;
    } else {
      box.innerHTML = `<span class="text-rose-400">${data.message}</span>`;
    }
  } catch (err) {
    box.innerHTML = `<span class="text-rose-400">BFS Search failed: ${err.message}</span>`;
  }
}

// -------------------------------------------------------------
// TAB 5: GREEDY OPTIMIZATION
// -------------------------------------------------------------
async function handleGreedyOptimization() {
  const budgetInput = document.getElementById("greedyBudgetInput");
  const budget = budgetInput ? (parseFloat(budgetInput.value) || 14000) : 14000;

  try {
    const res = await fetch(`${API_BASE}/api/greedy?budget=${budget}`);
    const data = await res.json();

    const paidList = document.getElementById("greedyPaidList");
    const unpaidList = document.getElementById("greedyUnpaidList");
    const paidTotal = document.getElementById("greedyPaidTotal");
    const unpaidCount = document.getElementById("greedyUnpaidCount");

    if (paidTotal) paidTotal.innerText = `Total Settled: Rs. ${Number(data.totalSpent).toFixed(2)}`;
    if (unpaidCount) unpaidCount.innerText = `${data.unpaidCount} deferred`;

    if (paidList) {
      paidList.innerHTML = "";
      if (data.paidBills.length === 0) {
        paidList.innerHTML = `<span class="text-slate-500 italic">Budget too low to fund any bill.</span>`;
      } else {
        data.paidBills.forEach(b => {
          paidList.innerHTML += `
            <div class="flex items-center justify-between p-3 rounded-xl bg-royal-850 border border-emerald-500/20">
              <div>
                <span class="font-semibold text-slate-100">${b.name}</span>
                <span class="text-[10px] text-emerald-300 bg-emerald-950 px-2 py-0.5 rounded-full ml-1.5 font-mono border border-emerald-500/30">Priority ${b.urgency}/5</span>
              </div>
              <span class="font-mono font-bold text-emerald-400">Rs. ${Number(b.amount).toFixed(2)}</span>
            </div>
          `;
        });
      }
    }

    if (unpaidList) {
      unpaidList.innerHTML = "";
      if (data.unpaidBills.length === 0) {
        unpaidList.innerHTML = `<span class="text-slate-500 italic">All obligations successfully settled!</span>`;
      } else {
        data.unpaidBills.forEach(b => {
          unpaidList.innerHTML += `
            <div class="flex items-center justify-between p-3 rounded-xl bg-royal-850/50 border border-slate-800">
              <span class="text-slate-400">${b.name} (Priority ${b.urgency}/5)</span>
              <span class="font-mono text-slate-500">Rs. ${Number(b.amount).toFixed(2)}</span>
            </div>
          `;
        });
      }
    }
  } catch (err) {
    console.warn("Greedy calculation warning:", err.message);
  }
}

// -------------------------------------------------------------
// MERGE SORT TOGGLE
// -------------------------------------------------------------
async function handleSort(order) {
  try {
    const res = await fetch(`${API_BASE}/api/sort?order=${order}`);
    const data = await res.json();
    renderLinkedList(data.sortedExpenses);
    showNotification(`Expenses sorted via Merge Sort: O(n log n) (${order.toUpperCase()})`);
  } catch (err) {
    showNotification("Sort failed: " + err.message, true);
  }
}

// -------------------------------------------------------------
// NOTIFICATIONS
// -------------------------------------------------------------
function showNotification(msg, isError = false) {
  const alert = document.getElementById("statusAlert");
  const text = document.getElementById("statusMessage");
  if (!alert || !text) return;
  text.innerText = msg;

  if (isError) {
    alert.className = "mb-6 p-4 rounded-xl text-xs font-medium border border-rose-500/30 bg-rose-950/80 text-rose-300 shadow-2xl flex items-center justify-between transition-all";
  } else {
    alert.className = "mb-6 p-4 rounded-xl text-xs font-medium border border-amber-500/30 bg-royal-900/90 text-amber-200 shadow-2xl flex items-center justify-between transition-all";
  }
  alert.classList.remove("hidden");

  setTimeout(() => {
    alert.classList.add("hidden");
  }, 4000);
}

function dismissAlert() {
  const alert = document.getElementById("statusAlert");
  if (alert) alert.classList.add("hidden");
}

// Attach all methods globally to window
window.switchTab = switchTab;
window.handleAddExpense = handleAddExpense;
window.handleDeleteExpense = handleDeleteExpense;
window.handleUndo = handleUndo;
window.handlePayBill = handlePayBill;
window.handleEnqueueBill = handleEnqueueBill;
window.showTraversal = showTraversal;
window.handleFindBFSPath = handleFindBFSPath;
window.handleGreedyOptimization = handleGreedyOptimization;
window.handleSort = handleSort;
window.dismissAlert = dismissAlert;
