import mongoose from 'mongoose';

export const MAX_LENGTHS = { projectName: 100, author: 100, summary: 5000 } as const;

const reportSchema = new mongoose.Schema(
  {
    projectName: { type: String, required: true, trim: true, maxlength: MAX_LENGTHS.projectName },
    author: { type: String, required: true, trim: true, maxlength: MAX_LENGTHS.author },
    summary: { type: String, required: true, trim: true, maxlength: MAX_LENGTHS.summary },
    generatedAt: { type: Date, default: () => new Date() },
  },
  { collection: 'reports', timestamps: true },
);

export type ReportInput = {
  projectName: string;
  author: string;
  summary: string;
};

export const ReportModel = mongoose.model('Report', reportSchema);
