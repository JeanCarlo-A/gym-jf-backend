# Plan del backend — Gym JF

TCU de la UCR para el Gimnasio J.F. Este documento es **solo del backend** (API Spring Boot + MySQL + Docker). El frontend tiene su propio plan: [../../PLAN_FRONTEND.md](../../PLAN_FRONTEND.md), y **no arranca hasta cerrar la Fase 3 de este plan**.

- **Horas TCU:** ~100 de 300 completadas. Las ~200 restantes se reparten entre backend y frontend, sin reducir alcance.
- **Capacidad:** hasta 8 h/día. **Fechas:** el cliente lo quiere en diciembre 2026; el límite real es marzo 2027.
- **Orden:** 1) subir evidencias pendientes (incluido Docker) → 2) cerrar el backend → 3) frontend.
- Las horas de este plan son **estimaciones** y se corrigen con el tiempo real de cada sesión.

---

## 1. Estado actual (auditado el 2026-11-11)

**Hecho y funcionando:** Docker + MySQL con los 3 scripts SQL, JWT stateless con BCrypt, errores `ProblemDetail`, CRUD en los 9 módulos (18 controladores, ~85 endpoints), valores enum en inglés alineados con triggers/SP.

**Evidencias ya entregadas (9):** configuración inicial · creación de BD en Workbench · diccionario · conexión BD + Swagger + tabla usuarios · índices y triggers · filtro JWT/Security 7 · Suscripciones · Exercise · Rutinas.

### Hallazgos transversales

| # | Hallazgo | Impacto |
|---|---|---|
| T1 | Ningún endpoint valida rol; sin ownership (cualquier usuario lee/edita datos de otro, incluida antropometría — Ley 8968) | Crítico |
| T2 | Errores de triggers (`SQLSTATE 45000`: aforo, stock) salen como **500 "Error interno"** y pierden el mensaje | Alto — rompe evidencias de Reservations/Inventory |
| T3 | No hay forma de crear un ADMIN (register fuerza CUSTOMER) | Alto |
| T4 | Sin `@Transactional`; todos los `Update*Request` sin validación; faltan `@Size`/`@Positive`/`@Digits` | Medio |
| T5 | Desajustes entidad↔SQL: `User` (nombre 100/150, correo 150/100), `Exercise` (nombre 100 vs 45, descripción 100 vs 255) | Medio |
| T6 | `PUT` cambia el estado de un pago, pero el trigger de saldo es solo `AFTER INSERT` → aprobar un pago no actualiza el saldo | Alto |
| T7 | Sin CORS, perfiles dev/prod, `show-sql` siempre activo, Swagger público, sin paginación, sin actuator, creates devuelven 200 (no 201) | Medio |
| T8 | Solo 1 test (`contextLoads`, necesita BD viva) | Medio |
| T9 | `JwtProperties` es código muerto; sin refresh ni logout | Bajo |
| T10 | Los procedimientos `sp_*` nunca se llaman desde Java | Medio |

### Qué falta por módulo para estar al 100% (contra los requerimientos RF)

