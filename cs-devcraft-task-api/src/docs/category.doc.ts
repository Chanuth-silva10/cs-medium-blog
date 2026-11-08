import { jsonBody, jsonResponse, ref } from './components.js';

const idParam = { $ref: '#/components/parameters/Id' };

export const categoryPaths = {
  '/categories': {
    post: {
      tags: ['Categories'],
      summary: 'Create a category',
      operationId: 'createCategory',
      requestBody: jsonBody('CategoryInput'),
      responses: {
        '201': jsonResponse('The new category', 'CategoryResponse'),
        '401': ref('Unauthorized'),
        '409': ref('Conflict'),
        '422': ref('ValidationError'),
      },
    },
    get: {
      tags: ['Categories'],
      summary: 'List all categories, newest first',
      operationId: 'listCategories',
      responses: {
        '200': jsonResponse('All categories', 'CategoryList'),
        '401': ref('Unauthorized'),
      },
    },
  },
  '/categories/{id}': {
    // Shared by every operation on this path.
    parameters: [idParam],
    get: {
      tags: ['Categories'],
      summary: 'Get one category',
      operationId: 'getCategory',
      responses: {
        '200': jsonResponse('The category', 'CategoryResponse'),
        '400': ref('BadRequest'),
        '401': ref('Unauthorized'),
        '404': ref('NotFound'),
      },
    },
    put: {
      tags: ['Categories'],
      summary: 'Replace a category',
      operationId: 'updateCategory',
      requestBody: jsonBody('CategoryInput'),
      responses: {
        '200': jsonResponse('The updated category', 'CategoryResponse'),
        '400': ref('BadRequest'),
        '401': ref('Unauthorized'),
        '404': ref('NotFound'),
        '422': ref('ValidationError'),
      },
    },
    delete: {
      tags: ['Categories'],
      summary: 'Delete a category (MANAGER or ADMIN)',
      operationId: 'deleteCategory',
      responses: {
        '200': jsonResponse('Deleted', 'Message'),
        '400': ref('BadRequest'),
        '401': ref('Unauthorized'),
        '403': ref('Forbidden'),
      },
    },
  },
};
