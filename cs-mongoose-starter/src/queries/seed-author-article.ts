import { AuthorModel, type AuthorInput } from '../models/author.model.js';
import { ArticleModel, TopicEnum, type ArticleInput, type CommentInput } from '../models/article.model.js';

export const seedAuthorAndArticle = async (): Promise<void> => {
  const authorInput: AuthorInput = {
    email: 'nimal.perera@devcraft.io',
    name: 'Nimal Perera',
    joinedOn: new Date(2023, 4, 12),
    location: {
      city: 'Colombo',
      country: 'LKA',
    },
  };

  const author = await AuthorModel.create(authorInput);

  const articleInput: ArticleInput = {
    title: 'Getting started with Mongoose',
    body: 'A practical walk-through of connecting Node.js to MongoDB.',
    readCount: 23,
    topics: [TopicEnum.NodeJS, TopicEnum.MongoDB, TopicEnum.TypeScript],
    published: false,
    author: author._id,
    comments: [
      {
        body: 'This cleared up schemas for me, thanks!',
        score: 14,
      },
    ],
  };

  const article = await ArticleModel.create(articleInput);

  // Add a comment to the existing article (insert into a child array)
  const newComment: CommentInput = {
    body: 'Following along in the next chapter too.',
    score: 32,
  };

  article.comments.push(newComment);
  const updatedArticle = await article.save();

  console.log('Created author:', author.toObject());
  console.log('Created article with comments:', updatedArticle.toObject());
};
