package com.Clinivora.dsa;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Indexed min-heap backed by DynamicArray + HashTable (custom DSA).
 * Default comparator: priority rank, then arrival time, then caseId.
 */
public class IndexedMinHeap<T> {
    public interface CaseKeyExtractor<T> {
        long caseId(T item);
    }

    private final DynamicArray<T> heap;
    private final HashTable<Long, Integer> indexByCaseId;
    private final Comparator<T> comparator;
    private final CaseKeyExtractor<T> keyExtractor;

    public IndexedMinHeap(Comparator<T> comparator, CaseKeyExtractor<T> keyExtractor) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
        this.keyExtractor = Objects.requireNonNull(keyExtractor, "keyExtractor");
        this.heap = new DynamicArray<>();
        this.indexByCaseId = new HashTable<>();
    }

    public static <T extends PrioritizedCase> IndexedMinHeap<T> forCases() {
        Comparator<T> comparator = Comparator
                .comparingInt((T item) -> item.priorityRank())
                .thenComparing(T::arrivalTime)
                .thenComparingLong(T::caseId);
        return new IndexedMinHeap<>(comparator, T::caseId);
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public boolean contains(long caseId) {
        return indexByCaseId.containsKey(caseId);
    }

    public void push(T item) {
        Objects.requireNonNull(item, "item");
        long caseId = keyExtractor.caseId(item);
        if (indexByCaseId.containsKey(caseId)) {
            throw new IllegalArgumentException("Case already in heap: " + caseId);
        }
        heap.add(item);
        int index = heap.size() - 1;
        indexByCaseId.put(caseId, index);
        siftUp(index);
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return heap.get(0);
    }

    public T pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        T root = heap.get(0);
        removeAt(0);
        return root;
    }

    public T removeByCaseId(long caseId) {
        Integer index = indexByCaseId.get(caseId);
        if (index == null) {
            throw new NoSuchElementException("Case not found: " + caseId);
        }
        T item = heap.get(index);
        removeAt(index);
        return item;
    }

    public void update(T item) {
        Objects.requireNonNull(item, "item");
        long caseId = keyExtractor.caseId(item);
        Integer index = indexByCaseId.get(caseId);
        if (index == null) {
            throw new NoSuchElementException("Case not found: " + caseId);
        }
        heap.set(index, item);
        siftUp(index);
        siftDown(index);
    }

    public void clear() {
        heap.clear();
        indexByCaseId.clear();
    }

    private void removeAt(int index) {
        int last = heap.size() - 1;
        T removed = heap.get(index);
        swap(index, last);
        heap.removeAt(last);
        indexByCaseId.remove(keyExtractor.caseId(removed));
        if (index < heap.size()) {
            siftUp(index);
            siftDown(index);
        }
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare(heap.get(index), heap.get(parent)) >= 0) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        int size = heap.size();
        while (true) {
            int left = index * 2 + 1;
            int right = left + 1;
            int smallest = index;
            if (left < size && comparator.compare(heap.get(left), heap.get(smallest)) < 0) {
                smallest = left;
            }
            if (right < size && comparator.compare(heap.get(right), heap.get(smallest)) < 0) {
                smallest = right;
            }
            if (smallest == index) {
                break;
            }
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int i, int j) {
        if (i == j) {
            return;
        }
        T a = heap.get(i);
        T b = heap.get(j);
        heap.set(i, b);
        heap.set(j, a);
        indexByCaseId.put(keyExtractor.caseId(a), j);
        indexByCaseId.put(keyExtractor.caseId(b), i);
    }

    /** Marker for default case priority ordering. */
    public interface PrioritizedCase {
        int priorityRank();

        java.time.LocalDateTime arrivalTime();

        long caseId();
    }
}
