# ms-barriodigital-report

Microservicio de reportería de BarrioDigital. Consume eventos de `requests.events`, los guarda en MySQL y expone KPIs por REST.

## Requisitos

- JDK 25 (LTS)
- Docker Desktop con la infraestructura de BarrioDigital

## Levantar

Desde la raíz de este repositorio:

```powershell
docker compose up -d --build
docker compose ps
```

El servicio queda en `http://localhost:8084`. Para detener los contenedores:

```powershell
docker compose down
```

## Publicar eventos de prueba

```powershell
'{"id":1,"tipo":"SOLICITUD_CREADA","comunaId":"Av. Siempre Viva 742","titulo":"PERMISO_OBRAS","descripcion":"Test","estado":"INGRESADO","timestamp":"2026-11-22T18:00:00"}' | docker exec -i kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:29092 --topic requests.events

'{"id":1,"tipo":"SOLICITUD_ACTUALIZADA","comunaId":"Av. Siempre Viva 742","titulo":"PERMISO_OBRAS","descripcion":"Test","estado":"RESUELTO","timestamp":"2026-11-22T20:00:00"}' | docker exec -i kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:29092 --topic requests.events
```

Revisa los datos:

```powershell
docker exec -it barriodigital-mysql mysql -uroot -proot barriodigital_report -e "SELECT * FROM evento_solicitud;"
```

Consulta los KPIs:

```powershell
curl.exe "http://localhost:8084/api/v1/report/kpis?range=last24h"
curl.exe "http://localhost:8084/api/v1/report/top-procedures?range=last7d"
curl.exe "http://localhost:8084/api/v1/report/kpis?range=cualquiercosa"
```

Compila y construye la imagen:

```powershell
.\mvnw clean compile "-Dmaven.test.skip=true"
docker build -t ms-barriodigital-report .
```
