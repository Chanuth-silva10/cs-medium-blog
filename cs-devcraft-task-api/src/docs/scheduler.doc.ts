import { ref } from './components.js';

const jobsResponse = (description: string) => ({
  description,
  content: {
    'application/json': {
      schema: {
        type: 'object',
        properties: {
          message: { type: 'string', example: description },
          jobs: {
            type: 'array',
            items: {
              type: 'object',
              properties: {
                name: { type: 'string', example: 'flag-overdue-tasks' },
                status: { type: 'string', example: 'idle' },
              },
            },
          },
        },
      },
    },
  },
});

// Added in Chapter 8 (node-cron): start and stop the background jobs. ADMIN only.
export const schedulerPaths = {
  '/start-daily-check': {
    post: {
      tags: ['Scheduler'],
      summary: 'Start the overdue check and the weekly summary (ADMIN only)',
      operationId: 'startScheduler',
      responses: {
        '200': jobsResponse('Schedulers started'),
        '401': ref('Unauthorized'),
        '403': ref('Forbidden'),
      },
    },
  },
  '/stop-daily-check': {
    post: {
      tags: ['Scheduler'],
      summary: 'Stop the background jobs (ADMIN only)',
      operationId: 'stopScheduler',
      responses: {
        '200': jobsResponse('Schedulers stopped'),
        '401': ref('Unauthorized'),
        '403': ref('Forbidden'),
      },
    },
  },
};
