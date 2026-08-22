# Reflexión — Sección 3: Despliegue integrado con Vibe Coding

## 1. Iteraciones necesarias y primer fallo

Siete mensajes en la bitácora de integración. En rigor, nada falló de verdad: los
mensajes 2, 3, 4, 5 y 7 son confirmaciones — Kiro ya había resuelto todo correctamente
en el primer prompt (Mensaje 1) y las siguientes vueltas fueron pedir verificación de
cosas que ya estaban bien. Lo más cercano a un "fallo" fue el Mensaje 6, un
`curl -I http://localhost:8080/` que devolvió `Failed to connect` — pero era el
comportamiento esperado: el backend no publica su puerto al host a propósito, así que
no era un error sino la confirmación de que el aislamiento de red funcionaba.

## 2. Tentación de tocar código de negocio o de interfaz

No, y hay una razón concreta: el contrato entre frontend y backend (nombres de campos,
estructura de `CotizacionResponse`) ya se había detectado roto y corregido durante la
Sección 2, antes de llegar a esta integración. Los únicos archivos tocados en esta
sección fueron de infraestructura (`.env`, `docker-compose.yml`, `nginx.conf`) — ninguno
de negocio ni de interfaz. Si esa reconciliación no se hubiera hecho antes, esta sección
habría sido exactamente el lugar donde aparece la tentación de "parchear rápido" el
frontend para que el demo funcione.

## 3. Comparación con la Sección 1

Sección 1 partió de una carpeta vacía y tuvo fricción real: 13 prompts, con al menos 3
errores genuinos (servicio "db" no encontrado, sintaxis de `curl` mal en PowerShell,
cliente `mysql` no instalado). Sección 3 integró piezas que ya habían pasado por el gate
disciplinado de la Sección 2, y el resultado fue casi todo verificación en vez de
construcción. La lectura: cuando lo que integrás ya se construyó con rigor, vibe coding
deja de ser "generar y corregir" para ser "conectar y confirmar" — la fricción se mudó
río arriba, a la sección donde se construyeron las piezas.

## 4. Qué seguiría siendo válido y qué necesitaría más estructura

Válido: la separación de red (backend sin puerto publicado al host, solo alcanzable vía
Nginx), los healthchecks encadenados (`depends_on: condition: service_healthy`), y el
hecho de que ni un ajuste de esta sección tocó lógica de negocio.

Necesitaría más estructura: las credenciales siguen en un `.env` plano (mismo punto que
en la reflexión de Sección 1), no hay TLS ni gestión de secretos, no hay CI/CD que
reconstruya las imágenes automáticamente, y la integración vive mezclada dentro de
`seccion-1-vibe-coding/` en vez de en su propia carpeta — en un equipo real, eso
ameritaría su propio pipeline de despliegue, no un directorio compartido con el
ambiente de desarrollo.
