import type { CodegenConfig } from '@graphql-codegen/cli';

const config: CodegenConfig = {
  overwrite: true,
  schema: 'src/schema.graphql',
  generates: {
    'src/generated/types.ts': {
      plugins: ['typescript', 'typescript-resolvers'],
      config: {
        // Without this mapping every DateTime would be typed as "any".
        scalars: {
          DateTime: 'Date',
        },
        useTypeImports: true,
      },
    },
  },
};

export default config;
