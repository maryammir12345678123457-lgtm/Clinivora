package com.Clinivora.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DynamicArrayTest {

    @Test
    void addGetRemoveAndGrow() {
        DynamicArray<Integer> array = new DynamicArray<>(2);
        array.add(1);
        array.add(2);
        array.add(3);
        assertEquals(3, array.size());
        assertEquals(2, array.get(1));
        assertEquals(Integer.valueOf(2), array.removeAt(1));
        assertEquals(2, array.size());
        assertEquals(3, array.get(1));
        assertTrue(array.contains(1));
        assertFalse(array.contains(2));
    }

    @Test
    void insertAtIndexAndClear() {
        DynamicArray<String> array = new DynamicArray<>();
        array.add("a");
        array.add("c");
        array.add(1, "b");
        assertEquals("b", array.get(1));
        array.clear();
        assertTrue(array.isEmpty());
    }
}
