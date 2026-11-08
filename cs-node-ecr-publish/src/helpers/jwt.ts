import jwt from 'jsonwebtoken';

export type TokenPayload = {
  id: string;
  role: string;
};

export const getJwtSecret = (): string => {
  const secret = process.env.JWT_SECRET;

  if (!secret) {
    throw new Error('JWT_SECRET is not set. Add it to your .env file.');
  }

  return secret;
};

export const generateToken = (payload: TokenPayload): string => {
  const expiresIn = Number(process.env.JWT_EXPIRY ?? 3600);

  return jwt.sign(payload, getJwtSecret(), { expiresIn });
};

export const decodeToken = (token: string): Promise<TokenPayload> => {
  const secret = getJwtSecret();

  return new Promise((resolve, reject) => {
    jwt.verify(token, secret, (err, decoded) => {
      if (err) {
        reject(err instanceof jwt.TokenExpiredError ? new Error('Token expired') : new Error('Token is not valid'));
        return;
      }
      resolve(decoded as TokenPayload);
    });
  });
};
