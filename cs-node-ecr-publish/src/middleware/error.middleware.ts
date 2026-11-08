import type { NextFunction, Request, Response } from 'express';
import mongoose from 'mongoose';

// Express 5 forwards errors thrown in async handlers here, so one bad request never crashes the server.
// It must be registered after all the routes. Express recognises an error handler by its 4 arguments,
// so `_next` has to stay in the signature even though it is not used.
// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const errorHandler = (err: unknown, _req: Request, res: Response, _next: NextFunction) => {
  if (err instanceof mongoose.Error.CastError) {
    return res.status(400).json({ message: `Invalid value "${err.value}" for ${err.path}` });
  }

  if (err instanceof mongoose.Error.ValidationError) {
    return res.status(422).json({ message: err.message });
  }

  if (typeof err === 'object' && err !== null && 'code' in err && err.code === 11000) {
    return res.status(409).json({ message: 'A record with the same unique value already exists' });
  }

  console.error(err);
  return res.status(500).json({ message: 'Internal Server Error' });
};
