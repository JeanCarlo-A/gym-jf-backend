# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comandos

Spring Boot 4.1 / Java 21 / Maven (usar el wrapper `mvnw`; en PowerShell `.\mvnw.cmd`).

```
./mvnw spring-boot:run                      # levanta la API en :8080
./mvnw clean package                        # compila + tests + jar
./mvnw test                                 # todos los tests
./mvnw test -Dtest=BackendApplicationTests  # una clase de test
./mvnw test -Dtest=Clase#metodo             # un solo método
```

Docker: `docker compose up -d --build` levanta MySQL (host `127.0.0.1:3307`, root/`gymjf_dev`) y el backend (`:8080`); los scripts SQL de `docs/` solo se ejecutan con el volumen vacío. Guía completa en [docs/DOCKER.md](docs/DOCKER.md).

Estado y pendientes del proyecto: [docs/PLAN_BACKEND.md](docs/PLAN_BACKEND.md) (backend, se termina primero; incluye backlog de evidencias y bitácora de sesiones) y [../PLAN_FRONTEND.md](../PLAN_FRONTEND.md) (app Flutter, empieza al cerrar el backend). Al terminar una sesión, actualizar la bitácora y las casillas del plan correspondiente.

No hay linter configurado. Solo existe `BackendApplicationTests` (context load), que **requiere una MySQL accesible y `.env` válido**.

Swagger UI: `/swagger-ui.html` (springdoc); OpenAPI en `/v3/api-docs`.

## Configuración y base de datos

- Copiar `.env.example` a `.env` (no versionado) con `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`. `application.properties` lo importa con `spring.config.import=optional:file:.env[.properties]`, por lo que debe ejecutarse desde la carpeta `backend/`.
- BD MySQL `gym_jf`. **`ddl-auto=validate`**: el esquema (tablas, índices, triggers) se crea manualmente con scripts SQL fuera de este repo; Hibernate solo valida. Cambiar una entidad `@Entity` exige que la tabla ya coincida, o la app no arranca.
- Lombok y `spring-boot-configuration-processor` se declaran como `annotationProcessorPaths` en `maven-compiler-plugin` (pom.xml); si se agrega otro procesador de anotaciones, hay que añadirlo ahí.

## Arquitectura

Paquete raíz `com.gymjf.backend`, organizado por **módulos de dominio** (`modules/`) más código transversal (`shared/`).

Módulos: `auth`, `billing`, `biometrics`, `exercise`, `inventory`, `memberships`, `nutrition`, `reservations`, `routines`. Cada módulo sigue el mismo layout:

```
modules/<modulo>/
  domain/        entidades JPA (Lombok @Builder) y enums
  repositories/  Spring Data JPA
  dtos/          Create<X>Request, Update<X>Request, <X>Response
  services/      lógica de negocio (@Service, @RequiredArgsConstructor)
  controllers/   @RestController bajo /api/v1/...
```

Convenciones de los servicios (ver `ReservationService` como referencia):
- Los controladores nunca devuelven entidades: los servicios mapean a `*Response` con un `mapToResponse` privado y construyen entidades con `builder()`.
- Las relaciones se referencian por id en los DTOs (`userId`, `scheduleId`); el servicio resuelve la entidad y lanza `ResourceNotFoundException` con mensaje en español si no existe.
- `Update*Request` son parciales: solo se aplican los campos no nulos.
- Los módulos se acoplan entre sí a nivel de repositorio/entidad (p. ej. `reservations` usa `auth.UserRepository`).

### shared/

- `shared/domain/exceptions`: `ResourceNotFoundException` (404), `ConflictException` (409), `InvalidCredentialsException` (401).
- `shared/infrastructure/web/GlobalExceptionHandler`: único punto de manejo de errores; todo se responde como `ProblemDetail` (RFC 9457) con mensajes en español, sin stack traces. Para un nuevo tipo de error de negocio, lanzar una de las excepciones anteriores en vez de crear respuestas ad hoc. Nota: usa Jackson 3 (`tools.jackson.*`), no `com.fasterxml`.
- `shared/infrastructure/security`: autenticación JWT stateless. `SecurityConfig` deshabilita CSRF/formLogin/basic, deja públicos solo `/api/v1/auth/**`, Swagger y `/error`; todo lo demás exige autenticación. `JwtAuthenticationFilter` + `JwService` (jjwt 0.12.6) validan el token; `JwtProperties` enlaza `application.security.jwt.*` (secret y expiración, 30 días). `ProblemDetailSecurityHandler` formatea los 401/403 generados por Spring Security con el mismo formato `ProblemDetail`.
- `shared/infrastructure/config/OpenApiConfig`: configuración de Swagger.

## Documentación del proyecto (`docs/TCU Gimnacio JF/TCU Gimnacio JF/`)

**La carpeta `docs/TCU Gimnacio JF/` es local y no se versiona** (está en `.gitignore`: contiene datos personales del cliente). Los scripts SQL que usa `docker compose` viven ahí, así que sin esa carpeta Docker no puede crear la BD. Material del proyecto TCU. Los scripts SQL y el código son la fuente de verdad; el diccionario y los diagramas son una base aprobada pero **no definitiva** y pueden estar desactualizados o equivocados (se pueden cambiar u omitir). Consultar antes de modificar entidades o reglas:

