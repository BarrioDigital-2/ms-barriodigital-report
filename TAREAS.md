# ms-barriodigital-report: Tareas y guía de trabajo

Microservicio de **reportería (KPIs)** de BarrioDigital (DSY1107). Consume los eventos de trámites que publica `ms-barriodigital-requests` en Kafka, los guarda en MySQL y expone indicadores por REST.

| Dato | Valor |
|------|-------|
| Puerto | `8084` |
| Paquete base | `cl.duoc.ms_barriodigital_report` |
| Stack | Spring Boot 4.1.2 · Java 17 · Maven |
| Topic que consume | `requests.events` (lo crea ms-requests al arrancar; key = id de la solicitud) |
| Topic de errores | `report.DLT` (lo crea report; ver tarea 2.6b) |
| Consumer group | `report-processor` |
| Base de datos | MySQL, esquema propio `barriodigital_report` |

---

## 1. Reglas de trabajo

### 1.1 Ramas (Git Flow simplificado)

```
main      ← solo código entregable. Nadie hace commit directo.
  └─ develop   ← aquí llegan TODOS los Pull Requests.
       └─ feature/report-xxx   ← una rama por bloque de trabajo, sale de develop.
```

- Cada bloque de tareas (ver §4) se hace en **su propia rama `feature/`** creada desde `develop`.
- Al terminar: **Pull Request hacia `develop`**, nunca hacia `main`.
- `develop` → `main` solo cuando el equipo decide entregar (PR revisado).
- Antes de abrir el PR, actualizar la rama con lo último de `develop`.

```powershell
# Empezar un bloque nuevo
git checkout develop
git pull origin develop
git checkout -b feature/report-base

# ... trabajar y commitear ...

# Antes de abrir el PR
git pull origin develop
git push -u origin feature/report-base
# Abrir el PR en GitHub: base = develop, compare = feature/report-base
```

### 1.2 Commits

Formato *Conventional Commits*, en español, **un commit por tarea**:

```
feat(report): consumer Kafka de eventos de solicitud
chore(report): configuración de puerto, MySQL y Kafka
fix(report): parseo de fecha del evento
docs(report): README de ejecución
```

Tipos: `feat` (funcionalidad), `fix` (corrección), `chore` (configuración), `refactor`, `docs`, `test`.

### 1.3 Si trabajas con un asistente de IA (Claude, Copilot, etc.)

Cópiale estas reglas al inicio de la sesión:

- **No hacer `git push`, `git pull`, `git commit` ni `git stash`.** Solo entregar los comandos para que los ejecute la persona.
- **Preguntar si hay dudas** antes de asumir algo: nombres, contratos, puertos o estructura.
- Explicar qué hace cada comando antes de ejecutarlo.
- Aplicar los principios SOLID (§1.4) y las convenciones de código (§1.5).
- No tocar repos de otros integrantes sin acordarlo (por ejemplo `ms-barriodigital-requests`).
- **Código simple** (ver §1.6): mínimas anotaciones, comentarios de una línea y nada que no se pueda explicar en una frase.

### 1.4 Principios SOLID aplicados a este microservicio

| Principio | Cómo se aplica aquí |
|-----------|---------------------|
| **S**: Responsabilidad única | El *consumer* solo recibe el mensaje y delega. El *service* calcula. El *controller* solo traduce HTTP. El *repository* solo consulta. |
| **O**: Abierto/cerrado | Los rangos de tiempo son un `enum RangoTiempo`. Agregar `last90d` = agregar una constante, sin tocar `if`s en el servicio. |
| **L**: Sustitución de Liskov | Cualquier implementación de `ReportService` debe poder reemplazar a otra sin romper el controller. |
| **I**: Segregación de interfaces | Interfaces pequeñas: `EventoRegistroService` (guardar eventos) separada de `ReportService` (consultar KPIs). |
| **D**: Inversión de dependencias | Controller y consumer dependen de **interfaces**, no de clases concretas. Inyección **por constructor** (`@RequiredArgsConstructor`), nunca `@Autowired` en campos. |

