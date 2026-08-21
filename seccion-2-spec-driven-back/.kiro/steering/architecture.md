# Arquitectura hexagonal objetivo

El backend debe organizarse en tres capas concéntricas, con dependencias apuntando siempre hacia el dominio (nunca al revés):

- **domain**: entidades, objetos de valor y excepciones de negocio. Sin dependencias a frameworks ni a infraestructura.
- **application**: puertos de entrada (casos de uso), puertos de salida (contratos de repositorio) y los servicios que implementan los casos de uso orquestando el dominio.
- **infrastructure**: adaptadores de entrada (controladores REST, DTOs, mappers) y adaptadores de salida (repositorios en memoria o JPA, configuración del framework).

## Representación textual de las dependencias permitidas

```
infrastructure.adapter.in.rest ---> application.port.in ---> domain
infrastructure.adapter.out.persistence ---> application.port.out ---> domain
application.service (implementa port.in, usa port.out) ---> domain
```
