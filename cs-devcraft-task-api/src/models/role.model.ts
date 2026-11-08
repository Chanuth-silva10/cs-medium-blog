import { Schema, model, type InferSchemaType } from 'mongoose';

export enum RoleEnum {
  Member = 'MEMBER',
  Manager = 'MANAGER',
  Admin = 'ADMIN',
}

const roleSchema = new Schema(
  {
    name: {
      type: String,
      enum: Object.values(RoleEnum),
      required: true,
      unique: true,
    },
    description: {
      type: String,
      default: null,
    },
  },
  {
    collection: 'roles',
    timestamps: true,
  },
);

type Role = InferSchemaType<typeof roleSchema>;

const RoleModel = model('Role', roleSchema);

export { RoleModel, type Role };
