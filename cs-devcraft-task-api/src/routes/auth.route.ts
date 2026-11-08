import { Router } from 'express';
import { createManager, getMe, loginUser, registerUser } from '../controllers/auth.controller.js';

export const authRoutes = (): Router => {
  const router = Router();

  router.post('/auth/register', registerUser);
  router.post('/auth/login', loginUser);
  router.post('/auth/managers', createManager);
  router.get('/auth/me', getMe);

  return router;
};
