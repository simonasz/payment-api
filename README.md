# Payment API
Sample Payment API application based on REST specifications.

## Documentation

* [Technologies](#technologies)
    - [Language](#language)
    - [Framework and tools](#framework-and-tools)
    - [Database](#database)
    
* [Run](#run)
    - [Run standalone Jar](#run-standalone-jar-with-dependencies)
    - [Run with Gradle](#run-with-gradle)
* [Build](#build)
* [Testing](#testing)
    - [Unit testing](#unit-test)
    - [Integration e2e testing](#integration-e2e-testing)
    - [Manual e2e testing with Postman](#manual-e2e-testing-using-postman)
* [Data models](#data-models)
    - [Customer](#customer)
    - [Account](#account)
    - [Transaction](#transaction)
* [API's](#apis)

## Technologies

### Language
Java 8

### Framework and tools
Java Spark

Google Guice (dependency injections)

Gradle (build tool)

JUnit, Mockito, Powermockito (for testing)


### Database
Application uses in memory H2 database. No configuration required.

No ORM was used to keep application simple.

### Multi-threading and thread safety

[Transaction](#transaction) creation operation is thread safe, because it depends on sender [Account](#account) state (balance property) and updated both sender [Account](#account) and receiver [Account](#account).  

## Run 

Default application port: 3000. Can be overridden with environment variable ```sparkPort```

#### Run standalone Jar (with dependencies)

**JRE (min 8 version) is required.** 

Run application with default port 3000
```
java -jar payment-api-0.0.1.jar
```

Run application with custom port (5887 in this example)
```
java -jar -DsparkPort=5887 payment-api-0.0.1.jar
```

#### Run with Gradle
Gradle command to run with default port 3000:
```
gradle run
```
Gradle command to run with custom port (5447 in example):
```
gradle run -DsparkPort=5447
```

## Build
Build Jar with dependencies:
```
gradle fatJar
```
Newly created file ```payment-api-0.0.1.jar``` will be placed in root dir. Later can run this file as explained [here](#run-included-standalone-jar-with-dependencies)

## Testing
#### Unit test

Application partialy covered by Unit tests with purpose to demonstrate how application can be tested using JUnit, Mockito and PowerMockito.

Gradle command line:
```
gradle test
```

#### Integration e2e testing
Application partially covered by automated integration tests (all success scenarios and some negative scenarios).

Integration tests start Spark server.

Implemented using JUnit. Rest client for API tests: Apache HTTPClient and Fluent API.

Gradle command line:
```
gradle integrationTest
```

Gradle command line with custom server port (5887 in this example):
```
gradle integrationTest -DsparkPort=5887
```
#### Manual e2e testing using Postman

Postman test collections (environment and requests) can be found [here](postman).

Tests collection uses environment variables to ease testing. Request responses also updates those variables. 

Test collection default url - http://127.0.0.1:3000 (can be changed in environment variables)

Instructions:
1. Import environment [payment.postman_environment.json](postman/payment.postman_environment.json)
2. Import collection [payment.postman_collection.json](postman/payment.postman_collection.json)
3. Select environment ```payment```
4. Run requests (using common sense)

## Data models
#### Customer
Represents client.

Has many [Account](#account)

```
id: Long [auto generated]
firstName: String
lastName: String
updated: timestamp
created: timestamp
```
#### Account

Represents bank account.

Belongs to [Customer](#customer)

Has many [Transaction](#transaction)
```
id: Long [auto generated]
title: String
balance: BigDecimal
updated: timestamp
created: timestamp
```
#### Transaction

Represents money transfer between accounts.

Belongs to sender [Account](#account) and receiver [Account](#account) 

```
id: Long [auto generated]
title: String
amount: BigDecimal
senderAccountId: Long
receiverAccountId: Long
updated: timestamp
created: timestamp
```
## API's

API definitions (created from Postman collection) can be found [here](https://documenter.getpostman.com/view/2575494/SWLe98mK). 