# FlashCards CLI Application

A console application written in Java 21 for learning using flashcards, managing a vocabulary database, and tracking user mistake statistics[cite: 34, 36, 38, 43]. The project emphasizes clean architecture (Separation of Concerns), dependency injection, full I/O isolation, and rigorous code quality control during the build process[cite: 35, 36, 37, 38, 43].

## Key Features & Technical Architecture

* **Layered Architecture & Dependency Injection:**
  * Separation of concerns between user interaction (`Main`), business logic (`FlashCardService`), console abstraction (`LoggedConsole`), and file persistence (`FlashCardFileStorage`)[cite: 35, 36, 37, 38].
  * Constructor-based dependency injection in the main application class, enabling control flow testing in complete isolation[cite: 38, 42].

* **Domain Logic & State Management:**
  * Validation of term and definition duplicates using Unicode normalization (`Normalizer.Form.NFC`) and case-insensitive, locale-neutral comparisons (`Locale.ROOT`)[cite: 36].
  * Use of a dedicated enumeration (`AddResult`) for explicitly communicating domain operation results instead of raw boolean values[cite: 33, 36, 38].
  * An answer verification mechanism during querying that, upon a mistake, indicates whether the user's definition matches another term in the database[cite: 38].
  * Tracking mistake statistics for individual cards and identifying the hardest flashcards (`findHardestCards`) while maintaining domain model encapsulation and immutable collection copies (`List.copyOf`)[cite: 34, 36].

* **I/O Abstraction, Persistence & CLI Handling:**
  * A custom I/O stream wrapper (`LoggedConsole`) capturing the full session history (`Scanner` and `System.out`), allowing its save and automatic update in a log file[cite: 37].
  * Support for importing and exporting card states along with mistake counts using the `java.nio.file` API (`Path`, `Files`) with enforced `UTF-8` encoding and a merge/update mechanism for existing records (`mergeImportedCards`)[cite: 35, 36].
  * Command-line argument parsing (`-import`, `-export`) allowing automatic loading of the flashcard database upon application startup and state saving upon termination[cite: 38].

* **Testing & Automated Quality Gates:**
  * A unit and integration test suite based on JUnit 5, utilizing the `@TempDir` annotation for safe and isolated filesystem operations testing[cite: 39, 40, 41, 42, 43].
  * Automatic code coverage reporting via the JaCoCo plugin integrated into the Maven `verify` lifecycle phase[cite: 43].
  * Rigorous static code analysis and vulnerability detection integrated into the `verify` phase (failing the build on violations: `failsOnViolation` / `failOnError`) using Checkstyle, PMD, SpotBugs, and FindSecBugs[cite: 43].

## Tech Stack

* **Language & Platform:** Java 21 (Java Core, NIO.2, Collections Framework)[cite: 35, 36, 43]
* **Build & Dependency Management:** Maven[cite: 43]
* **Testing & Code Coverage:** JUnit 5 (5.10.2), Maven Surefire (3.6.0), JaCoCo (0.8.15)[cite: 43]
* **Static Analysis & Security:** Checkstyle (12.1.1), PMD (7.28.0), SpotBugs (4.10.5.0) + FindSecBugs (1.14.0)[cite: 43]

## Web UI

A browser front end over the same `FlashCardService` and `FlashCardFileStorage`. It uses the JDK's built-in HTTP server, so it adds no dependencies.

```bash
mvn compile
java -cp target/classes grid.projects.WebApp Capitals.txt
```

Open http://localhost:8080. The file argument is optional. If you give one, cards are loaded from it on start and saved back to it after every change. You can pass a port as the second argument. The server listens on localhost only.
