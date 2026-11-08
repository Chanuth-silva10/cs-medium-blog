# CS EMI Docker Hub
A loan EMI calculator (Spring Boot 4, Java 21) packaged as a Docker image and pushed to Docker Hub with an access token. Part of the DevCraft series by CS Solution (Chapter 30).
## Build and run locally
```bash
docker build -t emi:dev .
docker run --rm -p 7560:7560 emi:dev
```
Open `http://localhost:7560`.
## Publish
Create an access token in Docker Hub (Account settings, Personal access tokens) and run:
```bash
export DOCKERHUB_USERNAME=chanuthsilva
export DOCKERHUB_TOKEN=<your access token>
scripts/publish.sh 1.0.0
```
The script logs in with `--password-stdin`, builds the image, tags it `1.0.0` and `latest`, pushes both and logs out.
To publish to another registry, set `REGISTRY`, for example `REGISTRY=localhost:5001`.
## Test
```bash
mvn verify
```
