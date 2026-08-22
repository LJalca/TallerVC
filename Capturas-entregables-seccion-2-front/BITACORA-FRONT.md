# Bitácora — Sección 2: Spec-Driven Development (Frontend)

Registro del flujo Specs (steering → `requirements.md` → `design.md` → `tasks.md` →
ejecución de tareas) para el proyecto `cotizador-frontend`. Formato equivalente al de la
bitácora del backend (`BITACORA-BACK.md`).

**Nota de honestidad metodológica**: a diferencia de la Sección 2 del backend, este flujo
no se ejecutó dentro de la UI de Kiro — no existen capturas de pantalla porque no hubo una
sesión visual de Kiro para el frontend. El trabajo se hizo con Claude Code (terminal, sin
interfaz gráfica de IDE agéntico), siguiendo el mismo flujo de fases y gates descrito en la
sección 3.6.6-3.6.8 del taller (steering → requirements → design → tasks → ejecución
tarea por tarea). Esta bitácora documenta ese flujo real, no una recreación de capturas que
nunca existieron.

## Steering (3.6.6.1 a 3.6.6.3)

- Se crearon tres archivos en `.kiro/steering/`: `architecture.md` (las tres
  responsabilidades livianas index.html+css / state.js / api.js / app.js y la regla de
  dependencia única, 3.6.6.1), `conventions.md` (nomenclatura de archivos, ids, clases CSS,
  funciones y constantes, 3.6.6.2) y `design-patterns.md` (Module pattern, Adapter, Factory
  simple y Observer ligero, 3.6.6.3) — deliberadamente sin arquitectura hexagonal, Repository
  ni Strategy, porque el frontend no tiene reglas de negocio propias que proteger.

## Feature Spec — `requirements.md` y `design.md`

- Estos dos archivos ya existían, generados en una sesión previa del equipo. Se auditó su
  contenido línea por línea contra la sección 3.3.4 del taller (componentes, estados de
  pantalla UI-E1 a UI-E5, reglas de interacción UI-01 a UI-04) y se confirmó que estaban
  completos y en notación EARS correcta. Se ubicaron en
  `seccion-2-spec-driven-front/.kiro/specs/cotizador-frontend/`.

## Feature Spec — `tasks.md`

- No existía. Se generó desde cero a partir de `design.md`, dividiendo la implementación en
  10 tareas ordenadas por dependencia (estructura → `index.html`/CSS → `state.js` → `api.js`
  → `app.js` en cuatro sub-tareas → CSS → checkpoint de verificación manual), cada una con
  su trazabilidad explícita a los REQ-001 a REQ-038 del `requirements.md`.

## Ejecución de tareas (3.6.7-3.6.8)

- Se delegó la implementación a una flota de sub-agentes (uno por archivo: `index.html`,
  `css/estilos.css`, `js/state.js`, `js/api.js`, `js/app.js`), coordinada por un agente
  supervisor que auditó el resultado combinado antes de aprobarlo — equivalente funcional al
  gate de revisión humana descrito en 3.6.4/3.6.5 para el backend.
- La auditoría encontró y corrigió 6 problemas antes de dar la implementación por buena:
  1. **REQ-006 incumplido**: un error de servidor al cargar el catálogo mostraba el mensaje
     genérico de error en vez de indicar que había que recargar la página.
  2. **REQ-033 (foco por teclado)**: `mostrarErroresValidacion` intentaba enfocar un
     `<fieldset>`, que no es focuseable sin `tabindex`; corregido para enfocar el primer
     checkbox del grupo.
  3. **REQ-035 (contraste WCAG AA)**: tres combinaciones de color no llegaban a 4.5:1
     (`.alerta--error` 3.99:1, `.recargo` 3.56:1, `.etiqueta-urgente` 3.56:1). Se agregaron
     tokens de color dedicados para texto (`--color-error-texto`, `--color-urgente-texto`)
     que sí cumplen el mínimo, sin tocar los colores de acento originales de `design.md`.
  4. Una regla de CSS muerta en el media query (`grid-template-columns` sobre un contenedor
     `flex`).
  5. Un selector CSS demasiado amplio que teñía de rojo checkboxes anidados.
  6. Un import sin usar en `app.js`.
- Verificaciones adicionales que pasaron limpias: sintaxis JS válida (`node --check`), cero
  ids huérfanos entre `index.html` y `app.js`, cero cálculo de negocio filtrado al cliente
  (barrido explícito por operadores aritméticos sobre precios/totales), cero `innerHTML`
  (todo el DOM dinámico vía `createElement`/`textContent`, sin riesgo XSS), y la regla de
  dependencia única respetada (`state.js`/`api.js` no tocan el DOM ni se importan entre sí).

## Reconciliación de contrato (hallazgo posterior a la implementación)

- Al verificar `renderizarResultado()` contra el DTO real del backend
  (`CotizacionResponse.java`, `com.tallerdae.cotizador`), se detectó que el contrato que
  `requirements.md`/`design.md` documentaban (`nombreCalzado`, `reparaciones[]` con
  precio/subtotal por línea, `sumaSubtotales`, `recargo`, `porcentajeRecargo`,
  `tiempoEstimadoEntregaDias`, `urgente: boolean`) no coincidía con lo que el backend
  realmente devuelve (`reparacionIds` como lista de UUIDs sin desglose, `nivelUrgencia`
  como enum `NORMAL`/`URGENTE`, `subtotal`, `recargoUrgencia`, `tiempoEstimadoDias`, sin
  `porcentajeRecargo`).
- Se corrigió `app.js`: los nombres de reparación ahora se resuelven contra el catálogo ya
  cargado en `state.js` (lookup por id, no un cálculo de negocio), la urgencia se deriva de
  `nivelUrgencia === 'URGENTE'`, y el recargo se muestra solo bajo esa misma condición (el
  backend nunca manda `null`, manda `0`). Se actualizó `css/estilos.css` (la tabla de
  reparaciones por línea se reemplazó por una lista simple, ya que el backend no expone
  precio ni subtotal por reparación). Se actualizaron `requirements.md` (REQ-018 a REQ-025 y
  el bloque de contrato de la API) y `design.md` (el pseudocódigo de `renderizarResultado` y
  la tabla de trazabilidad) para que documenten el contrato real, no uno inventado.

## Verificación manual (equivalente ligero a 3.7)

- Se sirvió `cotizador-frontend/` con un servidor estático local y se abrió en el navegador.
  Render correcto, sin errores de consola. Sin backend corriendo, se confirmó el
  comportamiento esperado de REQ-006 (aviso de error de catálogo).
- La validación completa de los 7 escenarios de la sección 4.6.1 (con el backend real
  conectado) queda pendiente para la Sección 3 — es trabajo de integración, no de esta
  sección.

## Entregables (3.8)

- `seccion-2-spec-driven-front/cotizador-frontend/`: `index.html`, `css/estilos.css`,
  `js/state.js`, `js/api.js`, `js/app.js`, `README.md`.
- `seccion-2-spec-driven-front/.kiro/`: `steering/` (3 archivos) y
  `specs/cotizador-frontend/` (`requirements.md`, `design.md`, `tasks.md`, tareas 1-9
  marcadas como completadas; la tarea 10, checkpoint de verificación con backend real, queda
  abierta a propósito).
- Esta bitácora, en `Capturas-entregables-seccion-2-front/BITACORA-FRONT.md`.
