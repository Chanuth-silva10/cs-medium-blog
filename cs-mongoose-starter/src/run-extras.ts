import { connectToDatabase, disconnectFromDatabase } from './db-connection.js';
import { AuthorModel } from './models/author.model.js';
import { ArticleModel, TopicEnum } from './models/article.model.js';
import {
  explainQueries,
  pagedArticles,
  readWithLean,
  showDuplicateError,
  showOrphanArticle,
  showValidationErrors,
} from './queries/extras.js';

const run = async (): Promise<void> => {
  await connectToDatabase();

  // start from an empty database and make sure the indexes exist before we test them
  await Promise.all([AuthorModel.deleteMany({}), ArticleModel.deleteMany({})]);
  await Promise.all([AuthorModel.init(), ArticleModel.init()]);

  const author = await AuthorModel.create({ email: 'nimal.perera@devcraft.io', name: 'Nimal Perera', joinedOn: new Date() });

  for (let i = 1; i <= 7; i++) {
    await ArticleModel.create({
      title: `Post ${i}`,
      body: 'Sample body',
      readCount: i * 10,
      topics: [TopicEnum.NodeJS],
      author: author._id,
    });
  }

  await showValidationErrors();
  await showDuplicateError();
  await readWithLean();
  console.log(await pagedArticles(2, 3));
  await explainQueries(author._id);
  await showOrphanArticle();

  await disconnectFromDatabase();
};

run().catch((error) => {
  console.error('Something went wrong:', error);
  process.exitCode = 1;
});
