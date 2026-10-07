# RideHailing

A ride-hailing backend built with **Spring Boot**, designed around a high-throughput driver location pipeline. Drivers stream their GPS positions to the API, which hands them off to **Kafka**; a consumer persists them to **PostgreSQL**, caches the latest position in **Redis**, and broadcasts live updates over **WebSocket (STOMP)**.

## Features

- **Driver & rider registration** and lookup
- **Ride lifecycle**: request a ride, complete it, query by driver, rider, id or status
- **Price estimation**: base fare + per-km rate with a surge multiplier, stored per ride
- **Real-time driver locations**
  - Asynchronous ingestion through Kafka (`202 Accepted` responses)
  - Latest position served from Redis, with PostgreSQL as fallback
  - Full location history per driver
  - Live broadcast to subscribers on `/topic/driver-locations`
  - Dead-letter topic (`driver-locations-dlq`) for events that fail processing
- **Health checks** via Spring Boot Actuator
- **k6 load test** simulating thousands of drivers pinging once per second

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 21, Spring Boot 4.1.0 (Web MVC, Data JPA, Actuator) |
| Database | PostgreSQL 16 |
| Messaging | Apache Kafka 3.7 (KRaft mode, no ZooKeeper) |
| Cache | Redis 7 |
| Real-time | Spring WebSocket + STOMP with SockJS |
| Build | Maven (wrapper included), Lombok |
| Containers | Docker (multi-stage build), Docker Compose |
| Testing | JUnit / Spring Boot Test, k6 for load testing |

## Architecture

```
                       POST /locations
  Driver app ───────────────────────────────▶ DriverLocationController
                                                      │  (202 Accepted)
                                                      ▼
                                              LocationProducer
                                                      │
                                                      ▼
                                       Kafka topic: driver-locations
                                                      │
                                                      ▼
                                              LocationConsumer
                              ┌───────────────────────┼────────────────────────┐
                              ▼                       ▼                        ▼
                         PostgreSQL                 Redis              WebSocket /topic/
                    (location history)     (driver:latest:{id})        driver-locations
                                                                               
                     on failure ──▶ Kafka topic: driver-locations-dlq
```

Reads of a driver's latest location check Redis first (`driver:latest:{driverId}`, 24h TTL) and fall back to PostgreSQL on a cache miss.

## Project Structure

```
src/main/java/com/ridehailing/
├── driver/          # Driver registration & lookup
├── rider/           # Rider registration & lookup
├── ride/            # Ride requests, status, completion
├── PriceEstimate/   # Fare calculation and stored estimates
└── location/        # Kafka producer/consumer, Redis cache, WebSocket config
    ├── config/
    ├── controller/
    ├── service/
    ├── repository/
    ├── model/
    └── dto/
```

Each module follows the same layout: `controller` → `service` (interface + impl) → `repository` → `model`, with `dto` records for requests and responses.

## Getting Started

### Prerequisites

- Docker and Docker Compose
- (For local development without Docker) JDK 21 and Maven, or just use the included `./mvnw`

### Run everything with Docker Compose

1. Create a `.env` file in the project root with the database password:

   ```env
   DB_PASSWORD=choose_a_password
   ```

2. Build and start the stack:

   ```bash
   docker compose up --build
   ```

This starts PostgreSQL, Kafka, Redis and the application. The app waits for all three dependencies to be healthy before starting.

| Service | Host port |
|---|---|
| API | `8080` |
| PostgreSQL | `5433` (mapped to 5432 in the container) |
| Kafka | `9092` |
| Redis | `6379` |

Check that it's up:

```bash
curl http://localhost:8080/actuator/health
```

### Run locally (app outside Docker)

Start only the infrastructure, then run the app with Maven:

```bash
docker compose up -d postgres kafka redis
./mvnw spring-boot:run
```

The repository has no `application.properties`, so the application is configured entirely through Spring's environment variables. When running on the host, provide the values yourself (note Postgres is exposed on port **5433** and Kafka advertises itself as `kafka:9092`, so you may need a hosts entry or an adjusted listener config):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/ridehailing
export SPRING_DATASOURCE_USERNAME=ridehailing
export SPRING_DATASOURCE_PASSWORD=choose_a_password
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
export SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

### Run the tests

```bash
./mvnw test
```

Unit tests cover the driver, rider, ride and price estimate services.

## API Reference

All endpoints accept and return JSON.

