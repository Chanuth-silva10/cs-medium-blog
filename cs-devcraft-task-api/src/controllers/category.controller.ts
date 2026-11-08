import type { Request, Response } from 'express';
import { CategoryModel, type CategoryInput } from '../models/category.model.js';

export const createCategory = async (req: Request, res: Response) => {
  const { name, description, color } = req.body;

  if (!name) {
    return res.status(422).json({ message: 'The field name is required' });
  }

  const input: CategoryInput = { name, description: description ?? null, color: color ?? '#2aa4e0' };
  const created = await CategoryModel.create(input);

  return res.status(201).json({ data: created });
};

export const getAllCategories = async (_req: Request, res: Response) => {
  const categories = await CategoryModel.find().sort('-createdAt').exec();
  return res.status(200).json({ data: categories });
};

export const getCategory = async (req: Request, res: Response) => {
  const { id } = req.params;
  const category = await CategoryModel.findById(id);

  if (!category) {
    return res.status(404).json({ message: `Category with id "${id}" not found.` });
  }

  return res.status(200).json({ data: category });
};

export const updateCategory = async (req: Request, res: Response) => {
  const { id } = req.params;
  const { name, description, color } = req.body;

  const category = await CategoryModel.findById(id);

  if (!category) {
    return res.status(404).json({ message: `Category with id "${id}" not found.` });
  }

  if (!name) {
    return res.status(422).json({ message: 'The field name is required' });
  }

  await CategoryModel.updateOne({ _id: id }, { name, description, color });
  const updated = await CategoryModel.findById(id);

  return res.status(200).json({ data: updated });
};

export const deleteCategory = async (req: Request, res: Response) => {
  const { id } = req.params;
  await CategoryModel.findByIdAndDelete(id);
  return res.status(200).json({ message: 'Category deleted successfully.' });
};
