# Базовый образ
# Вместо node:alpine используем Debian-based образ
FROM node:18-bullseye

# Обновляем пакеты и устанавливаем зависимости для сборки node-canvas
RUN apt-get update && apt-get install -y \
  build-essential \
  libcairo2-dev \
  libpango1.0-dev \
  libjpeg-dev \
  libgif-dev \
  librsvg2-dev \
  libfreetype6-dev \
  fontconfig \
  python3 \
  && rm -rf /var/lib/apt/lists/*


RUN ln -sf /usr/bin/python3 /usr/bin/python || true

# Копируем package.json
COPY package*.json ./

# Устанавливаем зависимости
RUN npm install

# Копируем остальной код
COPY . .

# Объявляем volume для гибкого использования (логи, конфиги)
#VOLUME ["/app/data"]

# Открываем порт
EXPOSE 3000

# Запускаем приложение
CMD ["node", "app.js"]