### 1.5 Convenciones de código

- Comentarios y logs **en español**.
- Valores fijos (topics, nombres) como **constantes** o en `application.properties`, nunca *hardcodeados* repetidos.
- DTOs para entrar y salir. **Nunca** exponer la entidad JPA en el controller.
- Lombok: `@Data @NoArgsConstructor @AllArgsConstructor` en DTOs (Jackson necesita el constructor vacío). `@Slf4j` para logs.
- Estructura de paquetes:

```
cl.duoc.ms_barriodigital_report
├── config/        (beans y constantes)
├── consumer/      (@KafkaListener)
├── controller/    (REST)
├── dto/           (entrada Kafka y respuestas REST)
├── exception/     (@RestControllerAdvice)
├── model/         (entidades JPA y enums)
├── repository/    (Spring Data JPA)
└── service/       (interfaces + impl/)
```

### 1.6 Código simple (criterio de la entrega)

Cada línea la tenemos que poder explicar en la evaluación. Regla: **si no lo puedes explicar en una frase, no va.** Spring ya hace mucho por defecto; no repetirlo.

| ❌ No poner (Spring ya lo hace o no aporta) | ✅ Sí poner (aporta algo explicable) |
|--------------------------------------------|--------------------------------------|
| `@Repository` en interfaces que extienden `JpaRepository` | `@Column(unique = true)` cuando un valor no se puede repetir |
| `@Column(name = "...")`: `fechaEvento` ya se convierte en `fecha_evento` | `@Modifying` + `@Query` cuando la consulta modifica datos |
| `@Column(length/nullable)`: la validación va en el DTO | `@Valid` + `@NotBlank`/`@NotNull` en los DTOs de entrada |
| `@Builder`, `@Builder.Default`, `@AllArgsConstructor` en entidades | `@Getter @Setter @NoArgsConstructor` en entidades |
| `@Data` en entidades (genera equals/hashCode problemáticos en JPA) | `@Data @NoArgsConstructor @AllArgsConstructor` en DTOs |
| Javadoc largo, text blocks `"""` | Un comentario de una línea donde el *por qué* no es obvio |

Ejemplo de entidad con este criterio:
```java
@Entity
@Table(name = "evento_solicitud")
@Getter
@Setter
@NoArgsConstructor
public class EventoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long solicitudId;
    private String tipoEvento;
    private String tipoTramite;
    private String estado;
    private LocalDateTime fechaEvento;
}
```

---

## 2. Levantar el entorno local (desde cero)

### 2.1 Requisitos

| Herramienta | Versión | Verificar con |
|-------------|---------|---------------|
| JDK | 17 | `java -version` |
| Docker Desktop | reciente, **encendido** | `docker ps` |
| Git | cualquiera | `git --version` |
| IDE | VS Code (Extension Pack for Java) o IntelliJ | — |

> Maven **no** hace falta instalarlo: cada repo trae el wrapper `mvnw`.

### 2.2 Clonar los repos

Crea una carpeta común (por ejemplo `barrioDigitalApp/`) y clona dentro:

```powershell
mkdir barrioDigitalApp; cd barrioDigitalApp

# Infraestructura (RabbitMQ + Kafka + MySQL en local)
git clone https://github.com/BarrioDigital-2/barriodigital-infra.git

# Este microservicio
git clone https://github.com/BarrioDigital-2/ms-barriodigital-report.git

# Opcional: productor real de eventos (solo para la prueba de punta a punta)
git clone https://github.com/BarrioDigital-2/ms-barriodigital-requests.git
```

Luego, dentro de `ms-barriodigital-report`, cámbiate a `develop`:

```powershell
cd ms-barriodigital-report
git checkout develop
```

### 2.3 Levantar la infraestructura con Docker

```powershell
cd barriodigital-infra/local
docker compose up -d
docker ps
```

Deben aparecer **5 contenedores**: `rabbit1`, `rabbit2`, `zookeeper`, `kafka`, `barriodigital-mysql`.

> Más detalles y problemas comunes en el README de `barriodigital-infra`.

