# Real-Time Chat Application

A full-stack real-time chat platform built with Spring Boot and Vue 3. It provides
user authentication, one-to-one and group chats, real-time messaging over WebSocket,
and supporting services such as email notifications and file uploads.

## Authors

Franciszek Dyląg, Mateusz Guzowski, Marcin Wojdalski

## Features

- JWT-based authentication with Google OAuth support
- Real-time messaging via WebSocket (STOMP)
- Group conversations and contact management
- File attachments and uploads
- Email confirmation and password reset flows
- Swagger/OpenAPI documentation

## Tech Stack

- **Frontend:** Vue 3, Vite, Tailwind CSS
- **Backend:** Spring Boot (REST + WebSocket), Spring Security
- **Data/Infra:** PostgreSQL, Redis, RabbitMQ
- **Containerization:** Docker, Docker Compose

## Architecture

**3-Tier Architecture**

- **Frontend:** Vue.js SPA
- **Backend:** Spring Boot API and WebSocket gateway
- **Database:** PostgreSQL

## Getting Started

### Prerequisites

- Java 21
- Node.js 20+
- Docker (recommended for infrastructure services)

### Environment Variables

Create a `.env` file at the repository root if you are using Docker Compose. The
backend reads these values when starting:

```
SPRING_PROFILES_ACTIVE=local
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-email-password
```

The frontend expects `VITE_BACKEND_URL` in `frontend/.env` (defaults to
`http://localhost:8080`).

### Run Infrastructure + Backend with Docker Compose

```sh
docker compose up --build
```

This starts PostgreSQL, Redis, RabbitMQ, and the Spring Boot backend. Useful ports:

- Backend API: `http://localhost:8080`
- RabbitMQ management UI: `http://localhost:15672`
- RedisInsight: `http://localhost:5540`

### Run the Frontend Locally

```sh
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

### Run the Backend Locally

```sh
cd backend/realtime-chat
./mvnw spring-boot:run
```

### Tests and Linting

```sh
# Backend tests
cd backend/realtime-chat
./mvnw test

# Frontend lint
cd frontend
npm run lint
```

### API Docs

When the backend is running, Swagger UI is available at:
`http://localhost:8080/swagger-ui/index.html`.
