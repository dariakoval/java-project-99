# Task Manager
Task Manager is a task management system. It allows you to set tasks, assign performers and change their statuses. To work with the system, registration and authentication are required.

This is an educational project based on the Spring Framework.

## Tests and linter status

[![Java CI](https://github.com/dariakoval/java-project-99/actions/workflows/generate.yml/badge.svg)](https://github.com/dariakoval/java-project-99/actions/workflows/generate.yml)
[![Maintainability](https://api.codeclimate.com/v1/badges/321e54143db5cce76bd9/maintainability)](https://codeclimate.com/github/dariakoval/java-project-99/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/321e54143db5cce76bd9/test_coverage)](https://codeclimate.com/github/dariakoval/java-project-99/test_coverage)

[Demo on Render](https://task-manager-0bps.onrender.com/)

[Interactive REST API documentation](https://task-manager-0bps.onrender.com/swagger-ui/index.html)

## Requirements

* JDK 20
* Gradle 8.4
* GNU Make

## Technology stack
Java, Gradle, Spring Boot, Spring Security, Spring Data JPA, GNU Make, Docker, H2 (development), PostgreSQL (production), PaaS Render, Sentry, Swagger.

## Setup

```bash
make setup
```

## Install

```bash
make install
```

## Run server

```bash
make start
# Open http://localhost:8080
# Username: hexlet@example.com
# Password: qwerty
```
