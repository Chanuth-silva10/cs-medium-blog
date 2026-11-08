import { Schema, model, type InferSchemaType, type HydratedDocument } from 'mongoose';

const authorSchema = new Schema(
  {
    email: {
      type: String,
      required: true,
      unique: true,
      index: true,
    },
    name: {
      type: String,
      required: true,
    },
    joinedOn: {
      type: Date,
      required: true,
    },
    location: {
      country: String,
      city: String,
    },
  },
  {
    timestamps: true,
    collection: 'authors',
  },
);

type Author = InferSchemaType<typeof authorSchema>;
type AuthorInput = Pick<Author, 'email' | 'name' | 'joinedOn' | 'location'>;
type AuthorDocument = HydratedDocument<Author>;

const AuthorModel = model('Author', authorSchema);

export { AuthorModel, type Author, type AuthorInput, type AuthorDocument };
