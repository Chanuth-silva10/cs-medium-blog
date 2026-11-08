# CS Docker Swarm Stack
Run a service on Docker Swarm: replicas, a routing mesh, resource limits, health checks, rolling updates and an automatic rollback. The service is a tiny Node.js 22 web server that tells which container answered and which version it is. Part of the DevCraft series by CS Solution (Chapter 32).
## Try it on one machine
You need Docker. A single computer can be a swarm of one node.
```bash
docker swarm init
docker run -d --name registry -p 5000:5000 registry:2
for v in 1.0.0 2.0.0; do
  docker build -t localhost:5000/devcraft/whoami:$v --build-arg APP_VERSION=$v .
  docker push localhost:5000/devcraft/whoami:$v
done
docker build -t localhost:5000/devcraft/whoami:3.0.0 --build-arg APP_VERSION=3.0.0 --build-arg BROKEN=true .
docker push localhost:5000/devcraft/whoami:3.0.0
docker stack deploy -c stack.yaml devcraft
curl http://localhost:8090/
```
Update to version 2: `IMAGE=localhost:5000/devcraft/whoami:2.0.0 docker stack deploy -c stack.yaml devcraft`. Update to the broken version 3 and watch Swarm go back to the previous one.
Clean up with `docker stack rm devcraft` and `docker swarm leave --force`.
## Files
| File | What it is |
| --- | --- |
| `app/server.mjs` | The web server: `/` and `/health` |
| `Dockerfile` | Node 22 on Alpine, non-root, with a health check |
| `stack.yaml` | The Swarm stack definition |
