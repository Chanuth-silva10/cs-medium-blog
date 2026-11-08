import express, { type ErrorRequestHandler } from 'express';
import mongoose from 'mongoose';
import { MAX_LENGTHS, ReportModel } from './report.model.js';
import { closeBrowser, renderReportHtml, renderReportPdf } from './report.service.js';

const PORT = Number(process.env.PORT ?? 4600);

const app = express();

app.use(express.json());

app.get('/', (_req, res) => {
  res.json({ message: 'DevCraft Report Generator is running' });
});

// "My Project: Q4!" becomes "my-project-q4", which is safe inside a Content-Disposition header.
const toFileName = (projectName: string): string => {
  const slug = projectName
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 60);

  return slug ? `${slug}-report.pdf` : 'report.pdf';
};

// Create a report (metadata saved to MongoDB) and return it as a PDF.
app.post('/reports', async (req, res) => {
  const body = (req.body ?? {}) as Record<string, unknown>;

  for (const field of ['projectName', 'author', 'summary'] as const) {
    const value = body[field];

    if (typeof value !== 'string' || value.trim() === '') {
      return res.status(422).json({ message: `${field} is required and must be a non-empty string` });
    }
    if (value.trim().length > MAX_LENGTHS[field]) {
      return res.status(422).json({ message: `${field} must be at most ${MAX_LENGTHS[field]} characters` });
    }
  }

  const saved = await ReportModel.create({
    projectName: body.projectName,
    author: body.author,
    summary: body.summary,
  });

  const html = renderReportHtml({
    projectName: saved.projectName,
    author: saved.author,
    summary: saved.summary,
    generatedAt: saved.generatedAt ?? new Date(),
  });
  const pdf = await renderReportPdf(html);

  res.setHeader('Content-Type', 'application/pdf');
  res.setHeader('Content-Disposition', `attachment; filename="${toFileName(saved.projectName)}"`);

  return res.send(pdf);
});

app.get('/reports', async (_req, res) => {
  const reports = await ReportModel.find().sort('-createdAt').exec();

  return res.json({ data: reports });
});

// Express 5 forwards errors from async handlers here, so a failed PDF or database call never crashes the process.
// Express only treats a function as an error handler when it declares all four parameters.
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const errorHandler: ErrorRequestHandler = (error, _req, res, _next) => {
  console.error(error);

  if (error instanceof SyntaxError) {
    return res.status(400).json({ message: 'Invalid JSON body' });
  }

  return res.status(500).json({ message: 'Internal Server Error' });
};

app.use(errorHandler);

const start = async (): Promise<void> => {
  const uri = process.env.MONGODB_URI;

  if (!uri) {
    throw new Error('MONGODB_URI is not set');
  }

  await mongoose.connect(uri);

  const server = app.listen(PORT, () => console.log(`Report Generator started on http://localhost:${PORT}`));

  // Docker sends SIGTERM on "docker compose down"; close everything cleanly instead of being killed.
  const shutdown = (): void => {
    server.close(() => {
      void Promise.all([closeBrowser(), mongoose.disconnect()]).finally(() => process.exit(0));
    });
  };

  process.on('SIGTERM', shutdown);
  process.on('SIGINT', shutdown);
};

start().catch((error) => {
  console.error('Failed to start:', error);
  process.exitCode = 1;
});
