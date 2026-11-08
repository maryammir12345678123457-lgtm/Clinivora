package com.Clinivora.dsa;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Binary search tree (custom DSA). Does not use ArrayList/HashMap internally.
 */
public class BinarySearchTree<T> {
    private static final class Node<E> {
        E value;
        Node<E> left;
        Node<E> right;

        Node(E value) {
            this.value = value;
        }
    }

    private final Comparator<T> comparator;
    private Node<T> root;
    private int size;

    public BinarySearchTree() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public BinarySearchTree(Comparator<T> comparator) {
        this.comparator = comparator != null
                ? comparator
                : (a, b) -> ((Comparable<? super T>) a).compareTo(b);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void insert(T value) {
        Objects.requireNonNull(value, "value");
        root = insert(root, value);
    }

    private Node<T> insert(Node<T> node, T value) {
        if (node == null) {
            size++;
            return new Node<>(value);
        }
        int cmp = comparator.compare(value, node.value);
        if (cmp < 0) {
            node.left = insert(node.left, value);
        } else if (cmp > 0) {
            node.right = insert(node.right, value);
        } else {
            node.value = value;
        }
        return node;
    }

    public boolean contains(T value) {
        Objects.requireNonNull(value, "value");
        Node<T> current = root;
        while (current != null) {
            int cmp = comparator.compare(value, current.value);
            if (cmp == 0) {
                return true;
            }
            current = cmp < 0 ? current.left : current.right;
        }
        return false;
    }

    public T find(T value) {
        Objects.requireNonNull(value, "value");
        Node<T> current = root;
        while (current != null) {
            int cmp = comparator.compare(value, current.value);
            if (cmp == 0) {
                return current.value;
            }
            current = cmp < 0 ? current.left : current.right;
        }
        return null;
    }

    public boolean remove(T value) {
        Objects.requireNonNull(value, "value");
        int before = size;
        root = remove(root, value);
        return size < before;
    }

    private Node<T> remove(Node<T> node, T value) {
        if (node == null) {
            return null;
        }
        int cmp = comparator.compare(value, node.value);
        if (cmp < 0) {
            node.left = remove(node.left, value);
        } else if (cmp > 0) {
            node.right = remove(node.right, value);
        } else {
            size--;
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            Node<T> successor = min(node.right);
            node.value = successor.value;
            node.right = removeMin(node.right);
        }
        return node;
    }

    public T min() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return min(root).value;
    }

    public T max() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        Node<T> current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.value;
    }

    public DynamicArray<T> inOrder() {
        DynamicArray<T> result = new DynamicArray<>(Math.max(1, size));
        inOrder(root, result);
        return result;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    private void inOrder(Node<T> node, DynamicArray<T> out) {
        if (node == null) {
            return;
        }
        inOrder(node.left, out);
        out.add(node.value);
        inOrder(node.right, out);
    }

    private Node<T> min(Node<T> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node<T> removeMin(Node<T> node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = removeMin(node.left);
        return node;
    }
}
