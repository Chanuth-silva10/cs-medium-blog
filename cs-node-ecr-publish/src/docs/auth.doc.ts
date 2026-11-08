import { jsonBody, jsonResponse, ref } from './components.js';

export const authPaths = {
  '/auth/register': {
    post: {
      tags: ['Authentication'],
      summary: 'Create a member account',
      operationId: 'register',
      security: [],
      requestBody: jsonBody('RegisterInput'),
      responses: {
        '201': jsonResponse('Account created with the MEMBER role', 'CreatedUser'),
        '409': ref('Conflict'),
        '422': ref('ValidationError'),
      },
    },
  },
  '/auth/login': {
    post: {
      tags: ['Authentication'],
      summary: 'Log in and receive a JWT',
      operationId: 'login',
      security: [],
      requestBody: jsonBody('LoginInput'),
      responses: {
        '200': jsonResponse('A signed JWT', 'Token'),
        '400': { description: 'Email or password is incorrect' },
        '422': ref('ValidationError'),
      },
    },
  },
  '/auth/me': {
    get: {
      tags: ['Authentication'],
      summary: 'Get the signed-in user',
      operationId: 'getMe',
      responses: {
        '200': { description: 'The current user, without the password' },
        '401': ref('Unauthorized'),
      },
    },
  },
  '/auth/managers': {
    post: {
      tags: ['Authentication'],
      summary: 'Create a manager account (ADMIN only)',
      operationId: 'createManager',
      requestBody: jsonBody('RegisterInput'),
      responses: {
        '201': jsonResponse('Account created with the MANAGER role', 'CreatedUser'),
        '401': ref('Unauthorized'),
        '403': ref('Forbidden'),
        '409': ref('Conflict'),
        '422': ref('ValidationError'),
      },
    },
  },
};
