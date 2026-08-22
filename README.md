# Taller de Desarrollo con IA: Vibe Coding & Spec-Driven Development


> **Universidad Politécnica Salesiana (UPS)**  
> **Sistema:** Cotizador de Reparación de Calzado (*Taller DAE*)

Este repositorio contiene la implementación práctica y los entregables del taller de desarrollo asistido por Inteligencia Artificial, integrando dos paradigmas clave: **Vibe Coding** (para exploración, infraestructura y prototipado rápido) y **Spec-Driven Development (SDD)** (para diseño riguroso guiado por especificaciones, arquitectura hexagonal, DDD y contratos formales).

---

## 📋 Tabla de Contenidos

1. [Estructura del Repositorio](#-estructura-del-repositorio)
2. [Arquitectura del Sistema](#-arquitectura-del-sistema)
3. [Stack Tecnológico](#-stack-tecnológico)
4. [Metodología de las Secciones](#-metodología-de-las-secciones)
   - [Sección 1: Vibe Coding & Infraestructura Base](#sección-1-vibe-coding--infraestructura-base)
   - [Sección 2: Spec-Driven Development (Backend & Frontend)](#sección-2-spec-driven-development-backend--frontend)
   - [Sección 3 y 4: Integración, Pruebas y Despliegue](#sección-3-y-4-integración-pruebas-y-despliegue)
5. [Instrucciones de Ejecución](#-instrucciones-de-ejecución)
   - [Despliegue Integrado con Docker Compose](#1-despliegue-integrado-con-docker-compose-recomendado)
   - [Ejecución Standalone / Desarrollo Local](#2-ejecución-standalone--desarrollo-local)
6. [Contrato de API REST](#-contrato-de-api-rest)
7. [Pruebas Automatizadas](#-pruebas-automatizadas)
8. [Bitácoras y Evidencias](#-bitácoras-y-evidencias)

---

## 📂 Estructura del Repositorio

```text
TallerVC/
├── README.md                                   # Documentación principal del proyecto
├── seccion-1-vibe-coding/                      # Infraestructura y orquestación con Docker
│   ├── docker-compose.yml                      # Definición de servicios (MySQL, Backend, Nginx, Adminer)
│   ├── .env                                    # Variables de entorno y credenciales de desarrollo
│   ├── Bitacora.md                             # Bitácora detallada de prompts y decisiones
│   ├── reflexion.md                            # Análisis crítico de riesgos, seguridad y gobierno
│   └── nginx/                                  # Configuración de proxy inverso y archivos web
│       ├── nginx.conf
│       └── logs/
├── seccion-2-spec-driven-back/                 # Backend desarrollado bajo enfoque SDD
│   ├── .kiro/                                  # Reglas de dirección (steering) y especificaciones (specs)
│   │   ├── specs/                              # requirements.md, design.md, tasks.md
│   │   └── steering/                           # architecture.md, conventions.md, design-patterns.md
│   └── cotizador-backend/                      # Código fuente Spring Boot 3.4
│       ├── pom.xml
│       ├── openapi.yaml                        # Contrato OpenAPI 3.0
│       ├── Dockerfile                          # Build multi-stage (Maven + JRE 17 Alpine)
│       └── src/
├── seccion-2-spec-driven-front/                # Frontend desarrollado bajo enfoque SDD
│   ├── .kiro/                                  # Especificaciones de interfaz y arquitectura cliente
│   └── cotizador-frontend/                     # Single Page Application Vanilla Web
│       ├── index.html                          # Punto de entrada único
│       ├── css/
│       │   └── estilos.css                     # Variables CSS, maquetación responsiva
│       └── js/
│           ├── state.js                        # Gestión de estado reactivo en memoria
│           ├── api.js                          # Cliente de API Fetch aislado
│           └── app.js                          # Orquestador UI y manipulación DOM
├── Capturas/                                   # Evidencias de la Sección 1
├── Capturas-entregables-seccion-2-back/        # Evidencias de aprobación de specs y bitácora backend
└── Capturas-seccion-4-despliegue/              # Evidencias de pruebas de integración y despliegue
```

---

## 🏗 Arquitectura del Sistema

El sistema implementa una arquitectura modular desacoplada:

```
                      ┌────────────────────────────────────────┐
                      │              Cliente Web               │
                      │         (Navegador / Frontend)         │
                      └───────────────────┬────────────────────┘
                                          │  HTTP :80
                                          ▼
                      ┌────────────────────────────────────────┐
                      │          Nginx (Reverse Proxy)         │
                      └──────────┬───────────────────┬─────────┘
            /* (Estáticos)       │                   │ /api/* (Proxy Pass)
                                 ▼                   ▼
                      ┌──────────────────┐   ┌───────────────────────────┐
                      │  HTML / CSS / JS │   │   Spring Boot 3 Backend   │
                      │(cotizador-front) │   │ (Arquitectura Hexagonal)  │
                      └──────────────────┘   └─────────────┬─────────────┘
                                                           │
                                                           ▼
                                             ┌───────────────────────────┐
                                             │          MySQL 8          │
                                             │      (Base de Datos)      │
                                             └───────────────────────────┘
```

- **Backend:** Diseñado bajo principios de **Arquitectura Hexagonal (Ports & Adapters)** y **Domain-Driven Design (DDD)**. El núcleo de dominio está completamente aislado de frameworks externos, utilizando entidades de dominio (`Cotizacion`, `Calzado`, `Reparacion`), Value Objects (`Dinero`), estrategias de cálculo (`NormalPricingStrategy`, `UrgentPricingStrategy`) y puertos de aplicación.
- **Frontend:** Arquitectura limpia en Vanilla JavaScript sin dependencias de compilación ni librerías externas. Aplica estricta separación de responsabilidades: `state.js` (solo estado), `api.js` (solo comunicación HTTP) y `app.js` (coordinador DOM).
- **Reverse Proxy:** Nginx centraliza el tráfico, sirviendo los archivos estáticos en la raíz y ruteando las peticiones `/api/` hacia el backend en una red interna aislada (`tallerdae-net`).

---

## 🛠 Stack Tecnológico

| Capa | Tecnologías |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.4.0, Spring Web, Maven |
| **Testing** | JUnit 5, jqwik (Property-based testing) |
| **Especificación de API** | OpenAPI 3.0 / Swagger (`openapi.yaml`) |
| **Frontend** | HTML5 Semántico, Vanilla CSS3 (Custom Properties), JavaScript ES6 Modules |
| **Base de Datos** | MySQL 8.0, Adminer 4 (administrador web) |
| **Infraestructura** | Docker, Docker Compose, Nginx 1.25 Alpine |

---

## 🔍 Metodología de las Secciones

### Sección 1: Vibe Coding & Infraestructura Base
- Creación ágil del entorno local reproducible mediante Docker Compose.
- Parametrización con variables de entorno (`.env`) y aislamiento de servicios en redes internas.
- Evaluación de riesgos en [reflexion.md](file:///c:/Users/LAJS/Documents/clj/MaeUPS/AplEmpre/TallerVC/seccion-1-vibe-coding/reflexion.md) sobre la "confianza ciega" en generación de código, seguridad de credenciales y gobernanza al promover a ambientes empresariales/staging.

### Sección 2: Spec-Driven Development (Backend & Frontend)
- Definición formal del sistema a través de **Steering Prompts** y ciclo de especificación en 3 fases:
  1. `requirements.md`: Definición de historias de usuario, reglas de negocio y restricciones.
  2. `design.md`: Modelado de dominio, diagramas de secuencia, invariantes y casos borde.
  3. `tasks.md`: Plan de trabajo granular ordenado respetando dependencias de capas.
- Control de alucinaciones y corrección temprana de discrepancias en contratos DTO/REST antes de la generación de código.

### Sección 3 y 4: Integración, Pruebas y Despliegue
- Configuración de multi-stage builds en Docker para optimizar el tamaño de las imágenes.
- Orquestación unificada en Docker Compose con healthchecks encadenados (`service_healthy`).
- Pruebas funcionales de casos de uso: cotizaciones estándar, cotizaciones con recargo por urgencia y múltiples reparaciones simultáneas.

---

## 🚀 Instrucciones de Ejecución

### 1. Despliegue Integrado con Docker Compose (Recomendado)

Asegúrate de tener instalado [Docker Desktop](https://www.docker.com/) en tu sistema.

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/LJalca/TallerVC.git
   cd TallerVC/seccion-1-vibe-coding
   ```

2. **Verificar el archivo de variables de entorno:**
   Revisa o ajusta las credenciales en `seccion-1-vibe-coding/.env` según sea necesario.

3. **Construir y levantar todos los servicios:**
   ```bash
   docker compose up -d --build
   ```

4. **Acceder a los servicios en el navegador:**
   - 🌐 **Aplicación Frontend & Cotizador:** [http://localhost](http://localhost) (o puerto configurado en `NGINX_PORT`, ej. `:80` o `:8080`)
   - 🗄️ **Adminer (Gestor MySQL):** [http://localhost:8081](http://localhost:8081)
     - *Servidor:* `mysql`
     - *Usuario:* `daeuser` (o el configurado en `.env`)
     - *Contraseña:* `daepassword`
     - *Base de datos:* `tallerdae`

5. **Detener el entorno:**
   ```bash
   docker compose down
   ```

---

### 2. Ejecución Standalone / Desarrollo Local

#### Backend (Spring Boot)
Requiere **Java 17+** y **Maven**:
```bash
cd seccion-2-spec-driven-back/cotizador-backend
mvn clean spring-boot:run
```
El servidor backend iniciará en `http://localhost:8080`.

#### Frontend (Vanilla JS)
Para probar el frontend de forma independiente sin Nginx:
1. Abre `seccion-2-spec-driven-front/cotizador-frontend/js/api.js`.
2. Modifica temporalmente la variable base si no utilizas un proxy inverso:
   ```javascript
   const API_BASE_URL = 'http://localhost:8080';
   ```
3. Inicia un servidor local estático (los ES Modules requieren protocolo HTTP):
   ```bash
   cd seccion-2-spec-driven-front/cotizador-frontend
   npx serve .
   # o con Python:
   python -m http.server 3000
   ```

---

## 📡 Contrato de API REST

El backend expone los siguientes endpoints (especificados en [openapi.yaml](file:///c:/Users/LAJS/Documents/clj/MaeUPS/AplEmpre/TallerVC/seccion-2-spec-driven-back/cotizador-backend/openapi.yaml)):

| Método | Endpoint | Descripción | Respuesta Exitosa |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/tipos-calzado` | Obtiene el catálogo de tipos de calzado y sus factores de dificultad | `200 OK` (JSON Array) |
| `GET` | `/api/tipos-reparacion` | Obtiene los tipos de reparación disponibles, precios base y tiempos estimados | `200 OK` (JSON Array) |
| `POST` | `/api/cotizaciones` | Calcula y genera una cotización detallada (subtotal, recargo por urgencia, total, días estimados) | `200 OK` / `201 Created` |

### Ejemplo de Payload de Cotización (`POST /api/cotizaciones`):

```json
{
  "tipoCalzadoId": "550e8400-e29b-41d4-a716-446655440001",
  "tipoReparacionIds": [
    "6ba7b810-9dad-11d1-80b4-00c04fd43001",
    "6ba7b810-9dad-11d1-80b4-00c04fd43002"
  ],
  "urgente": true
}
```

---

## 🧪 Pruebas Automatizadas

El backend incluye pruebas unitarias y de propiedades sobre el modelo de dominio (`Cotizacion`, `Dinero`, cálculo de recargos y validación de reglas de negocio):

```bash
cd seccion-2-spec-driven-back/cotizador-backend
mvn test
```

---

## 📑 Bitácoras y Evidencias

- **Bitácora Sección 1 (Vibe Coding):** [seccion-1-vibe-coding/Bitacora.md](file:///c:/Users/LAJS/Documents/clj/MaeUPS/AplEmpre/TallerVC/seccion-1-vibe-coding/Bitacora.md)
- **Reflexión Crítica y Análisis de Seguridad:** [seccion-1-vibe-coding/reflexion.md](file:///c:/Users/LAJS/Documents/clj/MaeUPS/AplEmpre/TallerVC/seccion-1-vibe-coding/reflexion.md)
- **Bitácora Sección 2 (SDD Backend):** [Capturas-entregables-seccion-2-back/BITACORA-BACK.md](file:///c:/Users/LAJS/Documents/clj/MaeUPS/AplEmpre/TallerVC/Capturas-entregables-seccion-2-back/BITACORA-BACK.md)
- **Capturas de Despliegue y Pruebas E2E:** Carpeta `Capturas-seccion-4-despliegue/`
