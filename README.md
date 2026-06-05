# TA Chart

Репозиторий содержит два варианта генерации PNG-графика для отчетов автотестов.

## Структура

```text
java/chart-renderer/  # локальный Java2D CLI renderer для Jenkins
js/chart-service/     # текущий Express/Chart.js HTTP service для hosting/Railway
```

## Рекомендованный вариант

Для Jenkins на слабом instance лучше использовать Java renderer:

- не требует Node.js, npm, Cairo/Pango/canvas native dependencies;
- не держит долгоживущий HTTP process;
- запускается одноразово после тестов;
- работает с маленькими JVM лимитами: `-Xms16m -Xmx64m`;
- пишет PNG в локальный файл, который Jenkins дальше загружает в нужный image hosting.

Сборка:

```bash
bash ./java/chart-renderer/build.sh
```

Запуск:

```bash
java -Xms16m -Xmx64m -Djava.awt.headless=true \
  -jar java/chart-renderer/build/libs/chart-renderer.jar \
  --passed 31 \
  --failed 1 \
  --title "Regress feature" \
  --output chart-output/chart.png
```

Smoke tests:

```bash
bash ./java/chart-renderer/test.sh
```

Подробности: [java/chart-renderer/README.md](java/chart-renderer/README.md).

## JS hosting variant

JS service оставлен как hosted HTTP вариант. Он поднимает Express endpoint:

```text
GET /chart.png?passed=19&failed=1&width=400&height=300&title=Web%20UI%20autotests
```

Этот вариант удобен, когда нужен внешний URL-renderer, например на Railway. Минус в контексте экономии ресурсов: нужен running service или внешний hosting.

Подробности: [js/chart-service/README.md](js/chart-service/README.md).

## Выбор решения

| Сценарий | Выбор |
| --- | --- |
| Jenkins уже знает counters и нужен PNG-файл | `java/chart-renderer` |
| Нужно избежать оплаты Railway/runtime | `java/chart-renderer` |
| Нужен внешний HTTP endpoint `/chart.png` | `js/chart-service` |
| Нужна максимальная совместимость с текущим Railway flow | `js/chart-service` |

Практическая схема для Jenkins:

1. Запустить автотесты.
2. Посчитать `passed` и `failed`.
3. Запустить `chart-renderer.jar`.
4. Загрузить PNG в image hosting.
5. Подставить публичный URL в Teams/email notification.
