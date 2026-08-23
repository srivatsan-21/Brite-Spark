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

## Day 2 Updates
- **Rate Limiting**: Added `HistoryService` to parse `outbox.jsonl` and calculate previous contacts over a rolling 7-day period. `ReminderService` now aggregates the past contact count across all known contact points for a resident and enforces a strict maximum of 2 contacts in any rolling 7-day period. If a resident has reached this limit, their pending appointments are skipped and logged to the console.
- **Deduplication vs Rate Limiting**: The system continues to deduplicate appointments by contact point. A consolidated message sent to a shared contact point counts as 1 contact for each resident whose appointments are included in the message. If a resident is rate-limited, their appointments are excluded from the consolidated message, meaning the message is directed only at the remaining eligible residents.

## What was rejected and why
- **Jackson Dependency**: In `HistoryService`, I rejected using Jackson to parse the JSON in `outbox.jsonl` because the project explicitly avoided it in `MockChannelService` (likely to minimize dependencies). Instead, I implemented a simple string parsing logic which is faster and keeps the footprint small.
- **Full Twilio Integration**: Rejected implementing a real Twilio SDK for Day 2 because we did not have actual API keys, which would cause the application to crash. I maintained the "Mock" paradigm for FCM.

## What was cut for time
- **Database Persistence for Rate Limits**: `HistoryService` currently reads and parses the entire `outbox.jsonl` every time a reminder job is triggered. For time, I cut implementing a caching layer or parsing this into the H2 database on startup.

## What the solution does not do
- The solution does not track the *exact timestamp* of when a resident was notified in the database. The `Appointment` model only tracks a boolean `isReminderSent`.
- It does not automatically rotate the rolling 7-day window incrementally; it computes it at the exact moment of execution (`LocalDateTime.now()`).

## What I would fix first
- **Performance**: I would optimize `outbox.jsonl` parsing by streaming it directly into an indexed database table (like SQLite or H2) so that rate-limit lookups per resident are O(1) instead of O(N).
- **Consolidation Logic**: I would enhance the `ReminderService` to build personalized messages per resident rather than per contact point, in case a shared contact point shouldn't expose another resident's private appointment details.
