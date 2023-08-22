# Maintain other individuals frontend

This service is responsible for updating the information held about other individuals in a trust registration.
A trust does not need to have other individuals.

To run locally using the micro-service provided by the service manager:

***sm2 --start MAINTAIN_TRUST_ALL***

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9799 but is defaulted to that in build.sbt).

`sbt run`
## Testing the service

This service uses [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) to
provide test coverage reports.

Use the following commands to run the tests with coverage and generate a report.

Run unit and integration tests:
```
sbt clean coverage test IntegrationTest/test coverageReport
```

Unit tests only:
```
sbt clean coverage test coverageReport
```

Integration tests only:
```
sbt clean coverage IntegrationTest/test coverageReport
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
