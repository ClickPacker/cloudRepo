## CloudRepo

### Запуск через nginx

1. Подготовить `.env` (в репозитории уже есть пример значений).
2. Запустить сервисы:
   ```bash
   docker compose up -d --build
   ```
3. Открыть приложение:
   - `http://localhost:8080` (редирект на форму входа)
   - `http://localhost:8080/api/auth/sign-in`

### Что поднимается

- `nginx` (входная точка, порт `8080`)
- `backend` (Spring Boot, внутри сети docker)
- `postgres`
- `redis`
- `minio` (`9000`, консоль `9001`)

### Примечание по маршрутам

Приложение работает с `server.servlet.context-path=/api`, поэтому все маршруты backend начинаются с `/api/*`.
