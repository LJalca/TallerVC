# Plan de Implementación: Cotizador de Calzado

## Resumen

Implementación del backend de cotizaciones para un taller de reparación de calzado siguiendo arquitectura hexagonal (Ports & Adapters) con Spring Boot y Java. El plan cubre la capa de dominio, los puertos de aplicación, los servicios, los adaptadores REST y los repositorios en memoria, junto con pruebas unitarias, de propiedades y de integración.

---

## Tareas

- [ ] 1. Configurar la estructura del proyecto Spring Boot
  - Crear el proyecto Maven/Gradle con las dependencias: `spring-boot-starter-web`, `jqwik`, `spring-boot-starter-test`, `junit-jupiter`
  - Definir los paquetes raíz: `com.tallerdae.cotizador.domain`, `com.tallerdae.cotizador.application`, `com.tallerdae.cotizador.infrastructure`
  - _Requisitos: 1.1, 2.1, 3.1_

- [ ] 2. Implementar el modelo de dominio
  - [ ] 2.1 Crear las entidades `Calzado` y `Reparacion`
    - Definir `Calzado` con campos `id (UUID)`, `nombre (String)`, `factorComplejidad (BigDecimal)`
    - Definir `Reparacion` con campos `id (UUID)`, `nombre (String)`, `precioBase (BigDecimal)`, `tiempoEstimadoDias (int)`
    - _Requisitos: 1.2, 2.2_
  - [ ] 2.2 Crear el value object `Dinero` y el enum `NivelUrgencia`
    - Implementar `Dinero` con `monto (BigDecimal)` y `moneda (String)`, métodos `sumar(Dinero)` y `aplicarPorcentaje(BigDecimal)`
    - Definir la constante `Dinero.ZERO` para recargo cero
    - Crear el enum `NivelUrgencia` con valores `NORMAL` y `URGENTE`
    - _Requisitos: 3.2, 3.3, 4.1, 4.2_
  - [ ] 2.3 Implementar la interfaz `UrgencyPricingStrategy` y sus dos implementaciones
    - Declarar la interfaz con el método `calcularRecargo(Dinero subtotal): Dinero`
    - Implementar `NormalPricingStrategy`: siempre retorna `Dinero.ZERO`
    - Implementar `UrgentPricingStrategy`: retorna `subtotal.aplicarPorcentaje(0.30)` — constante `RECARGO_URGENCIA_PORCENTAJE = 0.30`
    - _Requisitos: 3.3, 4.1_
  - [ ]* 2.4 Escribir prueba de propiedad P2 — Recargo cero/30 % según urgencia
    - **Propiedad 2: Recargo cero cuando nivelUrgencia=NORMAL; 30 % del subtotal cuando nivelUrgencia=URGENTE**
    - Clase `UrgencyPricingStrategyPropertyTest`, generadores `@ForAll BigDecimal subtotal`, `@ForAll NivelUrgencia nivelUrgencia`
    - **Valida: Requisitos 3.3, 4.1**
  - [ ] 2.5 Implementar la entidad `Cotizacion` con Factory Method `Cotizacion.crear(...)`
    - Constructor privado; método estático `crear(Calzado, List<Reparacion>, NivelUrgencia, UrgencyPricingStrategy)`
    - El método valida que la lista de reparaciones no esté vacía (RN-01), lanza `ValidacionException` si lo está
    - Implementar `calcularSubtotal()`: `Σ(precioBase_i × factorComplejidad)` (RN-02)
    - Implementar `calcularTiempoEstimado()`: RN-03 (sin urgencia) y RN-04 (con urgencia: `max(1, ⌈max/2⌉)`)
    - Asignar `id = UUID.randomUUID()`, `fechaCreacion = LocalDateTime.now()`, `recargo` vía estrategia, `total = subtotal + recargo` (RN-05)
    - _Requisitos: 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 5.1, 5.2, 6.1_
  - [ ]* 2.6 Escribir prueba de propiedad P1 — Subtotal es la suma exacta de productos precio × factor
    - **Propiedad 1: El subtotal es la suma exacta de productos precio × factor**
    - Clase `CotizacionSubtotalPropertyTest`, generadores `@ForAll BigDecimal factorComplejidad`, `@ForAll List<BigDecimal> preciosBases`
    - **Valida: Requisito 3.2**
  - [ ]* 2.7 Escribir prueba de propiedad P3 — Total siempre es subtotal + recargo
    - **Propiedad 3: El total siempre es subtotal + recargo**
    - Clase `CotizacionTotalPropertyTest`, combina generadores de P1 y P2
    - **Valida: Requisitos 3.3, 4.2**
  - [ ]* 2.8 Escribir prueba de propiedad P4 — Tiempo estimado respeta la fórmula según urgencia
    - **Propiedad 4: El tiempo estimado respeta la fórmula correcta según urgencia**
    - Clase `TiempoEstimadoPropertyTest`, generadores `@ForAll List<Integer> tiempos`, `@ForAll NivelUrgencia nivelUrgencia`
    - Verificar casos borde: días impares, mínimo de 1 día
    - **Valida: Requisitos 4.3, 5.1, 5.2**
  - [ ]* 2.9 Escribir prueba de propiedad P5 — Toda cotización válida tiene id y fechaCreacion no nulos
    - **Propiedad 5: Toda cotización generada tiene id UUID no nulo y fechaCreacion no nula**
    - Clase `CotizacionIdentidadPropertyTest`, request aleatorio válido completo
    - **Valida: Requisito 3.4**
  - [ ]* 2.10 Escribir prueba de propiedad P7 — Listas vacías de reparaciones siempre son rechazadas
    - **Propiedad 7: Listas vacías de reparaciones siempre son rechazadas**
    - Clase `ValidacionReparacionesPropertyTest`, generadores `@ForAll UUID calzadoId`, `@ForAll NivelUrgencia nivelUrgencia`
    - **Valida: Requisito 6.1**

