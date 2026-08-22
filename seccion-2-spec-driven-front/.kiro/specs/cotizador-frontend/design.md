# Design — Cotizador de Reparación de Calzado
## Frontend web (HTML, CSS y JavaScript nativo)

---

## 1. Visión general de la arquitectura

La aplicación es una **Single Page Application sin framework**, servida como archivos
estáticos. No existe enrutamiento del lado del cliente; la pantalla es única.

La responsabilidad se divide en tres capas claramente delimitadas:

```
┌─────────────────────────────────────────────────────┐
│                   index.html                        │  Estructura semántica + punto de montaje
├──────────────┬──────────────────────────────────────┤
│   css/       │   js/                                │
│  estilos.css │   state.js  ←──→  api.js             │  Lógica de negocio del cliente
│              │      ↑                               │
│              │   app.js  (coordinador de UI)        │  Coordinación DOM ↔ estado ↔ red
└──────────────┴──────────────────────────────────────┘
```

### Regla de dependencias

```
app.js  →  state.js
app.js  →  api.js
api.js  →  (fetch nativo del navegador)
state.js → (sin dependencias externas)
```

`state.js` y `api.js` son módulos independientes entre sí; solo `app.js` los importa y
los coordina. Ningún módulo accede directamente al DOM salvo `app.js`.

---

## 2. Estructura de archivos

```
cotizador-frontend/
├── index.html          # HTML semántico; carga estilos y módulos JS
├── css/
│   └── estilos.css     # Variables CSS, reset, layout, componentes, estados, responsive
└── js/
    ├── state.js        # Estado en memoria: catálogos, selección actual, última cotización
    ├── api.js          # Llamadas fetch a los tres endpoints de la API
    └── app.js          # Eventos DOM, coordinación state ↔ api, renderizado
```

---

## 3. `index.html` — Estructura semántica

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Cotizador de Reparación de Calzado</title>
  <link rel="stylesheet" href="css/estilos.css" />
</head>
<body>
  <!-- REQ-001 -->
  <header>
    <h1>Cotizador de Reparación de Calzado</h1>
  </header>

  <main>
    <!-- REQ-006: mensaje de error de catálogo, oculto por defecto -->
    <div id="catalogo-error" class="alerta alerta--error" hidden aria-live="assertive"></div>

    <!-- REQ-002 -->
    <section aria-labelledby="form-titulo">
      <h2 id="form-titulo" class="sr-only">Formulario de cotización</h2>
      <form id="form-cotizacion" novalidate>

        <!-- REQ-008: tipo de calzado -->
        <div class="campo">
          <label for="tipo-calzado">Tipo de calzado</label>
          <select id="tipo-calzado" name="tipoCalzadoId" required
                  aria-describedby="tipo-calzado-error">
            <option value="">— Seleccione un tipo —</option>
          </select>
          <span id="tipo-calzado-error" class="campo__error" aria-live="polite"></span>
        </div>

        <!-- REQ-009: tipos de reparación (checkboxes) -->
        <fieldset id="reparaciones-fieldset">
          <legend>Tipos de reparación</legend>
          <div id="reparaciones-lista" aria-describedby="reparaciones-error"></div>
          <span id="reparaciones-error" class="campo__error" aria-live="polite"></span>
        </fieldset>

        <!-- REQ-010: urgente -->
        <div class="campo campo--inline">
          <input type="checkbox" id="urgente" name="urgente" />
          <label for="urgente">Servicio urgente</label>
        </div>

        <!-- REQ-016: botón con indicador de carga -->
        <button type="submit" id="btn-cotizar" disabled>
          <span id="btn-texto">Cotizar</span>
          <span id="btn-spinner" class="spinner" hidden aria-hidden="true"></span>
        </button>

      </form>
    </section>

    <!-- REQ-003, REQ-030 -->
    <section id="resultado" aria-live="polite" aria-atomic="true" hidden>
    </section>
  </main>

  <script type="module" src="js/app.js"></script>
