# The AWS base image already contains the Lambda Runtime Interface Client (it talks to the Lambda
# service) and the Runtime Interface Emulator (it lets you test locally), so we write neither.
FROM public.ecr.aws/lambda/nodejs:22

# pg_dump comes with the PostgreSQL client. Keep its major version equal to, or higher than, the
# major version of your database server.
RUN microdnf install -y postgresql16 && microdnf clean all

# ${LAMBDA_TASK_ROOT} is /var/task, the working folder of the image.
COPY package.json package-lock.json ./
RUN npm ci --omit=dev
COPY src/ ./

# file name without extension, then the exported function: handler.mjs exports "handler".
CMD ["handler.handler"]