- [ ] 3. Checkpoint — Verificar dominio
  - Asegurar que todos los tests del dominio pasan. Consultar al usuario si hay dudas antes de continuar.

- [ ] 4. Implementar las excepciones de dominio
  - [ ] 4.1 Crear `ValidacionException` y `RecursoNoEncontradoException`
    - Ambas extienden `RuntimeException`
    - `RecursoNoEncontradoException` recibe el mensaje con los ids no encontrados
    - _Requisitos: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

- [ ] 5. Definir los puertos de aplicación (interfaces)
  - [ ] 5.1 Crear los puertos de entrada `GenerarCotizacionUseCase` y `ConsultarCatalogoUseCase`
    - `GenerarCotizacionUseCase`: método `generarCotizacion(CotizacionRequest): Cotizacion`
    - `ConsultarCatalogoUseCase`: métodos `listarCalzados(): List<Calzado>` y `listarReparaciones(): List<Reparacion>`
    - _Requisitos: 1.1, 2.1, 3.1_
  - [ ] 5.2 Crear los puertos de salida `CotizacionRepositoryPort`, `CalzadoRepositoryPort` y `ReparacionRepositoryPort`
    - `CotizacionRepositoryPort`: método `guardar(Cotizacion)`
    - `CalzadoRepositoryPort`: métodos `listarTodos()` y `buscarPorId(UUID): Optional<Calzado>`
    - `ReparacionRepositoryPort`: métodos `listarTodos()` y `buscarPorId(UUID): Optional<Reparacion>`
    - _Requisitos: 1.3, 2.3, 3.6_