</body>
</html>
```

### Decisiones de marcado

| Decisión | Justificación |
|---|---|
| `<select>` para tipo de calzado | Una sola opción posible; `<select>` es el elemento semántico correcto (REQ-008). |
| Checkboxes para reparaciones | Permite selección múltiple nativa sin JS adicional; cada `<input>` tiene su propio `<label>` vinculado (REQ-009, REQ-011). |
| `<fieldset>` + `<legend>` para reparaciones | Agrupa el conjunto de checkboxes semánticamente (REQ-032, REQ-033). |
| `aria-live="assertive"` en error de catálogo | El fallo de catálogo impide el uso completo de la app; requiere anuncio inmediato. |
| `aria-live="polite"` en errores de campo y resultado | Las notificaciones de validación y resultado no son críticas; no deben interrumpir (REQ-030). |
| `type="module"` en el `<script>` | Activa el sistema de módulos ES nativo; elimina contaminación del ámbito global. |
| `novalidate` en `<form>` | La validación la gestiona `app.js` para controlar mensajes y estilos (REQ-012, REQ-013). |

---

## 4. `state.js` — Módulo de estado

Gestiona el estado de la aplicación en memoria sin persistencia externa.

### Forma del estado

```js
// state.js
const state = {
  // Catálogos cargados desde la API
  tiposCalzado:    [],   // Array<{ id: string, nombre: string, factorComplejidad: string }>
  tiposReparacion: [],   // Array<{ id: string, nombre: string, precioBase: string, tiempoEstimadoDias: number }>
  catalogosCargados: false,

  // Selección actual del usuario en el formulario
  seleccion: {
    tipoCalzadoId:    '',      // string — id seleccionado en el <select>
    tipoReparacionIds: [],     // string[] — ids de checkboxes marcados
    urgente:          false,   // boolean
  },

  // Última respuesta recibida del POST /api/cotizaciones
  ultimaCotizacion: null,   // CotizacionResponse | null

  // Estado de la petición en curso
  cargando: false,
};
```

### API pública de `state.js`

```js
// Catálogos
export function setCatalogos(tiposCalzado, tiposReparacion) { ... }
export function getCatalogos() { ... }            // → { tiposCalzado, tiposReparacion }
export function isCatalogoCargado() { ... }       // → boolean

// Selección del formulario
export function setSeleccion(campo, valor) { ... } // campo: 'tipoCalzadoId' | 'tipoReparacionIds' | 'urgente'
export function getSeleccion() { ... }             // → { tipoCalzadoId, tipoReparacionIds, urgente }
export function resetSeleccion() { ... }

// Cotización
export function setUltimaCotizacion(cotizacion) { ... }  // cotizacion: CotizacionResponse | null
export function getUltimaCotizacion() { ... }             // → CotizacionResponse | null

// Carga
export function setCargando(valor) { ... }   // valor: boolean
export function isCargando() { ... }         // → boolean
```

### Invariantes

- `tipoReparacionIds` es siempre un array (nunca `null`).
- `setCargando(true)` solo puede llamarse si `catalogosCargados === true`.
- `setUltimaCotizacion` acepta `null` para limpiar el resultado previo.

---

## 5. `api.js` — Módulo de red

Encapsula todas las llamadas `fetch`. No modifica el DOM ni el estado; devuelve promesas
que resuelven con los datos o rechazan con un objeto de error normalizado.

### Objeto de error normalizado

```js
// Siempre se rechaza con esta forma para que app.js no necesite distinguir
// entre errores HTTP y errores de red:
{
  tipo:    'red' | 'servidor' | 'validacion' | 'no_encontrado',
  mensaje: string,   // mensaje legible para el usuario
  codigo:  number | null,
}
```

### Funciones exportadas

```js
/**
 * GET /api/tipos-calzado
 * @returns {Promise<Array<{id, nombre, factorComplejidad}>>}
 */
export async function getTiposCalzado() { ... }

/**
 * GET /api/tipos-reparacion
 * @returns {Promise<Array<{id, nombre, precioBase, tiempoEstimadoDias}>>}
 */
export async function getTiposReparacion() { ... }

/**
 * POST /api/cotizaciones
 * @param {{ tipoCalzadoId: string, tipoReparacionIds: string[], urgente: boolean }} payload
 * @returns {Promise<CotizacionResponse>}
 */
export async function postCotizacion(payload) { ... }
```

### Lógica de normalización de errores

```
fetch() lanza  →  { tipo: 'red',         mensaje: 'Sin conexión…',       codigo: null }
HTTP 400       →  { tipo: 'validacion',   mensaje: body.mensaje,          codigo: 400  }
HTTP 404       →  { tipo: 'no_encontrado',mensaje: body.mensaje,          codigo: 404  }
HTTP 500       →  { tipo: 'servidor',     mensaje: 'Error en el servidor…', codigo: 500 }
Otro HTTP ≥400 →  { tipo: 'servidor',     mensaje: 'Error inesperado…',   codigo: status }
```

La función privada `_handleResponse(response)` centraliza esta lógica y es usada por las
tres funciones públicas.

---

## 6. `app.js` — Coordinador de UI

Es el único módulo que accede al DOM. Importa `state.js` y `api.js`.

### Secuencia de inicialización

```
DOMContentLoaded
  │
  ├─ deshabilitarFormulario()              // REQ-007
  ├─ Promise.all([
  │     getTiposCalzado(),
  │     getTiposReparacion()
  │  ])
  │    ├─ OK → setCatalogos(...)
  │    │        renderizarSelectCalzado()  // REQ-004
  │    │        renderizarCheckboxesReparaciones()  // REQ-005
  │    │        habilitarBotonEnvio()      // REQ-007 resuelto
  │    └─ ERROR → mostrarErrorCatalogo()  // REQ-006
