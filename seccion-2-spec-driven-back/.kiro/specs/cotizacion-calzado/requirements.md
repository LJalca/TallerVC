# Documento de Requisitos

## Introducción

Este documento especifica los requisitos funcionales del backend de cotizaciones para un taller de reparación de calzado. El sistema permite a un cliente consultar los tipos de calzado y reparaciones disponibles, seleccionar uno o más servicios y obtener una cotización con subtotal, recargo por urgencia (si aplica) y tiempo estimado de entrega. El alcance excluye autenticación, pagos y persistencia real; un repositorio en memoria es suficiente para cubrir las operaciones descritas.

---

## Glosario

| Término | Definición |
|---|---|
| **Cotizador** | Sistema backend objeto de estos requisitos |
| **Cotizacion** | Entidad que agrupa el tipo de calzado, las reparaciones seleccionadas, el flag de urgencia, el subtotal, el recargo y el total |
| **Calzado** | Entidad que representa un tipo de calzado (p. ej. bota, zapatilla deportiva) con un factor de complejidad asociado |
| **Reparacion** | Entidad que representa un servicio de reparación con precio base y tiempo estimado en días |
| **Subtotal** | Suma de (precio_base_reparacion × factor_complejidad_calzado) para cada reparación seleccionada |
| **Recargo** | Importe adicional del 30 % sobre el subtotal cuando el servicio es urgente |
| **Total** | Subtotal + recargo (cuando aplica) |
| **Tiempo_Estimado** | Valor en días enteros calculado según RN-03 |
| **GenerarCotizacionUseCase** | Puerto de entrada que orquesta la creación de una cotización |
| **ConsultarCatalogoUseCase** | Puerto de entrada que expone los catálogos de calzado y reparaciones |
| **CotizacionRepositoryPort** | Puerto de salida para almacenar y recuperar cotizaciones |
| **CalzadoRepositoryPort** | Puerto de salida para recuperar los tipos de calzado disponibles |
| **ReparacionRepositoryPort** | Puerto de salida para recuperar las reparaciones disponibles |
| **CotizacionController** | Adaptador REST de entrada que expone los endpoints del sistema |
| **UrgencyPricingStrategy** | Estrategia (patrón Strategy) que encapsula el cálculo del recargo por urgencia |
| **FechaCreacion** | Marca temporal en formato ISO-8601 (`yyyy-MM-ddTHH:mm:ss`) asignada por el sistema en el momento en que se genera la cotización (RN-05) |

---

## Requisitos

### Requisito 1 — Consultar tipos de calzado disponibles

**Historia de usuario (HU-03):** Como cliente, quiero consultar los tipos de calzado disponibles antes de generar la cotización, para saber qué puedo seleccionar.

#### Criterios de aceptación

1. THE **Cotizador** SHALL exponer un endpoint REST `GET /api/tipos-calzado` que devuelve la lista completa de tipos de calzado registrados en el catálogo.
2. WHEN el catálogo de calzado contiene al menos un elemento, THE **CotizacionController** SHALL devolver una respuesta HTTP 200 con la lista de tipos de calzado en formato JSON, incluyendo identificador, nombre y factor de complejidad.
3. IF el **CalzadoRepositoryPort** no puede recuperar los datos, THEN THE **CotizacionController** SHALL devolver una respuesta HTTP 500 con un mensaje de error descriptivo.

---

### Requisito 2 — Consultar reparaciones disponibles

**Historia de usuario (HU-03):** Como cliente, quiero consultar las reparaciones disponibles antes de generar la cotización, para saber qué puedo seleccionar.

#### Criterios de aceptación

1. THE **Cotizador** SHALL exponer un endpoint REST `GET /api/tipos-reparacion` que devuelve la lista completa de reparaciones registradas en el catálogo.
2. WHEN el catálogo de reparaciones contiene al menos un elemento, THE **CotizacionController** SHALL devolver una respuesta HTTP 200 con la lista de reparaciones en formato JSON, incluyendo identificador, nombre, precio base y tiempo estimado en días.
3. IF el **ReparacionRepositoryPort** no puede recuperar los datos, THEN THE **CotizacionController** SHALL devolver una respuesta HTTP 500 con un mensaje de error descriptivo.

---

### Requisito 3 — Generar cotización sin urgencia

**Historia de usuario (HU-01):** Como cliente, quiero seleccionar un tipo de calzado y una o más reparaciones para obtener una cotización estimada del costo total.

**Reglas de negocio aplicables:** RN-05

#### Criterios de aceptación

