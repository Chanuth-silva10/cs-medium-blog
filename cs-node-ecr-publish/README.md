# CS Node ECR Publish
The DevCraft Task API (Node.js 22, TypeScript, Express, MongoDB) packaged as a Docker image and pushed to Amazon ECR from GitHub Actions, with OpenID Connect instead of stored AWS keys. Part of the DevCraft series by CS Solution (Chapter 31).
## Build and run the image
```bash
docker build -t devcraft/task-api:local .
docker run --rm -p 4500:4500 --env-file .env devcraft/task-api:local
```
Copy `.env.example` to `.env` first and point `MONGODB_URI` at a MongoDB you can reach.
## The pipeline
| Event | Job | What it does |
| --- | --- | --- |
| Pull request | `build` | Builds the image, pushes nothing |
| Push on `main` | `publish` | Logs in to AWS with OIDC, pushes `devcraft/task-api:sha-<7 characters>` to ECR |
Settings in the GitHub repository (Settings, Secrets and variables, Actions, Variables): `AWS_ROLE_ARN` and `AWS_REGION`. Neither is secret, and no AWS key is stored anywhere.
## AWS side
`aws/trust-policy.json` lets the GitHub repository assume the role, only from the `main` branch. `aws/ecr-push-policy.json` lets the role push to one repository and nothing else. The values in both files (account `123456789012`, region `eu-west-2`) are placeholders: replace them with yours.
