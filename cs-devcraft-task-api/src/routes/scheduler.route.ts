import { Router } from 'express';
import { jobs } from '../scheduler.js';

const status = () => jobs.map((job) => ({ name: job.name, status: job.getStatus() }));

export const schedulerRoutes = (): Router => {
  const router = Router();

  router.post('/start-daily-check', async (_req, res) => {
    for (const job of jobs) await job.start();
    return res.json({ message: 'Schedulers started', jobs: status() });
  });

  router.post('/stop-daily-check', async (_req, res) => {
    for (const job of jobs) await job.stop();
    return res.json({ message: 'Schedulers stopped', jobs: status() });
  });

  return router;
};
