package io.github.ciscoadmin.tachart;

import io.github.ciscoadmin.tachart.app.ChartRendererApplication;

public final class ChartRenderer {
    private ChartRenderer() {
    }

    public static void main(String[] args) {
        int exitCode = new ChartRendererApplication().run(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
