package com.Clinivora.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircularQueueTest {

    @Test
    void enqueueDequeueWrapAround() {
        CircularQueue<Integer> queue = new CircularQueue<>(3);
        queue.enqueue(1);
        queue.enqueue(2);
        assertEquals(1, queue.dequeue());
        queue.enqueue(3);
        queue.enqueue(4);
        assertTrue(queue.isFull());
        assertEquals(2, queue.dequeue());
        assertEquals(3, queue.dequeue());
        assertEquals(4, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void fullAndEmptyGuards() {
        CircularQueue<String> queue = new CircularQueue<>(1);
        queue.enqueue("x");
        assertThrows(IllegalStateException.class, () -> queue.enqueue("y"));
        queue.dequeue();
        assertThrows(java.util.NoSuchElementException.class, queue::dequeue);
    }
}
