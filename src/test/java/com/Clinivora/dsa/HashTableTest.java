package com.Clinivora.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashTableTest {

    @Test
    void putGetRemove() {
        HashTable<String, Integer> table = new HashTable<>();
        table.put("a", 1);
        table.put("b", 2);
        table.put("a", 3);
        assertEquals(2, table.size());
        assertEquals(3, table.get("a"));
        assertTrue(table.containsKey("b"));
        assertEquals(2, table.remove("b"));
        assertFalse(table.containsKey("b"));
    }

    @Test
    void growsAndListsKeys() {
        HashTable<Integer, String> table = new HashTable<>(4);
        for (int i = 0; i < 20; i++) {
            table.put(i, "v" + i);
        }
        assertEquals(20, table.size());
        assertEquals("v10", table.get(10));
        assertEquals(20, table.keys().size());
    }
}
