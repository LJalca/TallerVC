# Bitácora de sesión — Sección 4: Despliegue con Docker Compose

**Fecha:** 21 de agosto de 2026  
**Modalidad:** Vibe Coding (chat interactivo con Kiro)

---

## Mensaje 1

**Usuario:**  
Tengo un `docker-compose.yml` con MySQL y Nginx en `seccion-1-vibe-coding`, y dos proyectos ya construidos: `cotizador-backend` (Spring Boot, JAR, puerto 8080) en `seccion-2-spec-driven-back` y `cotizador-frontend` (HTML/CSS/JS) en `seccion-2-spec-driven-front`. Quiero extender el `docker-compose.yml` para que el backend corra como contenedor sin publicar su puerto al host, y que Nginx sirva el frontend y actúe como proxy para `/api/`. Todo debe seguir levantándose con un solo comando.

**Acción de Kiro:**  
Exploración completa de los tres proyectos. Se leyeron: `docker-compose.yml`, `nginx.conf`, `.env`, `Dockerfile` del backend, `pom.xml`, `application.properties` y la estructura del frontend.

**Hallazgos:**
- El backend ya tenía un `Dockerfile` en `cotizador-backend/` — no era necesario crear uno nuevo.
- `nginx.conf` ya tenía upstream y proxy para `/api/` comentados.
- `NGINX_PORT` no estaba en `.env`; el default era `8080`, colisionando con el puerto interno del backend.
- El backend usa Java 17 (no 21 como se mencionó inicialmente; el `pom.xml` confirma 17).

**Archivos modificados:**

`seccion-1-vibe-coding/.env` — se agregaron dos variables:
```dotenv
NGINX_PORT=80
ADMINER_PORT=8081
```

`seccion-1-vibe-coding/docker-compose.yml` — se añadió el servicio `backend` y se actualizó `nginx`:
- `backend`: `build.context` apunta al Dockerfile existente, `expose: 8080` (sin `ports`), `depends_on mysql (service_healthy)`, healthcheck propio.
- `nginx`: volumen del frontend cambiado a `../seccion-2-spec-driven-front/cotizador-frontend`, `depends_on backend (service_started)` agregado.

`seccion-1-vibe-coding/nginx/nginx.conf` — se activaron el upstream y el proxy:
- `upstream backend { server backend:8080; }`
- `location /api/` con `proxy_pass http://backend` (preserva el prefijo `/api/`).
- `location /` sirve estáticos con `try_files`.

---

## Mensaje 2

**Usuario:**  
El backend no debe publicar su puerto al host, solo debe ser accesible desde dentro de la red de Docker.

**Acción de Kiro:**  
Se verificó el `docker-compose.yml`. El servicio `backend` ya usaba `expose: 8080` sin `ports`, lo cual es exactamente el comportamiento pedido.

**Archivos modificados:** Ninguno. Ya estaba correcto.

**Explicación entregada:**  
- `expose` documenta el puerto solo dentro de la red Docker.
- `ports` es lo que publicaría el puerto al host — y no aparece en el servicio `backend`.

---

## Mensaje 3

**Usuario:**  
Haz que Nginx sirva los archivos de `cotizador-frontend` como contenido estático y agrega un `location /api/` que reenvíe al backend.

**Acción de Kiro:**  
Se verificaron `nginx.conf` y `docker-compose.yml`. Ambas configuraciones ya estaban en su lugar desde el Mensaje 1.

**Archivos modificados:** Ninguno. Ya estaba correcto.

**Explicación entregada:**
- `nginx.conf` tenía `upstream backend { server backend:8080; }` y `location /api/` activos.
- El volumen de Nginx montaba `../seccion-2-spec-driven-front/cotizador-frontend` en `/usr/share/nginx/html`.

---

## Mensaje 4

**Usuario:**  
Agrega un `depends_on` para que Nginx espere a que el backend haya arrancado antes de aceptar tráfico.

**Acción de Kiro:**  
Se verificó el `docker-compose.yml`. El `depends_on` en el servicio `nginx` ya incluía el backend.

**Archivos modificados:** Ninguno. Ya estaba correcto.

**Configuración confirmada:**
```yaml
nginx:
  depends_on:
    mysql:
      condition: service_healthy
    backend:
      condition: service_started
```

---

## Mensaje 5

**Usuario:**  
Revisa si `api.js` del frontend está usando una URL absoluta o una ruta relativa; si es absoluta, ajústala para que funcione detrás de Nginx sin cambiar de origen.

**Acción de Kiro:**  
Se leyó `seccion-2-spec-driven-front/cotizador-frontend/js/api.js`.

**Archivos modificados:** Ninguno. Ya estaba correcto.

**Hallazgo:**
```javascript
const API_BASE_URL = '';
// Llamadas resultantes: /api/tipos-calzado, /api/tipos-reparacion, /api/cotizaciones
```
Al ser rutas relativas, el browser las resuelve contra el mismo origen (Nginx), evitando CORS por completo.

---

## Mensaje 6

**Usuario:**  
`curl -I http://localhost:8080/` → `Failed to connect to localhost port 8080`

**Acción de Kiro:**  
Sin cambios. Se explicó que el error es el comportamiento esperado y correcto.

**Archivos modificados:** Ninguno.

**Explicación entregada:**  
El backend usa `expose` sin `ports`, por lo tanto el puerto 8080 no está vinculado al host. El acceso correcto es siempre a través de Nginx:
```bash
curl http://localhost:80/
curl http://localhost:80/api/tipos-calzado
```

---

## Mensaje 7

**Usuario:**  
"expone su API en el puerto 8080"

**Acción de Kiro:**  
Sin cambios. Se aclaró la distinción entre exponer dentro de la red Docker y publicar al host.

**Archivos modificados:** Ninguno.

**Explicación entregada:**  
El backend sí expone el puerto 8080 — pero solo dentro de `tallerdae-net`. Nginx lo alcanza en `backend:8080` sin problema. Lo que no hace es publicar ese puerto al host, que es exactamente lo pedido. El flujo correcto es:
```
Browser → localhost:80 → Nginx → backend:8080 (red interna) → Spring Boot
```

---

## Resumen de archivos afectados

| Archivo | Cambio |
|---|---|
| `seccion-1-vibe-coding/.env` | Agregadas `NGINX_PORT=80` y `ADMINER_PORT=8081` |
| `seccion-1-vibe-coding/docker-compose.yml` | Nuevo servicio `backend`; volumen y `depends_on` actualizados en `nginx` |
| `seccion-1-vibe-coding/nginx/nginx.conf` | Upstream y `location /api/` activados; volumen del frontend aplicado |
| `seccion-2-spec-driven-front/cotizador-frontend/js/api.js` | Sin cambios (ya usaba rutas relativas) |
| `seccion-2-spec-driven-back/cotizador-backend/Dockerfile` | Sin cambios (reutilizado tal cual) |
