import { AuthorModel } from '../models/author.model.js';
import { ArticleModel, TopicEnum } from '../models/article.model.js';

export const retrieveData = async (): Promise<void> => {
  // All authors
  const allAuthors = await AuthorModel.find();

  if (allAuthors.length === 0) {
    console.log('No authors found. Run the seed first.');
    return;
  }

  const first = allAuthors[0];

  // By id
  await AuthorModel.findById(first._id);

  // By email
  await AuthorModel.findOne({ email: first.email });

  // Only selected fields
  await AuthorModel.findOne({ email: first.email }, 'name email');

  // Authors in Sri Lanka, newest first
  const sriLankanAuthors = await AuthorModel.find({ 'location.country': 'LKA' }).sort('-createdAt');

  // All articles of an author, with the author document populated
  const withAuthor = await ArticleModel.find({ author: sriLankanAuthors[0]?._id }).populate('author').exec();

  // Same, but only the author's name
  await ArticleModel.find({ author: sriLankanAuthors[0]?._id }).populate('author', 'name').exec();

  // Articles with more than 20 reads, newest first
  const popular = await ArticleModel.find({ readCount: { $gt: 20 } }).sort('-createdAt');

  // Articles tagged with both Node.js and MongoDB, read count between 20 and 30
  const byTopics = await ArticleModel.find({
    topics: { $all: [TopicEnum.NodeJS, TopicEnum.MongoDB] },
    readCount: { $gte: 20, $lte: 30 },
  });

  console.log('Articles with populated author:', withAuthor.length);
  console.log('Popular articles:', popular.length);
  console.log('Articles by topic match:', byTopics.length);
};
