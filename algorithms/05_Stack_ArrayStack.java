package com.Clinivora.dsa;

import java.util.NoSuchElementException;

/**
 * Array-backed stack (custom DSA). Does not use ArrayList/HashMap internally.
 */
public class ArrayStack<T> {
    private static final int DEFAULT_CAPACITY = 8;

    private Object[] elements;
    private int size;

    public ArrayStack() {
        this(DEFAULT_CAPACITY);
    }

    public ArrayStack(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be >= 1");
        }
        this.elements = new Object[capacity];
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void push(T value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        T value = (T) elements[--size];
        elements[size] = null;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return (T) elements[size - 1];
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= elements.length) {
            return;
        }
        int newCapacity = elements.length * 2;
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        Object[] next = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            next[i] = elements[i];
        }
        elements = next;
    }
}
