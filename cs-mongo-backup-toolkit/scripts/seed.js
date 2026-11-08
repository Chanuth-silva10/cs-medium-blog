// Run with: mongosh "$SOURCE_URI" --file scripts/seed.js
// Creates the database "prolang" with two collections and a unique index.
const prolang = db.getSiblingDB('prolang');
prolang.dropDatabase();

const languages = [
  ['JavaScript', 1995, 'multi-paradigm'], ['Java', 1995, 'object-oriented'], ['Python', 1991, 'multi-paradigm'],
  ['C', 1972, 'procedural'], ['Go', 2009, 'concurrent'], ['Rust', 2010, 'systems'],
  ['TypeScript', 2012, 'multi-paradigm'], ['Kotlin', 2011, 'object-oriented'], ['PHP', 1995, 'scripting'],
  ['Ruby', 1995, 'object-oriented'],
].map(([name, years, paradigm]) => ({ name, years, paradigm, createdAt: new Date('2026-01-01T00:00:00Z') }));

prolang.languages.insertMany(languages);
prolang.languages.createIndex({ name: 1 }, { unique: true });

// A larger collection, so that the backup has some weight.
const bulk = [];
for (let i = 1; i <= 20000; i++) {
  bulk.push({ ref: `post-${i}`, title: `Post number ${i}`, views: i % 997, tags: ['db', i % 2 ? 'odd' : 'even'] });
}
prolang.posts.insertMany(bulk);
prolang.posts.createIndex({ views: -1 });

print(`languages: ${prolang.languages.countDocuments()}, posts: ${prolang.posts.countDocuments()}`);
