package com.Clinivora.ui;

import com.Clinivora.dsa.AdjacencyListGraph;
import com.Clinivora.dsa.ArrayStack;
import com.Clinivora.dsa.BinarySearchTree;
import com.Clinivora.dsa.CircularQueue;
import com.Clinivora.dsa.DoublyLinkedList;
import com.Clinivora.dsa.DynamicArray;
import com.Clinivora.dsa.HashTable;
import com.Clinivora.dsa.IndexedMinHeap;
import com.Clinivora.ui.helper.AlertHelper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DsaLabController {
    private enum Structure {
        /** Course name ArrayList — custom DynamicArray implementation. */
        DYNAMIC_ARRAY("ArrayList (DynamicArray)", "Access O(1), append amortized O(1), insert/remove O(n)"),
        /** Course name Linked List — custom DoublyLinkedList implementation. */
        DOUBLY_LINKED_LIST("Linked List (DoublyLinkedList)", "Insert/remove at ends O(1), search O(n)"),
        CIRCULAR_QUEUE("Queue (CircularQueue)", "Enqueue/dequeue/peek O(1)"),
        /** Course name Stack — custom ArrayStack implementation. */
        ARRAY_STACK("Stack (ArrayStack)", "Push/pop/peek amortized O(1)"),
        /** Course name HashMap — custom HashTable implementation. */
        HASH_TABLE("HashMap (HashTable)", "Average put/get/remove O(1), worst O(n)"),
        /** Course name Priority Queue — custom IndexedMinHeap implementation. */
        INDEXED_MIN_HEAP("Priority Queue (IndexedMinHeap)", "Push/pop/update O(log n), contains O(1)"),
        BINARY_SEARCH_TREE("Binary Search Tree", "Average insert/search/delete O(log n), worst O(n)"),
        ADJACENCY_LIST_GRAPH("Graph (AdjacencyList + BFS/DFS)", "Add edge O(1) amortized, BFS/DFS O(V+E)");

        private final String label;
        private final String complexity;

        Structure(String label, String complexity) {
            this.label = label;
            this.complexity = complexity;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class LabCase implements IndexedMinHeap.PrioritizedCase {
        private final long caseId;
        private final int priorityRank;
        private final LocalDateTime arrivalTime;

        private LabCase(long caseId, int priorityRank, LocalDateTime arrivalTime) {
            this.caseId = caseId;
            this.priorityRank = priorityRank;
            this.arrivalTime = arrivalTime;
        }

        @Override
        public int priorityRank() {
            return priorityRank;
        }

        @Override
        public LocalDateTime arrivalTime() {
            return arrivalTime;
        }

        @Override
        public long caseId() {
            return caseId;
        }

        @Override
        public String toString() {
            return "C" + caseId + "/P" + priorityRank;
        }
    }

    @FXML
    private ComboBox<Structure> structureBox;
    @FXML
    private ComboBox<String> operationBox;
    @FXML
    private TextField valueField;
    @FXML
    private Label complexityLabel;
    @FXML
    private FlowPane visualizerPane;
    @FXML
    private TextArea stateArea;
    @FXML
    private ProgressBar benchmarkProgress;
    @FXML
    private Label benchmarkLabel;

    private DynamicArray<String> dynamicArray = new DynamicArray<>();
    private DoublyLinkedList<String> linkedList = new DoublyLinkedList<>();
    private CircularQueue<String> circularQueue = new CircularQueue<>(8);
    private ArrayStack<String> arrayStack = new ArrayStack<>();
    private HashTable<String, String> hashTable = new HashTable<>();
    private IndexedMinHeap<LabCase> minHeap = IndexedMinHeap.forCases();
    private BinarySearchTree<Integer> bst = new BinarySearchTree<>();
    private AdjacencyListGraph<String> graph = new AdjacencyListGraph<>(false);
    private long nextCaseId = 1;

    @FXML
    private void initialize() {
        structureBox.setItems(FXCollections.observableArrayList(Structure.values()));
        structureBox.valueProperty().addListener((o, a, b) -> {
            updateOperations();
            refreshVisualizer();
        });
        structureBox.getSelectionModel().selectFirst();
        onReset();
    }

    private void updateOperations() {
        Structure structure = structureBox.getValue();
        if (structure == null) {
            return;
        }
        complexityLabel.setText("Complexity: " + structure.complexity);
        List<String> ops = switch (structure) {
            case DYNAMIC_ARRAY -> List.of("add", "removeLast", "contains");
            case DOUBLY_LINKED_LIST -> List.of("addLast", "addFirst", "removeFirst", "contains");
            case CIRCULAR_QUEUE -> List.of("enqueue", "dequeue", "peek");
            case ARRAY_STACK -> List.of("push", "pop", "peek");
            case HASH_TABLE -> List.of("put", "get", "remove");
            case INDEXED_MIN_HEAP -> List.of("push", "pop", "peek");
            case BINARY_SEARCH_TREE -> List.of("insert", "contains", "remove", "inOrder");
            case ADJACENCY_LIST_GRAPH -> List.of("addVertex", "addEdge", "bfs", "dfs");
        };
        operationBox.setItems(FXCollections.observableArrayList(ops));
        operationBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void onReset() {
        dynamicArray = new DynamicArray<>();
        linkedList = new DoublyLinkedList<>();
        circularQueue = new CircularQueue<>(8);
        arrayStack = new ArrayStack<>();
        hashTable = new HashTable<>();
        minHeap = IndexedMinHeap.forCases();
        bst = new BinarySearchTree<>();
        graph = new AdjacencyListGraph<>(false);
        nextCaseId = 1;

        dynamicArray.add("A");
        dynamicArray.add("B");
        dynamicArray.add("C");
        linkedList.addLast("A");
        linkedList.addLast("B");
        linkedList.addLast("C");
        circularQueue.enqueue("A");
        circularQueue.enqueue("B");
        arrayStack.push("A");
        arrayStack.push("B");
        hashTable.put("k1", "v1");
        hashTable.put("k2", "v2");
        LocalDateTime now = LocalDateTime.now();
        minHeap.push(new LabCase(nextCaseId++, 2, now));
        minHeap.push(new LabCase(nextCaseId++, 1, now.plusMinutes(1)));
        bst.insert(5);
        bst.insert(2);
        bst.insert(8);
        graph.addEdge("ER", "Triage");
        graph.addEdge("Triage", "Doctor");
        graph.addEdge("ER", "Queue");
        refreshVisualizer();
    }

    @FXML
    private void onGuide() {
        Structure structure = structureBox.getValue();
        if (structure == null) {
            return;
        }
        String help = switch (structure) {
            case DYNAMIC_ARRAY -> "ArrayList: type a value, choose add, press Run. removeLast drops the end.";
            case DOUBLY_LINKED_LIST -> "Linked List: addFirst / addLast. removeFirst takes the front.";
            case CIRCULAR_QUEUE -> "Queue: enqueue adds, dequeue serves the next person.";
            case ARRAY_STACK -> "Stack: push, then pop undoes the last item (same idea as patient-form Undo).";
            case HASH_TABLE -> "HashMap: value looks like key=value, then put / get / remove.";
            case INDEXED_MIN_HEAP -> "Priority Queue: value is 1-5 (ESI). Lower number is seen first. pop = next patient.";
            case BINARY_SEARCH_TREE -> "BST: type a number, insert or contains.";
            case ADJACENCY_LIST_GRAPH -> "Graph: addVertex name, or addEdge A-B, then bfs / dfs.";
        };
        stateArea.setText(help + "\n\nComplexity: " + structure.complexity);
    }

    @FXML
    private void onTrySample() {
        Structure structure = structureBox.getValue();
        if (structure == null) {
            return;
        }
        switch (structure) {
            case DYNAMIC_ARRAY -> {
                valueField.setText("D");
                runArray("add", "D");
            }
            case DOUBLY_LINKED_LIST -> {
                valueField.setText("Z");
                runList("addFirst", "Z");
            }
            case CIRCULAR_QUEUE -> {
                valueField.setText("Next");
                runQueue("enqueue", "Next");
            }
            case ARRAY_STACK -> {
                valueField.setText("UndoMe");
                runStack("push", "UndoMe");
            }
            case HASH_TABLE -> {
                valueField.setText("esi=P1");
                runHash("put", "esi=P1");
            }
            case INDEXED_MIN_HEAP -> {
                valueField.setText("1");
                runHeap("push", "1");
            }
            case BINARY_SEARCH_TREE -> {
                valueField.setText("3");
                runBst("insert", "3");
            }
            case ADJACENCY_LIST_GRAPH -> runGraph("bfs", "ER");
        }
        refreshVisualizer();
        stateArea.setText("Sample applied. Press How to use for the next step.\n" + stateArea.getText());
    }

    @FXML
    private void onRun() {
        Structure structure = structureBox.getValue();
        String op = operationBox.getValue();
        String raw = valueField.getText() == null ? "" : valueField.getText().trim();
        try {
            switch (structure) {
                case DYNAMIC_ARRAY -> runArray(op, raw);
                case DOUBLY_LINKED_LIST -> runList(op, raw);
                case CIRCULAR_QUEUE -> runQueue(op, raw);
                case ARRAY_STACK -> runStack(op, raw);
                case HASH_TABLE -> runHash(op, raw);
                case INDEXED_MIN_HEAP -> runHeap(op, raw);
                case BINARY_SEARCH_TREE -> runBst(op, raw);
                case ADJACENCY_LIST_GRAPH -> runGraph(op, raw);
            }
            refreshVisualizer();
        } catch (Exception ex) {
            AlertHelper.showError("Operation failed", ex.getMessage());
        }
    }

    private void runArray(String op, String raw) {
        switch (op) {
            case "add" -> dynamicArray.add(requireValue(raw));
            case "removeLast" -> {
                if (!dynamicArray.isEmpty()) {
                    dynamicArray.removeAt(dynamicArray.size() - 1);
                }
            }
            case "contains" -> stateArea.setText("contains(" + raw + ") = " + dynamicArray.contains(raw));
            default -> {
            }
        }
    }

    private void runList(String op, String raw) {
        switch (op) {
            case "addLast" -> linkedList.addLast(requireValue(raw));
            case "addFirst" -> linkedList.addFirst(requireValue(raw));
            case "removeFirst" -> linkedList.removeFirst();
            case "contains" -> stateArea.setText("contains(" + raw + ") = " + linkedList.contains(raw));
            default -> {
            }
        }
    }

    private void runQueue(String op, String raw) {
        switch (op) {
            case "enqueue" -> circularQueue.enqueue(requireValue(raw));
            case "dequeue" -> circularQueue.dequeue();
            case "peek" -> stateArea.setText("peek = " + circularQueue.peek());
            default -> {
            }
        }
    }

    private void runStack(String op, String raw) {
        switch (op) {
            case "push" -> arrayStack.push(requireValue(raw));
            case "pop" -> arrayStack.pop();
            case "peek" -> stateArea.setText("peek = " + arrayStack.peek());
            default -> {
            }
        }
    }

    private void runHash(String op, String raw) {
        String[] parts = raw.split("[=:]", 2);
        String key = parts[0].trim();
        String value = parts.length > 1 ? parts[1].trim() : key;
        switch (op) {
            case "put" -> hashTable.put(requireValue(key), value);
            case "get" -> stateArea.setText("get(" + key + ") = " + hashTable.get(key));
            case "remove" -> hashTable.remove(key);
            default -> {
            }
        }
    }

    private void runHeap(String op, String raw) {
        switch (op) {
            case "push" -> {
                int rank = raw.isBlank() ? 3 : Integer.parseInt(raw.trim());
                minHeap.push(new LabCase(nextCaseId++, rank, LocalDateTime.now()));
            }
            case "pop" -> stateArea.setText("pop = " + minHeap.pop());
            case "peek" -> stateArea.setText("peek = " + minHeap.peek());
            default -> {
            }
        }
    }

    private void runBst(String op, String raw) {
        switch (op) {
            case "insert" -> bst.insert(Integer.parseInt(requireValue(raw)));
            case "contains" -> stateArea.setText("contains = " + bst.contains(Integer.parseInt(raw)));
            case "remove" -> bst.remove(Integer.parseInt(requireValue(raw)));
            case "inOrder" -> {
                StringBuilder sb = new StringBuilder("inOrder: ");
                for (Integer value : bst.inOrder()) {
                    sb.append(value).append(' ');
                }
                stateArea.setText(sb.toString().trim());
            }
            default -> {
            }
        }
    }

    private void runGraph(String op, String raw) {
        switch (op) {
            case "addVertex" -> graph.addVertex(requireValue(raw).toUpperCase(Locale.ROOT));
            case "addEdge" -> {
                String[] parts = raw.split("[->,]", 2);
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Use format A-B for edge");
                }
                graph.addEdge(parts[0].trim().toUpperCase(Locale.ROOT), parts[1].trim().toUpperCase(Locale.ROOT));
            }
            case "bfs" -> stateArea.setText("BFS: " + join(graph.breadthFirst(requireValue(raw).toUpperCase(Locale.ROOT))));
            case "dfs" -> stateArea.setText("DFS: " + join(graph.depthFirst(requireValue(raw).toUpperCase(Locale.ROOT))));
            default -> {
            }
        }
    }

    private void refreshVisualizer() {
        visualizerPane.getChildren().clear();
        Structure structure = structureBox.getValue();
        if (structure == null) {
            return;
        }
        List<String> nodes = switch (structure) {
            case DYNAMIC_ARRAY -> toList(dynamicArray);
            case DOUBLY_LINKED_LIST -> {
                List<String> values = new ArrayList<>();
                for (String value : linkedList) {
                    values.add(value);
                }
                yield values;
            }
            case CIRCULAR_QUEUE -> List.of("size=" + circularQueue.size(),
                    circularQueue.isEmpty() ? "empty" : "front≈peekable",
                    "cap=" + circularQueue.capacity());
            case ARRAY_STACK -> List.of("size=" + arrayStack.size(),
                    arrayStack.isEmpty() ? "empty" : "top≈peekable");
            case HASH_TABLE -> {
                List<String> values = new ArrayList<>();
                for (String key : hashTable.keys()) {
                    values.add(key + "=" + hashTable.get(key));
                }
                yield values;
            }
            case INDEXED_MIN_HEAP -> List.of("size=" + minHeap.size(),
                    minHeap.isEmpty() ? "empty" : "min=" + minHeap.peek());
            case BINARY_SEARCH_TREE -> {
                List<String> values = new ArrayList<>();
                for (Integer value : bst.inOrder()) {
                    values.add(String.valueOf(value));
                }
                yield values;
            }
            case ADJACENCY_LIST_GRAPH -> {
                List<String> values = new ArrayList<>();
                for (String vertex : graph.vertices()) {
                    values.add(vertex + "→" + join(graph.neighbors(vertex)));
                }
                yield values;
            }
        };

        boolean alt = false;
        for (String node : nodes) {
            Label label = new Label(node);
            label.getStyleClass().add(alt ? "dsa-node-alt" : "dsa-node");
            visualizerPane.getChildren().add(label);
            alt = !alt;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(structure.label).append(" · ").append(nodes.size()).append(" visual nodes\n");
        for (String node : nodes) {
            summary.append(" • ").append(node).append('\n');
        }
        String previous = stateArea.getText();
        if (previous != null && (previous.startsWith("peek") || previous.startsWith("pop")
                || previous.startsWith("get") || previous.startsWith("contains")
                || previous.startsWith("BFS") || previous.startsWith("DFS")
                || previous.startsWith("inOrder"))) {
            stateArea.setText(previous + "\n\n" + summary.toString().trim());
        } else {
            stateArea.setText(summary.toString().trim());
        }
    }

    @FXML
    private void onBenchmark() {
        benchmarkLabel.setText("Running synthetic benchmark...");
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                int n = 25_000;
                updateProgress(0, 3);

                IndexedMinHeap<LabCase> heap = IndexedMinHeap.forCases();
                long heapStart = System.nanoTime();
                LocalDateTime base = LocalDateTime.of(2026, 1, 1, 8, 0);
                for (int i = 0; i < n; i++) {
                    heap.push(new LabCase(i + 1, (i % 5) + 1, base.plusSeconds(i)));
                }
                while (!heap.isEmpty()) {
                    heap.pop();
                }
                long heapNs = System.nanoTime() - heapStart;
                updateProgress(1, 3);

                List<LabCase> list = new ArrayList<>();
                long listStart = System.nanoTime();
                for (int i = 0; i < n; i++) {
                    list.add(new LabCase(i + 1, (i % 5) + 1, base.plusSeconds(i)));
                }
                list.sort(Comparator
                        .comparingInt(LabCase::priorityRank)
                        .thenComparing(LabCase::arrivalTime)
                        .thenComparingLong(LabCase::caseId));
                list.clear();
                long listNs = System.nanoTime() - listStart;
                updateProgress(2, 3);

                updateProgress(3, 3);
                return "n=" + n
                        + "\nPriority Queue (IndexedMinHeap) push+pop: " + (heapNs / 1_000_000.0) + " ms"
                        + "\nSorted list insert+sort+clear: " + (listNs / 1_000_000.0) + " ms"
                        + "\nWinner: " + (heapNs <= listNs ? "Priority Queue (IndexedMinHeap)" : "sorted list")
                        + " for this synthetic workload.";
            }
        };
        benchmarkProgress.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> {
            benchmarkProgress.progressProperty().unbind();
            benchmarkProgress.setProgress(1);
            benchmarkLabel.setText(task.getValue());
        });
        task.setOnFailed(e -> {
            benchmarkProgress.progressProperty().unbind();
            benchmarkLabel.setText("Benchmark failed: " + task.getException().getMessage());
        });
        Thread thread = new Thread(task, "dsa-benchmark");
        thread.setDaemon(true);
        thread.start();
    }

    private static String requireValue(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Value is required");
        }
        return raw.trim();
    }

    private static List<String> toList(DynamicArray<String> array) {
        List<String> values = new ArrayList<>();
        for (String value : array) {
            values.add(value);
        }
        return values;
    }

    private static String join(DynamicArray<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }
}