| Servicio | Dirección desde tu PC | Credenciales |
|----------|----------------------|--------------|
| Kafka | `localhost:9092` | — |
| MySQL | `localhost:3307` | `root` / `root` |
| RabbitMQ (panel) | http://localhost:15672 | `barriodigital` / `barrio2026Rabbit` |

> MySQL va en el **3307** porque el 3306 suele estar ocupado por un MySQL instalado en Windows.

### 2.4 Levantar este microservicio

```powershell
cd ms-barriodigital-report
.\mvnw spring-boot:run "-Dmaven.test.skip=true"
```

> En PowerShell el `-D...` va **entre comillas**, si no Maven no lo reconoce.

Debe terminar con `Started MsBarriodigitalReportApplication` y, unos segundos después, `report-processor: partitions assigned: [requests.events-0, -1, -2]`.

La base `barriodigital_report` se crea sola (`createDatabaseIfNotExist=true`) y las tablas también (`ddl-auto=update`).

### 2.5 Puertos de todo el sistema

| Servicio | Puerto |
|----------|--------|
| ms-barriodigital-requests | 8081 |
| ms-barriodigital-notifications | 8082 |
| ms-barriodigital-rabbit-admin | 8083 |
| ms-barriodigital-audit | 8087 |
| **ms-barriodigital-report** | **8084** |
| ms-barriodigital-catalog | 8085 (por crear) |
| ms-barriodigital-kafka-admin | 8086 (por crear) |
| Front Angular | 4200 |

---

## 3. Cómo probar

### 3.1 Publicar eventos a mano (no necesita ms-requests)

Usa la consola que trae el contenedor de Kafka. Cada línea es un mensaje:

```powershell
# Trámite creado
'{"id":1,"tipo":"SOLICITUD_CREADA","comunaId":"Av. Siempre Viva 742","titulo":"PERMISO_OBRAS","descripcion":"Test","estado":"INGRESADO","timestamp":"2026-11-19T10:00:00"}' | docker exec -i kafka kafka-console-producer --bootstrap-server kafka:9093 --topic requests.events

# El mismo trámite, resuelto 2 horas después
'{"id":1,"tipo":"SOLICITUD_ACTUALIZADA","comunaId":"Av. Siempre Viva 742","titulo":"PERMISO_OBRAS","descripcion":"Test","estado":"RESUELTO","timestamp":"2026-11-19T12:00:00"}' | docker exec -i kafka kafka-console-producer --bootstrap-server kafka:9093 --topic requests.events
```

> Las fechas de prueba deben caer dentro del rango que consultas (`last24h` = últimas 24 h desde **ahora**). Ajusta el `timestamp` a la fecha del día.

### 3.2 Revisar lo guardado en MySQL

```powershell
docker exec -it barriodigital-mysql mysql -uroot -proot barriodigital_report -e "SELECT * FROM evento_solicitud;"
```

### 3.3 Consultar los endpoints

```powershell
curl.exe "http://localhost:8084/api/report/kpis?range=last24h"
curl.exe "http://localhost:8084/api/report/top-procedures?range=last7d"
curl.exe "http://localhost:8084/api/report/kpis?range=cualquiercosa"   # debe dar 400
```

> En PowerShell usa `curl.exe` (con `.exe`). `curl` a secas es un alias de `Invoke-WebRequest`.

### 3.4 Prueba de punta a punta (opcional, con ms-requests)

1. Levantar requests apuntando al MySQL local:
   ```powershell
   cd ms-barriodigital-requests
   $env:DB_PORT="3307"; .\mvnw spring-boot:run "-Dmaven.test.skip=true"
   ```
2. `POST http://localhost:8081/api/v1/requests` con body:
   ```json
   {"tipoTramite":"PERMISO_OBRAS","vecinoEmail":"test@vecino.cl","direccion":"Av. Siempre Viva 742","descripcion":"Test"}
   ```
   > Este endpoint pide token de Azure AD. Si no tienes token, usa la opción 3.1.
