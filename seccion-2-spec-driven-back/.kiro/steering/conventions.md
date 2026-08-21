# Convenciones de nomenclatura

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquete raíz | minúsculas, invertido de dominio | com.tallerdae.cotizador |
| Entidad / Value Object de dominio | PascalCase, sustantivo, sin sufijo técnico | Cotizacion, Calzado, Dinero |
| Puerto de entrada (caso de uso) | PascalCase + sufijo UseCase | GenerarCotizacionUseCase |
| Puerto de salida (repositorio) | PascalCase + sufijo RepositoryPort | CotizacionRepositoryPort |
| Implementación de caso de uso | PascalCase + sufijo Service | GenerarCotizacionService |
| Adaptador REST de entrada | PascalCase + sufijo Controller | CotizacionController |
| Adaptador de persistencia | PascalCase + sufijo (InMemory\|Jpa)Adapter | InMemoryCotizacionRepositoryAdapter |
| DTO de request/response | PascalCase + sufijo Request / Response | CotizacionRequest, CotizacionResponse |
| Mapper dominio ⇄ DTO | PascalCase + sufijo Mapper | CotizacionMapper |
| Método | camelCase, verbo que expresa intención | calcularTotal(), generarCotizacion() |
| Constante | MAYÚSCULAS_CON_GUION_BAJO | RECARGO_URGENCIA_PORCENTAJE |
