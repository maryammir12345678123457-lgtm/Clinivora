package com.Clinivora.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTreeTest {

    @Test
    void insertContainsInOrder() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        tree.insert(5);
        tree.insert(2);
        tree.insert(8);
        tree.insert(1);
        assertTrue(tree.contains(2));
        assertFalse(tree.contains(9));
        DynamicArray<Integer> order = tree.inOrder();
        assertEquals(4, order.size());
        assertEquals(1, order.get(0));
        assertEquals(2, order.get(1));
        assertEquals(5, order.get(2));
        assertEquals(8, order.get(3));
    }

    @Test
    void removeAndMinMax() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        tree.insert(4);
        tree.insert(2);
        tree.insert(6);
        tree.insert(5);
        assertTrue(tree.remove(4));
        assertFalse(tree.contains(4));
        assertEquals(2, tree.min());
        assertEquals(6, tree.max());
    }
}
