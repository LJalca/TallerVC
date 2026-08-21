# Patrones de diseño a aplicar

| Patrón | Dónde se aplica | Justificación |
|---|---|---|
| Strategy | Cálculo del recargo por nivel de urgencia (UrgencyPricingStrategy) | Permite agregar nuevos niveles de urgencia sin modificar la lógica existente (abierto/cerrado) |
| Factory Method | Creación de Cotizacion mediante un método estático de fábrica que valida invariantes (RN-04, RN-05) | Garantiza que no exista una Cotizacion en estado inválido |
| Repository | CotizacionRepositoryPort, CalzadoRepositoryPort, ReparacionRepositoryPort | Desacopla el dominio de la tecnología de persistencia concreta |
| DTO + Mapper | CotizacionRequest/Response y CotizacionMapper | Evita que el modelo de dominio quede acoplado al contrato HTTP |
| Inyección de dependencias | Construcción de servicios de aplicación con sus puertos de salida | Invierte el control entre application e infrastructure (regla de dependencia hexagonal) |