3. Ver en la consola de report el log del evento recibido y consultar los KPIs.

---

## 4. Contrato del evento que llega de Kafka

Lo publica `ms-barriodigital-requests` (clase `SolicitudKafkaEvent`). **No lo cambiamos nosotros.**

```json
{
  "id": 1,
  "tipo": "SOLICITUD_CREADA",
  "comunaId": "Av. Siempre Viva 742",
  "titulo": "PERMISO_OBRAS",
  "descripcion": "Texto libre",
  "estado": "INGRESADO",
  "timestamp": "2026-11-19T10:00:00.123456789"
}
```

| Campo | Significado real |
|-------|------------------|
| `id` | ID de la solicitud |
| `tipo` | `SOLICITUD_CREADA` o `SOLICITUD_ACTUALIZADA` |
| `titulo` | **El tipo de trámite** (`tipoTramite`). El nombre confunde, pero es eso. |
| `comunaId` | Por ahora trae la dirección |
| *key del mensaje* | El id de la solicitud: los eventos de un mismo trámite llegan en orden |
| `estado` | Estado nuevo: `INGRESADO`, `ADMITIDO`, `EN_GESTION`, `EN_TERRENO`, `RESUELTO`, `RECHAZADO` |
| `timestamp` | `LocalDateTime.toString()` → parsear con `LocalDateTime.parse(...)` |

---

## 5. Tareas

Divididas en **3 bloques = 3 ramas = 3 Pull Requests**. Cada tarea es un commit.

### Bloque A: `feature/report-base` (configuración y persistencia)

#### 2.1 Limpieza del proyecto generado
- Borrar `src/test/java/.../MsBarriodigitalReportApplicationTests.java` (no hay tests en esta entrega y ese test falla sin MySQL).
- En `pom.xml`: completar `<name>ms-barriodigital-report</name>` y `<description>`.
- **Commit:** `chore(report): limpieza del proyecto generado`

#### 2.2 Configuración
**Archivo:** `src/main/resources/application.properties`

```properties
spring.application.name=ms-barriodigital-report
server.port=8084

# ========== MYSQL ==========
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3307}/${DB_NAME:barriodigital_report}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

# ========== KAFKA ==========
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
report.kafka.topic=${REPORT_TOPIC:requests.events}
spring.kafka.consumer.group-id=report-processor
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
# Si un mensaje no se puede leer, se registra el error y se salta (no queda en loop)
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
spring.kafka.consumer.properties.spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JacksonJsonDeserializer
# El productor manda su propio nombre de clase: lo ignoramos y usamos nuestro DTO
spring.kafka.consumer.properties.spring.json.use.type.headers=false
spring.kafka.consumer.properties.spring.json.value.default.type=cl.duoc.ms_barriodigital_report.dto.SolicitudEvent
spring.kafka.consumer.properties.spring.json.trusted.packages=cl.duoc.*

logging.level.org.apache.kafka=WARN
```
- `auto-offset-reset=earliest`: al arrancar por primera vez lee también los eventos que ya estaban en el topic.
- **Verificación:** la app arranca y se crea la base `barriodigital_report`.
- **Commit:** `chore(report): configuración de puerto, MySQL y Kafka`

#### 2.3 DTO del evento
**Archivo:** `dto/SolicitudEvent.java`, con los mismos campos del contrato (§4).
- **Commit:** `feat(report): DTO del evento de solicitud`

#### 2.4 Entidad JPA
**Archivo:** `model/EventoSolicitud.java` (tabla `evento_solicitud`)

| Campo | Tipo | Origen |
|-------|------|--------|
| `id` | `Long` (PK autoincremental) | — |
| `solicitudId` | `Long` | `evento.id` |
| `tipoEvento` | `String` | `evento.tipo` |
| `tipoTramite` | `String` | `evento.titulo` |
| `estado` | `String` | `evento.estado` |
| `fechaEvento` | `LocalDateTime` | `LocalDateTime.parse(evento.timestamp)` |

