import mongoose, { ConnectionOptions } from 'mongoose';
import dotenv from 'dotenv';

mongoose.Promise = global.Promise;
dotenv.config();

const { DB_HOST, DB_NAME, DB_PORT, DB_USER, DB_PASS } = process.env;

const connectToDatabase = async (): Promise<void> => {
  const options: ConnectionOptions = { useNewUrlParser: true, useFindAndModify: false, useCreateIndex: true, useUnifiedTopology: true };

  await mongoose.connect(`mongodb://${DB_HOST}:${DB_PORT}/${DB_NAME}`, options);
};

export { connectToDatabase };
