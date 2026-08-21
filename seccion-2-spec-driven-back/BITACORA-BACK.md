# Bitácora — Sección 2: Spec-Driven Development (Backend)

Registro de la ejecución del flujo Specs de Kiro (`requirements.md` → `design.md` → `tasks.md` → ejecución de tareas) para el proyecto `cotizador-backend`. Formato equivalente al de la bitácora de la Sección 1.

## Steering (3.6.2)

- Se cargaron tres archivos en `.kiro/steering/`: `architecture.md` (capas hexagonales, 3.4.1), `conventions.md` (nomenclatura, 3.4.2) y `design-patterns.md` (los cinco patrones con su justificación, 3.4.3), tal como los define el documento del taller.

## Feature Spec — `requirements.md`

- Kiro generó el primer `requirements.md` a partir de las historias de usuario y reglas de negocio.
- **Ajuste 1**: los endpoints generados no coincidían con el Anexo B (`/calzados`, `/reparaciones` en vez de `/api/tipos-calzado`, `/api/tipos-reparacion`). Se pidió corregir los paths exactos.
- **Ajuste 2**: Kiro había agregado un `Requisito 7` con un endpoint `GET /cotizaciones/{id}` no contemplado en el contrato de API. Se pidió eliminarlo por ser funcionalidad no especificada.
- Con ambos ajustes aplicados, se aprobó `requirements.md`.

## Feature Spec — `design.md`

- Primera versión: la entidad `Calzado` había sido renombrada a `TipoCalzado`, faltaba el Value Object `Dinero` (subtotal/recargo/total quedaban como `BigDecimal` plano) y faltaba el enum `NivelUrgencia`. Se pidió corregir los tres contra el modelo de dominio de 3.4.4.
- Segunda revisión: los cambios anteriores quedaron aplicados, pero con residuos de la versión previa (referencias sueltas a `TipoCalzado`/`TipoReparacion` en el diagrama de secuencia, la Propiedad 1 y la sección de Repository; firmas de `NormalPricingStrategy`/`UrgentPricingStrategy` todavía en `BigDecimal` en vez de `Dinero`; el generador de la Propiedad 7 seguía en `boolean urgente`). Se pidieron los ajustes puntuales citando línea por línea.
- Tercera revisión: quedó un único bug de sintaxis Mermaid (una línea del diagrama de secuencia sin salto de línea, `buscarPorId(tipoCalzadoId)` pegado a `alt no encontrado`). Se corrigió directamente en el archivo.
- Con el diagrama corregido, se aprobó `design.md`.

## Feature Spec — `tasks.md`

- Se revisó contra los dos criterios del gate (granularidad de tareas y orden de dependencias respetando la regla hexagonal). El `Task Dependency Graph` generado no viola la regla domain → application → infrastructure. Se aprobó sin ajustes.

## Ejecución de tareas (Paso 4, 3.6.5)

- **Tarea 1** (estructura del proyecto): primera ejecución dejó los puertos de entrada/salida anidados dentro de `domain/port/`, sin paquete `application` en absoluto — contradice `architecture.md`. Se pidió reestructurar a `application/port/in`, `application/port/out` y `application/service`, dejando `domain` solo con `model/` y `exception/`. Corregido.
- **Tareas 2.1, 2.2, 4.1**: `Calzado`, `Reparacion`, `Dinero`, `NivelUrgencia` y las dos excepciones de dominio — aprobadas sin observaciones, coinciden con `design.md`.
- Se marcó explícitamente que las tareas de pruebas (2.4, 2.6-2.10, 6.3, 6.4, 8.4, 9.5, 10.1), todas con `*` en `tasks.md`, se saltean por instrucción expresa de la sección 3.6.5 (ahorro de tokens).
- **Tareas 2.3 y 5.1/5.2**: se ejecutaron en paralelo (misma ola del dependency graph) antes que la 2.5. Se detectó que el proyecto no compilaba porque `Cotizacion.java` (Tarea 2.5, la entidad raíz con el Factory Method) no se había generado — los puertos ya la importaban. Se pidió ejecutar la Tarea 2.5 explícitamente.
- **Tarea 2.5** (`Cotizacion.crear(...)`): se verificaron a mano los tres escenarios del Anexo C contra la implementación — los tres cálculos (subtotal, recargo, total, tiempo estimado) dieron correctos. Aprobada, con una observación menor para revisar más adelante (escala decimal inconsistente entre subtotal y recargo).
- **Tareas 6.1 y 6.2** (servicios de aplicación): aprobadas, el flujo de `GenerarCotizacionService` coincide con el diagrama de secuencia de `design.md`.
- **Tareas 8.1, 8.2, 8.3** (adaptadores en memoria): aprobadas, datos semilla coinciden con `design.md`, UUIDs fijos para reproducibilidad.
- **Tareas 9.1 a 9.4** (DTOs, Mapper, Controller, manejo de errores): se detectó el hallazgo más importante de toda la sesión — el DTO `CotizacionHttpRequest` usaba los campos `calzadoId`/`reparacionIds`/`nivelUrgencia`, que no coinciden con el contrato del Anexo B (`tipoCalzadoId`/`tipoReparacionIds`/`urgente`). Esto habría roto la integración con el frontend en la Sección 3 (todo request habría llegado con los campos en `null`). Se corrigió directamente: se renombraron los campos del DTO HTTP, se agregó la traducción `urgente → NivelUrgencia` en el controller, y se renombró `recargo` a `recargoUrgencia` y se agregó el campo `moneda` en `CotizacionResponse`, siguiendo el Anexo B al pie de la letra.

## Contrato OpenAPI y empaquetado (cierre de 3.6.5)

- Se generó `openapi.yaml` a partir del Anexo B, ya con los nombres de campo corregidos. Se agregó una respuesta `404` en `POST /api/cotizaciones` que el Anexo B no documenta explícitamente pero que el backend sí implementa (por RN de recursos no encontrados).
- Se compiló el proyecto (`mvn package -DskipTests`) y se generó `cotizador-0.0.1-SNAPSHOT.jar`.
- Se creó el `Dockerfile` siguiendo el modelo de referencia del Anexo F (build multi-stage Maven + JRE 17 Alpine).

## Validación automatizada (3.7, opcional)

- Se escribieron tres pruebas JUnit 5 equivalentes a los tres escenarios del Anexo C, probando directamente `Cotizacion.crear(...)`: cotización simple sin urgencia, cotización urgente con recargo, y rechazo por falta de reparaciones.
- Resultado: `Tests run: 3, Failures: 0, Errors: 0` — los tres en verde.

## Entregables (3.8)

- Se reorganizó la carpeta del proyecto: el código Maven (`pom.xml`, `src/`, `Dockerfile`, `openapi.yaml`) se movió a `seccion-2-spec-driven-back/cotizador-backend/`, dejando `.kiro/` (steering + specs) en la raíz de `seccion-2-spec-driven-back/`, siguiendo la estructura de referencia de la sección 3.6.9.
