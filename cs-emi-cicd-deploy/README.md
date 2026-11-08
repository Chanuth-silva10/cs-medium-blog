# CS EMI CI/CD Deploy
A loan EMI calculator (Spring Boot 4, Java 21) with a GitHub Actions pipeline: test, build and publish a Docker image, deploy it on a server over SSH with a health check and an automatic rollback. Part of the DevCraft series by CS Solution (Chapter 29).
## The pipeline
| Job | Runs when | What it does |
| --- | --- | --- |
| `test` | pull requests and pushes to `main` | `mvn verify` on JDK 21 |
| `publish` | pushes to `main` | builds the image, pushes `sha-<7 characters>` and `latest` |
| `deploy` | after `publish` | runs `deploy/remote-deploy.sh` on the server over SSH |
## Settings in the GitHub repository
Secrets (Settings, Secrets and variables, Actions), ideally on an environment named `production`:
| Secret | Value |
| --- | --- |
| `DOCKERHUB_USERNAME` | Your Docker Hub user name |
| `DOCKERHUB_TOKEN` | A Docker Hub access token with read and write rights |
| `SERVER_HOST` | The server address |
| `SERVER_PORT` | The SSH port, usually 22 |
| `SERVER_USER` | The deploy user on the server |
| `SERVER_SSH_KEY` | The private key of a key pair made only for deployment |
| `SERVER_HOST_KEY` | The public host key of the server, from `ssh-keyscan -p 22 <host>` |
Variable (optional): `APP_URL`, for example `https://emi.203-0-113-10.sslip.io`, checked at the end of the pipeline.
## Try the deploy script without a server
The script needs only Docker. Any registry works, for example a local one:
```bash
docker build -t localhost:5000/devcraft/cs-emi:sha-1111111 .
docker push localhost:5000/devcraft/cs-emi:sha-1111111
HOST_PORT=7560 CONTAINER_NAME=emi-test bash deploy/remote-deploy.sh localhost:5000/devcraft/cs-emi:sha-1111111
```
## Test
```bash
mvn verify
```
