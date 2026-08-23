# AI Usage

In building and modifying this project, AI (Gemini 3.1 Pro) was used for:
- **Scaffolding & Boilerplate:** Generating the initial `HistoryService.java` skeleton to parse the JSON logs.
- **Implementation Strategy:** Designing the logic to enforce the rolling 7-day rate limit efficiently without introducing external dependencies (like Jackson).
- **Mock Generation:** Generating the mock logic for Firebase Cloud Messaging (FCM) integration and extending the `Contact` model.
- **Debugging & Refactoring:** Fixing an early compilation error related to missing Jackson dependencies by rewriting the parser to use basic string operations.
- **Documentation:** Assisting in drafting `DECISIONS.md` and this usage file.

*All code has been reviewed, understood, and manually verified to meet the system requirements.*
