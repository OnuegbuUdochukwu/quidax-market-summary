# Project Documentation: Quidax Market Summary

## 1. Project Overview
The Quidax Market Summary project is a Spring Boot microservice designed to consume data from the public Quidax API and enrich it with custom business logic. It builds upon the foundation of the first project by not only fetching market data but also by calculating the 24-hour price change percentage for each trading pair.

The service exposes a single REST endpoint that provides a clean, user-friendly summary of market performance, making it ideal for use in dashboards or other frontend applications.

```
GET /api/v1/summary: Fetches a summary for all available markets, including a calculated 24h percentage change.
```

## 2. Core Dependencies
This project utilizes the same core dependencies as the first project, defined in the pom.xml file.

- **spring-boot-starter-web**: The essential package for building REST APIs in Spring. It includes an embedded Tomcat server, the Spring MVC framework for handling HTTP requests, and the Jackson library for JSON conversion.

- **lombok**: A helper library that reduces boilerplate code by automatically generating methods like getters, setters, and constructors through annotations.

## 3. Project Structure and Components
The project follows a standard layered architecture, separating concerns into distinct packages. This makes the application organized and easy to maintain.

```
/dto/
 └── MarketSummary.java       (Data Model for our API's response)
/service/
 └── SummaryService.java      (Business Logic and API communication)
/controller/
 └── SummaryController.java   (API Endpoint Layer)
```

## 4. Detailed Class Explanations

### dto/MarketSummary.java - The Data Model

This class is a Data Transfer Object (DTO). Its purpose is to define the structure of the JSON objects we will send back to the client. It is a custom-defined structure that combines raw data from the Quidax API with our own calculated data.

```java
package com.quidaxproject.summary.dto;

import lombok.Data;

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

`@Data`: A Lombok annotation that automatically generates all the necessary boilerplate code at compile time, including getters for every field, setters for every field, and a useful `toString()` method.

### Fields

These properties define the structure of our final summary object. We have chosen clear, user-friendly names. The `priceChangePercent` field is unique to this DTO and does not exist in the raw API response; it's a product of our own business logic.

## service/SummaryService.java - The Business Logic Layer

This class is the heart of the application. It handles fetching data from the external Quidax API and applying our custom logic to transform that data into the desired summary format.

```java
package com.quidaxproject.summary.service;
// ... imports

@Service // 1. Spring Annotation
public class SummaryService {

    // 2. API Constant
    private static final String QUIDAX_API_TICKERS_URL = "https://api.quidax.com/api/v1/tickers";

    // 3. HTTP Client
    private final RestTemplate restTemplate;

    // 4. Constructor
    public SummaryService() {
        this.restTemplate = new RestTemplate();
    }

    // 5. Main public method
    public List<MarketSummary> getMarketSummaries() {
        // ... implementation ...
    }

    // 6. Private calculation method
    private String calculatePriceChangePercent(String lastPrice, String openPrice) {
        // ... implementation ...
    }

