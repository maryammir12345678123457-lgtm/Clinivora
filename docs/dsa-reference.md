# Clinivora DSA Reference

All structures live in `com.Clinivora.dsa` and avoid `ArrayList` / `HashMap` as internals.

## DynamicArray\<T\>

Resizable array with doubling growth.

| Operation   | Average | Worst |
|-------------|---------|-------|
| get/set     | O(1)    | O(1)  |
| add (end)   | O(1)*   | O(n)  |
| insert/remove | O(n)  | O(n)  |

\*amortized

## DoublyLinkedList\<T\>

Node-based list with head/tail pointers.

| Operation        | Complexity |
|------------------|------------|
| addFirst/addLast | O(1)       |
| removeFirst/Last | O(1)       |
| contains/remove  | O(n)       |

Used for patient timeline presentation order.

## CircularQueue\<T\>

Fixed-capacity ring buffer.

| Operation | Complexity |
|-----------|------------|
| enqueue   | O(1)       |
| dequeue   | O(1)       |
| peek      | O(1)       |

Used by routine queue “serve next” simulation.

## ArrayStack\<T\>

Array-backed LIFO stack.

| Operation | Complexity |
|-----------|------------|
| push/pop/peek | O(1)* |

Used for navigation history and patient-form undo.

## HashTable\<K,V\>

Open addressing with linear probing + tombstones.

| Operation     | Average | Worst |
|---------------|---------|-------|
| put/get/remove| O(1)    | O(n)  |

Used by IndexedMinHeap index map and report aggregation demos.

## IndexedMinHeap\<T\>

Binary heap over `DynamicArray` with `HashTable` case-id index.

Comparator: priority rank → arrival time → caseId.

| Operation      | Complexity |
|----------------|------------|
| push/pop/update| O(log n)   |
| contains       | O(1)       |

Powers the Emergency Room priority queue.

## BinarySearchTree\<T\>

Ordered BST with optional comparator.

| Operation          | Average | Worst |
|--------------------|---------|-------|
| insert/search/delete | O(log n) | O(n) |

## AdjacencyListGraph\<V\>

Adjacency lists stored as `HashTable<V, DynamicArray<V>>`.

| Operation | Complexity |
|-----------|------------|
| addVertex/addEdge | O(1) amortized |
| BFS/DFS | O(V+E) |

Used in DSA Lab care-flow demos.

## Lab benchmark

DSA Laboratory runs a synthetic workload comparing:

1. `IndexedMinHeap` push+pop of *n* prioritized cases
2. `ArrayList` insert + sort + clear with the same comparator

Executed on a background JavaFX `Task` so the UI stays responsive.
