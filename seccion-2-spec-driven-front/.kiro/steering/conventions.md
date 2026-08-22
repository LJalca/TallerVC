# Convenciones de nomenclatura del frontend

| Elemento | Convención | Ejemplo |
|---|---|---|
| Archivos JavaScript | camelCase, un sustantivo que describe su responsabilidad | `api.js`, `app.js`, `state.js` |
| IDs de elementos HTML | kebab-case, descriptivo del control | `tipo-calzado-select`, `boton-cotizar`, `resultado-cotizacion` |
| Clases CSS | kebab-case, BEM ligero (`bloque__elemento--modificador`) solo donde aporte claridad | `cotizador__resultado`, `cotizador__resultado--urgente` |
| Funciones | camelCase, verbo que expresa intención | `obtenerTiposCalzado()`, `renderizarResultado()`, `construirRequestCotizacion()` |
| Variables de estado | camelCase, sustantivo | `cotizacionActual`, `tiposCalzadoDisponibles`, `servicioEsUrgente` |
| Constantes de configuración | MAYÚSCULAS_CON_GUION_BAJO | `API_BASE_URL` |
| Eventos personalizados (si se usan) | kebab-case con prefijo del módulo de origen | `cotizador:resultado-listo` |
