# Rewards API — Spring Boot

A RESTful API that calculates customer reward points from purchase transactions.

---

## Points Calculation Rule

| Purchase Amount | Points Earned                                           |
|-----------------|---------------------------------------------------------|
| ≤ $50           | 0 points                                                |
| $50 – $100      | 1 point per dollar over $50                             |
| > $100          | 50 pts (for $50–$100 band) + 2 pts per dollar over $100 |

**Example:** $120 purchase = (2 × $20) + (1 × $50) = **90 points**

---

## Stack

- Java 17 · Spring Boot 3.2 · Spring Data JPA · H2 (in-memory) · Maven

---

## Project Structure

```
src/main/java/com/rewards/
├── RewardsApplication.java
├── controller/
│   └── RewardsController.java
├── service/
│   └── RewardsService.java
├── repository/
│   └── TransactionRepository.java       ← Spring Data JPA interface
├── entity/
│   └── Transaction.java                 ← @Entity, seeded via data.sql
├── dto/
│   ├── MonthlyReward.java
│   ├── CustomerRewardSummary.java
│   └── ErrorResponse.java
└── exception/
    ├── CustomerNotFoundException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── schema.sql                           ← DDL for H2
├── data.sql                             ← 28 seed transactions (Jan–Mar 2024)
└── application.properties

src/test/java/com/rewards/
├── RewardsIntegrationTest.java          ← @SpringBootTest end-to-end tests
├── controller/RewardsControllerTest.java
└── service/RewardsServiceTest.java
```

---

## Running the Application

```bash
mvn spring-boot:run
```

Server starts at `http://localhost:8080`.
H2 console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:rewardsdb`).

---

## Running Tests

```bash
mvn test
```

---

## Canonical Endpoint

```
GET /api/rewards?customerId={id}&months={n}
```

### Query Parameters

| Parameter    | Type    | Required | Default | Constraint |
|--------------|---------|----------|---------|------------|
| `customerId` | String  | **Yes**  | —       | —          |
| `months`     | Integer | No       | `3`     | ≥ 1        |

### Examples

```
GET /api/rewards?customerId=C001          → single object, last 3 months
GET /api/rewards?customerId=C001&months=2 → single object, last 2 months
```

---

## Response Schema

### `200 OK`

```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "monthlyRewards": [
    { "month": 1, "year": 2024, "totalAmount": 395.00, "rewardPoints": 365 },
    { "month": 2, "year": 2024, "totalAmount": 263.00, "rewardPoints": 148 },
    { "month": 3, "year": 2024, "totalAmount": 235.00, "rewardPoints": 210 }
  ],
  "totalRewardPoints": 723
}
```

### Error — `4xx / 5xx`

```json
{
  "timestamp": "2024-03-15T10:30:00",
  "path": "/api/rewards",
  "code": 404,
  "message": "Customer not found: UNKNOWN",
  "details": null
}
```

Validation errors populate `details` as an array — one entry per violated constraint:

```json
{
  "timestamp": "2024-03-15T10:30:00",
  "path": "/api/rewards",
  "code": 400,
  "message": "Validation failed",
  "details": [
    "getRewards.months: must be greater than or equal to 1"
  ]
}
```

Missing `customerId` returns `400 Bad Request`.

---

## Mock Dataset

| Customer      | Jan  | Feb  | Mar  | Total    |
|---------------|------|------|------|----------|
| Alice Johnson | 365  | 148  | 210  | **723**  |
| Bob Martinez  | 475  | 195  | 370  | **1040** |
| Carol Smith   |  70  | 365  | 170  | **605**  |
| David Lee     | 850  | 105  | 215  | **1170** |
