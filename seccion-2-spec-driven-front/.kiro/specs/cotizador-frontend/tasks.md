# Plan de Implementación: Cotizador de Reparación de Calzado (Frontend)

## Resumen

Implementación de la pantalla única del cotizador en HTML, CSS y JavaScript nativo (sin
framework, sin bundler), siguiendo la separación liviana de responsabilidades definida en
`architecture.md` (`index.html`+`css/estilos.css` → `state.js` → `api.js` → `app.js`). El
plan respeta la regla de dependencia única: `state.js` y `api.js` no se conocen entre sí ni
tocan el DOM; toda coordinación pasa por `app.js`.

---

## Tareas

- [x] 1. Configurar la estructura del proyecto
  - Crear `cotizador-frontend/` con `index.html`, `css/estilos.css`, `js/state.js`, `js/api.js`, `js/app.js`
  - Sin dependencias de frameworks ni gestores de paquetes en tiempo de ejecución
  - _Requisitos: REQ-037, REQ-038_

- [x] 2. Construir el marcado semántico de `index.html`
  - [x] 2.1 Encabezado y estructura base
    - `<header>` con `<h1>Cotizador de Reparación de Calzado</h1>`
    - `<main>` con el bloque de error de catálogo (`#catalogo-error`, oculto, `aria-live="assertive"`)
    - _Requisitos: REQ-001, REQ-006_
  - [x] 2.2 Formulario y sus campos
    - `<form id="form-cotizacion" novalidate>` con `<h2 id="form-titulo" class="sr-only">`
    - `<select id="tipo-calzado">` con opción neutra "— Seleccione un tipo —" y `<span>` de error con `aria-describedby`
    - `<fieldset id="reparaciones-fieldset">` con `<div id="reparaciones-lista">` y `<span>` de error
    - `<input type="checkbox" id="urgente">` desmarcado por defecto
    - `<label for="...">` vinculado a cada campo
    - `<button type="submit" id="btn-cotizar" disabled>` con `<span id="btn-texto">` y `<span id="btn-spinner" hidden>`
    - _Requisitos: REQ-002, REQ-007, REQ-008, REQ-009, REQ-010, REQ-011, REQ-016, REQ-031, REQ-032_
  - [x] 2.3 Sección de resultado
    - `<section id="resultado" aria-live="polite" aria-atomic="true" hidden>` vacía
    - `<script type="module" src="js/app.js">` al final del `<body>`
    - _Requisitos: REQ-003, REQ-030_

- [x] 3. Implementar `js/state.js` — módulo de estado
  - Definir la forma del estado: `tiposCalzado`, `tiposReparacion`, `catalogosCargados`, `seleccion` (`tipoCalzadoId`, `tipoReparacionIds`, `urgente`), `ultimaCotizacion`, `cargando`
  - Exportar: `setCatalogos()`, `getCatalogos()`, `isCatalogoCargado()`, `setSeleccion()`, `getSeleccion()`, `resetSeleccion()`, `setUltimaCotizacion()`, `getUltimaCotizacion()`, `setCargando()`, `isCargando()`
  - Invariantes: `tipoReparacionIds` siempre array (nunca `null`); `setUltimaCotizacion` acepta `null` para limpiar
  - No debe tocar el DOM ni hacer `fetch` (regla de dependencia única)
  - _Requisitos: REQ-014, REQ-026_

- [x] 4. Implementar `js/api.js` — módulo de red
  - [x] 4.1 Funciones públicas de fetch
    - `getTiposCalzado()` → `GET /api/tipos-calzado`
    - `getTiposReparacion()` → `GET /api/tipos-reparacion`
    - `postCotizacion(payload)` → `POST /api/cotizaciones` con `Content-Type: application/json`
    - _Requisitos: REQ-004, REQ-005, REQ-015_
  - [x] 4.2 Normalización de errores (`_handleResponse` privada)
    - `fetch()` lanza → `{ tipo: 'red', mensaje, codigo: null }`
    - HTTP 400 → `{ tipo: 'validacion', mensaje: body.mensaje, codigo: 400 }`
    - HTTP 404 → `{ tipo: 'no_encontrado', mensaje: body.mensaje, codigo: 404 }`
    - HTTP 500 / otro ≥400 → `{ tipo: 'servidor', mensaje, codigo }`
    - No debe tocar el DOM ni conocer el estado (regla de dependencia única)
    - _Requisitos: REQ-027, REQ-028, REQ-029_

