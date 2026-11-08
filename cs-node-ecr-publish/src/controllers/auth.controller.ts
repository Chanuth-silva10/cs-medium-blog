import type { Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import { UserModel, type UserInput } from '../models/user.model.js';
import { RoleModel, RoleEnum } from '../models/role.model.js';
import { generateToken } from '../helpers/jwt.js';
import type { AuthRequest } from '../middleware/auth.middleware.js';

const createUserWithRole = async (req: Request, res: Response, roleName: RoleEnum) => {
  const { fullName, email, password } = req.body;

  if (!fullName || !email || !password) {
    return res.status(422).json({ message: 'The fields fullName, email and password are required' });
  }

  const role = await RoleModel.findOne({ name: roleName });

  if (!role) {
    return res.status(500).json({ message: 'Roles are not seeded yet' });
  }

  const input: UserInput = {
    fullName,
    email,
    password: bcrypt.hashSync(password, 10),
    role: role._id,
  };

  const created = await UserModel.create(input);

  return res.status(201).json({
    data: { id: created._id, fullName: created.fullName, email: created.email, role: roleName },
  });
};

export const registerUser = (req: Request, res: Response) => createUserWithRole(req, res, RoleEnum.Member);

export const createManager = (req: Request, res: Response) => createUserWithRole(req, res, RoleEnum.Manager);

export const loginUser = async (req: Request, res: Response) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(422).json({ message: 'The fields email and password are required' });
  }

  // The generic tells TypeScript that `role` is the full role document after populate.
  const user = await UserModel.findOne({ email }).populate<{ role: { name: RoleEnum } }>('role');

  if (!user || !bcrypt.compareSync(password, user.password)) {
    return res.status(400).json({ message: 'Email or password is incorrect' });
  }

  const token = generateToken({ id: user._id.toString(), role: user.role.name });

  return res.status(200).json({ token, expiresIn: Number(process.env.JWT_EXPIRY ?? 3600) });
};

export const getMe = async (req: AuthRequest, res: Response) => {
  const user = await UserModel.findById(req.user?.id).select('-password').populate('role');

  if (!user) {
    return res.status(404).json({ message: 'User not found' });
  }

  return res.status(200).json({ data: user });
};
