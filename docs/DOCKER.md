# Docker — Gym JF (backend)

Levanta MySQL y el backend con un solo comando. Todos los comandos se ejecutan **desde la carpeta `backend/`**.

| Servicio | Contenedor | Puerto en tu PC |
|---|---|---|
| MySQL 8.4 | `gym_jf_db` | `127.0.0.1:3307` |
| Backend (Spring Boot) | `gym_jf_backend` | `8080` → Swagger: http://localhost:8080/swagger-ui.html |

> El MySQL del contenedor usa el **3307** porque el 3306 lo ocupa tu MySQL local.

## Requisitos

- Docker Desktop abierto (que diga "Engine running").
- `backend/.env` con `JWT_SECRET` (el compose lo lee de ahí). El resto es opcional, ver `.env.example`.

## Uso diario

| Comando | Qué hace |
|---|---|
| `docker compose up -d` | Inicia todo en segundo plano (crea lo que falte) |
| `docker compose up -d --build` | Igual, pero recompila el backend. **Usar tras cambiar código Java** |
| `docker compose stop` | Pausa los contenedores sin borrarlos |
| `docker compose start` | Reanuda los pausados |
| `docker compose restart backend` | Reinicia solo el backend (sin recompilar) |
| `docker compose down` | Borra los contenedores. **Los datos de la BD se conservan** |
| `docker compose down -v` | Borra contenedores **y la base de datos** (el volumen) |

## Ver qué pasa

| Comando | Qué hace |
|---|---|
| `docker compose ps` | Estado de los contenedores |
| `docker compose logs -f backend` | Logs del backend en vivo (Ctrl+C para salir) |
| `docker compose logs -f db` | Logs de MySQL |
| `docker compose logs --tail 100 backend` | Últimas 100 líneas |

## Compilar

| Comando | Qué hace |
|---|---|
| `docker compose build` | Compila la imagen del backend sin iniciarla |
| `docker compose build --no-cache` | Compila desde cero (si algo quedó raro) |
| `docker compose up -d --build backend` | Recompila y reinicia solo el backend |

## Base de datos

**Conectar Workbench:** host `127.0.0.1`, puerto `3307`, usuario `root`, contraseña `gymjf_dev` (o `DOCKER_DB_PASSWORD` si la cambiaste en `.env`).

| Comando | Qué hace |
|---|---|
| `docker compose exec db mysql -uroot -pgymjf_dev gym_jf` | Abre una consola MySQL dentro del contenedor |
| `docker compose exec -T db mysql -uroot -pgymjf_dev gym_jf < ruta/script.sql` | Ejecuta un script SQL |
| `docker compose exec db mysqldump -uroot -pgymjf_dev gym_jf > respaldo.sql` | Respaldo de la BD |
| `docker compose exec -T db mysql -uroot -pgymjf_dev < "docs/TCU Gimnacio JF/TCU Gimnacio JF/SQL/Datos de prueba.sql"` | Crea los usuarios de prueba (re-ejecutable) |

**Usuarios de prueba** (tras ejecutar el script de arriba): `admin@gymjf.com` / `Admin123!` (ADMIN) y `cliente@gymjf.com` / `Cliente123!` (CUSTOMER). Hay que ejecutarlo después de cada `down -v`; no se carga solo para no mezclar datos de prueba con el esquema.

### Importante: los scripts de `docs/.../SQL/` solo corren la primera vez

MySQL los ejecuta únicamente cuando el volumen está **vacío**. Si cambias un script SQL (esquema, triggers, procedimientos) y quieres aplicarlo desde cero:

```
docker compose down -v
docker compose up -d --build
```

Si no quieres perder datos, ejecuta el script a mano con el comando de arriba (los de triggers y procedimientos son re-ejecutables).

## Solo la BD en Docker (backend desde tu IDE)

```
docker compose up -d db
./mvnw spring-boot:run
```

Para que el backend local apunte al contenedor, en `backend/.env` usa `DB_URL=jdbc:mysql://localhost:3307/gym_jf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` y `DB_PASSWORD=gymjf_dev`.

## Limpieza

| Comando | Qué hace |
|---|---|
| `docker image prune` | Borra imágenes huérfanas (`<none>`) |
| `docker builder prune` | Borra la caché de compilación |
| `docker compose down -v --rmi local` | Borra todo lo de este proyecto: contenedores, datos e imagen del backend |

## Problemas comunes

| Síntoma | Causa y solución |
|---|---|
| `port is already allocated` (3307 u 8080) | Otro programa usa el puerto. Ciérralo o cambia `DB_HOST_PORT` en `.env` |
| El backend se reinicia en bucle | `docker compose logs backend`. Suele ser `JWT_SECRET` vacío o la BD aún iniciando |
| `Schema-validation` en los logs | Una entidad Java no coincide con el esquema SQL. Corrige la entidad o el script y recrea con `down -v` |
| Cambié código y no se nota | Faltó `--build`: `docker compose up -d --build` |
| Cambié un `.sql` y no se nota | Los scripts solo corren con volumen vacío: `down -v` y `up -d --build` |
| `Cannot connect to the Docker daemon` | Docker Desktop está cerrado |
| Acentos rotos en mensajes de triggers (`mÃ¡ximo`) | La BD se creó sin la imagen propia de MySQL. `docker compose down -v` y `up -d --build` (usa `docker/mysql/`, que fuerza utf8mb4 al ejecutar los scripts) |
