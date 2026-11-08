import { connectToDatabase, disconnectFromDatabase } from './db-connection.js';
import { seedAuthorAndArticle } from './queries/seed-author-article.js';

const run = async (): Promise<void> => {
  await connectToDatabase();
  console.log(`Connected to MongoDB for ${process.env.APP_NAME ?? 'the app'} successfully!`);

  await seedAuthorAndArticle();

  await disconnectFromDatabase();
  console.log('Disconnected from MongoDB.');
};

run().catch((error) => {
  console.error('Something went wrong:', error);
  process.exitCode = 1;
});
