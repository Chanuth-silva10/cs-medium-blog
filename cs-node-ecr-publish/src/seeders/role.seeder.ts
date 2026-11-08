import bcrypt from 'bcryptjs';
import { RoleModel, RoleEnum } from '../models/role.model.js';
import { UserModel } from '../models/user.model.js';

const ROLES = [
  { name: RoleEnum.Member, description: 'Creates tasks and sees the tasks they created' },
  { name: RoleEnum.Manager, description: 'Sees every task and can delete tasks and categories' },
  { name: RoleEnum.Admin, description: 'Full access, including creating manager accounts' },
];

// Runs at startup. Creates the roles and a first admin account if they don't exist yet,
// so it is safe to run on every boot.
export const seedRolesAndAdmin = async (): Promise<void> => {
  for (const role of ROLES) {
    await RoleModel.updateOne({ name: role.name }, { $setOnInsert: role }, { upsert: true });
  }

  const email = process.env.ADMIN_EMAIL;
  const password = process.env.ADMIN_PASSWORD;

  if (!email || !password) {
    console.warn('ADMIN_EMAIL or ADMIN_PASSWORD is not set, skipping the admin account.');
    return;
  }

  const adminRole = await RoleModel.findOne({ name: RoleEnum.Admin });
  const existingAdmin = await UserModel.findOne({ email });

  if (adminRole && !existingAdmin) {
    await UserModel.create({
      fullName: 'DevCraft Admin',
      email,
      password: bcrypt.hashSync(password, 10),
      role: adminRole._id,
    });
    console.log(`Seeded admin account: ${email}`);
  }
};
