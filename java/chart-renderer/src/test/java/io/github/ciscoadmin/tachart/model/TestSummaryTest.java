package io.github.ciscoadmin.tachart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestSummaryTest {
    @Test
    void calculatesTotalAndFailedPercentage() {
        TestSummary summary = new TestSummary(19, 1);

        assertEquals(20, summary.total());
        assertEquals(5, summary.failedPercentage());
        assertEquals(18.0, summary.failedAngleDegrees(), 0.001);
    }

    @Test
    void rejectsNegativeCounters() {
        assertThrows(IllegalArgumentException.class, () -> new TestSummary(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new TestSummary(0, -1));
    }

    @Test
    void rejectsOverflowingTotal() {
        assertThrows(IllegalArgumentException.class, () -> new TestSummary(Long.MAX_VALUE, 1));
    }
}
