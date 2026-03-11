## CloudRepo

### Запуск через nginx

1. Подготовить `.env`, положить его в корневую директорию проекта: 
   ```text
   POSTGRES_DB=
   POSTGRES_USER=
   POSTGRES_PASSWORD=
   POSTGRES_PORT=
   
   REDIS_HOST=
   REDIS_USER=
   REDIS_PASSWORD=
   
   MINIO_HOSTNAME=
   MINIO_SECURE=
   MINIO_ROOT_USER=
   MINIO_ROOT_PASSWORD=
   
   DB_URL=
   DB_USERNAME=
   DB_PASSWORD=
   ```
   
   Для тестового запуска можно использовать следующие данные:
   ```text
   POSTGRES_DB=cloud_repo
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=1627
   POSTGRES_PORT=5432
   
   REDIS_HOST=localhost
   
   MINIO_HOSTNAME=http://127.0.0.1
   MINIO_SECURE=false
   MINIO_ROOT_USER=minioadmin
   MINIO_ROOT_PASSWORD=minioadmin
   
   DB_URL=jdbc:postgresql://localhost:5432/cloud_repo
   DB_USERNAME=postgres
   DB_PASSWORD=postgres
   ```

2. Запустить сервисы при помощи Docker Compose:
   ```bash
   docker compose up -d --build
   ```
3. Открыть приложение:
   - `http://localhost:8080/api/auth/sign-in`

### В процессе поднимаются:

- `nginx` (входная точка, порт `8080`)
- `backend` (Spring Boot, внутри сети docker)
- `postgres`
- `redis`
- `minio` (`9000`, консоль `9001`)

### Примечание по маршрутам:

Приложение работает с `server.servlet.context-path=/api`, поэтому все маршруты backend начинаются с `/api/*`.
