import { Schema, model, type InferSchemaType } from 'mongoose';

const categorySchema = new Schema(
  {
    name: {
      type: String,
      required: true,
      unique: true,
    },
    description: {
      type: String,
      default: null,
    },
    color: {
      type: String,
      default: '#2aa4e0',
    },
  },
  {
    collection: 'categories',
    timestamps: true,
  },
);

type Category = InferSchemaType<typeof categorySchema>;
type CategoryInput = Pick<Category, 'name' | 'description' | 'color'>;

const CategoryModel = model('Category', categorySchema);

export { CategoryModel, type Category, type CategoryInput };
