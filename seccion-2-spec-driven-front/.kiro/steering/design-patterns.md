# Patrones de diseño a aplicar en el frontend

Se sugieren cuatro patrones livianos, todos expresables en JavaScript nativo sin librerías. Los cinco patrones del backend (Strategy, Factory Method, Repository, DTO+Mapper, inyección de dependencias) no se piden aquí: el frontend no tiene reglas de negocio propias ni múltiples implementaciones intercambiables que justifiquen esa maquinaria.

| Patrón | Dónde se aplica | Justificación |
|---|---|---|
| Module pattern (ES Modules) | `api.js` y `state.js` exportan únicamente funciones públicas con `export`; el resto de su contenido queda privado al archivo | Oculta detalles internos sin necesitar clases ni un framework |
| Adapter | `api.js` traduce el contrato OpenAPI del backend a funciones JavaScript simples (`obtenerTiposCalzado()`, `generarCotizacion()`) | Si el contrato HTTP cambia, solo se ajusta este archivo; el resto de la app no lo nota |
| Factory simple (función constructora) | Una función `construirRequestCotizacion(estado)` arma el body de `POST /api/cotizaciones` a partir del estado actual | Evita construir ese objeto en más de un lugar si mañana se agrega otro punto de entrada (por ejemplo, un botón "cotizar de nuevo") |
| Observer ligero (callback de suscripción) | `state.js` expone una función `onCambio(callback)` que `app.js` usa para volver a pintar cuando el estado cambia | Evita que `app.js` tenga que acordarse de llamar a `render()` manualmente después de cada acción |