- Seguir el ejemplo de §1.6: solo `@Entity`, `@Table`, `@Id`/`@GeneratedValue` y Lombok `@Getter @Setter @NoArgsConstructor`.
- **Commit:** `feat(report): entidad de eventos de solicitud`

### Bloque B: `feature/report-consumer` (recepción de eventos)

#### 2.5 Repository
**Archivo:** `repository/EventoSolicitudRepository.java` (extiende `JpaRepository`)

Consultas necesarias (JPQL o `nativeQuery`):
1. **Trámites por hora**: contar `SOLICITUD_CREADA` desde una fecha, agrupado por hora.
2. **Ranking de tipos**: contar `SOLICITUD_CREADA` por `tipoTramite` desde una fecha, orden descendente.
3. **Tiempo de resolución**: por cada `solicitudId`, diferencia entre la fecha de `SOLICITUD_CREADA` y la de `estado = RESUELTO`. Promedio en minutos u horas.
   - Pista MySQL: `AVG(TIMESTAMPDIFF(MINUTE, c.fecha_evento, r.fecha_evento))` con un *self-join* de la tabla (`c` = creada, `r` = resuelta).
- Para los resultados agregados, una interfaz de proyección simple en vez de `Object[]`. Ej.: `interface ConteoPorTipo { String getTipoTramite(); Long getCantidad(); }`, y en la query `SELECT e.tipoTramite AS tipoTramite, COUNT(e) AS cantidad ...`.
- **Commit:** `feat(report): consultas de KPIs en el repository`

#### 2.6 Consumer + servicio de registro
**Archivos:** `service/EventoRegistroService.java` (interfaz), `service/impl/EventoRegistroServiceImpl.java`, `consumer/ReportConsumer.java`

- `ReportConsumer`: `@KafkaListener(topics = "${report.kafka.topic}")`, loguea y **delega** a `EventoRegistroService.registrar(evento)`. Sin lógica propia.
- `EventoRegistroServiceImpl`: convierte el DTO en entidad (parsea la fecha) y guarda.
- Si el `timestamp` viene mal: loguear con `log.warn` y descartar el evento, sin romper el listener.
- **Verificación:** publicar un evento (§3.1) → aparece en la tabla (§3.2).
- **Commit:** `feat(report): consumer Kafka que registra eventos de solicitud`

#### 2.6b DLT de report (`report.DLT`)
Mismo patrón que ya funciona en **ms-barriodigital-audit**: copiar y cambiar `audit` por `report`.
- `config/KafkaTopicConfig.java`: `NewTopic` `report.DLT` (3 particiones, `delete`, 14 días = `1209600000`). Ver `KafkaTopicConfig` de audit.
- `config/KafkaErrorConfig.java`: `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` → `report.DLT`, `FixedBackOff(1000L, 3)`. Copiar `KafkaErrorConfig` de audit.
- `application.properties`: agregar el producer (`spring.kafka.producer.key-serializer=...StringSerializer` y `value-serializer=...JacksonJsonSerializer`).
- **Verificación:** `'esto no es json' | docker exec -i kafka kafka-console-producer --bootstrap-server kafka:9093 --topic requests.events` → aparece en `report.DLT` (`kafka-console-consumer --topic report.DLT --from-beginning`).
- **Commit:** `feat(report): DLT con 3 reintentos hacia report.DLT`

### Bloque C: `feature/report-api` (KPIs por REST)

#### 2.7 Enum de rangos
**Archivo:** `model/RangoTiempo.java`
- Constantes `LAST_24H("last24h", Duration.ofHours(24))`, `LAST_7D`, `LAST_30D`.
- Método `static RangoTiempo desde(String valor)` que lanza `IllegalArgumentException` si el valor no existe.
- Método `LocalDateTime fechaDesde()` → `LocalDateTime.now().minus(duracion)`.
- **Commit:** `feat(report): enum de rangos de tiempo`

#### 2.8 Servicio de KPIs
**Archivos:** `service/ReportService.java` (interfaz), `service/impl/ReportServiceImpl.java`
- `KpiResponse obtenerKpis(RangoTiempo rango)`
- `List<TopProcedureResponse> obtenerTopTramites(RangoTiempo rango)`
- **Commit:** `feat(report): servicio de cálculo de KPIs`

