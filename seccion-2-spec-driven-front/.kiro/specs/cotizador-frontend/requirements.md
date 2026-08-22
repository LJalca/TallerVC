# Requirements — Cotizador de Reparación de Calzado
## Frontend web (HTML, CSS y JavaScript nativo)

---

## 1. Alcance y restricciones generales

- La aplicación es una **pantalla única** sin rutas, navegación ni vistas adicionales.
- No se implementa ninguna regla de negocio para calcular totales ni tiempos estimados;
  esos valores provienen exclusivamente de la respuesta del endpoint `POST /api/cotizaciones`.
- El frontend se construye con **HTML, CSS y JavaScript nativo** (sin frameworks ni librerías
  de terceros salvo lo declarado explícitamente en este documento).
- Las opciones de los desplegables se obtienen en tiempo de ejecución desde los endpoints
  de catálogo `GET /api/tipos-calzado` y `GET /api/tipos-reparacion`; no se codifican de forma
  estática en el HTML.

### Contrato de la API (referencia)

#### Endpoints de catálogo

| Método | Ruta | Respuesta exitosa |
|--------|------|-------------------|
| GET | `/api/tipos-calzado` | `200 OK` — array de `{ id, nombre, factorComplejidad }` |
| GET | `/api/tipos-reparacion` | `200 OK` — array de `{ id, nombre, precioBase, tiempoEstimadoDias }` |

#### Endpoint de cotización

| Método | Ruta | Cuerpo de petición | Respuesta exitosa |
|--------|------|--------------------|-------------------|
| POST | `/api/cotizaciones` | `{ tipoCalzadoId, tipoReparacionIds[], urgente }` | `201 Created` — `CotizacionResponse` |

**CotizacionResponse:**
```json
{
  "nombreCalzado":             "string",
  "reparaciones":              [{ "nombreReparacion": "string", "precioBase": "string", "subtotal": "string" }],
  "sumaSubtotales":            "string",
  "recargo":                   "string | null",
  "porcentajeRecargo":         "string | null",
  "total":                     "string",
  "tiempoEstimadoEntregaDias": "number",
  "urgente":                   "boolean"
}
```

**ErrorResponse (HTTP 400, 404, 500):**
```json
{ "codigo": "number", "mensaje": "string" }
```

---

## 2. Estructura de la pantalla

### REQ-001 — Encabezado

**While** la página está cargada,
**the system shall** mostrar un encabezado visible que contenga el nombre de la aplicación
"Cotizador de Reparación de Calzado".

### REQ-002 — Formulario de cotización

**While** la página está cargada,
**the system shall** presentar un formulario con los campos necesarios para solicitar una
cotización, agrupados en una única sección visible sin desplazamiento lateral.

### REQ-003 — Sección de resultado

**While** la página está cargada,
**the system shall** reservar un área de resultado inicialmente vacía (oculta o colapsada)
que se complete con la respuesta del servidor tras un envío exitoso del formulario.

---

## 3. Carga inicial de catálogos

### REQ-004 — Carga de tipos de calzado al iniciar

**When** la página termina de cargar,
**the system shall** realizar una petición `GET /api/tipos-calzado` y poblar el desplegable
de tipo de calzado con las opciones devueltas, usando el campo `id` como valor interno y
`nombre` como texto visible para el usuario.

### REQ-005 — Carga de tipos de reparación al iniciar

**When** la página termina de cargar,
**the system shall** realizar una petición `GET /api/tipos-reparacion` y poblar la lista de
tipos de reparación con las opciones devueltas, usando el campo `id` como valor interno y
`nombre` como texto visible para el usuario.

### REQ-006 — Error en la carga del catálogo

**When** cualquiera de las peticiones de catálogo falla (error de red o respuesta no 2xx),
**the system shall** mostrar un mensaje de error en la página que indique que los catálogos
no pudieron cargarse y que el usuario debe recargar la página para reintentar.

### REQ-007 — Formulario bloqueado sin catálogos

**While** los catálogos no se han cargado exitosamente,
**the system shall** mantener el botón de envío deshabilitado para impedir el envío del
formulario con datos incompletos.

---

## 4. Campos del formulario

### REQ-008 — Campo "Tipo de calzado"

**When** el usuario interactúa con el campo "Tipo de calzado",
**the system shall** mostrar una lista desplegable (`<select>`) poblada dinámicamente con
las opciones obtenidas de `GET /api/tipos-calzado`, con una opción inicial neutra
"— Seleccione un tipo —" que no sea válida para enviar.

