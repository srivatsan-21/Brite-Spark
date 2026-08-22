# Technical Decisions

## Tech Stack
- **Framework**: Spring Boot (Java 17). Chosen for its robust MVC and REST capabilities, and the ability to fulfill the requirement for "springboot with minimal code".
- **Database**: H2 Database (In-Memory). Chosen to store the parsed datasets (`contacts.csv` and `appointments.csv`) and support the requested CRUD operations natively with minimal configuration and no external dependencies.
- **ORM**: Spring Data JPA. Allows mapping Java objects to the H2 database seamlessly.

## Architecture
- **Layered Architecture**: Controllers for REST/CRUD endpoints, Services for business logic (Reminder Engine, Mock Channel Service, Data Import), and Repositories for Data Access.

## Tools
- **Gradle**: Build automation tool used for dependency management and building the Spring Boot application.
- **Java Cryptography Architecture (MessageDigest)**: Used to port the deterministic mock channel logic from Python (SHA-256) into Java to keep the application cohesive.
