import { Router } from 'express';
import {
  createCategory,
  deleteCategory,
  getAllCategories,
  getCategory,
  updateCategory,
} from '../controllers/category.controller.js';

export const categoryRoutes = (): Router => {
  const router = Router();

  router.post('/categories', createCategory);
  router.get('/categories', getAllCategories);
  router.get('/categories/:id', getCategory);
  router.put('/categories/:id', updateCategory);
  router.delete('/categories/:id', deleteCategory);

  return router;
};
