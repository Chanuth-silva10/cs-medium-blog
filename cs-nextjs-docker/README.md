# CS Next.js Docker
A small Next.js 16 application (App Router, TypeScript) packaged as a lean, non-root Docker image with the standalone output. Part of the DevCraft series by CS Solution (Chapter 36).
## Run without Docker
```bash
npm install
npm run dev
```
Open `http://localhost:3000`.
## Build and run the image
```bash
docker build -t cs-nextjs:1.0.0 --build-arg SITE_NAME="DevCraft Notes" .
docker run --rm -p 8080:3000 -e APP_ENV_LABEL=staging cs-nextjs:1.0.0
```
Open `http://localhost:8080`. `SITE_NAME` is fixed when the image is built; `APP_ENV_LABEL` is read when the container runs.
