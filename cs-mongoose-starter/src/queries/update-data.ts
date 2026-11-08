import { AuthorModel } from '../models/author.model.js';
import { ArticleModel, TopicEnum } from '../models/article.model.js';

export const updateData = async (): Promise<void> => {
  const allAuthors = await AuthorModel.find();
  const author = await AuthorModel.findById(allAuthors[0]?._id);

  if (author) {
    author.location = {
      country: 'USA',
      city: 'San Francisco',
    };
    const updatedAuthor = await author.save();
    console.log('Updated author location:', updatedAuthor.location);
  }

  const article = await ArticleModel.findOne({ author: author?._id });

  if (article) {
    // { new: true } returns the modified document instead of the original
    const updatedArticle = await ArticleModel.findOneAndUpdate(
      { _id: article._id },
      { published: true, topics: [TopicEnum.GraphQL, TopicEnum.AWS, TopicEnum.Docker] },
      { new: true },
    );
    console.log('Updated article:', updatedArticle?.title, updatedArticle?.published);
  }
};
