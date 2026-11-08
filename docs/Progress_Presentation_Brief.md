# Clinivora — Progress Presentation Brief

**For:** Group leader progress talk (professor may propose changes)  
**Stack:** Java 21 · JavaFX FXML · SQLite · custom DSAs · Maven

---

## 1. Open (30 seconds)

- Introduce the **group leader**.
- **Topic:** Clinivora — a desktop emergency / clinic queue management app.
- **Goal:** Register patients, triage ER cases by ESI priority, assign doctors, run a routine queue, and report on operations.
- This is a **progress** presentation, not the final demo.

---

## 2. Architecture — FXML + Controllers (what the professor asked for)

We code in **Java**, but the **user interface is FXML**.

| Layer | What it is | Example |
|-------|------------|---------|
| **View** | `.fxml` layout (labels, tables, buttons) | `LoginView.fxml`, `EmergencyRoomView.fxml` |
| **Controller** | `.java` mapped by `fx:controller` — stands between FXML and business logic | `LoginController.java` |
| **Service / DSA** | Domain logic + custom data structures | `TriageService`, `IndexedMinHeap` |
| **Database** | SQLite persistence | `data/Clinivora.db` |

**Rule we follow:** multiple screens → **multiple FXML files** (14 views). Controllers never build the whole window in pure Java; they bind and control the FXML UI.

**Live demo tip:** Open `LoginView.fxml` next to `LoginController.java` and show `fx:controller="com.Clinivora.ui.LoginController"`.

```
FXML views  →  Java Controllers  →  Services  →  Custom DSAs + SQLite
```

---

## 3. Data structures (at least four — we present five)

We implement **course DSAs ourselves** (not `java.util.ArrayList` / `HashMap` as the structure internals). DSA Lab shows **course names** with our class names in parentheses.

| Course name (say this) | Our class | Where it is used in Clinivora | Why this structure |
|------------------------|-----------|-------------------------------|--------------------|
| **ArrayList** | `DynamicArray` | Growing collections (lab, graph vertices, list building) | Fast index access; append is amortized O(1) |
| **HashMap** | `HashTable` | ESI counts in reports; heap case→index map; graph adjacency | Average O(1) key lookup |
| **Priority Queue** | `IndexedMinHeap` | **ER Priority Queue** — who is seen next | Always extract highest urgency (then earliest arrival) |
| **Linked List** | `DoublyLinkedList` | Patient / case **timeline** events | Efficient insert at ends; natural event sequence |
| **Stack** | `ArrayStack` | Patients form **Undo**; optional nav history | LIFO — last edit undone first |

**Bonus (if asked):** CircularQueue (routine waiting line), Binary Search Tree, Graph + BFS/DFS — all in **DSA Laboratory**.

**Live demo tip:** Open **DSA Laboratory** and pick each of the five from the combo box.

---

## 4. Algorithm (required)

### Primary: ESI triage with Priority Queue (Indexed Min-Heap)

**Problem:** Among triaged ER cases, which patient should be next?

**Steps (`TriageService.buildPriorityQueue`):**

1. Load emergency cases with status **TRIAGED**.
2. For each case with an ESI assessment (P1–P5), **push** into an Indexed Min-Heap.
3. Ordering key: **priority rank** (P1 before P5), then **earlier arrival time**.
4. **Pop** repeatedly until empty → ordered list for the ER Priority Queue table.

**Complexity:** push / pop ≈ **O(log n)** each; building the ordered board ≈ **O(n log n)**.

**Why not a plain sorted ArrayList every time?** Heap supports efficient priority updates and repeated “next patient” extracts; we also benchmark heap vs sorted list in DSA Lab.

**Live demo tip:** Show **Emergency Room → Priority Queue**, then open DSA Lab → **Priority Queue (IndexedMinHeap)** and run push/pop.

### Alternate (if time): Graph BFS

In DSA Lab → **Graph (AdjacencyList + BFS/DFS)** — breadth-first search visits neighbors level by level, **O(V+E)**.

---

## 5. Progress so far (status update)

- Multi-screen **FXML** app with role-based navigation (Admin, Reception, Triage, Doctor)
- **SQLite** persistence, demo accounts, soft-delete / accounts / audit
- **Themes** (Light, Dark, Pink, Colorful) and **languages** (English, Urdu RTL, Chinese)
- **Dashboard** ops board (KPIs, ESI mix, arrivals chart, waits, pipeline, quick actions)
- **Emergency Room** boards: Needs Triage · Priority Queue · Active floor
- **Reports** with charts + PDF/CSV export
- **DSA Laboratory** for teaching demos + unit tests for core services/DSAs

We welcome feedback and modifications from the professor.

---

## 6. Suggested speaking order (≈ 5–7 minutes)

1. Introduce leader + Clinivora topic  
2. FXML ↔ Controller architecture (one screen)  
3. Five DSAs with one product use each  
4. Triage Priority Queue algorithm  
5. Current progress + “open to changes”  

---

## 7. Quick Q&A cheat sheet

| If they ask… | Say… |
|--------------|------|
| Why not Java `ArrayList`? | We built custom structures to learn internals; course API matches ArrayList / HashMap / etc. |
| Where is FXML? | `src/main/resources/com/Clinivora/view/*.fxml` |
| Where is the heap used live? | Emergency Room Priority Queue via `TriageService` |
| Database? | SQLite file under `data/` — not a DSA; DSAs are in-memory structures in Java |
| Unit tests? | Yes — DSA tests + service tests including ER board coverage |

---

## Demo credentials (if showing the app)

| User | Password | Role |
|------|----------|------|
| admin | AdminDemo!2026 | ADMIN |
| triage | TriageDemo!2026 | TRIAGE |
| reception | ReceptionDemo!2026 | RECEPTION |
| doctor | DoctorDemo!2026 | DOCTOR |

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;C:\Tools\apache-maven-3.9.6\bin;" + $env:Path
cd "D:\Maryam Project\Clinivora"
mvn javafx:run
```
