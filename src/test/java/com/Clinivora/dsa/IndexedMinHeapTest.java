package com.Clinivora.dsa;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IndexedMinHeapTest {

    private static final class CaseItem implements IndexedMinHeap.PrioritizedCase {
        private final long caseId;
        private final int priorityRank;
        private final LocalDateTime arrivalTime;

        private CaseItem(long caseId, int priorityRank, LocalDateTime arrivalTime) {
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
    }

    @Test
    void ordersByPriorityThenArrivalThenCaseId() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 8, 0);
        IndexedMinHeap<CaseItem> heap = IndexedMinHeap.forCases();
        heap.push(new CaseItem(3, 2, base.plusMinutes(5)));
        heap.push(new CaseItem(1, 1, base.plusMinutes(10)));
        heap.push(new CaseItem(2, 1, base));
        heap.push(new CaseItem(4, 1, base));

        assertEquals(2, heap.pop().caseId());
        assertEquals(4, heap.pop().caseId());
        assertEquals(1, heap.pop().caseId());
        assertEquals(3, heap.pop().caseId());
    }

    @Test
    void removeAndUpdateByCaseId() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 9, 0);
        IndexedMinHeap<CaseItem> heap = IndexedMinHeap.forCases();
        heap.push(new CaseItem(10, 3, base));
        heap.push(new CaseItem(11, 2, base));
        heap.removeByCaseId(11);
        assertFalse(heap.contains(11));
        heap.update(new CaseItem(10, 1, base));
        assertEquals(10, heap.peek().caseId());
        assertEquals(1, heap.peek().priorityRank());
    }
}