- **Auth/Usuarios (RF-1):** recuperación de contraseña (1.1.4), enforcement de roles (1.1.3), perfil `/me` + objetivo físico (1.2.1–1.2.4), consentimiento informado (R-1.2, sin columna), avisos/push (1.3).
- **Memberships (RF-7):** tipos de plan (pase diario/promo), `endDate` calculado (7.2), panel de restricciones Libre/Selectiva (7.3), matriz de acceso (7.3.1), pase diario que caduca 00:00 (7.3.2–3), job de vencimiento. El default `ACTIVE` contradice `sp_renovar_suscripcion` (que crea `PENDING`).
- **Billing (RF-5):** aprobación admin (5.1.4), referencia duplicada + alerta (5.1.5–6), doble confirmación (R-1.3), cierre de caja (5.2.2), filtros (5.2.3), dashboard/reportes (5.3), CxC desde compra/suscripción (5.4.1), umbral de morosidad 15 000 / 30 días + job (5.4.4), suspensión total y bloqueo de check-in (5.4.5), `/me` (5.4.3). Sin validar monto ≤ saldo.
- **Reservations (RF-2):** disponibilidad por día (2.2.1), cancelación con anticipación (2.2.3, usar `sp_cancelar_reserva`), duplicados, lista de espera (2.3.2), check-in QR (2.4.1), notificación de arribo (2.4.2), contador en vivo (2.4.3), no-show a 20 min (2.5), sanciones (2.5.1), Zumba con fecha y saldo (2.6). Hoy `PUT` salta el aforo y el cliente puede crear reservas `COMPLETED`.
- **Inventory (RF-6):** descripción/beneficios (6.1.1), filtro por categoría (6.1.3), carrito y método de pago/retiro (6.2), total recalculado y precio tomado del producto, stock coherente en update/delete, estados `EXPIRED`/pagado (6.3.2), expiración 24–48 h con reintegro de stock (6.3.3), alerta de stock bajo (6.4.1), historial de ventas (6.4.2).
- **Routines/Exercise (RF-3.1–3.2):** filtros por grupo muscular y "en casa", búsqueda (3.1.3–4), rutinas "en casa" (3.2.2), plantillas asignables (3.2.3), lectura rutina + ejercicios, orden, duplicados; bug JSON `forHome`/`isForHome`.
- **Nutrition (RF-4):** "Mi Despensa" (4.2.2), filtros por objetivo y tiempo de comida (4.1.2, 4.3.3), recomendación por objetivo (4.3.1; requiere el objetivo del usuario como enum), sync de favoritos offline (4.3.2), búsqueda de ingredientes, UNIQUE en favoritos y en (receta, ingrediente).
- **Biometrics (RF-3.3):** altura `DECIMAL(3,2)` implica metros (el doc habla de cm) → decidir y validar; historial comparativo y series para gráficas (3.3.2–3), consentimiento, ownership.
- **Integraciones:** Supabase Storage (fotos/comprobantes) y Firebase FCM (push). OCR y QR se hacen en el móvil.

---

## 2. Backlog de evidencias (se hacen primero)

☐ pendiente · ☑ entregada.

**Formato de cada PDF:** "Estudiante: Jean Carlo Arias Moya C30721", un párrafo por paso con la redacción de las evidencias anteriores, capturas de Swagger (las toma Claude con Chromium; si la sesión perdió el navegador se reinstala Playwright) y el marcador **"Aquí va la captura de base de datos"** donde se pega la captura de Workbench (`127.0.0.1:3307`, root / `gymjf_dev`).

| ID | Evidencia | Estado | Depende de |
|---|---|---|---|
| E-01…E-09 | Las 9 ya entregadas | ☑ | — |
| E-10 | **Implementación de Docker** (Dockerfile, compose, `up`, Workbench en 3307, `docs/DOCKER.md`) | ☐ | nada — ya funciona |
| E-11 | Procedimientos almacenados + migración a valores en inglés | ☐ | nada |
| E-12 | **Reservations** (schedules + reservations; prueba del trigger de aforo) | ☐ | P0.2 |
| E-13 | **Biometrics** (fichas antropométricas) | ☐ | P0.1 |
| E-14 | **Nutrition** (recetas, ingredientes, receta-ingredientes, favoritas) | ☐ | P0.1 |
| E-15 | **Billing** (cuentas por cobrar + transacciones; trigger de saldo) | ☐ | P0.2 |
| E-16 | **Inventory** (productos, pedidos, detalle; triggers de stock) | ☐ | P0.2 |

Las evidencias de fases posteriores se agregan aquí (E-17, E-18…), una por módulo o hito al cerrarlo.

---

## 3. Fases

### Fase 0 — Preparar y subir evidencias pendientes (~25 h)

- ☑ **P0.1 Entorno de pruebas (~3 h):** `docker compose up`, crear ADMIN y CUSTOMER de prueba con un script SQL de semilla en `docs/…/SQL/` (resuelve T3 para pruebas), verificar login. *Hecho: `Datos de prueba.sql` (ver [DOCKER.md](DOCKER.md)).*
- ☑ **P0.2 Arreglo mínimo de errores de trigger (~2 h):** manejar `SQLSTATE 45000` en `GlobalExceptionHandler` (409 con el mensaje en español). Sin esto E-12, E-15 y E-16 mostrarían un 500. *Hecho y probado: aforo y stock responden 409 con el mensaje. Además se corrigió la codificación de los acentos en los triggers dentro de Docker (imagen MySQL propia en `docker/mysql/`).*
- **P0.3 Redactar y generar E-10…E-16 (~20 h, ~2–3 h por módulo):** CRUD completo en Swagger, capturas, PDF y marcadores para BD. Los bugs que aparezcan se anotan en la bitácora y no se arreglan en esta fase salvo que bloqueen.

