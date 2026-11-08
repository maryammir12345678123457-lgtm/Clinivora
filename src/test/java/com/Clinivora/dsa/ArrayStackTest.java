package com.Clinivora.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayStackTest {

    @Test
    void pushPopPeek() {
        ArrayStack<Integer> stack = new ArrayStack<>(2);
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(3, stack.peek());
        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.size());
    }

    @Test
    void emptyPopThrows() {
        ArrayStack<String> stack = new ArrayStack<>();
        assertThrows(java.util.NoSuchElementException.class, stack::pop);
    }
}
