import { Schema, model, type InferSchemaType } from 'mongoose';

const userSchema = new Schema(
  {
    fullName: {
      type: String,
      required: true,
    },
    email: {
      type: String,
      required: true,
      unique: true,
    },
    password: {
      type: String,
      required: true,
    },
    role: {
      type: Schema.Types.ObjectId,
      ref: 'Role',
      required: true,
      index: true,
    },
  },
  {
    collection: 'users',
    timestamps: true,
  },
);

type User = InferSchemaType<typeof userSchema>;
type UserInput = Pick<User, 'fullName' | 'email' | 'password' | 'role'>;

const UserModel = model('User', userSchema);

export { UserModel, type User, type UserInput };
