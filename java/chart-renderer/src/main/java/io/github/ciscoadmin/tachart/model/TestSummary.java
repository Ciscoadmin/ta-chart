package io.github.ciscoadmin.tachart.model;

public record TestSummary(long passed, long failed) {
    public static final long MAX_TOTAL = 100_000;

    public TestSummary {
        if (passed < 0) {
            throw new IllegalArgumentException("--passed must be >= 0");
        }
        if (failed < 0) {
            throw new IllegalArgumentException("--failed must be >= 0");
        }
        try {
            long total = Math.addExact(passed, failed);
            if (total > MAX_TOTAL) {
                throw new IllegalArgumentException(
                        "passed and failed total must be <= " + MAX_TOTAL
                );
            }
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("passed and failed total is too large", exception);
        }
    }

    public long total() {
        return passed + failed;
    }

    public boolean hasResults() {
        return total() > 0;
    }

    public double failedAngleDegrees() {
        return hasResults() ? failed * 360.0 / total() : 0.0;
    }

    public long failedPercentage() {
        return hasResults() ? Math.round(failed * 100.0 / total()) : 0L;
    }
}
