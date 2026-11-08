import type { Request, Response } from 'express';
import { TaskModel, TaskStatus, TaskPriority } from '../models/task.model.js';
import type { AuthRequest } from '../middleware/auth.middleware.js';

export const createTask = async (req: AuthRequest, res: Response) => {
  const { title, description, status, priority, category, dueDate } = req.body;

  if (!title || !category) {
    return res.status(422).json({ message: 'The fields title and category are required' });
  }

  const created = await TaskModel.create({
    title,
    description: description ?? null,
    status: status ?? TaskStatus.Todo,
    priority: priority ?? TaskPriority.Medium,
    category,
    dueDate: dueDate ?? null,
    createdBy: req.user?.id ?? null,
  });

  return res.status(201).json({ data: created });
};

export const getAllTasks = async (_req: Request, res: Response) => {
  const tasks = await TaskModel.find().populate('category').sort('-createdAt').exec();
  return res.status(200).json({ data: tasks });
};

// Only the tasks created by the signed-in user.
export const getMyTasks = async (req: AuthRequest, res: Response) => {
  const tasks = await TaskModel.find({ createdBy: req.user?.id }).populate('category').sort('-createdAt').exec();
  return res.status(200).json({ data: tasks });
};

export const getTask = async (req: Request, res: Response) => {
  const { id } = req.params;
  const task = await TaskModel.findById(id).populate('category').exec();

  if (!task) {
    return res.status(404).json({ message: `Task with id "${id}" not found.` });
  }

  return res.status(200).json({ data: task });
};

export const updateTask = async (req: Request, res: Response) => {
  const { id } = req.params;
  const { title, description, status, priority, category, dueDate } = req.body;

  const task = await TaskModel.findById(id);

  if (!task) {
    return res.status(404).json({ message: `Task with id "${id}" not found.` });
  }

  await TaskModel.updateOne({ _id: id }, { title, description, status, priority, category, dueDate });
  const updated = await TaskModel.findById(id).populate('category').exec();

  return res.status(200).json({ data: updated });
};

export const deleteTask = async (req: Request, res: Response) => {
  const { id } = req.params;
  await TaskModel.findByIdAndDelete(id);
  return res.status(200).json({ message: 'Task deleted successfully.' });
};