### REQ-009 — Campo "Tipos de reparación"

**When** el usuario interactúa con el campo "Tipos de reparación",
**the system shall** permitir la selección de **una o más** opciones obtenidas de
`GET /api/tipos-reparacion`, a través de checkboxes o un `<select multiple>`, de modo que
el valor enviado a la API sea un array con los `id` seleccionados (`tipoReparacionIds`).

### REQ-010 — Campo "Urgente"

**When** el usuario interactúa con el campo "Urgente",
**the system shall** presentar un control de tipo checkbox o toggle que mapee a un valor
booleano (`true` / `false`) y que esté desmarcado por defecto.

### REQ-011 — Etiquetas de campo

**While** el formulario está visible,
**the system shall** asociar una etiqueta (`<label>`) descriptiva a cada campo del formulario
mediante el atributo `for`/`id` correspondiente.

---

## 5. Validación del formulario en el cliente

### REQ-012 — Tipo de calzado obligatorio

**When** el usuario intenta enviar el formulario sin haber seleccionado un tipo de calzado,
**the system shall** impedir el envío y mostrar un mensaje de error junto al campo indicando
que debe seleccionar un tipo de calzado, sin recargar la página.

### REQ-013 — Al menos una reparación obligatoria

**When** el usuario intenta enviar el formulario sin haber seleccionado al menos un tipo de
reparación,
**the system shall** impedir el envío y mostrar un mensaje de error junto al grupo de
reparaciones indicando que debe elegir al menos una.

### REQ-014 — Persistencia de valores tras error de validación

**When** la validación del cliente detecta un error,
**the system shall** conservar las selecciones que el usuario ya haya realizado en todos los
campos del formulario.

---

## 6. Envío de la solicitud a la API

### REQ-015 — Construcción y envío de la petición

**When** el formulario pasa la validación del cliente,
**the system shall** enviar una petición `POST /api/cotizaciones` con cabecera
`Content-Type: application/json` y el cuerpo JSON:
```json
{
  "tipoCalzadoId":    "<id seleccionado>",
  "tipoReparacionIds": ["<id1>", "<id2>", ...],
  "urgente":           true | false
}
```

### REQ-016 — Estado de carga

**While** la petición `POST /api/cotizaciones` está en curso,
**the system shall** deshabilitar el botón de envío y mostrar un indicador visual de carga
para comunicar al usuario que se está procesando la solicitud.

### REQ-017 — Restauración del botón tras la petición

**When** la petición `POST /api/cotizaciones` concluye (con éxito o con error),
**the system shall** volver a habilitar el botón de envío y ocultar el indicador de carga.

---

## 7. Manejo de la respuesta exitosa (HTTP 201)

### REQ-018 — Presentación del nombre del calzado

**When** la API responde con código HTTP 201 y el cuerpo contiene el campo `nombreCalzado`,
**the system shall** mostrar el valor de `nombreCalzado` en el área de resultado.

### REQ-019 — Presentación del detalle de reparaciones

**When** la API responde con código HTTP 201 y el cuerpo contiene el array `reparaciones`,
**the system shall** mostrar cada línea de reparación con su `nombreReparacion`, `precioBase`
y `subtotal` en el área de resultado, formateados con dos decimales.

### REQ-020 — Presentación de la suma de subtotales

**When** la API responde con código HTTP 201 y el cuerpo contiene el campo `sumaSubtotales`,
**the system shall** mostrar el valor de `sumaSubtotales` en el área de resultado como
subtotal previo a recargos, formateado con dos decimales.

### REQ-021 — Presentación del recargo por urgencia

**When** la API responde con código HTTP 201 y los campos `recargo` y `porcentajeRecargo`
son distintos de `null`,
**the system shall** mostrar ambos valores en el área de resultado indicando claramente que
corresponden al recargo por servicio urgente.

### REQ-022 — Omisión del recargo cuando no aplica

**When** la API responde con código HTTP 201 y los campos `recargo` y `porcentajeRecargo`
son `null`,
**the system shall** omitir la fila de recargo en el área de resultado sin mostrar valores
vacíos ni la palabra "null".

### REQ-023 — Presentación del total

