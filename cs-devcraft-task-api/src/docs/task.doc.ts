import { jsonBody, jsonResponse, ref } from './components.js';

const idParam = { $ref: '#/components/parameters/Id' };

export const taskPaths = {
  '/tasks': {
    post: {
      tags: ['Tasks'],
      summary: 'Create a task',
      operationId: 'createTask',
      requestBody: jsonBody('TaskInput'),
      responses: {
        '201': jsonResponse('The new task', 'TaskResponse'),
        '401': ref('Unauthorized'),
        '422': ref('ValidationError'),
      },
    },
    get: {
      tags: ['Tasks'],
      summary: 'List every task (MANAGER or ADMIN)',
      operationId: 'listTasks',
      responses: {
        '200': jsonResponse('All tasks, each with its category', 'TaskList'),
        '401': ref('Unauthorized'),
        '403': ref('Forbidden'),
      },
    },
  },
  '/tasks/me': {
    get: {
      tags: ['Tasks'],
      summary: 'List the tasks I created',
      operationId: 'listMyTasks',
      responses: {
        '200': jsonResponse('Tasks created by the signed-in user', 'TaskList'),
        '401': ref('Unauthorized'),
      },
    },
  },
  '/tasks/{id}': {
    parameters: [idParam],
    get: {
      tags: ['Tasks'],
      summary: 'Get one task',
      operationId: 'getTask',
      responses: {
        '200': jsonResponse('The task with its category', 'TaskResponse'),
        '400': ref('BadRequest'),
        '401': ref('Unauthorized'),
        '404': ref('NotFound'),
      },
    },
    patch: {
      tags: ['Tasks'],
      summary: 'Update part of a task',
      operationId: 'updateTask',
      requestBody: jsonBody('TaskUpdate'),
      responses: {
        '200': jsonResponse('The updated task', 'TaskResponse'),
        '400': ref('BadRequest'),
        '401': ref('Unauthorized'),
        '404': ref('NotFound'),
        '422': ref('ValidationError'),
      },
    },
    delete: {
      tags: ['Tasks'],
      summary: 'Delete a task (MANAGER or ADMIN)',
      operationId: 'deleteTask',
      responses: {
        '200': jsonResponse('Deleted', 'Message'),
        '400': ref('BadRequest'),
        '401': ref('Unauthorized'),
        '403': ref('Forbidden'),
      },
    },
  },
};
