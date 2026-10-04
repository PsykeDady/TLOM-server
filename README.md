## TLOM Server

TLOM Server is the authoritative backend for the Player Client. It targets Java 21, uses Quarkus 3.39, Maven, Quarkus REST/Jackson, Jakarta Validation, JUnit 5 and REST Assured.

## Development

Run the server with Java 21:

```sh
./mvnw quarkus:dev
```

Run the React client separately from `../TLOM` with `npm start`. The development client uses `http://localhost:8080/api/v1` by default; set `REACT_APP_TLOM_API_URL` to override it. CORS permits the standard React development ports 3000 and 3001.

## Architecture

The project is organized by component type: `controller` contains REST resources, `service` contains application logic, `repository` contains in-memory data access, and `model` contains domain and API models. Controllers call services, which call repositories. `PackageService -> JourneyService` creates Player-owned runtime occurrences after a validated installation. No ports, adapters, ORM or external infrastructure are used.

Implemented features are `player`, `localization`, `packagecontent` and `journey`. The deterministic development identity is exposed at `GET /api/v1/me`; authentication is intentionally absent. On server startup, the `healthy-lifestyle` package is installed with its declared defaults, so the development Player starts with a pending Daily walk worth `1 HC`.

## API

- `GET /api/v1/me`: current development Player.
- `GET /api/v1/localization/{locale}`: English fallback resource with revision `1` and ETag support. Italian overlays the small migrated label set; unsupported locales return English.
- `GET /api/v1/packages`, `GET /api/v1/packages/{id}`: catalog definitions without runtime state.
- `POST /api/v1/packages/{id}/install`: Player intent with optional exposed parameter values. The server resolves defaults/bindings and installation is idempotent by package ID/version.
- `GET /api/v1/me/journey`: Player installations plus runtime currencies, campaigns, missions, Goals and GoalOccurrences.
- `POST /api/v1/goal-occurrences/{id}/complete`: authoritative completion. The server resolves rewards and adds ledger facts only once.
- `GET /api/v1/me/wallet`: Wallet projection calculated from Ledger entries.

Errors are JSON `{code, message}` responses. Client-provided reward amounts, balances, prices and completion state are never trusted.

## Cache And Persistence

The React API client caches read responses in memory. Localization implements `ETag`/`If-None-Match`; the package catalog is read through the same client cache helper but does not yet expose an ETag. Journey and Wallet are server-authoritative projections refreshed after commands. Repositories are in memory: restarting the server resets Player state and reapplies the Healthy Lifestyle bootstrap. React refresh while the server remains running reconstructs migrated journey and wallet state from server responses.

Party/Master, authentication, persistence and migrations, Package updates/reconfiguration, full Store/Purchase authority, scheduling, offline write synchronization and complete localization remain deferred. Completion retry safety currently derives from the stored occurrence and ledger source key; `X-Request-Id` is not independently persisted.

Run server tests with `./mvnw test`.
# tlom-server

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.


```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/tlom-server-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Bean validation using Hibernate Validator and Jakarta Validation annotations
