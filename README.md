# Restful Booker API Automation

REST Assured + TestNG + Maven framework that tests the public
[Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html) API
(`https://restful-booker.herokuapp.com`).

> ## Execution status of this delivery
> **This version has not been run to a passing state by the author.**
> The environment that generated the project could not reach Maven Central or
> `restful-booker.herokuapp.com`, so the author could not run Maven or the tests.
> A first run on a Windows machine (without credentials configured) compiled
> and executed, and exposed one framework bug: every `POST /booking` returned
> `418 I'm a Teapot` because of a multi-value `Accept` header. That is fixed in this
> version, but the fix has **not** been confirmed against the live API by the author.
> No pass/fail counts, screenshots or reports are included; run `mvn clean test`
> with `RB_USERNAME` / `RB_PASSWORD` set (see below) to produce a real
> report and summary.
> Some negative-test expectations (for example which missing fields the demo server
> rejects) are based on documented/observed behaviour of the API and should be confirmed
> on your run. See [Known limitations](#15-known-limitations-of-the-public-demo-api).

---

## 1. Project overview

The framework automates all six documented endpoints (auth, create, get, full update,
partial update, delete) with positive, lifecycle and negative tests. It uses typed POJOs
(Jackson), reusable request/response specifications, JSON Schema validation, a
token cache, per-test cleanup, and an ExtentReports HTML report.

## 2. Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| JDK | 17 or newer (project targets Java 17) | `java -version` |
| Maven | 3.8.x or newer | `mvn -version` |
| Internet access | to Maven Central and `restful-booker.herokuapp.com` | |

`JAVA_HOME` must point to the JDK, and `mvn -version` must report that same JDK.

## 3. Technology stack

| Purpose | Library | Version |
|---------|---------|---------|
| HTTP / API testing | REST Assured (+ json-schema-validator) | 5.4.0 |
| Test runner | TestNG | 7.9.0 |
| JSON binding | Jackson Databind | 2.17.0 |
| Assertions | Hamcrest + TestNG `Assert` | 2.2 / 7.9.0 |
| Reporting | ExtentReports (Spark reporter) | 5.1.1 |
| Logging | SLF4J + Logback | 2.0.12 / 1.4.14 |
| Test data | Plain Java (`ThreadLocalRandom`) | - |
| Build | Maven Compiler 3.12.1, Surefire 3.2.5 | - |

## 4. Project structure

```text
RestfulBookerAPIAutomation/
├── pom.xml
├── testng.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/restfulbooker/
    │   │   ├── base/BaseTest.java                 parent of all tests: clients, report hooks, cleanup
    │   │   ├── config/ConfigReader.java           reads config + environment overrides
    │   │   ├── constants/FrameworkConstants.java  endpoints, schema names, group names, report paths
    │   │   ├── models/                            AuthRequest, AuthResponse, BookingRequest,
    │   │   │                                      BookingResponse, BookingDates
    │   │   ├── specs/RequestSpecFactory.java      base URI, headers, timeouts, logging
    │   │   ├── specs/ResponseSpecFactory.java     status / content type / response time expectations
    │   │   ├── clients/AuthClient.java            POST /auth
    │   │   ├── clients/BookingClient.java         POST, GET, PUT, PATCH, DELETE /booking
    │   │   ├── utils/TestDataFactory.java         random valid bookings
    │   │   ├── utils/TokenManager.java            cached auth token
    │   │   ├── utils/JsonSchemaValidatorUtil.java schema assertions
    │   │   └── reporting/                         ExtentReportManager, ReportingFilter
    │   └── resources/
    │       ├── config.properties
    │       ├── logback.xml
    │       └── schemas/ auth-response-schema.json, booking-response-schema.json,
    │                    booking-id-response-schema.json
    └── test/java/com/restfulbooker/tests/
        ├── AuthTests.java
        ├── CreateBookingTests.java
        ├── GetBookingTests.java
        ├── UpdateBookingTests.java
        ├── PartialUpdateBookingTests.java
        ├── DeleteBookingTests.java
        ├── CrudLifecycleTests.java
        └── NegativeScenarioTests.java
```

Two small additions to the suggested layout: `ResponseSpecFactory` (the reusable response
specifications) and `CrudLifecycleTests` (the end-to-end lifecycle test).
`reports/`, `logs/` and `test-output/` are created at run time.

## 5. Installation and setup

1. Extract `RestfulBookerAPIAutomation.zip`.
2. Open a terminal in the extracted `RestfulBookerAPIAutomation` folder.
3. Provide credentials (next section).
4. Run `mvn clean test`. Maven downloads the dependencies on the first run.

## 6. Configuration

Settings live in `src/main/resources/config.properties`:

| Key | Default | Meaning |
|-----|---------|---------|
| `base.url` | `https://restful-booker.herokuapp.com` | API root |
| `connection.timeout` | `15000` | connect timeout, ms |
| `response.timeout` | `30000` | socket/read timeout, ms |
| `response.time.threshold` | `8000` | max accepted response time, ms, for positive checks |
| `username` / `password` | *(blank)* | API credentials |

**Credentials are intentionally blank so no secret ships in the project.** The demo
username and password are published in the *CreateToken* section of the
[API documentation](https://restful-booker.herokuapp.com/apidoc/index.html). Supply them
with one of the following (highest priority first):

1. Environment variables `RB_USERNAME` and `RB_PASSWORD`
2. JVM properties: `mvn clean test -Dusername=... -Dpassword=...`
3. A git-ignored file `src/main/resources/config.local.properties` containing
   `username=...` and `password=...`

Any key can be overridden the same way: environment variable `RB_` + key in upper case with
dots as underscores (`base.url` -> `RB_BASE_URL`, `response.time.threshold` ->
`RB_RESPONSE_TIME_THRESHOLD`).

Windows PowerShell (current session):

```powershell
$env:RB_USERNAME = "<username from the API docs>"
$env:RB_PASSWORD = "<password from the API docs>"
mvn clean test
```

Windows cmd:

```bat
set RB_USERNAME=<username from the API docs>
set RB_PASSWORD=<password from the API docs>
mvn clean test
```

macOS / Linux:

```bash
export RB_USERNAME='<username from the API docs>'
export RB_PASSWORD='<password from the API docs>'
mvn clean test
```

If credentials are missing, tests fail immediately with a message telling you which variable to set.

## 7. Run all tests

```bash
mvn clean test
```

## 8. Run individual groups

Groups: `auth`, `positive`, `negative`, `crud`.

```bash
mvn clean test -Dgroups=auth
mvn clean test -Dgroups=positive
mvn clean test -Dgroups=negative
mvn clean test -Dgroups=crud
mvn clean test -Dgroups="auth,negative"
```

Run a single class or method (Surefire ignores the suite file when `-Dtest` is used):

```bash
mvn clean test -Dtest=AuthTests
mvn clean test -Dtest=NegativeScenarioTests#shouldRejectMalformedJsonPayload
```

Use a different suite file: `mvn clean test -Dsuite.file=my-suite.xml`

## 9. Run from Eclipse or IntelliJ IDEA

**IntelliJ IDEA:** File -> Open -> select the `pom.xml` -> *Open as Project*; wait for Maven import;
set *Project SDK* to JDK 17+. Add `RB_USERNAME` / `RB_PASSWORD` under *Run -> Edit Configurations ->
Environment variables* (or use `config.local.properties`). Right-click `testng.xml` -> *Run*.

**Eclipse:** File -> Import -> Maven -> *Existing Maven Projects* -> choose the folder. Install the
TestNG plugin if prompted (Help -> Eclipse Marketplace -> "TestNG"). Add the environment variables in
*Run Configurations -> Environment*. Right-click `testng.xml` -> *Run As -> TestNG Suite*.

The report is created by hooks inside `BaseTest`, so it is also produced when you run a single class from the IDE.

## 10. Generate and open the HTML report

The report is generated automatically at the end of every run.

```bash
# Windows
start reports\ExtentReport.html
# macOS
open reports/ExtentReport.html
# Linux
xdg-open reports/ExtentReport.html
```

It lists each test with name, description, status (pass/fail/skip), timestamps, failure stack
traces and a masked request/response log (passwords and tokens appear as `****`). It is written
for passing and failing runs alike.

## 11. API endpoint summary

| Operation | Method and path | Auth | Documented success |
|-----------|-----------------|------|--------------------|
| Create token | `POST /auth` | - | 200 `{"token": "..."}`; bad login -> 200 `{"reason": "Bad credentials"}` |
| Create booking | `POST /booking` | - | 200 `{"bookingid": n, "booking": {...}}` |
| List / filter ids | `GET /booking?firstname=&lastname=&checkin=&checkout=` | - | 200 `[{"bookingid": n}, ...]` |
| Get booking | `GET /booking/{bookingid}` | - | 200 booking JSON; unknown id -> 404 `Not Found` |
| Full update | `PUT /booking/{bookingid}` | token cookie or Basic | 200 booking JSON |
| Partial update | `PATCH /booking/{bookingid}` | token cookie or Basic | 200 booking JSON |
| Delete | `DELETE /booking/{bookingid}` | token cookie or Basic | **201** `Created` |

Missing/invalid auth on PUT, PATCH or DELETE returns 403 `Forbidden`.

## 12. How the framework is organised

- **Models (POJOs)** - `AuthRequest`, `AuthResponse`, `BookingRequest`, `BookingResponse`, `BookingDates`.
  Jackson serialises and deserialises them; unknown JSON fields are ignored and `null` fields are omitted, so
  tests can send incomplete payloads. `BookingRequest` also models the booking returned by GET/PUT/PATCH and has
  `equals()` so whole bookings can be compared.
- **Specifications** - `RequestSpecFactory.baseSpec()` sets base URI, JSON `Content-Type`/`Accept`, timeouts and
  logging. `ResponseSpecFactory.jsonResponse(status)` checks status, JSON content type and response time;
  `textResponse(status)` checks status and response time only (for bodies like `Created`). The status code is
  always a parameter because endpoints differ.
- **Clients** - `AuthClient` and `BookingClient` hide URLs, path/query parameters and auth headers and return the raw
  `Response` so tests decide what to assert.
- **Utilities** - `TestDataFactory` (random valid data), `TokenManager` (token fetched once and cached),
  `JsonSchemaValidatorUtil` (schema assertion), `ConfigReader` (configuration and environment overrides).
- **Reporting** - `ExtentReportManager` builds `reports/ExtentReport.html` and `reports/test-execution-summary.txt`;
  `ReportingFilter` adds masked request/response details to the log and to the report.
- **Logging** - REST Assured logs the method and URI of each request; SLF4J/Logback writes to the console and
  `logs/framework.log`. Headers are never logged, and `password`/`token` JSON values are masked.
- **BaseTest** - starts a report entry per test, records the result, and deletes every booking a test created
  (`createTrackedBooking`) so tests are independent and leave no data behind.
- **Test classes** - one class per endpoint, plus `CrudLifecycleTests` and `NegativeScenarioTests`.

## 13. Test coverage matrix

| Area | Class | Scenarios |
|------|-------|-----------|
| Auth | `AuthTests` | valid token (status, JSON, time, schema, non-empty); wrong password; unknown user; empty credentials; invalid token on a protected call; cached token accepted |
| Create | `CreateBookingTests` | valid create (status, content type, time, schema, positive id, all fields); distinct ids; read-back |
| Get | `GetBookingTests` | by id (status, schema, all fields, time); id matches created booking; filter by firstname+lastname; filter by checkin/checkout; unknown name -> empty list; list without filters |
| Full update | `UpdateBookingTests` | PUT with token; PUT with Basic auth; GET confirms |
| Partial update | `PartialUpdateBookingTests` | firstname only; firstname+totalprice; totalprice only; unchanged fields verified; GET confirms |
| Delete | `DeleteBookingTests` | create -> delete (201 `Created`) -> GET 404 `Not Found` |
| Lifecycle | `CrudLifecycleTests` | create -> get -> PUT -> PATCH -> delete -> 404 in one method |
| Negative | `NegativeScenarioTests` | invalid credentials; PATCH/PUT/DELETE without auth (403, data unchanged); unknown/malformed ids (404); write on nonexistent id (404/405); malformed JSON; empty payload; each mandatory field missing; invalid dates; unsupported method / unknown endpoint |

## 14. Troubleshooting

| Symptom | Fix |
|---------|-----|
| `release version 17 not supported` / `invalid target release` | Maven is running on an older JDK. Install JDK 17+, set `JAVA_HOME`, check `mvn -version`. |
| `mvn` is not recognized | Install Maven and add its `bin` folder to `PATH`. |
| `Could not transfer artifact` / `PKIX path building failed` | Network/proxy/certificate problem reaching Maven Central. Configure the proxy in `~/.m2/settings.xml` or import your corporate CA into the JDK truststore. |
| `No 'username' configured...` | Set `RB_USERNAME` / `RB_PASSWORD` (section 6). |
| `Could not obtain an auth token (Bad credentials)` | The credentials are wrong; re-check the values in the API docs. |
| `UnknownHostException`, `Connect timed out`, `SocketTimeoutException` | No internet access or the demo site is slow/asleep. Open the base URL in a browser; raise `connection.timeout` / `response.timeout`. |
| `Expected response time ... to be a value less than ...` | The public server was slow. Raise `response.time.threshold` (for example `RB_RESPONSE_TIME_THRESHOLD=15000`). |
| `418 I'm a teapot` | The API only accepts a plain `Accept: application/json`. Do not use REST Assured's `setAccept(ContentType.JSON)` / `.accept(ContentType.JSON)`: it sends several media types and triggers 418. `RequestSpecFactory` already sends the plain string. |
| `No tests were executed` | A `-Dgroups` value that matches nothing, or `-Dtest` combined with a wrong name. |
| IDE shows red imports | Reload the Maven project (IntelliJ: Maven tool window -> Reload; Eclipse: Maven -> Update Project). |
| Failing tests but green build is wanted | Run `mvn clean test -Dmaven.test.failure.ignore=true`. |

## 15. Known limitations of the public demo API

- It is a shared, public sandbox: anyone can create or delete bookings, and the data resets periodically.
  Tests therefore create their own bookings, use random names with unique suffixes, and delete what they create.
  The "list all ids" test accepts any non-empty list because other people's data is present.
- Validation is permissive and partly undocumented. Missing mandatory fields and malformed JSON are expected to
  produce an error status (the API returns 500 rather than 400 for many of these), so those tests accept any 4xx/5xx
  and verify that no booking id is returned. Date values are not necessarily validated, so the invalid-date test
  accepts either a rejection or a well-formed create response.
- A bad login returns HTTP 200 with `{"reason":"Bad credentials"}` instead of 401; the auth tests assert that.
- PUT/PATCH/DELETE on a nonexistent id return 405 instead of 404; those tests accept either.
- Successful DELETE returns **201 `Created`**, not 200/204.
- The date filters are documented as `checkin` on-or-after and `checkout` on-or-before; the date-filter test uses a
  one-day margin plus name filters. If the server semantics differ, adjust `shouldFilterBookingsByDates`.
- Tokens are shared by the whole run (`TokenManager`); if the server restarts mid-run, re-run the suite.
- Response times depend on the free hosting; adjust `response.time.threshold` if needed.

## 16. Expected report locations

| File | Created by |
|------|-----------|
| `reports/ExtentReport.html` | ExtentReports (open in a browser) |
| `reports/test-execution-summary.txt` | total / passed / failed / skipped counts of the run |
| `logs/framework.log` | Logback file appender |
| `target/surefire-reports/` | Surefire/TestNG XML and HTML (Maven runs) |
| `test-output/` | TestNG default output (IDE runs) |
