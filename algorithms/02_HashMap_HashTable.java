package com.Clinivora.dsa;

import java.util.Objects;

/**
 * Open-addressing hash table with linear probing (custom DSA).
 * Does not use ArrayList/HashMap internally.
 */
public class HashTable<K, V> {
    private static final Object TOMBSTONE = new Object();
    private static final double LOAD_FACTOR = 0.7;

    private Object[] keys;
    private Object[] values;
    private int size;
    private int capacity;

    public HashTable() {
        this(16);
    }

    public HashTable(int capacity) {
        if (capacity < 4) {
            capacity = 4;
        }
        this.capacity = nextPowerOfTwo(capacity);
        this.keys = new Object[this.capacity];
        this.values = new Object[this.capacity];
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        if ((size + 1.0) / capacity > LOAD_FACTOR) {
            resize(capacity * 2);
        }
        int index = findSlot(key);
        if (keys[index] == null || keys[index] == TOMBSTONE) {
            size++;
        }
        keys[index] = key;
        values[index] = value;
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        Objects.requireNonNull(key, "key");
        int index = findExisting(key);
        if (index < 0) {
            return null;
        }
        return (V) values[index];
    }

    public boolean containsKey(K key) {
        return findExisting(key) >= 0;
    }

    @SuppressWarnings("unchecked")
    public V remove(K key) {
        Objects.requireNonNull(key, "key");
        int index = findExisting(key);
        if (index < 0) {
            return null;
        }
        V old = (V) values[index];
        keys[index] = TOMBSTONE;
        values[index] = null;
        size--;
        return old;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            keys[i] = null;
            values[i] = null;
        }
        size = 0;
    }

    public DynamicArray<K> keys() {
        DynamicArray<K> result = new DynamicArray<>(Math.max(1, size));
        for (int i = 0; i < capacity; i++) {
            if (keys[i] != null && keys[i] != TOMBSTONE) {
                @SuppressWarnings("unchecked")
                K key = (K) keys[i];
                result.add(key);
            }
        }
        return result;
    }

    private int findSlot(K key) {
        int index = hash(key);
        int firstTombstone = -1;
        while (keys[index] != null) {
            if (keys[index] == TOMBSTONE) {
                if (firstTombstone < 0) {
                    firstTombstone = index;
                }
            } else if (Objects.equals(keys[index], key)) {
                return index;
            }
            index = (index + 1) & (capacity - 1);
        }
        return firstTombstone >= 0 ? firstTombstone : index;
    }

    private int findExisting(K key) {
        int index = hash(key);
        while (keys[index] != null) {
            if (keys[index] != TOMBSTONE && Objects.equals(keys[index], key)) {
                return index;
            }
            index = (index + 1) & (capacity - 1);
        }
        return -1;
    }

    private int hash(K key) {
        int h = key.hashCode();
        h ^= (h >>> 16);
        return h & (capacity - 1);
    }

    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        Object[] oldKeys = keys;
        Object[] oldValues = values;
        int oldCapacity = capacity;
        capacity = nextPowerOfTwo(newCapacity);
        keys = new Object[capacity];
        values = new Object[capacity];
        size = 0;
        for (int i = 0; i < oldCapacity; i++) {
            if (oldKeys[i] != null && oldKeys[i] != TOMBSTONE) {
                put((K) oldKeys[i], (V) oldValues[i]);
            }
        }
    }

    private static int nextPowerOfTwo(int value) {
        int n = 1;
        while (n < value) {
            n <<= 1;
        }
        return n;
    }
}
