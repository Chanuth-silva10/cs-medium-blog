import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import prettier from 'eslint-config-prettier';

export default tseslint.config(
  {
    ignores: ['dist', 'node_modules', 'eslint.config.mjs'],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['src/**/*.ts'],
    languageOptions: {
      ecmaVersion: 2023,
      sourceType: 'module',
      parserOptions: {
        project: './tsconfig.json',
      },
    },
    rules: {
      'no-var': 'error',
      'prefer-const': 'error',
      semi: ['error', 'always'],
      indent: ['error', 2, { SwitchCase: 1 }],
      'no-multi-spaces': 'error',
      'space-in-parens': ['error', 'never'],
      'no-multiple-empty-lines': ['error', { max: 1 }],
    },
  },
  prettier,
);
