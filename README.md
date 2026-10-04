## TLOM Server

TLOM Server is the authoritative backend for the Player Client. It targets Java 21, uses Quarkus 3.39, Maven, Quarkus REST/Jackson, Jakarta Validation, JUnit 5 and REST Assured.

## Development

Run the server with Java 21:

```sh
./mvnw quarkus:dev
```

Run the React client separately from `../TLOM` with `npm start`. The development client uses `http://localhost:8080/api/v1` by default; set `REACT_APP_TLOM_API_URL` to override it. CORS permits the standard React development ports 3000 and 3001.

## PostgreSQL

The server uses PostgreSQL, Hibernate ORM and Flyway. Flyway owns schema evolution; Hibernate validates the schema and never creates or drops it. With a Docker-compatible runtime running, Quarkus Dev Services starts an isolated PostgreSQL container automatically for `./mvnw quarkus:dev` and `./mvnw test`. Podman is supported through its rootless Docker-compatible socket:

```sh
systemctl --user enable --now podman.socket
export DOCKER_HOST="unix://$XDG_RUNTIME_DIR/podman/podman.sock"
./mvnw test
```

To use an existing local PostgreSQL instance instead, create a database and export standard Quarkus datasource variables before starting the server:

```sh
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/tlom
export QUARKUS_DATASOURCE_USERNAME=tlom
export QUARKUS_DATASOURCE_PASSWORD=tlom
./mvnw quarkus:dev
```

For a production-profile deployment, set `TLOM_DB_URL`, `TLOM_DB_USERNAME`, and `TLOM_DB_PASSWORD`. On a fresh database, `src/main/resources/db/migration/V1__initial_schema.sql` is applied before startup bootstrap.

## Architecture

The project is organized by feature: `player`, `localization`, `packagecontent`, `journey`, `store`, and `purchase`; shared HTTP error handling remains in `shared`. REST resources call services, which call repositories. Cross-feature service calls are limited to genuine workflows: `PackageService -> JourneyService` creates Player-owned runtime occurrences after a validated installation, while purchases follow `PurchaseRest -> PurchaseService -> StoreService/JourneyService -> PurchaseRepository/JourneyRepository`. Hibernate entity classes remain next to the feature that owns them; no global persistence layer exists.

Implemented features are `player`, `localization`, `packagecontent`, `journey`, `store`, and `purchase`. The deterministic development identity is exposed at `GET /api/v1/me`; authentication is intentionally absent. On server startup, the `healthy-lifestyle` package is installed with its declared defaults, so the development Player starts with a pending Daily walk worth `1 HC`.

## API

- `GET /api/v1/me`: current development Player.
- `GET /api/v1/localization/{locale}`: English fallback resource with revision `1` and ETag support. Italian overlays the small migrated label set; unsupported locales return English.
- `GET /api/v1/packages`, `GET /api/v1/packages/{id}`: catalog definitions without runtime state.
- `POST /api/v1/packages/{id}/install`: Player intent with optional exposed parameter values. The server resolves defaults/bindings and installation is idempotent by package ID/version.
- `GET /api/v1/me/journey`: Player installations plus runtime currencies, campaigns, missions, Goals and GoalOccurrences.
- `POST /api/v1/goal-occurrences/{id}/complete`: authoritative completion. The server resolves rewards and adds ledger facts only once.
- `GET /api/v1/me/wallet`: Wallet projection calculated from Ledger entries.
- `GET /api/v1/me/stores`: Store and StoreItem definitions materialized only from installed Packages.
- `POST /api/v1/stores/{storeId}/purchases`: purchase intent containing only `storeItemId` and `requestId`.
- `GET /api/v1/me/purchases`: authoritative Purchase history with the immutable price snapshot paid.

Errors are JSON `{code, message}` responses. Client-provided reward amounts, balances, prices and completion state are never trusted. Purchase debits are negative Ledger entries with `PURCHASE:<purchaseId>` source keys; the Wallet remains a projection of all Ledger entries. A repeated Purchase `requestId` returns the original Purchase and does not create another debit, while a different request ID can buy the same item again when funds permit.

## Persistence Boundary

PostgreSQL persists the deterministic development Player, Package Installations and their configuration snapshots, GoalOccurrence state, immutable Ledger entries, and Purchase price snapshots. Package Definitions, localization content, currencies/goals/stores materialized from Package Definitions, and Wallet balances are not tables: they are static or derived projections. Wallet remains `SUM(ledger_entry.amount)` scoped by Player and currency, never a mutable persisted balance.

Package installation is unique per Player/package/version. Ledger source keys are unique per Player. Purchase request IDs are unique per Player, so retrying a request after restart returns its original Purchase without another debit. Purchase transactions pessimistically lock the Player row before checking the Ledger balance, then persist Purchase and debit in one Jakarta transaction. Goal completion locks its GoalOccurrence and persists completion plus reward credits in one transaction.

The React API client caches read responses in memory. Localization implements `ETag`/`If-None-Match`; the package and installed Store catalogs use the same cache helper but do not yet expose an ETag. Journey, Wallet and Purchase history are server-authoritative projections refreshed after commands. Restarting the server preserves the persisted authoritative runtime state and rebuilds projections from PostgreSQL plus the static Package catalog.

Party/Master, authentication, Package updates/reconfiguration, scheduling, offline write synchronization and complete localization remain deferred.

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
