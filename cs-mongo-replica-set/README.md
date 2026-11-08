# CS Mongo Replica Set
A three member MongoDB replica set that starts with one command, using Docker Compose. Part of the DevCraft series by CS Solution (Chapter 34).
## Start
```bash
docker compose up -d --wait
bash scripts/status.sh
```
The members listen on `27021`, `27022` and `27023`. `mongo1` has the highest priority and becomes the primary.
## Connect
From another container on the same Docker network:
```
mongodb://mongo1:27021,mongo2:27022,mongo3:27023/shop?replicaSet=dbrs
```
From your computer, add this line to your hosts file (`/etc/hosts`, or `C:\Windows\System32\drivers\etc\hosts` on Windows) and use the same address:
```
127.0.0.1 mongo1 mongo2 mongo3
```
Without it, connect to one member directly: `mongodb://localhost:27021/shop?directConnection=true`.
## Try a failure
```bash
bash scripts/failover-demo.sh
```
## Stop
```bash
docker compose down        # keeps the data
docker compose down -v     # removes the data too
```
