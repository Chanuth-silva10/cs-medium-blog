import mongoose from 'mongoose';
import { AuthorModel } from '../models/author.model.js';
import { ArticleModel } from '../models/article.model.js';

// 1. What the schema protects you from: a bad article is refused before it reaches the database.
export const showValidationErrors = async (): Promise<void> => {
  try {
    await ArticleModel.create({ title: 'No body', topics: ['Cobol'] });
  } catch (error) {
    const validation = error as mongoose.Error.ValidationError;
    console.log(validation.name); // ValidationError
    console.log(Object.keys(validation.errors)); // [ 'author', 'body', 'topics.0' ]
    console.log(validation.message);
  }
};

// 2. The unique index on email: a second author with the same email is refused by MongoDB itself.
export const showDuplicateError = async (): Promise<void> => {
  const input = { email: 'duplicate@devcraft.io', name: 'First', joinedOn: new Date() };
  await AuthorModel.create(input);

  try {
    await AuthorModel.create({ ...input, name: 'Second' });
  } catch (error) {
    const duplicate = error as { code?: number; keyValue?: Record<string, unknown> };
    if (duplicate.code === 11000) {
      console.log('That email is already taken:', duplicate.keyValue);
    } else {
      throw error;
    }
  }
};

// 3. lean() returns plain objects: lighter and faster when you only read.
export const readWithLean = async (): Promise<void> => {
  const hydrated = await ArticleModel.findOne();
  const plain = await ArticleModel.findOne().lean();

  console.log(typeof hydrated?.save); // function
  console.log(typeof (plain as { save?: unknown } | null)?.save); // undefined
};

// 4. Paging: count first, then skip and limit.
export const pagedArticles = async (page: number, pageSize: number) => {
  const total = await ArticleModel.countDocuments();
  const items = await ArticleModel.find()
    .sort({ readCount: -1 })
    .skip((page - 1) * pageSize)
    .limit(pageSize)
    .select('title readCount');

  return { page, pageSize, total, pages: Math.ceil(total / pageSize), items };
};

// 5. Does the index help? explain() tells you how MongoDB ran the query.
export const explainQueries = async (authorId: mongoose.Types.ObjectId): Promise<void> => {
  type Plan = { queryPlanner: { winningPlan: { stage: string; inputStage?: { stage: string } } } };

  const byAuthor = (await ArticleModel.find({ author: authorId }).explain('queryPlanner')) as unknown as Plan;
  const byTitle = (await ArticleModel.find({ title: 'Post 3' }).explain('queryPlanner')) as unknown as Plan;

  console.log('by author:', byAuthor.queryPlanner.winningPlan.inputStage?.stage); // IXSCAN
  console.log('by title:', byTitle.queryPlanner.winningPlan.stage); // COLLSCAN
};

// 6. A reference can point at nothing: after deleting the author, populate gives back null.
export const showOrphanArticle = async (): Promise<void> => {
  const author = await AuthorModel.create({ email: 'gone@devcraft.io', name: 'Gone', joinedOn: new Date() });
  await ArticleModel.create({ title: 'Orphan', body: 'Written by someone who left', author: author._id });
  await AuthorModel.deleteOne({ _id: author._id });

  const article = await ArticleModel.findOne({ title: 'Orphan' }).populate('author');
  console.log('author after populate:', article?.author); // null
};
