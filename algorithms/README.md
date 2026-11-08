# Clinivora algorithms

Same order on every machine. Class files live in `src/main/java/com/Clinivora/dsa`. This folder is the aligned index for the course.

| # | Course name | Class | Algorithm |
|---|-------------|-------|-----------|
| 1 | ArrayList | DynamicArray.java | append / index access |
| 2 | HashMap | HashTable.java | chaining lookup |
| 3 | Priority Queue | IndexedMinHeap.java | ESI extract-min (triage) |
| 4 | Linked List | DoublyLinkedList.java | timeline insert at ends |
| 5 | Stack | ArrayStack.java | LIFO undo |

Primary product algorithm: `TriageService.buildPriorityQueue` pushes triaged cases into IndexedMinHeap (P1 first, then earlier arrival) and pops them in order. O(log n) per push/pop.

Copies of the five classes are beside this file so the folder can be opened on its own.
