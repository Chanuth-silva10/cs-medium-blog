import type { NextFunction, Request, Response } from 'express';
import { decodeToken, type TokenPayload } from '../helpers/jwt.js';
import { RoleEnum } from '../models/role.model.js';

export type AuthRequest = Request & { user?: TokenPayload };

const PUBLIC_ROUTES = ['/', '/health', '/auth/register', '/auth/login'];

// Which roles may call which route. A route not listed here is open to any authenticated user.
type AclEntry = { method: string; path: RegExp; roles: RoleEnum[] };

const ROUTES_ACL: AclEntry[] = [
  { method: 'POST', path: /^\/auth\/managers$/, roles: [RoleEnum.Admin] },
  { method: 'GET', path: /^\/tasks$/, roles: [RoleEnum.Manager, RoleEnum.Admin] },
  { method: 'DELETE', path: /^\/tasks\/[^/]+$/, roles: [RoleEnum.Manager, RoleEnum.Admin] },
  { method: 'DELETE', path: /^\/categories\/[^/]+$/, roles: [RoleEnum.Manager, RoleEnum.Admin] },
  { method: 'POST', path: /^\/(start|stop)-daily-check$/, roles: [RoleEnum.Admin] },
];

// Express matches routes case-insensitively and ignores a trailing slash, so "/TASKS/" reaches the
// same handler as "/tasks". Normalise the path the same way, otherwise those variants skip the rules.
const normalizePath = (path: string): string => path.toLowerCase().replace(/\/+$/, '') || '/';

const isPublic = (path: string): boolean => PUBLIC_ROUTES.includes(path);

const isForbidden = (method: string, path: string, role: string): boolean => {
  const rule = ROUTES_ACL.find((r) => r.method === method && r.path.test(path));
  return rule ? !rule.roles.includes(role as RoleEnum) : false;
};

export const authMiddleware = async (req: AuthRequest, res: Response, next: NextFunction) => {
  const path = normalizePath(req.path);

  if (isPublic(path)) {
    return next();
  }

  // Expect the standard "Authorization: Bearer <token>" header.
  const [scheme, token] = req.headers['authorization']?.split(' ') ?? [];

  if (scheme !== 'Bearer' || !token) {
    return res.status(401).json({ message: 'Unauthorized access' });
  }

  try {
    const decoded = await decodeToken(token);

    if (!decoded?.id) {
      return res.status(401).json({ message: 'Unauthorized access' });
    }

    if (isForbidden(req.method, path, decoded.role)) {
      return res.status(403).json({ message: 'You do not have permission to access this resource' });
    }

    req.user = decoded;
    return next();
  } catch {
    return res.status(401).json({ message: 'Unauthorized access' });
  }
};
