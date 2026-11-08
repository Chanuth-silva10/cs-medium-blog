import { Schema, model, Types, type InferSchemaType, type HydratedDocument } from 'mongoose';

enum TopicEnum {
  NodeJS = 'Node.js',
  TypeScript = 'TypeScript',
  SpringBoot = 'Spring Boot',
  Docker = 'Docker',
  Kubernetes = 'Kubernetes',
  AWS = 'AWS',
  GraphQL = 'GraphQL',
  MongoDB = 'MongoDB',
}

const commentSchema = new Schema(
  {
    body: {
      type: String,
      required: true,
    },
    score: {
      type: Number,
      default: 0,
    },
  },
  {
    timestamps: true,
  },
);

const articleSchema = new Schema(
  {
    title: {
      type: String,
      required: true,
    },
    body: {
      type: String,
      required: true,
    },
    readCount: {
      type: Number,
      default: 0,
    },
    topics: {
      type: [String],
      enum: Object.values(TopicEnum),
    },
    published: {
      type: Boolean,
      required: true,
      default: false,
    },
    author: {
      type: Schema.Types.ObjectId,
      ref: 'Author',
      required: true,
      index: true,
    },
    comments: [commentSchema],
  },
  {
    collection: 'articles',
    timestamps: true,
  },
);

type Comment = InferSchemaType<typeof commentSchema>;
type CommentInput = Pick<Comment, 'body' | 'score'>;

type Article = InferSchemaType<typeof articleSchema>;
type ArticleInput = {
  title: string;
  body: string;
  readCount: number;
  topics: TopicEnum[];
  published: boolean;
  author: Types.ObjectId;
  comments: CommentInput[];
};
type ArticleDocument = HydratedDocument<Article>;

const ArticleModel = model('Article', articleSchema);

export {
  ArticleModel,
  TopicEnum,
  type Article,
  type ArticleInput,
  type ArticleDocument,
  type Comment,
  type CommentInput,
};
