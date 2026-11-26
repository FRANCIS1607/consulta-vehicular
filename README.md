# Consulta Vehicular (SRI + ANT)

Sistema basado en microservicios para validar RUC (persona natural), placa vehicular y licencia/puntos ANT.

## Estructura
- `sri-service`: Microservicio Spring Boot 4.2.0 (Java 17) en puerto `8081`.
- `ant-service`: Microservicio Spring Boot 4.2.0 (Java 17) en puerto `8082` con caché Redis (`REDIS_URL`).
- `frontend`: Aplicación Angular 18 en puerto `4200`.

## Ejecución rápida
1. Iniciar `sri-service` (`mvn spring-boot:run` o desde IntelliJ) -> `http://localhost:8081/swagger-ui.html`
2. Configurar variable `REDIS_URL` y iniciar `ant-service` -> `http://localhost:8082/swagger-ui.html`
3. En `frontend/`: `npm install && npm start` -> `http://localhost:4200`
