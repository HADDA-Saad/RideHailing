# RideHailing

A backend for a ride-hailing platform, in the spirit of Uber or Careem, built to explore one core question:

> **How do you keep track of thousands of moving drivers in real time without slowing the system down?**

This project is my answer to that question. It focuses on the *design ideas* behind a live ride-hailing system as much as on the code itself.

---

## The Idea

In a ride-hailing app, every driver's phone constantly reports its position. A naive system would save each update straight to a database before replying, which works for ten drivers and breaks for ten thousand.

RideHailing takes a different approach built on three concepts:

### 1. Don't make the driver wait
When a driver sends a location, the system simply acknowledges it and places it in a queue. The driver's app is never held up by storage or processing. Like a waiter who hands your order to the kitchen and moves on to the next table.

### 2. Work behind the scenes
A separate process picks locations off the queue at its own pace, saves them permanently, and updates the "latest known position" of each driver. If something fails, the problematic message is set aside instead of blocking everything else.

### 3. Keep everyone up to date instantly
Instead of riders repeatedly asking "where is my driver?", the system **pushes** each new position to connected apps the moment it is processed, so a driver moves smoothly on the map.

This style of design is called **event-driven architecture**: things happen (events), and different parts of the system react independently.

---

## How It Works

```
 Driver app
     │  "I'm here"
     ▼
   API  ──────────────▶  Queue  ──────────────▶  Background worker
 (instant reply)        (Kafka)                        │
                                      ┌────────────────┼────────────────┐
                                      ▼                ▼                ▼
                                 Permanent         Fast memory       Live push
                                  history          (latest spot)    to rider apps
                              (PostgreSQL)           (Redis)        (WebSocket)
```

When someone asks for a driver's current position, the system checks fast memory first and only goes to the permanent database if needed. This keeps frequent reads quick.

---

## What the Platform Does

- **Drivers and riders** can register and be looked up
- **Rides** can be requested and completed, and searched by driver, rider or status
- **Prices** are estimated from a base fare, the distance, and a surge multiplier for busy periods
- **Driver locations** are collected continuously, stored as a history, and broadcast live
- **Failed events** are redirected to a separate "problem" queue so nothing is silently lost
- **System health** can be monitored through a built-in health endpoint
- **Load testing** simulates thousands of drivers sending updates to see how the system behaves under pressure

---

## Tools & Technologies

Each tool was chosen for a specific job:

| Tool | Role in the project | Why it's here |
|---|---|---|
| **Java 21** | Main language | Strong, mature ecosystem for backend systems |
| **Spring Boot** | Application framework | Speeds up building structured, production-style web services |
| **PostgreSQL** | Permanent database | Reliable storage for users, rides, prices and location history |
| **Apache Kafka** | Message queue | Absorbs bursts of driver updates and decouples receiving from processing |
| **Redis** | Fast in-memory cache | Serves each driver's latest position almost instantly |
| **WebSocket (STOMP)** | Live connection | Pushes updates to apps instead of making them ask repeatedly |
| **Docker & Docker Compose** | Packaging and setup | Runs the whole system with a single command |
| **k6** | Load testing | Measures how the system holds up with many simultaneous drivers |
| **JUnit** | Automated tests | Checks that the core business logic behaves correctly |

---

## Project Organization

The code is divided into independent areas, each responsible for one part of the business:

- **Driver**: who the drivers are
- **Rider**: who the riders are
- **Ride**: trips and their status (waiting, active, completed, cancelled)
- **Price Estimate**: how fares are calculated
- **Location**: the real-time pipeline (queue, cache, live updates)

Each area keeps its own logic separate, which makes the system easier to understand, test and extend.

---

## Running It

You need [Docker](https://www.docker.com/) installed.

1. Create a file named `.env` in the project folder containing a database password:
   ```
   DB_PASSWORD=choose_a_password
   ```
2. Start everything:
   ```bash
   docker compose up --build
   ```
3. Check that it's running: <http://localhost:8080/actuator/health>

To try the load test, install [k6](https://k6.io/) and run `k6 run load-test.js`.

---

## Perspectives & Next Steps

This is a working foundation, and there is a clear path to make it closer to a real product:

- **Smart driver matching:** automatically find the nearest available driver for a rider using location data
- **Live ride tracking:** let a rider follow their driver's approach and trip in real time
- **Real distance and pricing:** compute distance from actual routes and adjust surge pricing based on demand
- **Accounts and security:** add login, roles for drivers and riders, and secure access to the API
- **Input checks and clear errors:** validate data and return helpful messages when something goes wrong
- **Mobile or web front end:** build rider and driver apps on top of this backend
- **Scaling up:** run several copies of the service and split the queue across them to handle larger cities
- **Monitoring:** dashboards and alerts to watch the system's health and performance
- **Automated delivery:** run tests and build checks automatically on every change

---

## What This Project Demonstrates

- Designing a system around **events** rather than direct, blocking calls
- Combining a **database, a queue, a cache, and live connections**, each used for what it does best
- Thinking about **performance and failure**, not only about features
- Packaging a multi-service system so anyone can run it easily

---

## Author

**Saad Hadda**: [GitHub](https://github.com/HADDA-Saad) · [LinkedIn](https://www.linkedin.com/in/saad-hadda-a6b703352)