### Fase 1 — Fundamentos transversales (~28 h)

Resuelve T1 y T4–T9. Va **antes** de las reglas de negocio porque todas dependen de roles y de saber quién es el usuario.

1. **Roles (~8 h):** `@EnableMethodSecurity`, `@PreAuthorize` por endpoint (admin vs cliente), exponer el id del usuario autenticado (principal propio) para ownership → `/me` y filtros por dueño.
2. **Validación (~8 h):** `@Size`/`@Positive`/`@Digits` según longitudes SQL, validar los `Update*Request`, `@Transactional` en servicios, 201 en creates, corregir T5 y `forHome`.
3. **Configuración (~6 h):** CORS, perfiles dev/prod (`show-sql` solo en dev), Swagger gating, actuator + healthcheck Docker, quitar `JwtProperties` muerto, paginación en listados grandes.
4. **Tests (~6 h):** Testcontainers MySQL + pruebas de servicio y seguridad para lo crítico.

Cierre → **E-17** (roles y seguridad con pruebas 401/403).

### Fase 2 — Reglas de negocio por módulo (~165 h)

Orden por prioridad para el cliente. Cada módulo termina con su evidencia.

| Orden | Módulo | Contenido principal | Horas | Evidencia |
|---|---|---|---|---|
| 2.1 | Auth/Usuarios | `/me` y perfil, objetivo físico (enum), bootstrap admin, consentimiento informado, recuperación de contraseña (requiere correo) | ~18 | E-18 |
| 2.2 | Memberships | tipos de plan/pase diario, `endDate` calculado, ajustes globales (toggle de restricción), filtro de acceso por plan, caducidad 00:00, job de vencimiento, `sp_renovar_suscripcion` | ~25 | E-19 |
| 2.3 | Billing | aprobar/rechazar pago (corrige T6), referencia duplicada, monto ≤ saldo, doble confirmación, CxC desde compras/suscripciones, umbral de morosidad + job + bloqueo, cierre de caja, filtros, dashboard/reportes, `sp_procesar_pago_sinpe` | ~35 | E-20 |
| 2.4 | Reservations | disponibilidad, cancelación con anticipación, duplicados, aforo también en update, QR check-in, contador en vivo, no-show + sanciones, lista de espera, Zumba con fecha y saldo | ~35 | E-21 |
| 2.5 | Inventory | categoría/descripción, carrito + checkout, total y precio desde producto, stock en update/delete, expiración 24–48 h, alerta de stock bajo, historial de ventas | ~25 | E-22 |
| 2.6 | Routines/Exercise | filtros y búsqueda, "en casa", plantillas asignables, lectura anidada, orden, duplicados | ~10 | E-23 |
| 2.7 | Nutrition | Mi Despensa, filtros, recomendación por objetivo, UNIQUE favoritos, sync offline | ~12 | E-24 |
| 2.8 | Biometrics | unidades/validación, historial comparativo y series para gráficas, consentimiento, ownership | ~6 | E-25 |
| 2.9 | Integraciones | Supabase Storage (imágenes/comprobantes), Firebase FCM (avisos, arribo, stock bajo, morosidad) | ~15 | E-26 |

**Cambios de esquema** (columna de consentimiento, tabla de ajustes, UNIQUE de favoritos, tipo/fecha en `horarios`): se editan los `.sql` de `docs/…/SQL/`, se agrega una migración re-ejecutable y se recrea con `docker compose down -v` (ver [DOCKER.md](DOCKER.md)). El diccionario puede cambiarse: ya fue aprobado.

### Fase 3 — Cierre del backend (~10 h)

Matriz final RF × estado, actualizar `CLAUDE.md` y `docs/`, prueba de punta a punta en Docker y evidencia **E-27** (despliegue completo). **Con esto el backend queda listo y se habilita el plan del frontend.**

**Total backend restante estimado: ≈ 230 h** (25 + 28 + 165 + 10).

---

## 4. Calendario

A 8 h/día (~40 h/semana) el backend completo toma ≈ 6 semanas, o sea hasta **mediados/finales de diciembre**. Conviene hablar con el cliente: lo más probable es que diciembre alcance para el backend y una demo, y que el frontend se entregue entre enero y marzo.

