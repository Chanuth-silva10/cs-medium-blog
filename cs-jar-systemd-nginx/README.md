# CS JAR Systemd Nginx
A loan EMI calculator (Spring Boot 4, Java 21) deployed as a plain JAR on a Linux server: a systemd service keeps it running, Nginx exposes it, and a small script ships new versions with an automatic rollback. Part of the DevCraft series by CS Solution (Chapter 33).
## Run it locally
```bash
mvn spring-boot:run
```
Open `http://localhost:7570`.
## Package
```bash
mvn clean package
```
The JAR is `target/emi.jar`.
## Deploy files
| File | Where it goes on the server |
| --- | --- |
| `deploy/emi.service` | `/etc/systemd/system/emi.service` |
| `deploy/emi.nginx.conf` | `/etc/nginx/sites-available/<your domain>` |
| `deploy/deploy.sh` | stays on your computer |
Ship a new version:
```bash
SERVER=deploy@203.0.113.10 deploy/deploy.sh target/emi.jar
```
## Test
```bash
mvn verify
```
