package com.Clinivora.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdjacencyListGraphTest {

    @Test
    void undirectedEdgesAndNeighbors() {
        AdjacencyListGraph<String> graph = new AdjacencyListGraph<>(false);
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        assertTrue(graph.hasEdge("A", "B"));
        assertTrue(graph.hasEdge("B", "A"));
        assertEquals(2, graph.neighbors("A").size());
        assertEquals(2, graph.edgeCount());
    }

    @Test
    void bfsAndDfs() {
        AdjacencyListGraph<String> graph = new AdjacencyListGraph<>(true);
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        DynamicArray<String> bfs = graph.breadthFirst("A");
        assertEquals("A", bfs.get(0));
        assertTrue(bfs.contains("D"));
        DynamicArray<String> dfs = graph.depthFirst("A");
        assertEquals("A", dfs.get(0));
        assertEquals(4, dfs.size());
    }
}