### Drivers — `/drivers`

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/drivers` | `{ "name", "email" }` | Register a driver |
| `GET` | `/drivers/{id}` | — | Get a driver by id |

### Riders — `/riders`

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/riders` | `{ "name", "email" }` | Register a rider |
| `GET` | `/riders/{id}` | — | Get a rider by id |

### Rides — `/rides`

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/rides` | `{ "driverId", "riderId" }` | Request a ride (created as `ACTIVE`) |
| `PATCH` | `/rides/{id}/complete` | — | Mark a ride `COMPLETED` |
| `GET` | `/rides/{id}` | — | Get a ride by id |
| `GET` | `/rides/driver/{driverId}` | — | Rides for a driver |
| `GET` | `/rides/rider/{riderId}` | — | Rides for a rider |
| `GET` | `/rides/status/{status}` | — | Rides by status: `WAITING`, `ACTIVE`, `COMPLETED`, `CANCELLED` |

### Price estimates — `/prices`

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/prices` | `{ "rideId", "distanceKm" }` | Calculate and store an estimate |
| `GET` | `/prices/{rideId}` | — | Get the stored estimate for a ride |

Pricing formula: `(baseFare + distanceKm × perKmRate) × surgeMultiplier`, currently `5.0`, `2.5` and `1.0`.

### Driver locations — `/locations`

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/locations` | `{ "driverId", "latitude", "longitude" }` | Publish a location to Kafka (returns `202 Accepted`) |
| `GET` | `/locations/driver/{driverId}` | — | Latest known location (Redis → PostgreSQL) |
| `GET` | `/locations/history/{driverId}` | — | All stored locations for a driver |

### WebSocket

- SockJS/STOMP endpoint: `/ws`
- Subscribe to `/topic/driver-locations` to receive every location event as it is processed
- Application destination prefix: `/app`

### Example

```bash
# Register a driver and a rider
curl -X POST localhost:8080/drivers -H 'Content-Type: application/json' \
  -d '{"name":"Amine","email":"amine@example.com"}'
curl -X POST localhost:8080/riders -H 'Content-Type: application/json' \
  -d '{"name":"Sara","email":"sara@example.com"}'

# Request a ride and estimate its price
curl -X POST localhost:8080/rides -H 'Content-Type: application/json' \
  -d '{"driverId":1,"riderId":1}'
curl -X POST localhost:8080/prices -H 'Content-Type: application/json' \
  -d '{"rideId":1,"distanceKm":8.4}'

# Push a driver location and read it back
curl -X POST localhost:8080/locations -H 'Content-Type: application/json' \
  -d '{"driverId":1,"latitude":34,"longitude":-7}'
curl localhost:8080/locations/driver/1
```

## Load Testing

`load-test.js` is a [k6](https://k6.io/) script that simulates drivers (ids 1–500) around Rabat, each posting one location per second to `POST /locations`. It ramps up, holds a sustained load, then ramps down.

Install k6 following the [official instructions](https://grafana.com/docs/k6/latest/set-up/install-k6/) (the `k6` package in `package.json` is not the k6 binary), then run:

```bash
k6 run load-test.js
```

Adjust the `stages` at the top of the script to match your machine; the default profile peaks at 10,000 virtual users.

## Known Issues / Roadmap

Things worth knowing about the current state of the code:

- **Location coordinates are `Long`.** `DriverLocationRequest` declares `latitude`/`longitude` as `Long`, so fractional values (like those sent by `load-test.js`) will fail to deserialize. Changing them to `double` matches the entity and response DTO.
- **Load test status check.** `load-test.js` asserts `status is 200`, but `POST /locations` returns `202`.
- **Ambiguous route.** `GET /riders/{id}` and `GET /riders/{email}` map to the same pattern; Spring will refuse to start with this ambiguity. Give the email lookup its own path (e.g. `/riders/email/{email}`).
- **WebSocket origins.** `setAllowedOriginPatterns("")` allows no origins; use `"*"` or a specific list for browser clients.
- **No validation or error handling.** Missing entities throw a plain `RuntimeException` (HTTP 500); there is no bean validation or `@ControllerAdvice`.
- **Rides don't verify drivers/riders** exist, don't calculate a price, and never use the `WAITING` or `CANCELLED` statuses yet.
- **Driver matching** (finding the nearest available driver) is not implemented.
- **No `application.properties`** is committed; configuration is supplied through environment variables.
- Logging uses `System.out`; swap for SLF4J.

## License

No license has been specified yet. Add a `LICENSE` file to clarify how others may use this code.