- [ ] 6. Implementar los servicios de aplicación
  - [ ] 6.1 Implementar `ConsultarCatalogoService`
    - Implementar `ConsultarCatalogoUseCase`
    - Inyectar `CalzadoRepositoryPort` y `ReparacionRepositoryPort` por constructor
    - Delegar directamente a los repositorios correspondientes
    - _Requisitos: 1.2, 2.2_
  - [ ] 6.2 Implementar `GenerarCotizacionService`
    - Implementar `GenerarCotizacionUseCase`
    - Inyectar `CalzadoRepositoryPort`, `ReparacionRepositoryPort` y `CotizacionRepositoryPort` por constructor
    - Flujo: validar ids → resolver `Calzado` (lanzar `RecursoNoEncontradoException` si no existe) → resolver `List<Reparacion>` (lanzar con ids faltantes) → seleccionar estrategia según `NivelUrgencia` → `Cotizacion.crear(...)` → `cotizacionRepository.guardar(...)` → retornar cotización
    - _Requisitos: 3.2, 3.3, 3.4, 3.6, 4.1, 4.2, 4.3, 6.1, 6.3, 6.5_
  - [ ]* 6.3 Escribir prueba de propiedad P8 — Ids inexistentes siempre producen error de no encontrado
    - **Propiedad 8: Ids inexistentes siempre producen RecursoNoEncontradoException**
    - Clase `ValidacionIdsPropertyTest`, UUID aleatorio no presente en el repositorio
    - **Valida: Requisitos 6.3, 6.5**
  - [ ]* 6.4 Escribir prueba de propiedad P6 — Toda cotización generada es recuperable por su id
    - **Propiedad 6: Persistencia — toda cotización generada es recuperable por su id**
    - Clase `CotizacionPersistenciaPropertyTest`, request aleatorio válido + repositorio en memoria
    - **Valida: Requisito 3.6**

- [ ] 7. Checkpoint — Verificar servicios de aplicación
  - Asegurar que todos los tests de aplicación pasan. Consultar al usuario si hay dudas antes de continuar.

- [ ] 8. Implementar los adaptadores de persistencia en memoria
  - [ ] 8.1 Implementar `InMemoryCalzadoRepositoryAdapter`
    - Implementar `CalzadoRepositoryPort` usando `Map<UUID, Calzado>`
    - Definir 4 UUIDs fijos como constantes e inicializar los datos semilla: Bota de cuero (1.50), Zapatilla deportiva (1.10), Zapato formal (1.25), Sandalia (0.90)
    - _Requisitos: 1.2, 6.3_
  - [ ] 8.2 Implementar `InMemoryReparacionRepositoryAdapter`
    - Implementar `ReparacionRepositoryPort` usando `Map<UUID, Reparacion>`
    - Definir 4 UUIDs fijos como constantes e inicializar los datos semilla: Cambio de suela (35000, 5d), Limpieza profunda (15000, 2d), Costura de refuerzo (20000, 3d), Tintado (25000, 4d)
    - _Requisitos: 2.2, 6.5_
  - [ ] 8.3 Implementar `InMemoryCotizacionRepositoryAdapter`
    - Implementar `CotizacionRepositoryPort` usando `Map<UUID, Cotizacion>`
    - No requiere datos semilla
    - _Requisito: 3.6_
  - [ ]* 8.4 Escribir pruebas unitarias para los adaptadores en memoria
    - Verificar que los datos semilla de `InMemoryCalzadoRepositoryAdapter` estén inicializados correctamente
    - Verificar que los datos semilla de `InMemoryReparacionRepositoryAdapter` estén inicializados correctamente
    - Verificar que `InMemoryCotizacionRepositoryAdapter.guardar(...)` persiste la cotización recuperable por su id
    - _Requisitos: 1.2, 2.2, 3.6_

