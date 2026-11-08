# CS GraphQL Custom Scalars
A GraphQL API with Node.js 22, TypeScript and Apollo Server 5, with a strict custom `DateTime` scalar and generated resolver types. Part of the DevCraft series by CS Solution (Chapter 37).
## Run
```bash
npm install
npm start
```
The API starts on `http://localhost:7580`. Open it in a browser to use Apollo Sandbox.
## Scripts
| Command | What it does |
| --- | --- |
| `npm start` | Starts the server |
| `npm run codegen` | Regenerates `src/generated/types.ts` from `src/schema.graphql` |
| `npm run build` | Type-checks the project |
| `npm test` | Runs the 29 tests |
## Try it
```graphql
mutation {
  registerMember(input: { name: "Sara Silva", email: "sara@example.com", joinedAt: "2026-11-03T09:30:00+05:30" }) {
    id
    joinedAt
  }
}
```
The answer has `joinedAt` as `2026-11-03T04:00:00.000Z`.
