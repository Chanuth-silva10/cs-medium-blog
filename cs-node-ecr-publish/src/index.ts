import express from 'express';
import swaggerUi from 'swagger-ui-express';
import { connectToDatabase } from './db-connection.js';
import { seedRolesAndAdmin } from './seeders/role.seeder.js';
import { apiDocumentation } from './docs/api-doc.js';
import { getJwtSecret } from './helpers/jwt.js';
import { authMiddleware } from './middleware/auth.middleware.js';
import { authRoutes } from './routes/auth.route.js';
import { categoryRoutes } from './routes/category.route.js';
import { taskRoutes } from './routes/task.route.js';
import { schedulerRoutes } from './routes/scheduler.route.js';
import { errorHandler } from './middleware/error.middleware.js';

const PORT = Number(process.env.PORT ?? 4500);

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.get('/', (_req, res) => {
  res.json({ message: `${process.env.APP_NAME ?? 'DevCraft'} Task API is running` });
});

// Interactive API documentation (public).
app.use('/documentation', swaggerUi.serve, swaggerUi.setup(apiDocumentation));

// Everything below the auth middleware requires a valid JWT, except the whitelisted public routes.
app.use(authMiddleware);

app.use('/', authRoutes());
app.use('/', categoryRoutes());
app.use('/', taskRoutes());
app.use('/', schedulerRoutes());

// Must come after every route so it can catch their errors.
app.use(errorHandler);

// Fail fast: without a secret no token can be signed or verified, so refuse to start.
getJwtSecret();

app.listen(PORT, async () => {
  await connectToDatabase();
  await seedRolesAndAdmin();
  console.log(`DevCraft Task API started on http://localhost:${PORT}`);
});