    // 7. Private inner class for external data
    @Data
    private static class QuidaxTicker {
        private String open;
        private String low;
        private String high;
        private String last;
        private String vol;
    }
}
```

> **@Service:** This annotation registers the class as a service bean with the Spring container. This tells Spring to create and manage a single instance of this class for the application.

> **API Constant:** A static final string that holds the URL for the Quidax tickers endpoint. This prevents "magic strings" and makes the code easier to maintain.

> **RestTemplate:** Spring's classic synchronous client for making HTTP requests to external APIs.

> **Constructor:** Initializes the RestTemplate instance when Spring creates the SummaryService bean.

> **getMarketSummaries() Method:** This is the primary public method.
>
> - It uses `restTemplate.exchange()` to call the Quidax API.
> - `ParameterizedTypeReference` is used to help Jackson correctly deserialize the JSON response into `Map<String, QuidaxTicker>`.
> - It initializes an empty `ArrayList` to hold our final `MarketSummary` objects.
> - It iterates through the map of tickers received from the API. For each entry (each market):
    >   - It creates a new `MarketSummary` object.
>   - It populates the object with data from the `QuidaxTicker` (e.g., `setPrice`, `setVolume`).
>   - It calls the private helper method `calculatePriceChangePercent()` to compute the 24h change.
> - The fully populated `MarketSummary` object is added to the list.
> - Finally, it returns the complete list of summaries.

> **calculatePriceChangePercent() Method:** This private helper method contains the core business logic.
>
> - **BigDecimal:** It uses the `java.math.BigDecimal` class to handle the price calculations. This is the standard and correct way to work with financial data in Java, as it avoids the precision errors inherent in float and double types.
> - **Calculation:** It computes the percentage change using the formula `((current price - opening price) / opening price) * 100`.
> - **Error Handling:** It's wrapped in a try-catch block to gracefully handle cases where the price data might not be a valid number (`NumberFormatException`) or where division by zero occurs (`ArithmeticException`). In case of an error, it returns a neutral "0.00%".
> - **Formatting:** It uses `String.format("%+.2f%%", ...)` to format the final output. The `%+.2f` part is a format specifier that means:
    >   - `+`: Always include a sign (+ or -).
>   - `.2`: Round to two decimal places.
>   - `f`: Treat the number as a floating-point type.
>
> The trailing `%%` adds a literal percent sign to the output.

> - **calculatePriceChangePercent() Method**
> This private helper method contains the core business logic.

> - **BigDecimal**
It uses the `java.math.BigDecimal` class to handle the price calculations. This is the standard and correct way to work with financial data in Java, as it avoids the precision errors inherent in float and double types.

> - **Calculation**
It computes the percentage change using the formula `((current price - opening price) / opening price) * 100`.

> - **Error Handling**
It's wrapped in a try-catch block to gracefully handle cases where the price data might not be a valid number (`NumberFormatException`) or where division by zero occurs (`ArithmeticException`). In case of an error, it returns a neutral "0.00%".

> - **Formatting**
>It uses `String.format("%+.2f%%", ...)` to format the final output. The `%+.2f` part is a format specifier that means:
>
>- `+`: Always include a sign (+ or -).
>- `.2`: Round to two decimal places.
>- `f`: Treat the number as a floating-point type.
>
>The trailing `%%` adds a literal percent sign to the output.

> - **QuidaxTicker Inner Class**
This private static inner class is used to model the exact JSON structure returned by the Quidax API. This is a powerful design pattern because it decouples our application from the external API. If Quidax were to change their response field names, we would only need to update this inner class, and the rest of our application (like our MarketSummary DTO) would remain unchanged.

## controller/SummaryController.java - The API Endpoint Layer
This class is the entry point for all external requests. It's a thin layer that handles HTTP routing and delegates the actual work to the SummaryService.

```java
package com.quidaxproject.summary.controller;
// ... imports

@RestController // 1. Spring Annotation
@RequestMapping("/api/v1") // 2. Base Path
public class SummaryController {

    private final SummaryService summaryService; // 3. Dependency

    // 4. Constructor Injection
    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/summary") // 5. Endpoint Mapping
    public List<MarketSummary> getMarketSummaries() {
        return summaryService.getMarketSummaries();
    }
}
```

### @RestController
A convenience annotation that combines `@Controller` and `@ResponseBody`. It tells Spring that this class will handle HTTP requests and that its methods will return data (which Spring will serialize to JSON) directly in the response body.

### @RequestMapping("/api/v1")
This sets a base URL path for all endpoints in this class. All mappings inside this controller will be prefixed with `/api/v1`.

### Dependency Declaration
A final field for the SummaryService is declared.

### Constructor Injection
This is the recommended way to handle dependencies in Spring. By providing a constructor that requires a SummaryService instance, we are telling Spring to automatically inject the managed SummaryService bean when it creates the SummaryController.

### @GetMapping("/summary")
This annotation maps HTTP GET requests for the full path `/api/v1/summary` to the `getMarketSummaries()` method. The method simply calls the service to get the data and returns the resulting list. Spring's Jackson integration handles the conversion of the `List<MarketSummary>` object into a JSON array.