Orden de prioridad si hay que ajustar: Fase 0 → Fase 1 → 2.1 → 2.2 → 2.3 → 2.4 → 2.5 → resto. Es un orden, no un recorte.

---

## 5. Definición de "módulo al 100%"

☐ CRUD sin errores · ☐ validaciones (create y update) · ☐ roles y ownership · ☐ errores en español sin 500 · ☐ reglas de negocio del RF implementadas · ☐ prueba en Swagger y automática · ☐ evidencia PDF subida · ☐ `CLAUDE.md` y docs actualizados.

---

## 6. Seguimiento por módulo

| Módulo | CRUD | Validación | Roles/ownership | Reglas RF | Pruebas | Evidencia |
|---|---|---|---|---|---|---|
| Auth/Usuarios | ☑ | ☐ | ☐ | ☐ | ☐ | ☑ (E-04, E-06) |
| Memberships | ☑ | ☐ | ☐ | ☐ | ☐ | ☑ (E-07) |
| Billing | ☑ | ☐ | ☐ | ☐ | ☐ | ☐ (E-15) |
| Reservations | ☑ | ☐ | ☐ | ☐ | ☐ | ☐ (E-12) |
| Inventory | ☑ | ☐ | ☐ | ☐ | ☐ | ☐ (E-16) |
| Routines | ☑ | ☐ | ☐ | ☐ | ☐ | ☑ (E-09) |
| Exercise | ☑ | ☐ | ☐ | ☐ | ☐ | ☑ (E-08) |
| Nutrition | ☑ | ☐ | ☐ | ☐ | ☐ | ☐ (E-14) |
| Biometrics | ☑ | ☐ | ☐ | ☐ | ☐ | ☐ (E-13) |

El CRUD aparece como hecho porque el código existe; solo está probado donde hay evidencia entregada.

---

## 7. Bitácora de sesiones

| Fecha | Horas | Qué se hizo | Evidencia(s) generada(s) | Pendiente / bloqueos |
|---|---|---|---|---|
| 2026-11-11 | — | Auditoría completa, Docker funcionando, este plan | — | Subir E-10…E-16 |
| 2026-11-11 | (anotar horas reales) | P0.1 y P0.2: usuarios de prueba, manejo de errores de triggers (409), arreglo de acentos en Docker | — | P0.3: generar E-10…E-16 |

**Horas acumuladas TCU: 100 / 300** (actualizar en cada fila).

---

## 8. Decisiones abiertas

1. **Altura de antropometría:** ¿metros (SQL actual) o cm (doc de requerimientos)? — decidir en 2.8.
2. **Almacenamiento de imágenes:** se asume Supabase Storage (C4 dice "Supabase / Cloudinary") — 2.9.
3. **Recuperación de contraseña** necesita correo (SMTP o servicio): elegir proveedor — 2.1.
4. **Calidad extra** no exigida por los RF (Testcontainers, paginación, actuator): incluida porque protege el cierre; R-4.1 limita el alcance funcional, no la calidad técnica.

---

## 9. Cómo verificar cada fase

- **Fase 0:** `docker compose up -d` sano; PDFs E-10…E-16 con capturas reales; ningún 500 en las pruebas de triggers.
- **Fase 1:** un CUSTOMER recibe 403 en endpoints de admin y no ve datos de otro usuario; tests verdes con Testcontainers.
- **Fase 2:** por módulo, casos de prueba de cada RF (p. ej. reserva que excede el aforo → mensaje; 3 ausencias → bloqueo de 24–48 h; deuda > 15 000 → acceso bloqueado), en Swagger y como test automático.
- **Fase 3:** matriz RF × estado sin pendientes; despliegue limpio con `down -v` + `up -d --build`.

---

## Referencias

- Código: `backend/src/main/java/com/gymjf/backend/{modules,shared}`; seguridad en `shared/infrastructure/security/SecurityConfig.java`; errores en `shared/infrastructure/web/GlobalExceptionHandler.java`.
- Base de datos: `docs/TCU Gimnacio JF/TCU Gimnacio JF/SQL/*.sql`.
- Docker: `docker-compose.yml`, `Dockerfile`, [DOCKER.md](DOCKER.md).
- Evidencias entregadas (no modificar): `docs/TCU Gimnacio JF/TCU Gimnacio JF/Evidencias (Imagenes)/`.
