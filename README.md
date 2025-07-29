# Quidax Market Summary API 📈

A Spring Boot REST API that provides a 24-hour summary for each market on Quidax, including the 24h percentage price change, trading volume, and high/low prices.

This project consumes the public Quidax API, fetches the raw ticker data, and then applies custom business logic to calculate the percentage change for each market.

## Features

- Provides a clean, summarized list of all active markets.
- Calculates the 24-hour price change percentage for each market.
- Uses BigDecimal for all financial calculations to ensure precision.
- Built with a clean, layered architecture (Controller, Service, DTO).

## Technologies Used

- Java 17
- Spring Boot 3
- Maven
- Lombok

## API Endpoint

| Method | Endpoint          | Description                                   |
|--------|-------------------|-----------------------------------------------|
| GET    | /api/v1/summary    | Get the 24-hour summary for all markets.     |

## Export to Sheets

### Example Usage:
A GET request to `http://localhost:8080/api/v1/summary` will return a JSON array similar to this:

```json
[
  {
    "market": "BTC/NGN",
    "price": "103100000.0",
    "volume": "2.5165",
    "high": "104000000.0",
    "low": "102500000.0",
    "priceChangePercent": "+1.08%"
  },
  {
    "market": "ETH/NGN",
    "price": "5200000.0",
    "volume": "53.48",
    "high": "5300000.0",
    "low": "5150000.0",
    "priceChangePercent": "-0.95%"
  }
]
```

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- JDK (Java Development Kit) 17 or later
- Maven

### Installation & Running the App

Clone the repository:

```bash
git clone https://github.com/OnuegbuUdochukwu/quidax-market-summary.git
```

Navigate to the project directory:

```bash
cd quidax-market-summary
```

Run the application using the Maven wrapper:

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`.
