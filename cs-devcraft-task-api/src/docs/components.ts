// Shared OpenAPI building blocks. Paths point at these with $ref instead of repeating them.

// Small helpers that keep the path files short and readable.
export const jsonBody = (schema: string, required = true) => ({
  required,
  content: { 'application/json': { schema: { $ref: `#/components/schemas/${schema}` } } },
});

export const jsonResponse = (description: string, schema: string) => ({
  description,
  content: { 'application/json': { schema: { $ref: `#/components/schemas/${schema}` } } },
});

export const ref = (response: string) => ({ $ref: `#/components/responses/${response}` });

const message = (example: string) => ({
  type: 'object',
  properties: { message: { type: 'string', example } },
});

const objectId = { type: 'string', example: '6650f1c2a8b4e3d2c1f0a9b8' };

const schemas = {
  Category: {
    type: 'object',
    properties: {
      _id: objectId,
      name: { type: 'string', example: 'Backend' },
      description: { type: 'string', nullable: true, example: 'Server side work' },
      color: { type: 'string', example: '#2aa4e0' },
      createdAt: { type: 'string', format: 'date-time' },
      updatedAt: { type: 'string', format: 'date-time' },
    },
  },
  CategoryInput: {
    type: 'object',
    required: ['name'],
    properties: {
      name: { type: 'string', example: 'Backend' },
      description: { type: 'string', example: 'Server side work' },
      color: { type: 'string', example: '#2aa4e0' },
    },
  },
  Task: {
    type: 'object',
    properties: {
      _id: objectId,
      title: { type: 'string', example: 'Wire up the dashboard' },
      description: { type: 'string', nullable: true, example: 'Connect the charts to the API' },
      status: { type: 'string', enum: ['TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED'], example: 'TODO' },
      priority: { type: 'string', enum: ['LOW', 'MEDIUM', 'HIGH'], example: 'HIGH' },
      category: { $ref: '#/components/schemas/Category' },
      dueDate: { type: 'string', format: 'date-time', nullable: true },
      createdBy: { ...objectId, nullable: true },
      overdue: { type: 'boolean', example: false, description: 'Set by the nightly overdue check' },
      createdAt: { type: 'string', format: 'date-time' },
      updatedAt: { type: 'string', format: 'date-time' },
    },
  },
  TaskInput: {
    type: 'object',
    required: ['title', 'category'],
    properties: {
      title: { type: 'string', example: 'Wire up the dashboard' },
      description: { type: 'string', example: 'Connect the charts to the API' },
      status: { type: 'string', enum: ['TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED'], default: 'TODO' },
      priority: { type: 'string', enum: ['LOW', 'MEDIUM', 'HIGH'], default: 'MEDIUM' },
      category: objectId,
      dueDate: { type: 'string', format: 'date-time', example: '2026-12-01T00:00:00.000Z' },
    },
  },
  TaskUpdate: {
    type: 'object',
    description: 'Send only the fields you want to change.',
    properties: {
      title: { type: 'string' },
      description: { type: 'string' },
      status: { type: 'string', enum: ['TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED'], example: 'IN_PROGRESS' },
      priority: { type: 'string', enum: ['LOW', 'MEDIUM', 'HIGH'] },
      category: objectId,
      dueDate: { type: 'string', format: 'date-time' },
    },
  },
  RegisterInput: {
    type: 'object',
    required: ['fullName', 'email', 'password'],
    properties: {
      fullName: { type: 'string', example: 'Nimal Perera' },
      email: { type: 'string', format: 'email', example: 'nimal@devcraft.io' },
      password: { type: 'string', format: 'password', example: 'pass1234' },
    },
  },
  LoginInput: {
    type: 'object',
    required: ['email', 'password'],
    properties: {
      email: { type: 'string', format: 'email', example: 'nimal@devcraft.io' },
      password: { type: 'string', format: 'password', example: 'pass1234' },
    },
  },
  CreatedUser: {
    type: 'object',
    properties: {
      data: {
        type: 'object',
        properties: {
          id: objectId,
          fullName: { type: 'string', example: 'Nimal Perera' },
          email: { type: 'string', example: 'nimal@devcraft.io' },
          role: { type: 'string', enum: ['MEMBER', 'MANAGER', 'ADMIN'], example: 'MEMBER' },
        },
      },
    },
  },
  Token: {
    type: 'object',
    properties: {
      token: { type: 'string', example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' },
      expiresIn: { type: 'integer', example: 3600 },
    },
  },
  Message: message('Task deleted successfully.'),
  CategoryResponse: { type: 'object', properties: { data: { $ref: '#/components/schemas/Category' } } },
  CategoryList: {
    type: 'object',
    properties: { data: { type: 'array', items: { $ref: '#/components/schemas/Category' } } },
  },
  TaskResponse: { type: 'object', properties: { data: { $ref: '#/components/schemas/Task' } } },
  TaskList: { type: 'object', properties: { data: { type: 'array', items: { $ref: '#/components/schemas/Task' } } } },
};

// Every error in the API has the same shape: { "message": "..." }.
const errorResponse = (description: string, example: string) => ({
  description,
  content: { 'application/json': { schema: message(example) } },
});

const responses = {
  BadRequest: errorResponse('Malformed id', 'Invalid value "abc" for _id'),
  Unauthorized: errorResponse('Missing, invalid or expired token', 'Unauthorized access'),
  Forbidden: errorResponse('Your role is not allowed to do this', 'You do not have permission to access this resource'),
  NotFound: errorResponse('Nothing matches this id', 'Task with id "..." not found.'),
  Conflict: errorResponse('A unique value is already taken', 'A record with the same unique value already exists'),
  ValidationError: errorResponse('Required fields are missing or invalid', 'The field name is required'),
  ServerError: errorResponse('Unexpected error', 'Internal Server Error'),
};

const parameters = {
  Id: {
    name: 'id',
    in: 'path',
    required: true,
    description: 'The MongoDB id of the resource',
    schema: { type: 'string', example: '6650f1c2a8b4e3d2c1f0a9b8' },
  },
};

const securitySchemes = {
  bearerAuth: { type: 'http', scheme: 'bearer', bearerFormat: 'JWT' },
};

export const components = { schemas, responses, parameters, securitySchemes };