**When** la API responde con código HTTP 201 y el cuerpo contiene el campo `total`,
**the system shall** mostrar el valor de `total` en el área de resultado de forma destacada,
formateado con dos decimales.

### REQ-024 — Presentación del tiempo estimado de entrega

**When** la API responde con código HTTP 201 y el cuerpo contiene el campo
`tiempoEstimadoEntregaDias`,
**the system shall** mostrar ese valor en el área de resultado acompañado de la unidad "días".

### REQ-025 — Indicación de servicio urgente en el resultado

**When** la API responde con código HTTP 201 y el campo `urgente` es `true`,
**the system shall** mostrar una etiqueta o distintivo visible en el área de resultado que
indique que la cotización corresponde a un servicio urgente.

### REQ-026 — Limpieza del área de resultado previa

**When** el usuario envía una nueva cotización y la API responde con código HTTP 201,
**the system shall** reemplazar cualquier resultado o mensaje de error previo en el área de
resultado con los datos de la nueva respuesta.

---

## 8. Manejo de errores del servidor

### REQ-027 — Error de validación o recurso no encontrado (HTTP 400 / 404)

**When** la API responde con código HTTP 400 o 404 y el cuerpo contiene el campo `mensaje`,
**the system shall** mostrar en el área de resultado el contenido de `mensaje` de forma
legible, sin recargar la página.

### REQ-028 — Error interno del servidor (HTTP 500)

**When** la API responde con código HTTP 500 o cualquier código de error no previsto,
**the system shall** mostrar en el área de resultado un mensaje genérico que informe al
usuario que ocurrió un problema en el servidor y que puede reintentar la operación.

### REQ-029 — Error de red o tiempo de espera agotado

**When** la petición `POST /api/cotizaciones` falla por un error de red o se agota el tiempo
de espera,
**the system shall** mostrar en el área de resultado un mensaje que indique que no fue posible
conectar con el servidor y sugiera verificar la conexión a internet.

---

## 9. Accesibilidad

### REQ-030 — Región dinámica en el área de resultado

**When** el área de resultado recibe contenido nuevo (resultado o error),
**the system shall** notificar a los lectores de pantalla mediante el atributo
`aria-live="polite"` presente en dicho contenedor desde la carga inicial de la página.

### REQ-031 — Estado deshabilitado accesible del botón

**While** el botón de envío está deshabilitado,
**the system shall** mantener el atributo `disabled` en el elemento `<button>` para que la
tecnología asistiva comunique correctamente el estado al usuario.

### REQ-032 — Mensajes de error asociados al campo correspondiente

**When** se muestra un mensaje de error de validación junto a un campo,
**the system shall** vincular el mensaje al campo mediante `aria-describedby` para que los
lectores de pantalla lo anuncien al enfocar dicho campo.

### REQ-033 — Navegación por teclado

**While** el formulario está visible,
**the system shall** permitir que todos los campos y el botón de envío sean alcanzables y
operables únicamente con el teclado, siguiendo el orden visual de la pantalla.

---

## 10. Apariencia y comportamiento visual

### REQ-034 — Diseño adaptable

**While** la página se visualiza en viewports de ancho entre 320 px y 1440 px,
**the system shall** adaptar el diseño del formulario y el área de resultado para que sean
legibles y utilizables sin desbordamiento horizontal ni scroll horizontal.

### REQ-035 — Contraste de texto

**While** la página está cargada,
**the system shall** garantizar que el contraste entre el texto y su fondo cumpla como mínimo
la relación 4.5:1 definida por WCAG 2.1 nivel AA para texto normal.

### REQ-036 — Retroalimentación visual del estado del botón

**While** el botón de envío está deshabilitado,
**the system shall** aplicar un estilo visual diferenciado (opacidad reducida o color
alterado) que comunique visualmente que la acción no está disponible.

---

## 11. Calidad del código

### REQ-037 — Sin dependencias de frameworks en tiempo de ejecución

**While** la aplicación está operativa,
**the system shall** funcionar utilizando únicamente HTML, CSS y JavaScript nativo del
navegador, sin requerir la descarga ni inclusión de frameworks, bibliotecas de componentes
ni gestores de paquetes en tiempo de ejecución.

### REQ-038 — Un único archivo HTML de entrada

**While** la aplicación está operativa,
**the system shall** ser iniciable abriendo un único archivo `index.html`, desde el cual se
referencien los recursos CSS y JavaScript necesarios.
