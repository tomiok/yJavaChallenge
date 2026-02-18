# API REST - ABM de Clientes

API REST CRUD para la gestion de clientes, desarrollada con **Java 21**, **Spring Boot 3.2** y **PostgreSQL 16**. Todo el proyecto se ejecuta mediante **Docker**, sin necesidad de tener Java instalado localmente.

## Requisitos previos

- [Docker](https://docs.docker.com/get-docker/) y [Docker Compose](https://docs.docker.com/compose/install/)

## Como ejecutar

```bash
docker compose up --build
```

Esto levanta dos contenedores:

| Servicio | Puerto externo | Descripcion |
|----------|---------------|-------------|
| **app** (Spring Boot) | `8080` | API REST |
| **db** (PostgreSQL 16) | `5433` | Base de datos (puerto interno 5432) |

> El puerto externo de PostgreSQL es `5433` para evitar conflictos con una instancia local. La aplicacion se conecta internamente al puerto `5432`.

La base de datos se inicializa automaticamente con el archivo `schema.sql`, que crea la tabla, el stored procedure y los datos de prueba.

#### Ver apartado de SonarQube para correr con el perfil adecuado para el análisis.

### Detener los contenedores

```bash
docker compose down
```

Para eliminar tambien el volumen de datos persistente:

```bash
docker compose down -v
```

## Documentacion Swagger

Con la aplicacion corriendo, acceder a:

```
http://localhost:8080/swagger-ui.html
```

## Endpoints

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| `GET` | `/api/clients` | Listar todos los clientes |
| `GET` | `/api/clients/{id}` | Obtener un cliente por ID |
| `GET` | `/api/clients/search?name=xxx` | Buscar clientes por nombre (parcial, case-insensitive) |
| `POST` | `/api/clients` | Crear un nuevo cliente |
| `PUT` | `/api/clients/{id}` | Actualizar un cliente existente |
| `DELETE` | `/api/clients/{id}` | Eliminar un cliente |

### Ejemplo: crear un cliente

```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ana",
    "lastName": "Garcia",
    "companyName": "AG Soluciones",
    "taxId": "27-11111111-1",
    "birthDate": "1995-03-20",
    "phoneNumber": "1155551234",
    "email": "ana.garcia@example.com"
  }'
```

### Ejemplo: buscar por nombre (usa stored procedure)

```bash
curl "http://localhost:8080/api/clients/search?name=juan"
```

## Validaciones

El DTO de entrada (`ClientRequestDTO`) aplica las siguientes validaciones:

| Campo | Regla |
|-------|-------|
| `firstName` | Obligatorio, maximo 100 caracteres |
| `lastName` | Obligatorio, maximo 100 caracteres |
| `companyName` | Obligatorio, maximo 150 caracteres |
| `taxId` | Obligatorio, formato `XX-XXXXXXXX-X` |
| `birthDate` | Obligatorio, debe ser una fecha pasada |
| `phoneNumber` | Obligatorio, maximo 30 caracteres |
| `email` | Obligatorio, formato de email valido, maximo 150 caracteres |

Ademas, se valida que no existan duplicados de `taxId` ni `email` (devuelve HTTP 409).

## Manejo de errores

| Codigo HTTP | Caso |
|-------------|------|
| `400` | Error de validacion (campos invalidos) |
| `404` | Cliente no encontrado |
| `409` | CUIT o email duplicado |
| `500` | Error interno del servidor |

Todas las respuestas de error siguen un formato consistente con `timestamp`, `status`, `error` y `message`.

## Stored Procedure

Se incluye la funcion `search_clients_by_name(p_name VARCHAR)` que realiza una busqueda parcial case-insensitive sobre los campos `nombre` y `apellido` usando `ILIKE`.

## Tests

Para ejecutar los tests unitarios:

```bash
docker run --rm -v $(pwd):/app -w /app maven:3.9-eclipse-temurin-21 mvn test
```

- **29 tests** (15 de servicio + 14 de controller)
- Cobertura: **85% instrucciones**, **91% lineas**, **100% branches**
- Reporte JaCoCo generado en `target/site/jacoco/index.html`

## SonarQube (analisis de calidad de codigo)

El proyecto incluye un contenedor de SonarQube para analisis estatico local. Los servicios de SonarQube estan bajo el perfil `sonar`, por lo que **no se inician** con `docker compose up --build` (solo se levantan `app` y `db`).

### 1. Levantar SonarQube

```bash
docker compose --profile sonar up -d
```

Esperar a que este disponible en `http://localhost:9000` (puede tardar ~1 minuto). Credenciales por defecto: `admin` / `admin`.

### 2. Generar un token

1. Ingresar a `http://localhost:9000` con `admin` / `admin` (en el primer login se pedira cambiar la contraseña)
2. Crear un proyecto local (agregar nombre, debe ser igual al de sonar.ProjectKey en pom.xml, en este caso, clients)
3. Ir a Analysis Method -> locally
4. Generar token
5. Copiar el token generado (no se vuelve a mostrar)
6. Captura [local](/images/img.png) de ejemplo

### 3. Ejecutar el analisis (con token de ejemplo)

```bash
docker run --rm --network javachallenge_default \
  -v "$(pwd)":/app -w /app \
  maven:3.9-eclipse-temurin-21 \
  mvn verify sonar:sonar \
    -Dsonar.host.url=http://sonarqube:9000 \
    -Dsonar.token=sqp_cb3e923f488eb8a9ca83aa33dbc3775f6bc661be
```

### 4. Ver resultados

Abrir `http://localhost:9000` y seleccionar el proyecto **clients**.

## Registro de errores (Logback)

El proyecto utiliza **SLF4J + Logback** como herramienta de registro de errores. La configuracion se encuentra en `src/main/resources/logback-spring.xml` y define tres appenders: consola, archivo general (`logs/application.log`) y archivo exclusivo de errores (`logs/error.log`). Los archivos rotan automaticamente por tamanio (10 MB) y por dia, con un historial de 30 dias para el log general y 60 dias para el de errores. Todas las excepciones capturadas por el `GlobalExceptionHandler` se registran con su stack trace completo en el log de errores, lo que facilita el diagnostico de problemas en cualquier entorno.

## Stack

- Java 21
- Spring Boot 3.2.5
- Spring Data JPA
- PostgreSQL 16
- Docker / Docker Compose
- Swagger (springdoc-openapi)
- JaCoCo (cobertura de tests)
- Lombok
- JUnit 5 / Mockito
