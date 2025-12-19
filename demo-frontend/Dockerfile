FROM node:20-alpine

WORKDIR /app

# Install deps first (better layer caching)
COPY package*.json ./
COPY vite.config.* ./
COPY tsconfig*.json ./
RUN npm ci

# Copy the rest
COPY . .

# Vite dev server
EXPOSE 5173

# --host 0.0.0.0 makes it reachable from your host (otherwise it binds to localhost inside the container)
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]
