# Project Documentation: Quidax Market Summary (v2)

## 1. Project Overview
The Quidax Market Summary project is a Spring Boot microservice designed to consume data from the public Quidax API and enrich it with custom business logic. It fetches data for all markets, calculates the 24-hour price change percentage for each, and presents this information in a clean, user-friendly format.

The service correctly handles the nested JSON structure of the Quidax API by using a layered DTO (Data Transfer Object) approach.

`GET /api/v1/summary`: Fetches a summary for all available markets, including a calculated 24h percentage change.

## 2. Core Dependencies
- **spring-boot-starter-web**: The essential package for building REST APIs, including an embedded Tomcat server and the Jackson JSON library.
- **lombok**: A utility library that reduces boilerplate code by automatically generating methods like getters and setters.
## 3 Project Structure and Components

The project follows a standard layered architecture. The DTO package is structured to accurately model the complex, nested responses from the Quidax API.

```
/dto/
 ├── MarketSummary.java       (Final, user-facing data model)
 ├── Ticker.java              (Innermost: The raw ticker data)
 ├── MarketData.java          (Middle Layer: Contains Ticker and timestamp)
 └── QuidaxResponse.java      (Wrapper for the API's top-level response)
/service/
 └── SummaryService.java      (Business Logic and API communication)
/controller/
 └── SummaryController.java   (API Endpoint Layer)
```
## 4. Detailed Class Explanations

### The DTO Layer (The Data Models)

Our DTOs are designed to exactly match the nested structure of the Quidax API response.

📄 **MarketSummary.java**

**Purpose:** This is the final object we present to our users. It combines data fetched from the API with our own calculated `priceChangePercent` field.

**Code:**

```java
@Data
public class MarketSummary {
    private String market;
    private String price;
    private String volume;
    private String high;
    private String low;
    private String priceChangePercent;
}
```

📄 **Ticker.java, MarketData.java, QuidaxResponse.java**

**Purpose:** These three classes work together to model the raw, nested response from the Quidax API, allowing Jackson to parse it correctly. Their structure is identical to the one we finalized in Project 1.

## service/SummaryService.java - The Business Logic Layer

This class contains the core logic. It fetches data, "unwraps" the nested JSON, performs calculations, and builds the final response.

**URL Constant:** The service uses the correct, working URL for fetching all tickers: `https://api.quidax.com/api/v1/markets/tickers`.

### getMarketSummaries() Method:

**Action:** This is the primary method that orchestrates the entire process.

**Logic:**

- It tells RestTemplate to expect a top-level QuidaxResponse object.
- It gets the `Map<String, MarketData>` from the response body.
- It iterates through this map. For each market, it unwraps the innermost Ticker object from the MarketData value.
- It uses the data from the unwrapped Ticker to populate a MarketSummary object.
- It calls the `calculatePriceChangePercent()` helper method to compute the final field.
- It adds the complete MarketSummary object to a list, which is then returned.

## Code:

```java
public List<MarketSummary> getMarketSummaries() {
    String url = "https://api.quidax.com/api/v1/markets/tickers";
    ResponseEntity<QuidaxResponse> response = restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    QuidaxResponse body = response.getBody();
    List<MarketSummary> summaries = new ArrayList<>();

    if (body != null && "success".equals(body.getStatus())) {
        Map<String, MarketData> marketDataMap = body.getData();
        for (Map.Entry<String, MarketData> entry : marketDataMap.entrySet()) {
            Ticker ticker = entry.getValue().getTicker(); // Unwrapping step
            if (ticker != null) {
                MarketSummary summary = new MarketSummary();
                summary.setMarket(entry.getKey().replace("_", "/").toUpperCase());
                summary.setPrice(ticker.getPrice());
                // ... set other fields
                summary.setPriceChangePercent(
                    calculatePriceChangePercent(ticker.getPrice(), ticker.getOpen())
                );
                summaries.add(summary);
            }
        }
    }
    return summaries;
}
```

### calculatePriceChangePercent() Method:

**Purpose:** A private helper method containing the specific business logic for the percentage calculation.

**Logic:** It uses the `BigDecimal` class for high-precision financial math, preventing common floating-point errors. It calculates the percentage change and formats the result as a signed string (e.g., +2.15%).

## controller/SummaryController.java - The API Layer

This class has not changed. It remains a thin layer responsible only for exposing the GET `/api/v1/summary` endpoint and delegating the request to the SummaryService. It is completely unaware of the complex data fetching and transformation logic, which is the sign of a well-architected service.