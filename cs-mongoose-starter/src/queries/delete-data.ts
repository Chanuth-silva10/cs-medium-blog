import { AuthorModel } from '../models/author.model.js';
import { ArticleModel } from '../models/article.model.js';

export const deleteData = async (): Promise<void> => {
  // Delete an author by email
  await AuthorModel.findOneAndDelete({ email: 'nimal.perera@devcraft.io' });

  // Delete a single article by a filter
  await ArticleModel.deleteOne({ title: 'Getting started with Mongoose' });

  // Delete every article whose title contains "how to"
  await ArticleModel.deleteMany({ title: /how to/i });

  const remaining = await ArticleModel.find().populate('author').exec();
  console.log('Remaining articles:', remaining.length);
};
