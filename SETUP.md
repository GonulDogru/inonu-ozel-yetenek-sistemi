# Setup Guide

This guide explains how to run the Inonu OYS project locally.

## Requirements

- Java 17
- Node.js 18 or newer
- Docker Desktop
- Git

## 1. Start PostgreSQL

From the repository root:

```powershell
docker compose up -d db
```

PostgreSQL runs on:

```text
localhost:15432
```

## 2. Start Backend

Open `inonu-oys-backend` in IntelliJ IDEA or run the Spring Boot app with these environment variables:

```text
DB_URL=jdbc:postgresql://localhost:15432/inonu_oys
DB_USERNAME=oys_admin
DB_PASSWORD=<local-db-password>
BOOTSTRAP_ENABLED=false
JWT_SECRET=<change-this-to-a-long-random-secret>
```

Backend URL:

```text
http://localhost:8080
```

Flyway migrations run automatically when the backend starts.

## 3. Start Frontend

```powershell
cd inonu-oys-frontend
npm install
npm run dev -- --host 127.0.0.1
```

Frontend URL:

```text
http://127.0.0.1:5173/
```

## 4. Build Checks

Frontend:

```powershell
cd inonu-oys-frontend
npm.cmd run build
```

Backend:

```powershell
cd inonu-oys-backend
.\gradlew.bat test
```

## Troubleshooting

If the frontend opens but data does not load, check that:

- PostgreSQL Docker container is running.
- Backend is running on port `8080`.
- Backend uses the PostgreSQL URL with port `15432`.

If Docker containers are stopped:

```powershell
docker compose up -d db pgadmin
```

If Vite is not reachable:

```powershell
cd inonu-oys-frontend
npm run dev -- --host 127.0.0.1
```
