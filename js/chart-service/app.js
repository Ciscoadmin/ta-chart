const express = require('express');
const { ChartJSNodeCanvas } = require('chartjs-node-canvas');
const Chart = require('chart.js');

const app = express();
const port = 3000;

app.get('/chart.png', async (req, res) => {
  // Логируем query-параметры запроса
  console.log(`[LOG] /chart.png query: ${JSON.stringify(req.query)}`);

  // Получаем параметры из URL
  const rawW = parseInt(req.query.width) || 400;
  const rawH = parseInt(req.query.height) || 400;
  // Гарантируем минимум 350×250
  const width = Math.max(rawW, 350);
  const height = Math.max(rawH, 250);

  const passed = parseInt(req.query.passed) || 0;
  const failed = parseInt(req.query.failed) || 0;
  const title = req.query.title || 'Web UI autotests';
  const total = passed + failed;

  // Создаём новый Canvas под каждый запрос
  const chartJSNodeCanvas = new ChartJSNodeCanvas({
    width,
    height,
    backgroundColour: 'white', 
    chartJs: Chart,
  });

    // Масштабируем шрифты относительно базового размера (400)
  const scaleFactor = Math.min(width, height) / 400;

  // Плагин для вывода общего числа по центру и процента «failed» возле сектора
  const customLabelsPlugin = {
    id: 'customLabels',
    afterDraw: (chart) => {
      const { ctx, config } = chart;
      const dataset = config.data.datasets[0];
      if (!dataset) return;

      // 1) Текст в центре (total)
      const metaFirstArc = chart.getDatasetMeta(0).data[0];
      if (!metaFirstArc || !metaFirstArc.outerRadius) return;
      const radius = metaFirstArc.outerRadius;
      // Размер шрифта пропорционален радиусу
      const fontSize = Math.round(radius * 0.4);

      ctx.save();
      ctx.font = `${fontSize}px sans-serif`;
      ctx.textBaseline = 'middle';
      ctx.textAlign = 'center';
      ctx.fillStyle = '#000';
      // Пишем total в геометрическом центре
      ctx.fillText(total, (chart.chartArea.width / 2) + 40, (chart.chartArea.height / 2) + 20);
      ctx.restore();

      // 2) Процент возле сектора «failed» (dataIndex=1)
      // Если failed = 0, то метка не нужна
      if (failed > 0 && dataset.data.length > 1) {
        const metaFailedArc = chart.getDatasetMeta(0).data[1];
        if (!metaFailedArc || !metaFailedArc.outerRadius) return;

        // Вычисляем процент
        const sum = dataset.data.reduce((acc, val) => acc + val, 0);
        const failedValue = dataset.data[1];
        const percentage = ((failedValue / sum) * 100).toFixed(0) + '%';

        // Координаты точки посередине дуги
        const angle = (metaFailedArc.startAngle + metaFailedArc.endAngle) / 2;
        const arcRadius = metaFailedArc.outerRadius + 7; // чуть за пределы сектора
        const x = metaFailedArc.x - 30  + arcRadius * Math.cos(angle);
        const y = metaFailedArc.y + arcRadius * Math.sin(angle);

        // Рисуем процент
        ctx.save();
        // Можно сделать чуть меньше шрифт, чем центр
        ctx.font = `${Math.round(fontSize * 0.4)}px sans-serif`;
        ctx.textBaseline = 'middle';
        ctx.textAlign = 'left';
        ctx.fillStyle = '#000';
        ctx.fillText(percentage, x, y);
        ctx.restore();
      }
    },
  };

 // Конфигурация диаграммы
  const config = {
    type: 'doughnut',
    data: {
      labels: [`${passed} passed`, `${failed} failed`],
      datasets: [
        {
          data: [passed, failed],
          backgroundColor: ['#8fd78f', '#ff625e'],
        },
      ],
    },
    options: {
      responsive: false,
      maintainAspectRatio: false,
      animation: {
        duration: 0, // Отключаем анимацию для стабильного отрисовывания
      },
      plugins: {
        title: {
          display: true,
          text: title,
          font: { size: 20 * scaleFactor, weight: 'bold' },
        },
        legend: {
          display: true,
          position: 'right',
          labels: {
            // Масштабируем шрифт легенды
            font: {
              size: 18 * scaleFactor,
            },
            boxWidth: 40 * scaleFactor,
            boxHeight: 30 * scaleFactor,
            padding: 20 * scaleFactor,
          },
        },
        tooltip: { enabled: false },
      },
      layout: {
        padding: { left: 40, bottom: 20 },
      },
      cutout: '70%',
    },
    plugins: [customLabelsPlugin],
  };

  try {
    const imageBuffer = await chartJSNodeCanvas.renderToBuffer(config);
    res.set('Content-Type', 'image/png');
    res.send(imageBuffer);
  } catch (err) {
    res.status(500).send('Error generating chart');
  }
});

app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});
