# Arquitectura objetivo del frontend

En lugar de las tres capas hexagonales del backend, el frontend usa una separación mucho más liviana en tres responsabilidades dentro de la carpeta `js/`, pensada para JavaScript sin framework y sin bundler:

| Archivo | Responsabilidad | Puede depender de |
|---|---|---|
| `index.html` + `css/estilos.css` | Estructura y estilo visual. Expone elementos con `id` estables para que `app.js` los enlace. No contiene lógica. | Nada |
| `js/state.js` | Mantiene en memoria el estado de la aplicación (catálogo cargado, selección actual del usuario, última cotización recibida). | Nada — no toca el DOM ni hace fetch |
| `js/api.js` | Único módulo que conoce las URLs del backend; hace fetch y traduce las respuestas HTTP a objetos JavaScript simples. | Nada — no conoce el DOM ni el estado |
| `js/app.js` | Escucha eventos del DOM, coordina `state.js` y `api.js`, y decide qué volver a pintar en pantalla. | `state.js` y `api.js` |

## Regla de dependencia única

Equivalente liviano de la regla hexagonal del backend: `state.js` y `api.js` no se conocen entre sí ni conocen el DOM; toda coordinación pasa por `app.js`. Esto evita, por ejemplo, que una función de renderizado termine haciendo fetch directamente, o que `api.js` manipule elementos del HTML.

## Principio de adaptación al stack

La especificación técnica no se copia entre proyectos: se adapta a lo que el stack necesita para mantenerse ordenado. Un backend con reglas de negocio pide capas y puertos; un frontend sin framework que solo pinta un formulario y llama tres endpoints pide, como mucho, separar "qué se ve" de "qué se sabe" de "cómo se habla con el servidor". No se aplica arquitectura hexagonal, patrón Repository ni Strategy aquí: ese frontend no protege reglas de negocio propias — esas ya viven en el backend.
