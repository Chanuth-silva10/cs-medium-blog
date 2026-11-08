import { Router } from 'express';
import {
  createTask,
  deleteTask,
  getAllTasks,
  getMyTasks,
  getTask,
  updateTask,
} from '../controllers/task.controller.js';

export const taskRoutes = (): Router => {
  const router = Router();

  router.post('/tasks', createTask);
  router.get('/tasks', getAllTasks);
  // Must be registered before "/tasks/:id", otherwise "me" is treated as a task id.
  router.get('/tasks/me', getMyTasks);
  router.get('/tasks/:id', getTask);
  router.patch('/tasks/:id', updateTask);
  router.delete('/tasks/:id', deleteTask);

  return router;
};
