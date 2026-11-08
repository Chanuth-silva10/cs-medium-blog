# CS EMI Nginx Proxy
A loan EMI calculator (Spring Boot 4, Java 21, Thymeleaf) packaged with Docker and published behind an Nginx reverse proxy. Part of the DevCraft series by CS Solution (Chapter 28).
## Run it locally
```bash
docker compose up -d --build
```
Open `http://localhost:8080`. Only Nginx is published; the application itself is reachable on the Docker network only.
Check what the application sees through the proxy:
```bash
curl http://localhost:8080/whoami
```
## Try HTTPS locally
Create a self-signed certificate (the browser will warn, which is expected) and start the HTTPS service:
```bash
mkdir -p nginx/certs
docker run --rm -v "$PWD/nginx/certs:/certs" alpine:3.20 sh -c "apk add --no-cache openssl >/dev/null && openssl req -x509 -newkey rsa:2048 -nodes -days 30 -keyout /certs/emi.key -out /certs/emi.crt -subj '/CN=localhost'"
docker compose --profile tls up -d nginx-tls
curl -k https://localhost:8443/whoami
```
## Files
| File | What it is |
| --- | --- |
| `Dockerfile` | Two stages, runs as a non-root user, has a health check |
| `compose.yaml` | The application and Nginx (and an optional HTTPS Nginx) |
| `nginx/emi.conf` | The site for a real server (Nginx installed on the host) |
| `nginx/emi.compose.conf` | The same site for the compose setup |
| `nginx/emi.compose-tls.conf` | The site after HTTPS is enabled |
## Test
```bash
mvn test
```