- [ ] 9. Implementar los DTOs, el mapper y el controlador REST
  - [ ] 9.1 Crear los DTOs `CotizacionRequest`, `CotizacionResponse`, `CalzadoResponse` y `ReparacionResponse`
    - `CotizacionRequest`: `tipoCalzadoId (UUID)`, `reparacionIds (List<UUID>)`, `nivelUrgencia (NivelUrgencia)`
    - `CotizacionResponse`: `id`, `fechaCreacion (String ISO-8601)`, `tipoCalzadoId`, `nombreCalzado`, `reparacionIds`, `nivelUrgencia`, `subtotal`, `recargo`, `total`, `tiempoEstimadoDias`
    - `CalzadoResponse`: `id`, `nombre`, `factorComplejidad`
    - `ReparacionResponse`: `id`, `nombre`, `precioBase`, `tiempoEstimadoDias`
    - _Requisitos: 1.2, 2.2, 3.5, 4.4_
  - [ ] 9.2 Implementar `CotizacionMapper`
    - Método `toResponse(Cotizacion): CotizacionResponse`
    - Formatear `fechaCreacion` como `yyyy-MM-ddTHH:mm:ss`
    - _Requisitos: 3.5, 4.4_
  - [ ] 9.3 Implementar `CotizacionController` con los tres endpoints
    - `GET /api/tipos-calzado` → HTTP 200 con `List<CalzadoResponse>`
    - `GET /api/tipos-reparacion` → HTTP 200 con `List<ReparacionResponse>`
    - `POST /api/cotizaciones` → HTTP 201 con `CotizacionResponse`; delegar en los casos de uso y usar `CotizacionMapper`
    - _Requisitos: 1.1, 1.2, 2.1, 2.2, 3.1, 3.5, 4.4_
  - [ ] 9.4 Implementar `GlobalExceptionHandler` (`@ControllerAdvice`)
    - Capturar `ValidacionException` → HTTP 400
    - Capturar `RecursoNoEncontradoException` → HTTP 404
    - Capturar `RuntimeException` genérica → HTTP 500
    - Respuesta JSON con campos `error`, `mensaje` y `timestamp`
    - _Requisitos: 1.3, 2.3, 6.2, 6.4, 6.6_
  - [ ]* 9.5 Escribir pruebas unitarias del controlador y del mapper
    - Verificar que `CotizacionController` traduce `ValidacionException` → HTTP 400
    - Verificar que `CotizacionController` traduce `RecursoNoEncontradoException` → HTTP 404
    - Verificar que `CotizacionMapper` convierte correctamente una cotización NORMAL y una URGENTE
    - _Requisitos: 3.5, 4.4, 6.2, 6.4_

- [ ] 10. Escribir pruebas de integración end-to-end
  - [ ]* 10.1 Escribir pruebas de integración con Spring Boot Test + MockMvc
    - `GET /api/tipos-calzado` → HTTP 200 y cuerpo con los 4 datos semilla
    - `GET /api/tipos-reparacion` → HTTP 200 y cuerpo con los 4 datos semilla
    - `POST /api/cotizaciones` → HTTP 201 cotización `NivelUrgencia.NORMAL` con ejemplo concreto
    - `POST /api/cotizaciones` → HTTP 201 cotización `NivelUrgencia.URGENTE` con ejemplo concreto
    - `POST /api/cotizaciones` → HTTP 400 con lista vacía de reparaciones
    - `POST /api/cotizaciones` → HTTP 404 con id de calzado inexistente
    - `POST /api/cotizaciones` → HTTP 404 con id de reparación inexistente
    - _Requisitos: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.5, 4.4, 6.2, 6.4, 6.6_

- [ ] 11. Checkpoint final — Verificar implementación completa
  - Asegurar que todos los tests (unitarios, de propiedades e integración) pasan. Consultar al usuario si hay dudas antes de entregar.

---

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido.
- Cada tarea referencia los requisitos correspondientes para trazabilidad.
- Los checkpoints garantizan validación incremental por capas (dominio → aplicación → infraestructura → REST).
- Las pruebas de propiedades usan **jqwik** con un mínimo de 100 iteraciones por propiedad (`tries = 100`).
- Las pruebas unitarias usan **JUnit 5 + Mockito**; las de integración usan **Spring Boot Test + MockMvc**.
- Los UUIDs fijos de los adaptadores en memoria se definen como constantes para garantizar reproducibilidad.

---

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2.1", "2.2", "4.1"] },
    { "id": 2, "tasks": ["2.3", "5.1", "5.2"] },
    { "id": 3, "tasks": ["2.4", "2.5"] },
    { "id": 4, "tasks": ["2.6", "2.7", "2.8", "2.9", "2.10", "6.1", "6.2"] },
    { "id": 5, "tasks": ["6.3", "6.4", "8.1", "8.2", "8.3"] },
    { "id": 6, "tasks": ["8.4", "9.1"] },
    { "id": 7, "tasks": ["9.2", "9.3", "9.4"] },
    { "id": 8, "tasks": ["9.5", "10.1"] }
  ]
}
```
