import mongoose from 'mongoose';

const connectToDatabase = async (): Promise<void> => {
  const uri = process.env.MONGODB_URI;

  if (!uri) {
    throw new Error('MONGODB_URI is not set. Add it to your .env file.');
  }

  await mongoose.connect(uri);
};

const disconnectFromDatabase = async (): Promise<void> => {
  await mongoose.disconnect();
};

export { connectToDatabase, disconnectFromDatabase };