1. THE **Cotizador** SHALL exponer un endpoint REST `POST /api/cotizaciones` que acepta un objeto JSON con el identificador del tipo de calzado, la lista de identificadores de reparaciones seleccionadas y el flag de urgencia.
2. WHEN se recibe una solicitud válida con al menos una reparación y el flag de urgencia en `false`, THE **GenerarCotizacionUseCase** SHALL calcular el subtotal como la suma de (precio_base_reparacion × factor_complejidad_calzado) para cada reparación seleccionada.
3. WHEN el subtotal ha sido calculado sin urgencia, THE **GenerarCotizacionUseCase** SHALL establecer el recargo en cero y el total igual al subtotal.
4. WHEN se genera una cotización, THE **GenerarCotizacionUseCase** SHALL asignar a la cotización un identificador único y una **FechaCreacion** correspondiente al instante de generación en formato ISO-8601 (`yyyy-MM-ddTHH:mm:ss`).
5. WHEN la cotización es generada con éxito, THE **CotizacionController** SHALL devolver una respuesta HTTP 201 con el identificador de la cotización, la **FechaCreacion**, subtotal, recargo, total y tiempo estimado de entrega en días.
6. WHEN se recibe una solicitud válida, THE **CotizacionRepositoryPort** SHALL persistir la cotización generada en el repositorio en memoria.

---

### Requisito 4 — Generar cotización con urgencia

**Historia de usuario (HU-02):** Como cliente, quiero marcar el servicio como urgente para conocer el recargo aplicable y el nuevo tiempo estimado de entrega.

**Reglas de negocio aplicables:** RN-05

#### Criterios de aceptación

1. WHEN se recibe una solicitud válida con al menos una reparación y el flag de urgencia en `true`, THE **UrgencyPricingStrategy** SHALL calcular el recargo como el 30 % del subtotal.
2. WHEN el recargo ha sido calculado, THE **GenerarCotizacionUseCase** SHALL establecer el total como la suma del subtotal más el recargo.
3. WHEN el servicio es urgente, THE **GenerarCotizacionUseCase** SHALL calcular el tiempo estimado de entrega reduciendo a la mitad el máximo de los tiempos individuales de las reparaciones seleccionadas, redondeando hacia arriba, con un mínimo de 1 día.
4. WHEN la cotización con urgencia es generada con éxito, THE **CotizacionController** SHALL devolver una respuesta HTTP 201 con el identificador de la cotización, la **FechaCreacion**, subtotal, recargo, total y el tiempo estimado de entrega ajustado.

---

### Requisito 5 — Cálculo del tiempo estimado de entrega (sin urgencia)

**Historia de usuario (HU-01):** Como cliente, quiero seleccionar una o más reparaciones para obtener un tiempo estimado de entrega.

#### Criterios de aceptación

1. WHEN el flag de urgencia es `false`, THE **GenerarCotizacionUseCase** SHALL calcular el tiempo estimado de entrega como el máximo de los tiempos estimados en días de las reparaciones seleccionadas.
2. WHEN la lista de reparaciones seleccionadas contiene un único elemento, THE **GenerarCotizacionUseCase** SHALL establecer el tiempo estimado de entrega igual al tiempo estimado de esa reparación.

---

### Requisito 6 — Validación de la solicitud de cotización

**Historia de usuario (HU-01 / HU-02):** Como cliente, quiero recibir mensajes de error claros si mi solicitud es inválida.

#### Criterios de aceptación

1. IF la solicitud no incluye ninguna reparación seleccionada, THEN THE **GenerarCotizacionUseCase** SHALL rechazar la solicitud con un error de validación que indique que se requiere al menos una reparación.
2. IF la solicitud rechazada no contiene reparaciones, THEN THE **CotizacionController** SHALL devolver una respuesta HTTP 400 con un mensaje de error descriptivo.
3. IF el identificador del tipo de calzado recibido no existe en el catálogo, THEN THE **GenerarCotizacionUseCase** SHALL rechazar la solicitud con un error que indique que el tipo de calzado no fue encontrado.
4. IF el identificador de calzado no es encontrado, THEN THE **CotizacionController** SHALL devolver una respuesta HTTP 404 con un mensaje de error descriptivo.
5. IF algún identificador de reparación recibido no existe en el catálogo, THEN THE **GenerarCotizacionUseCase** SHALL rechazar la solicitud con un error que indique qué identificadores no fueron encontrados.
6. IF algún identificador de reparación no es encontrado, THEN THE **CotizacionController** SHALL devolver una respuesta HTTP 404 con un mensaje de error descriptivo.

