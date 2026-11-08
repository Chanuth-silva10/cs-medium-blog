# CS Spring Boot RabbitMQ
Two Spring Boot 4 applications (Java 21) that talk through RabbitMQ. When a user signs up, the **producer** answers right away and puts a message on a queue. The **consumer** takes the message from the queue and sends the welcome email. Failing messages are retried and then moved to a dead letter queue. Part of the DevCraft series by CS Solution (Chapter 26).
```
POST /signups --> [producer] --> exchange devcraft.signups --> queue welcome-email --> [consumer] --> email
                                                                    | (after 3 failed tries)
                                                                    v
                                                          queue welcome-email.dlq
```
## Run
Start RabbitMQ with the web interface (http://localhost:15672, user `devcraft`, password `devcraftPwd`):
```bash
docker run -d --name devcraft-rabbit -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=devcraft -e RABBITMQ_DEFAULT_PASS=devcraftPwd rabbitmq:4-management-alpine
```
Start the two applications in two terminals:
```bash
cd producer && mvn spring-boot:run
cd consumer && mvn spring-boot:run
```
Send a signup:
```bash
curl -X POST http://localhost:7520/signups -H "Content-Type: application/json" -d '{"name":"Nadia Perera","email":"nadia@example.com"}'
```
The consumer log shows the email. To watch the retries and the dead letter queue, start the consumer with `SIMULATE_MAIL_OUTAGE=true`.
## Settings
| Environment variable | Default | Meaning |
| --- | --- | --- |
| `RABBIT_HOST` / `RABBIT_PORT` | `localhost` / `5672` | Where RabbitMQ runs |
| `RABBIT_USER` / `RABBIT_PASSWORD` | `devcraft` / `devcraftPwd` | Credentials |
| `SIMULATE_MAIL_OUTAGE` | `false` | Consumer only: make every email fail |
## Test
The tests of both applications start a real RabbitMQ with Testcontainers, so Docker must be running.
```bash
cd producer && mvn test
cd consumer && mvn test
```
