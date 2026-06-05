# JS Chart Service

Hosted HTTP renderer based on Express, Chart.js and `chartjs-node-canvas`.

This is the current service-oriented variant that can be deployed to Railway or another Node.js hosting provider. It exposes a PNG endpoint and renders the chart on demand.

## Endpoint

```text
GET /chart.png
```

Query parameters:

| Parameter | Default | Description |
| --- | --- | --- |
| `passed` | `0` | Passed test count |
| `failed` | `0` | Failed test count |
| `width` | `400` | Output width, minimum `350` |
| `height` | `400` | Output height, minimum `250` |
| `title` | `Web UI autotests` | Chart title |

Example:

```text
/chart.png?passed=19&failed=1&width=400&height=300&title=Regress%20feature
```

## Local Run

```bash
cd js/chart-service
npm ci
npm start
```

Open:

```text
http://localhost:3000/chart.png?passed=19&failed=1&width=400&height=300&title=Regress%20feature
```

## Docker

Build from the service directory:

```bash
docker build -t ta-chart-js ./js/chart-service
```

Run:

```bash
docker run --rm -p 3000:3000 ta-chart-js
```

## Railway

When deploying this variant, set the service root/build context to:

```text
js/chart-service
```

The app listens on port `3000`.

## Notes

- This variant needs native canvas dependencies in the image. The Dockerfile uses a Debian-based Node image and installs Cairo/Pango related packages.
- It is useful as an HTTP renderer, but it is not the cheapest option for Jenkins-only chart generation because a hosting runtime must stay available.
- For Jenkins local file generation, prefer `java/chart-renderer`.
