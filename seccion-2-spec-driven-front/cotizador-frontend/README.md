# Cotizador de Reparación de Calzado — Frontend

Pantalla única construida con **HTML, CSS y JavaScript nativo**. Sin framework, sin
bundler, sin dependencias de npm y sin recursos de CDN en tiempo de ejecución
(REQ-037).

## Estructura

```
cotizador-frontend/
├── index.html          # Único punto de entrada (REQ-038)
├── css/
│   └── estilos.css     # Variables CSS, reset, layout, componentes, responsive
└── js/
    ├── state.js        # Estado en memoria (sin DOM, sin fetch)
    ├── api.js          # Llamadas fetch a la API (sin DOM, sin estado)
    └── app.js          # Coordinador de UI (único módulo que toca el DOM)
```

### Regla de dependencia única

`state.js` y `api.js` no se conocen entre sí ni acceden al DOM. Toda la coordinación
pasa por `app.js`, que es el único que importa a ambos.

Ninguna regla de cálculo vive en el cliente: el subtotal, el recargo, el total y el
tiempo estimado llegan ya calculados en la respuesta de `POST /api/cotizaciones` y se
muestran tal cual.

## Cómo abrirlo

El proyecto no requiere instalación ni compilación. Basta con abrir `index.html`.

### Detrás del proxy (modo previsto)

`js/api.js` define `API_BASE_URL = ''`, es decir, **rutas relativas** (`/api/tipos-calzado`,
`/api/tipos-reparacion`, `/api/cotizaciones`). El frontend está pensado para servirse
detrás de un proxy Nginx que expone el backend en el **mismo origen**. En ese escenario
se abre la URL del proxy en el navegador y todo funciona sin configuración adicional.

### Desarrollo standalone

Abrir el archivo con doble clic (`file://`) carga la interfaz, pero las peticiones a la
API fallarán: los ES Modules requieren origen HTTP y, además, no hay un backend en el
mismo origen. Las dos opciones son:

1. **Levantar un servidor estático con proxy hacia el backend** (recomendado, mantiene
   `API_BASE_URL` vacío y el mismo origen). Por ejemplo, con Nginx apuntando `/api/` al
   backend y el resto a esta carpeta.

2. **Apuntar temporalmente al backend por su host y puerto**: cambiar en `js/api.js`

   ```js
   const API_BASE_URL = 'http://localhost:8080';
   ```

   Esto exige que el backend habilite CORS para el origen del frontend. Es un ajuste
   solo para desarrollo: el valor que debe quedar versionado es la cadena vacía.

## Endpoints que consume

| Método | Ruta | Uso |
|--------|------|-----|
| GET | `/api/tipos-calzado` | Poblar el desplegable de tipo de calzado al cargar |
| GET | `/api/tipos-reparacion` | Poblar los checkboxes de reparación al cargar |
| POST | `/api/cotizaciones` | Generar la cotización con la selección del formulario |

Si alguno de los dos catálogos falla al iniciar, se muestra un aviso en la página y el
botón de envío permanece deshabilitado hasta que se recargue.

## Especificación

Los documentos de referencia viven en `../.kiro/`:

- `.kiro/specs/cotizador-frontend/requirements.md` — REQ-001 a REQ-038
- `.kiro/specs/cotizador-frontend/design.md` — diseño técnico y trazabilidad
- `.kiro/specs/cotizador-frontend/tasks.md` — plan de implementación
- `.kiro/steering/` — arquitectura, convenciones y patrones transversales
