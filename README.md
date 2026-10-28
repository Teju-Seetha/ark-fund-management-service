# Ark Fund Management Service

A fund accounting and transaction engine built with Spring Boot 3, Java 17, and PostgreSQL/H2. It handles fund allocations, transaction processing (capital calls, distributions, fee adjustments), balance constraints, and LP portfolio reporting.

---

## Getting Started

The service runs locally with zero external infrastructure setup using an in-memory database.

### Run via IntelliJ IDEA
1. Open IntelliJ IDEA -> **File** -> **Open...** -> select `ark-fund-management-service`.
2. Let Maven sync project dependencies.
3. Open `src/main/java/com/ark/fundmgmt/ArkFundManagementApplication.java`.
4. Click the green **Run** icon next to `main()`.
5. The application starts on `http://localhost:8080`.

### Run via Terminal
```bash
./mvnw clean test spring-boot:run

```
### API Documentation & Verification
Interactive OpenAPI docs and test schemas are available once the service starts:
Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI 3 Contract: http://localhost:8080/v3/api-docs
### Verification Flow:
1. Fetch Funds:
   GET /api/v1/funds returns initial funds (fund-101, fund-102).
2. Submit a Contribution:
   POST /api/v1/transactions
   `{
   "fundId": "fund-101",
   "investorId": "inv-001",
   "type": "CONTRIBUTION",
   "amount": 50000.00,
   "currency": "USD",
   "notes": "Initial capital call drawdown",
   "idempotencyKey": "tx-seq-001"
   }`
3. Check Idempotency: 
   Re-submitting the exact payload returns 409 Conflict.
4. Test Balance Boundary:
   Submitting a DISTRIBUTION for $60,000.00 against fund-101 (balance $50,000.00) rejects with 422 Unprocessable Entity.
5. View Statements:
   GET /api/v1/reports/funds/fund-101 returns the updated balance ($50000.0000), total credits, debits, and LP position summaries.

### Embedded Database (H2 Console)
Initial test funds and institutional LP accounts seed automatically via data.sql.
* URL: http://localhost:8080/h2-console
* Driver Class: org.h2.Driver
* JDBC URL: jdbc:h2:mem:arkdb
* Username: sa
* Password: (leave empty)

### SQL
* Fund balances :
  SELECT id, name, currency, current_balance FROM funds;

* Registered LPs : 
SELECT id, name, email FROM investors;

* Immutable Ledger :
SELECT id, idempotency_key, fund_id, investor_id, type, direction, amount, currency, transaction_date
FROM transactions
ORDER BY created_at DESC;

### Health & Monitoring

Spring Boot Actuator endpoints are exposed for container health probes and telemetry:

⚬ Health Probe: http://localhost:8080/actuator/health

⚬ Metrics: http://localhost:8080/actuator/metrics

## Test Suite

```bash
mvn test
```
* `ConcurrencyRaceConditionTest`: 50 threads debiting concurrently using `SELECT FOR UPDATE` locking to prevent lost updates and balance drift.
* `TransactionServiceTest`: Validates contribution credits, distribution debits, overdraw rejections, multi-currency matching, and idempotency deduplication.

### Technical Notes
* Monetary Precision: All amounts use BigDecimal (scale=4, RoundingMode.HALF_EVEN) to eliminate binary floating-point rounding issues.
* Concurrency: Pessimistic write locking (SELECT ... FOR UPDATE) with a 5000ms query hint prevents race conditions and pool starvation under concurrent debit operations.
* Auditability: Transaction is strictly append-only (updatable = false on columns, no setters). Adjustments are made via compensating transactions rather than destructive edits.
* Caching: Caffeine Window TinyLFU caching for statements, with @CacheEvict invalidation on transaction writes.
* Error Handling: RFC 7807 ProblemDetail structures for validation (400), not found (404), duplicates (409), and overdraw attempts (422).