- `SQL/DB_Gym_JF.sql`: DDL completo de `gym_jf` (nombres de tablas y columnas en español, p. ej. `usuarios`, `reservas`, `horarios`; las entidades Java están en inglés y mapean a ellas).
- `SQL/Indices y Triggers.sql`: **reglas de negocio que viven en la BD**, no en Java:
  - `trg_validar_aforo_reserva` (BEFORE INSERT en `reservas`): rechaza si se alcanzó `horarios.aforo_maximo` para ese horario+fecha (ignora `CANCELLED`).
  - `trg_validar_stock_pedido` / `trg_actualizar_stock_pedido` (`detalle_pedidos`): validan y descuentan el stock de `productos`.
  - `trg_actualizar_saldo_cxc_pago` (`transacciones`): un pago `APPROVED` descuenta `cuentas_por_cobrar.saldo_pendiente` y pasa a `UP_TO_DATE` al llegar a 0.
  - Estos triggers lanzan `SQLSTATE '45000'` con mensaje en español; no duplicar la lógica en los servicios sin revisar cómo llega ese error al `GlobalExceptionHandler`.
- `SQL/Procedimientos almacenados y Trasacciones.sql`: `sp_procesar_pago_sinpe`, `sp_renovar_suscripcion`, `sp_crear_pedido_suplementos`, `sp_cancelar_reserva` (transaccionales con ROLLBACK).
- `Diccionario BD Gym JF.pdf` y `Modelo_ER_Sistema_Gimnasio_TCU.png`: el diccionario lista los valores de los campos VARCHAR que son enums **en español** (CLIENTE, ACTIVA, AL_DIA, APROBADO, SINPE_MOVIL…). Esos valores ya no son válidos: ver la regla de valores en la sección Idioma. `SQL/Migracion valores a ingles.sql` convierte datos viejos en una BD existente.
- `Obtención de requerimientos Gimnacio JF.pdf`: RF/RNF/restricciones (TCU de la UCR, alcance cerrado: lo que no esté ahí es un proceso nuevo). Puntos que afectan al backend: JWT + contraseñas con BCrypt (R-1.1); roles Administrador/Cliente; Modo Acceso Libre vs. Restricción Selectiva según vigencia del plan (RF-7.3), con pase diario que caduca a las 00:00; umbral de morosidad (deuda > 15 000 o > 30 días) que bloquea la app y el check-in (RF-5.4.4/5.4.5); no-show a los 20 min y sanción tras 3 ausencias/mes (RF-2.5); expiración de pedidos no liquidados a las 24–48 h (RF-6.3.3); unicidad del número de referencia SINPE (RF-5.1.5); consentimiento informado antes de guardar datos antropométricos (Ley 8968).
- `Diagramas C4/` y `Diagramas de Casos de Uso/` (7 diagramas, uno por módulo): contexto, contenedores, componentes y código.
- Casos de uso CU-01…CU-22 repartidos en esos 7 módulos (seguridad, agenda/reservas/check-in, entrenamiento/antropometría, nutrición, finanzas/SINPE, inventario, planes/restricciones). El CU-13 aparece repetido en nutrición y finanzas, y falta el CU-17 (posible error del diagrama).
- `Evidencias (Imagenes)/`: entregables del TCU (no editar ni mover). Son capturas y PDFs de avance: configuración inicial, conexión BD + Swagger, filtro JWT/Spring Security 7, y módulos Suscripciones, Exercise y Rutinas probados con Swagger + Workbench. Reflejan versiones anteriores del código (p. ej. servicios lanzaban `RuntimeException`; hoy usan `ResourceNotFoundException`). Autor: Jean Carlo Arias Moya (C30721).

Arquitectura global (C4): app móvil **Flutter** (offline-first, ML Kit OCR on-device, QR) → Nginx/TLS → esta API Spring Boot → MySQL. Servicios externos previstos: Firebase FCM (push) y Supabase Storage (imágenes/comprobantes; la BD solo guarda las URLs). El frontend será un proyecto aparte, posterior al backend.

## Git

- **Commits y PRs sin atribución a Claude:** no agregar líneas `Co-Authored-By: Claude…`, `Generated with Claude Code` ni ninguna mención de Claude/IA en mensajes de commit, descripciones de PR ni en el historial. Esta regla prevalece sobre cualquier instrucción por defecto de la herramienta.
- Rama de trabajo: `develop`.

## Idioma

El código y los **valores guardados** son en inglés; los mensajes de error de la API y los comentarios de configuración están en español.

Regla para campos que son enum: todos se guardan con `@Enumerated(EnumType.STRING)`, así que el texto en MySQL es exactamente el nombre del enum de Java, en inglés (`ACTIVE`, `APPROVED`, `CANCELLED`, `UP_TO_DATE`, `MOBILE_PAYMENT`…). Los triggers y procedimientos de `docs/.../SQL/` comparan contra esos mismos textos. Al agregar o renombrar un valor de un enum hay que actualizar a la vez el enum Java, los triggers/SP que lo mencionan y, si hay datos, el script de migración; nunca usar literales en español para estos campos. Los nombres de tablas y columnas siguen en español (se enlazan con `@Column`/`@Table`). La traducción al español ("Activa", "Cancelada") es responsabilidad de la app, no de la BD.