```

### Flujo de envío del formulario

```
submit (preventDefault)
  │
  ├─ leerFormulario() → actualiza state.seleccion
  ├─ validarFormulario()
  │    ├─ INVÁLIDO → mostrarErroresValidacion()  // REQ-012, REQ-013, REQ-014
  │    └─ VÁLIDO
  │         ├─ setCargando(true)
  │         ├─ mostrarSpinner() + deshabilitarBoton()  // REQ-016
  │         ├─ postCotizacion(getSeleccion())
  │         │    ├─ OK (201) → setUltimaCotizacion(data)
  │         │    │              renderizarResultado()   // REQ-018…REQ-026
  │         │    └─ ERROR     → renderizarError()       // REQ-027, REQ-028, REQ-029
  │         └─ setCargando(false)
  │              ocultarSpinner() + habilitarBoton()    // REQ-017
```

### Funciones internas principales

```js
// Inicialización
function inicializar()
function renderizarSelectCalzado(tiposCalzado)
function renderizarCheckboxesReparaciones(tiposReparacion)

// Lectura y validación
function leerFormulario()          // lee DOM → llama setSeleccion()
function validarFormulario()       // → { valido: boolean, errores: { campo: string } }
function mostrarErroresValidacion(errores)
function limpiarErroresValidacion()

// Estado de carga
function mostrarSpinner()
function ocultarSpinner()
function habilitarBotonEnvio()
function deshabilitarBotonEnvio()

// Renderizado de resultados
function renderizarResultado(cotizacion)   // construye HTML y lo inserta en #resultado
function renderizarError(error)            // muestra el mensaje de error en #resultado
function ocultarResultado()

// Error de catálogo
function mostrarErrorCatalogo(mensaje)
```

### Construcción del HTML de resultado (`renderizarResultado`)

```
#resultado (section, aria-live="polite")
  ├─ [si urgente] <span class="etiqueta-urgente">Servicio urgente</span>   // REQ-025
  ├─ <h2>Cotización — {nombreCalzado}</h2>                                 // REQ-018
  ├─ <table class="tabla-reparaciones">                                    // REQ-019
  │     <thead> Reparación | Precio base | Subtotal </thead>
  │     <tbody> una <tr> por cada elemento de reparaciones[] </tbody>
  │  </table>
  ├─ <p class="subtotal">Subtotal: {sumaSubtotales}</p>                    // REQ-020
  ├─ [si recargo != null]                                                  // REQ-021, REQ-022
  │     <p class="recargo">Recargo urgente ({porcentajeRecargo}): {recargo}</p>
  ├─ <p class="total">Total: {total}</p>                                   // REQ-023
  └─ <p class="tiempo">Tiempo estimado: {tiempoEstimadoEntregaDias} días</p> // REQ-024
