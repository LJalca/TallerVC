# Cierre del taller: comparación de experiencia

## 5.1 Tabla de reflexión comparativa

| Dimensión | Sección 1 — Vibe Coding | Sección 2 — Spec-Driven Dev. |
|---|---|---|
| N.º de iteraciones con la IA | 13 prompts (bitácora completa, incluye 3 errores reales de configuración) | Backend: ~9 rondas de ajuste documentadas en gates (requirements, design, tasks). Frontend: proceso equivalente sin chat de Kiro (ver nota metodológica en `BITACORA-FRONT.md`) |
| ¿Revisaste todo el código generado? | Parcial — la reflexión de Sección 1 admite "confianza ciega" cuando funcionaba al primer intento | Sí — cada fase (requirements/design/tasks) tuvo gate explícito antes de aprobar, y la ejecución encontró y corrigió errores reales antes de aceptar el resultado |
| Nivel de confianza en el resultado (1-5) | 3 — funcional, pero con supuestos sin verificar (ver reflexión: `depends_on` sin `condition`, valores por defecto implícitos) | 5 — validado contra escenarios Gherkin del Anexo C (backend, tests en verde) y contra el contrato real end-to-end (frontend, confirmado en la integración de Sección 3) |
| ¿Podrías explicar cada decisión de diseño? | Parcial — las decisiones de Kiro no siempre se cuestionaron antes de aceptarlas | Sí — cada patrón de diseño y cada capa hexagonal tiene su justificación documentada en los steering files |
| ¿Otra persona podría retomar tu trabajo sin vos? | Con esfuerzo — la bitácora existe, pero no hay spec formal de por qué se tomó cada decisión | Sí — `requirements.md`/`design.md`/`tasks.md` documentan el qué y el por qué de cada pieza |

## 5.2 Discusión final

### 1. ¿En qué momento de la Sección 2 sentiste que la especificación te "frenaba"? ¿Ese freno era injustificado o evitó un error?

El gate de `design.md` del backend frenó dos veces por desalineación real con la
especificación técnica (nombres de clase, DTOs con campos que no coincidían con el
Anexo B). No fue injustificado: si esos gates no hubieran frenado, el DTO HTTP habría
llegado a producción con campos `calzadoId`/`reparacionIds` en vez de
`tipoCalzadoId`/`tipoReparacionIds` — toda petición del frontend habría fallado en
silencio.

### 2. Si tuvieras que mantener este cotizador en producción durante tres años con un equipo rotativo, ¿qué artefactos de la Sección 2 agradecerías tener?

El contrato OpenAPI (fuente de verdad entre back y front) y los `requirements.md` con
notación EARS — permiten a alguien nuevo entender el *por qué* de una regla de negocio
sin tener que leer el código primero. Sin esto, el bug real que encontramos (contrato
roto entre `CotizacionResponse` real y lo que el frontend esperaba) habría sido mucho
más difícil de diagnosticar sin documentación de por medio.

### 3. Piensa en tu proyecto de curso o en tu trabajo actual: identifica una tarea de esta semana que debería tratarse como vibe coding y otra que debería tratarse como spec-driven development. Justifica ambas con la matriz de decisión de la sesión teórica.

> _Pendiente — completar con una tarea real del curso o del trabajo actual del
> estudiante. No se completa aquí porque requiere contexto personal que no está
> disponible en este repositorio._

### 4. La Sección 3 mezcló un ambiente creado con vibe coding (Sección 1) con proyectos creados con spec-driven development (Sección 2). ¿Ese punto de integración debería, a su vez, tratarse con más o con menos estructura que las piezas que conecta? Justifica tu respuesta.

Menos — y la evidencia lo confirma: de los 7 mensajes de la bitácora de integración, 5
fueron confirmaciones de que Kiro ya había hecho lo correcto. Cuando las piezas que se
conectan ya pasaron por un gate disciplinado, la integración es mayormente cableado, no
diseño — exigirle el mismo rigor sería sobre-especificar un problema de plomería.