#### 2.9 DTOs de respuesta
**Archivos:** `dto/KpiResponse.java`, `dto/TopProcedureResponse.java`

Ejemplo de respuesta de `/kpis`:
```json
{
  "rango": "last24h",
  "totalTramites": 12,
  "tramitesPorHora": [ { "hora": "2026-11-19T10:00", "cantidad": 3 } ],
  "tiempoPromedioResolucionMinutos": 120.5
}
```
Ejemplo de `/top-procedures`:
```json
[ { "tipoTramite": "PERMISO_OBRAS", "cantidad": 7 } ]
```
- **Commit:** `feat(report): DTOs de respuesta de KPIs`

#### 2.10 Controller
**Archivo:** `controller/ReportController.java` (`@RequestMapping("/api/report")`)
- `GET /kpis?range=last24h` (por defecto `last24h`)
- `GET /top-procedures?range=last7d` (por defecto `last7d`)
- Convierte el `range` con `RangoTiempo.desde(...)` y delega al servicio.
- **Commit:** `feat(report): endpoints REST de KPIs`

#### 2.11 Manejo de errores
**Archivo:** `exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`)
- `IllegalArgumentException` → **400** con `{ "error": "Rango no válido: ...", "valoresPermitidos": [...] }`.
- **Commit:** `feat(report): manejo de errores de la API`

#### 2.11b Dockerfile (para el despliegue en AWS)
Copiar `Dockerfile` y `.dockerignore` de **ms-barriodigital-catalog** y cambiar `EXPOSE 8085` por `EXPOSE 8084`.
- **Verificación:** `docker build -t ms-barriodigital-report .` termina sin errores.
- **Commit:** `build(report): Dockerfile multi-etapa para despliegue`

#### 2.12 Prueba completa y documentación
- Ejecutar todo §3 y pegar en el PR la salida de los `curl` (evidencia).
- Agregar al `README.md` cómo levantar y probar (puede resumir §2 y §3 de este archivo).
- **Commit:** `docs(report): README de ejecución y pruebas`

---

## 6. Checklist antes de abrir cada PR

- [ ] La rama sale de `develop` y el PR apunta a `develop`
- [ ] Compila: `.\mvnw clean compile "-Dmaven.test.skip=true"`
- [ ] Arranca y se probó lo del bloque según §3
- [ ] Un commit por tarea, con formato `tipo(report): descripción`
- [ ] Controller y consumer dependen de interfaces; inyección por constructor
- [ ] Sin entidades JPA expuestas en la API
- [ ] Comentarios y logs en español
- [ ] Código simple según §1.6: sin anotaciones que Spring ya resuelve solo
- [ ] Sin credenciales nuevas *hardcodeadas* (usar `${VARIABLE:default}`)

---

## 7. Problemas conocidos

| Problema | Solución |
|----------|----------|
| `InconsistentClusterIdException` al levantar Kafka | `docker compose down`, `docker volume rm barriodigital-rabbitmq_kafka_data` y volver a levantar |
| `ClassNotFoundException SolicitudKafkaEvent` | Falta `spring.json.use.type.headers=false` y `value.default.type` apuntando a **nuestro** DTO |
| `No qualifying bean KafkaTemplate` / no consume | Usar `spring-boot-starter-kafka`, no `spring-kafka` directo |
| `Communications link failure` (MySQL) | MySQL no está arriba o el puerto no es 3307: revisar `docker ps` |
| PowerShell: `Unknown lifecycle phase ".test.skip=true"` | Poner `"-Dmaven.test.skip=true"` entre comillas |
| El consumer no recibe nada | `requests.events` lo crea ms-requests al arrancar: levantar requests una vez, o crearlo con kafka-admin (`POST /api/v1/kafka-admin/topics`) |
| Scripts `.sh` fallan en el contenedor (`^M`) | Guardarlos con fin de línea LF |