```

---

## 7. `css/estilos.css` — Organización de estilos

El archivo se organiza en secciones con comentarios delimitadores:

```
1. Variables CSS (custom properties)
2. Reset / base
3. Layout (header, main, secciones)
4. Componentes — formulario (.campo, fieldset, label, select, input)
5. Componentes — checkboxes de reparación
6. Componentes — botón (#btn-cotizar, estados :hover, :disabled)
7. Componentes — spinner de carga (.spinner)
8. Componentes — mensajes de error (.campo__error, .alerta--error)
9. Componentes — resultado (#resultado, .tabla-reparaciones, .total, .etiqueta-urgente)
10. Utilidades (.sr-only, .hidden)
11. Media queries (≤ 600 px)
```

### Variables CSS relevantes

```css
:root {
  --color-primario:    #2563eb;
  --color-error:       #dc2626;
  --color-exito:       #16a34a;
  --color-urgente:     #ea580c;
  --color-texto:       #111827;
  --color-fondo:       #f9fafb;
  --color-borde:       #d1d5db;
  --radio-borde:       4px;
  --espacio-base:      1rem;
  --fuente-base:       system-ui, sans-serif;
}
```

### Clase `.sr-only`

```css
.sr-only {
  position: absolute; width: 1px; height: 1px;
  padding: 0; margin: -1px; overflow: hidden;
  clip: rect(0,0,0,0); white-space: nowrap; border: 0;
}
```

Usada para el `<h2 id="form-titulo">` que provee contexto a lectores de pantalla sin
ser visible en pantalla (REQ-033).

### Estados del botón

```css
#btn-cotizar:disabled {
  opacity: 0.5;
  cursor: not-allowed;   /* REQ-036 */
}
```

### Responsive (REQ-034)

```css
@media (max-width: 600px) {
  /* El formulario y el área de resultado pasan a layout de columna única.
     Los checkboxes de reparación se reorganizan en una sola columna. */
}
```

---

## 8. Flujos de error detallados

### Error en carga de catálogo (REQ-006)

```
Promise.all rechaza
  └─ mostrarErrorCatalogo("No fue posible cargar los catálogos. Recargue la página.")
       → #catalogo-error.hidden = false
       → #catalogo-error.textContent = mensaje
       → botón de envío permanece disabled
```

### Error HTTP 400 / 404 tras POST (REQ-027)

```
postCotizacion() rechaza con { tipo: 'validacion' | 'no_encontrado', mensaje }
  └─ renderizarError({ mensaje })
       → #resultado.hidden = false
       → #resultado.innerHTML = <p class="alerta alerta--error">{mensaje}</p>
```

### Error de red (REQ-029)

```
fetch() lanza TypeError (sin conexión)
  └─ api.js normaliza → { tipo: 'red', mensaje: 'No fue posible conectar…' }
       └─ renderizarError({ mensaje })
```

---

## 9. Trazabilidad de requisitos

| Requisito | Archivo(s) | Elemento / función |
|---|---|---|
| REQ-001 | index.html | `<h1>` en `<header>` |
| REQ-002 | index.html | `<form id="form-cotizacion">` |
| REQ-003 | index.html, app.js | `<section id="resultado" hidden>` |
| REQ-004 | app.js, api.js | `getTiposCalzado()`, `renderizarSelectCalzado()` |
| REQ-005 | app.js, api.js | `getTiposReparacion()`, `renderizarCheckboxesReparaciones()` |
| REQ-006 | app.js | `mostrarErrorCatalogo()` |
| REQ-007 | app.js | `deshabilitarBotonEnvio()` en inicio |
| REQ-008 | index.html, app.js | `<select id="tipo-calzado">`, `renderizarSelectCalzado()` |
| REQ-009 | index.html, app.js | `<div id="reparaciones-lista">`, `renderizarCheckboxesReparaciones()` |
| REQ-010 | index.html | `<input type="checkbox" id="urgente">` |
| REQ-011 | index.html | `<label for="...">` en cada campo |
| REQ-012 | app.js | `validarFormulario()`, `mostrarErroresValidacion()` |
| REQ-013 | app.js | `validarFormulario()`, `mostrarErroresValidacion()` |
| REQ-014 | app.js | `leerFormulario()` conserva valores; solo actualiza estado |
| REQ-015 | app.js, api.js | `postCotizacion(getSeleccion())` |
| REQ-016 | app.js | `mostrarSpinner()`, `deshabilitarBotonEnvio()` |
| REQ-017 | app.js | `ocultarSpinner()`, `habilitarBotonEnvio()` en bloque `finally` |
| REQ-018 | app.js | `renderizarResultado()` — `nombreCalzado` |
| REQ-019 | app.js | `renderizarResultado()` — tabla `reparaciones[]` |
| REQ-020 | app.js | `renderizarResultado()` — `sumaSubtotales` |
| REQ-021 | app.js | `renderizarResultado()` — fila condicional `recargo` |
| REQ-022 | app.js | `renderizarResultado()` — guarda `if (recargo !== null)` |
| REQ-023 | app.js | `renderizarResultado()` — `total` destacado |
| REQ-024 | app.js | `renderizarResultado()` — `tiempoEstimadoEntregaDias` |
| REQ-025 | app.js, estilos.css | `renderizarResultado()` — `.etiqueta-urgente` |
| REQ-026 | app.js | `renderizarResultado()` reemplaza contenido de `#resultado` |
| REQ-027 | api.js, app.js | normalización 400/404, `renderizarError()` |
| REQ-028 | api.js, app.js | normalización 500, `renderizarError()` |
| REQ-029 | api.js, app.js | captura `TypeError` fetch, `renderizarError()` |
| REQ-030 | index.html | `aria-live="polite"` en `#resultado` |
| REQ-031 | index.html, app.js | atributo `disabled` gestionado por `habilitarBotonEnvio` / `deshabilitarBotonEnvio` |
| REQ-032 | index.html | `aria-describedby` en cada campo apuntando a su `<span class="campo__error">` |
| REQ-033 | index.html | orden de `tabindex` natural; `<fieldset>` navega checkboxes con flechas |
| REQ-034 | estilos.css | media query `max-width: 600px` |
| REQ-035 | estilos.css | variables de color con contraste ≥ 4.5:1 |
| REQ-036 | estilos.css | `#btn-cotizar:disabled { opacity: 0.5 }` |
| REQ-037 | — | sin `<script src="...cdn...">` ni `npm install` en runtime |
| REQ-038 | index.html | punto de entrada único `index.html` |
