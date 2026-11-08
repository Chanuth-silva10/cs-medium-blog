import cron from 'node-cron';
import { TaskModel, TaskStatus } from './models/task.model.js';

const TIMEZONE = 'Asia/Colombo';
const OPEN_STATUSES = [TaskStatus.Todo, TaskStatus.InProgress];

// Flag unfinished tasks whose due date has passed, and clear the flag on tasks that are no longer late
// (finished, cancelled, or given a new due date).
export const flagOverdueTasks = async (): Promise<void> => {
  const now = new Date();

  const flagged = await TaskModel.updateMany(
    { dueDate: { $lt: now }, status: { $in: OPEN_STATUSES }, overdue: false },
    { overdue: true },
  );
  const cleared = await TaskModel.updateMany(
    { overdue: true, $or: [{ status: { $nin: OPEN_STATUSES } }, { dueDate: { $gte: now } }, { dueDate: null }] },
    { overdue: false },
  );

  console.log(`[cron] overdue check: ${flagged.modifiedCount} flagged, ${cleared.modifiedCount} cleared`);
};

// A weekly snapshot of the board. Logged for now; an email digest is a natural next step.
export const weeklySummary = async (): Promise<void> => {
  const open = await TaskModel.countDocuments({ status: { $in: OPEN_STATUSES } });
  const overdue = await TaskModel.countDocuments({ overdue: true });
  const done = await TaskModel.countDocuments({ status: TaskStatus.Done });

  console.log(`[cron] weekly summary: ${open} open, ${overdue} overdue, ${done} done`);
};

// createTask builds a job without starting it, so the API decides when the jobs run.
export const overdueJob = cron.createTask('0 0 * * *', flagOverdueTasks, {
  name: 'flag-overdue-tasks',
  timezone: TIMEZONE,
  noOverlap: true,
});

export const weeklySummaryJob = cron.createTask('0 8 * * MON', weeklySummary, {
  name: 'weekly-summary',
  timezone: TIMEZONE,
  noOverlap: true,
});

export const jobs = [overdueJob, weeklySummaryJob];
