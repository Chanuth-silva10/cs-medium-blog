import { components } from './components.js';
import { authPaths } from './auth.doc.js';
import { categoryPaths } from './category.doc.js';
import { taskPaths } from './task.doc.js';
import { schedulerPaths } from './scheduler.doc.js';

export const apiDocumentation = {
  openapi: '3.0.3',
  info: {
    version: '1.0.0',
    title: 'DevCraft Task API',
    description: 'Project and task management REST API for the DevCraft platform.',
    contact: { name: 'CS Solution', email: 'hello@devcraft.io' },
    license: { name: 'MIT', url: 'https://opensource.org/licenses/MIT' },
  },
  servers: [{ url: `http://localhost:${process.env.PORT ?? 4500}`, description: 'Local server' }],
  tags: [
    { name: 'Authentication', description: 'Accounts and tokens' },
    { name: 'Categories', description: 'Group tasks by area' },
    { name: 'Tasks', description: 'The work itself' },
    { name: 'Scheduler', description: 'Background jobs' },
  ],
  // Every operation needs a JWT by default. Public ones override this with `security: []`.
  security: [{ bearerAuth: [] }],
  paths: {
    ...authPaths,
    ...categoryPaths,
    ...taskPaths,
    ...schedulerPaths,
  },
  components,
};
