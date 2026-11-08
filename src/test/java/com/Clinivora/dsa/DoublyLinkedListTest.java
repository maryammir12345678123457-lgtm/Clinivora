package com.Clinivora.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoublyLinkedListTest {

    @Test
    void addRemoveEnds() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("b");
        list.addFirst("a");
        list.addLast("c");
        assertEquals(3, list.size());
        assertEquals("a", list.peekFirst());
        assertEquals("c", list.peekLast());
        assertEquals("a", list.removeFirst());
        assertEquals("c", list.removeLast());
        assertEquals("b", list.peekFirst());
    }

    @Test
    void containsAndRemoveMiddle() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        assertTrue(list.contains(2));
        assertTrue(list.remove(2));
        assertFalse(list.contains(2));
        assertEquals(2, list.size());
    }
}