- [x] 5. Implementar `js/app.js` — inicialización y carga de catálogos
  - Secuencia `DOMContentLoaded`: `deshabilitarFormulario()` → `Promise.all([getTiposCalzado(), getTiposReparacion()])`
  - Éxito: `setCatalogos()`, `renderizarSelectCalzado()`, `renderizarCheckboxesReparaciones()`, `habilitarBotonEnvio()`
  - Error: `mostrarErrorCatalogo()` — botón de envío permanece deshabilitado
  - _Requisitos: REQ-004, REQ-005, REQ-006, REQ-007_

- [x] 6. Implementar la validación del formulario en `js/app.js`
  - `leerFormulario()` lee el DOM y llama `setSeleccion()`
  - `validarFormulario()` → `{ valido, errores: { campo: mensaje } }` (tipo de calzado y al menos una reparación obligatorios)
  - `mostrarErroresValidacion()` / `limpiarErroresValidacion()` — conservan la selección ya hecha por el usuario
  - _Requisitos: REQ-012, REQ-013, REQ-014_

- [x] 7. Implementar el flujo de envío en `js/app.js`
  - `submit` con `preventDefault()` → si inválido, `mostrarErroresValidacion()`; si válido, continuar
  - `setCargando(true)` → `mostrarSpinner()` + `deshabilitarBotonEnvio()`
  - `postCotizacion(getSeleccion())`
  - `finally`: `setCargando(false)` → `ocultarSpinner()` + `habilitarBotonEnvio()`
  - _Requisitos: REQ-015, REQ-016, REQ-017_

- [x] 8. Implementar el renderizado de resultados y errores en `js/app.js`
  - [x] 8.1 `renderizarResultado(cotizacion)`
    - Etiqueta de urgente condicional (`urgente === true`), `nombreCalzado`, tabla de `reparaciones[]` (nombre, precio base, subtotal), `sumaSubtotales`, fila de recargo condicional (`recargo !== null`), `total` destacado, `tiempoEstimadoEntregaDias`
    - Reemplaza cualquier resultado o error previo en `#resultado`
    - _Requisitos: REQ-018, REQ-019, REQ-020, REQ-021, REQ-022, REQ-023, REQ-024, REQ-025, REQ-026_
  - [x] 8.2 `renderizarError(error)` y `ocultarResultado()`
    - Muestra `error.mensaje` recibido del servidor (400/404), un mensaje genérico en 500, o de conexión en error de red
    - `ocultarResultado()` se dispara al cambiar cualquier selección tras ver un resultado previo
    - _Requisitos: REQ-027, REQ-028, REQ-029_

- [x] 9. Implementar `css/estilos.css`
  - Variables CSS (`--color-primario`, `--color-error`, `--color-urgente`, etc.), reset, layout, componentes de formulario, checkboxes, botón (incluye `:disabled { opacity: 0.5 }`), spinner, mensajes de error, resultado, `.sr-only`
  - Media query `max-width: 600px` — layout de una sola columna
  - Contraste de texto mínimo 4.5:1 (WCAG 2.1 AA)
  - _Requisitos: REQ-034, REQ-035, REQ-036_

- [ ] 10. Checkpoint — Verificación manual contra los escenarios de la sección 3.3.4
  - Botón deshabilitado sin selección completa; resultado visible tras cotizar; resultado oculto al cambiar selección; mensaje de error del servidor visible sin perder la selección
  - Recorrer la tabla de trazabilidad de requisitos (sección 9 de `design.md`) verificando cada REQ-001 a REQ-038 contra el comportamiento real en el navegador
  - Consultar al usuario si hay dudas antes de dar por cerrada la implementación
