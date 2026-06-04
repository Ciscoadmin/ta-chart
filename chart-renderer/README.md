# Chart Renderer

Small local Java2D renderer for Jenkins notifications. It creates a PNG doughnut chart without external dependencies, Node.js, native canvas libraries, or a long-running HTTP service.

## Build

Linux/Jenkins:

```bash
bash ./chart-renderer/build.sh
```

Windows:

```powershell
.\chart-renderer\build.ps1
```

The jar is written to:

```text
chart-renderer/build/libs/chart-renderer.jar
```

## Run

```bash
java -Xms16m -Xmx64m -Djava.awt.headless=true \
  -jar chart-renderer/build/libs/chart-renderer.jar \
  --passed 19 \
  --failed 1 \
  --width 400 \
  --height 300 \
  --scale 2 \
  --output chart-output/chart.png
```

`--width` and `--height` are logical dimensions. The default `--scale 2` creates a high-DPI `800x600` PNG for crisp rendering in Teams/email at a displayed size of about `400x300`.

Optional title:

```bash
java -Xms16m -Xmx64m -Djava.awt.headless=true \
  -jar chart-renderer/build/libs/chart-renderer.jar \
  --passed 120 \
  --failed 7 \
  --title "Test target checkout" \
  --output chart-output/chart.png
```

PowerShell run example:

```powershell
java -Xms16m -Xmx64m "-Djava.awt.headless=true" `
  -jar .\chart-renderer\build\libs\chart-renderer.jar `
  --passed 19 `
  --failed 1 `
  --output chart-output\chart.png
```

## Jenkins Usage

Run the renderer after test result counters are known, then upload `chart.png` to the existing image host:

```bash
java -Xms16m -Xmx64m -Djava.awt.headless=true \
  -jar notifications/chart-renderer.jar \
  --passed "${PASSED}" \
  --failed "${FAILED}" \
  --output chart-output/chart.png
```

When both counters are zero, the renderer writes a neutral placeholder chart instead of failing. This keeps Teams/email notifications renderable even when a run produced no test results.

The renderer exits with a non-zero code when counters are invalid or the output cannot be written.
