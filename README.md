# Brite-Spark: Reminder That Reaches

This is a Spring Boot implementation of the "Reminder That Reaches" project.

## How to Run (From a clean clone)

1. Ensure you have Java 17 installed.
2. In the root directory of this repository, run the following command to start the server:

```bash
./gradlew bootRun
```
*(On Windows, use `gradlew.bat bootRun`)*

3. The application will start and automatically import `contacts.csv` and `appointments.csv` into the in-memory H2 database.
4. To trigger the reminder job and generate the `outbox.jsonl` output along with the success report in the console, run:

```bash
curl -X POST http://localhost:8080/api/reminders/trigger
```

## Features Implemented for "The Floor"
- **Strict Enforcement Gateway:** Opt-outs and quiet hours are deeply enforced in a gateway that wraps the messaging channels, preventing bypass.
- **Deduplication by Contact Point:** If multiple appointments share the same phone number or email, they are combined into a single message.
- **Language Tracking:** Unsupported languages fall back to English, but these fallbacks are tracked and explicitly reported in the success metrics.
- **Measurable Success:** At the end of the reminder trigger, a console report prints total appointments processed, successful reaches, and language fallbacks.
