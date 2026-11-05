# Mini-Redis

**A Redis-inspired in-memory key-value store built from scratch in Java.**

Mini-Redis is a lightweight TCP server that implements a subset of the Redis Serialization Protocol (RESP), enabling communication with Redis-compatible clients.

The project explores the internals of an in-memory database through custom data structures, protocol parsing, concurrent request handling, and disk persistence using an Append-Only File (AOF).

## Features

- **TCP Server:** Handles client connections using Java's `ServerSocket`.
- **RESP Protocol:** Implements RESP request parsing and response encoding.
- **Command Execution:** Supports Redis-style commands through a custom command execution layer.
- **Custom LRU Cache:** Implements Least Recently Used eviction using a HashMap and a doubly linked list.
- **TTL Expiration:** Supports time-based key expiration.
- **AOF Persistence:** Appends write operations to an Append-Only File.
- **AOF Replay:** Reconstructs the in-memory state from persisted commands during server startup.
- **Concurrent Clients:** Uses a fixed thread pool to handle multiple client connections.
- **Configurable Server:** Supports environment-based configuration for port, persistence path, and cache capacity.
- **Dockerized:** Uses a multi-stage Docker build.
- **Cloud Deployed:** Deployed on Railway with TCP connectivity.

## Supported Commands

| Command | Description | Example |
|---|---|---|
| `PING` | Checks server availability | `PING` |
| `ECHO` | Returns the supplied message | `ECHO hello` |
| `SET` | Stores a key-value pair | `SET name Prisha` |
| `GET` | Retrieves a stored value | `GET name` |
| `DEL` | Deletes a key | `DEL name` |
| `EXISTS` | Checks whether a key exists | `EXISTS name` |
| `EXPIRE` | Sets a key's expiration time | `EXPIRE name 60` |
| `QUIT` | Closes the client connection | `QUIT` |

## Architecture


                  Redis Client
                       |
                  TCP Connection
                       |
                +--------------+
                |  TCP Server  |
                +--------------+
                       |
                +--------------+
                | ClientHandler|
                +--------------+
                       |
                +--------------+
                | CommandParser|
                +--------------+
                       |
                +----------------+
                | CommandExecutor|
                +----------------+
                    /        \
                   /          \
           +-----------+   +-----------+
           | DataStore |   | AofWriter |
           +-----------+   +-----------+
                 |               |
           +-----------+    Append-Only
           | LruCache  |       File
           +-----------+         |
                           +-------------+
                           | AofReplayer |
                           +-------------+


### Storage and Eviction

The in-memory storage layer uses a custom LRU cache built using:

- **HashMap:** Provides constant-time key lookup.
- **Doubly Linked List:** Maintains the order of recently accessed keys.
- **LRU Eviction:** Removes the least recently used key when the configured capacity is reached.

### Persistence and Recovery

Mini-Redis uses Append-Only File persistence to retain data across server restarts.

1. Mutating commands are appended to the AOF file.
2. The AOF writer records operations on disk.
3. During startup, the AOF replayer reads the persisted commands.
4. The commands are re-executed to reconstruct the in-memory state.

## Tech Stack

- **Language:** Java 21
- **Networking:** Java Sockets (`java.net`)
- **Build Tool:** Maven
- **Containerization:** Docker
- **Deployment:** Railway

## Getting Started

### Prerequisites

- Java 21 or later
- Maven

### 1. Clone the repository

```bash
git clone https://github.com/Prisha2205/Mini-Redis.git
cd Mini-Redis
```

### 2. Build the project

```bash
mvn clean package
```

### 3. Start the server

```bash
java -jar target/mini-redis-1.0.0.jar
```

By default, the server listens on port `6380`.

## Configuration

Mini-Redis supports configuration through environment variables.

| Variable | Default Value | Description |
|---|---|---|
| `PORT` | `6380` | Server listening port |
| `AOF_PATH` | `data/appendonly.aof` | Path to the persistence file |
| `MAX_KEYS` | `1000` | Maximum number of keys in the store |

Example:

```bash
PORT=6380 MAX_KEYS=500 AOF_PATH=data/appendonly.aof java -jar target/mini-redis-1.0.0.jar
```

## Connecting with redis-cli

Mini-Redis supports communication with Redis-compatible clients using its implemented RESP subset.

### Local connection

```bash
redis-cli -h localhost -p 6380
```

### Example commands

```redis
PING
SET language Java
GET language
EXISTS language
EXPIRE language 60
DEL language
```

### Railway deployment

The application is deployed on Railway using its Dockerfile-based deployment and TCP Proxy.

Connect using the public TCP host and port assigned by Railway:

```bash
redis-cli -h reseau.proxy.rlwy.net -p 53193
```

R

## Docker

The project includes a = Dockerfile that builds the application using Maven and runs it using a lightweight Java runtime image.

### Build the image

```bash
docker build -t mini-redis .
```

### Run the container

```bash
docker run -p 6380:6380 mini-redis
```

### Persist data using a Docker volume

```bash
docker run -p 6380:6380 \
  -v redis-data:/data \
  mini-redis
```

The volume ensures that the AOF file stored under `/data` survives container recreation.

## Future Improvements

- Expand Redis command compatibility.
- Improve RESP handling for edge cases and binary-safe payloads.
- Add automated integration and concurrency tests.
- Improve AOF recovery and persistence guarantees.
- Add support for additional Redis data types.

---

**Mini-Redis — understanding databases by building one.**
