# notification-service
Service that receives an allowance update message via SQS and emails the user about their allowance change using SES.  
I never used SES prior to this so I hope its ok! I only ran it against localStacks SES.

### Prerequisites
I used
- Java 21
- Maven 3.8.7 to build, but I've tested the mvnw wrapper and it also builds and runs it

### Building And Running Locally
To build the application
```shell
mvn clean install
```
- `run_app.sh` (add your pexels API to it) runs the application locally on port`8081` against the docker containers in `employee-service/docker_scripts.sh`

### Improvements (Due to time constraints)
- Adding unit tests
- For the advanced case to implement a retry mechanism, it could be tested using wiremock scenarios which would be configured to e.g. return a 500 for the first invocation and then a 200 on the second and we could verify that the application logged that the external service failed and then succeeded to send an email.
- Add a circuit breaker if the Pexels service is down and a rate limiter not to go above their API quotas.
- API to expose the EmailAudit table
- Ideally the AllowanceUpdateMessage would not contain the user personal information (name, surname, email) and it would call employee-service or look up in its own relevant employee table.