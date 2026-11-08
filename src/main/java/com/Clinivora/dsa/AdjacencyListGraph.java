package com.Clinivora.dsa;

import java.util.Objects;

/**
 * Directed/undirected adjacency-list graph (custom DSA).
 * Internals use DynamicArray + HashTable only (no ArrayList/HashMap).
 */
public class AdjacencyListGraph<V> {
    private final HashTable<V, DynamicArray<V>> adjacency;
    private final DynamicArray<V> vertices;
    private final boolean directed;
    private int edgeCount;

    public AdjacencyListGraph() {
        this(false);
    }

    public AdjacencyListGraph(boolean directed) {
        this.directed = directed;
        this.adjacency = new HashTable<>();
        this.vertices = new DynamicArray<>();
        this.edgeCount = 0;
    }

    public boolean isDirected() {
        return directed;
    }

    public int vertexCount() {
        return vertices.size();
    }

    public int edgeCount() {
        return edgeCount;
    }

    public void addVertex(V vertex) {
        Objects.requireNonNull(vertex, "vertex");
        if (adjacency.containsKey(vertex)) {
            return;
        }
        adjacency.put(vertex, new DynamicArray<>());
        vertices.add(vertex);
    }

    public boolean hasVertex(V vertex) {
        return adjacency.containsKey(vertex);
    }

    public void addEdge(V from, V to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        addVertex(from);
        addVertex(to);
        DynamicArray<V> fromList = adjacency.get(from);
        if (!fromList.contains(to)) {
            fromList.add(to);
            edgeCount++;
        }
        if (!directed) {
            DynamicArray<V> toList = adjacency.get(to);
            if (!toList.contains(from)) {
                toList.add(from);
            }
        }
    }

    public boolean hasEdge(V from, V to) {
        DynamicArray<V> neighbors = adjacency.get(from);
        return neighbors != null && neighbors.contains(to);
    }

    public DynamicArray<V> neighbors(V vertex) {
        DynamicArray<V> neighbors = adjacency.get(vertex);
        if (neighbors == null) {
            throw new IllegalArgumentException("Unknown vertex: " + vertex);
        }
        DynamicArray<V> copy = new DynamicArray<>(Math.max(1, neighbors.size()));
        for (V n : neighbors) {
            copy.add(n);
        }
        return copy;
    }

    public DynamicArray<V> vertices() {
        DynamicArray<V> copy = new DynamicArray<>(Math.max(1, vertices.size()));
        for (V v : vertices) {
            copy.add(v);
        }
        return copy;
    }

    public DynamicArray<V> breadthFirst(V start) {
        if (!hasVertex(start)) {
            throw new IllegalArgumentException("Unknown vertex: " + start);
        }
        DynamicArray<V> order = new DynamicArray<>();
        HashTable<V, Boolean> visited = new HashTable<>();
        CircularQueue<V> queue = new CircularQueue<>(Math.max(4, vertices.size() * 2 + 1));
        queue.enqueue(start);
        visited.put(start, Boolean.TRUE);
        while (!queue.isEmpty()) {
            V current = queue.dequeue();
            order.add(current);
            DynamicArray<V> neighbors = adjacency.get(current);
            for (V next : neighbors) {
                if (!visited.containsKey(next)) {
                    visited.put(next, Boolean.TRUE);
                    queue.enqueue(next);
                }
            }
        }
        return order;
    }

    public DynamicArray<V> depthFirst(V start) {
        if (!hasVertex(start)) {
            throw new IllegalArgumentException("Unknown vertex: " + start);
        }
        DynamicArray<V> order = new DynamicArray<>();
        HashTable<V, Boolean> visited = new HashTable<>();
        ArrayStack<V> stack = new ArrayStack<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            V current = stack.pop();
            if (visited.containsKey(current)) {
                continue;
            }
            visited.put(current, Boolean.TRUE);
            order.add(current);
            DynamicArray<V> neighbors = adjacency.get(current);
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                V next = neighbors.get(i);
                if (!visited.containsKey(next)) {
                    stack.push(next);
                }
            }
        }
        return order;
    }

    public void clear() {
        adjacency.clear();
        vertices.clear();
        edgeCount = 0;
    }
}
