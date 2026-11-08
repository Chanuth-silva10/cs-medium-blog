// Runs once, from the "rs-init" service. It is safe to run again: if the set already exists, it does nothing.
const config = {
  _id: 'dbrs',
  members: [
    { _id: 0, host: 'mongo1:27021', priority: 3 },
    { _id: 1, host: 'mongo2:27022', priority: 2 },
    { _id: 2, host: 'mongo3:27023', priority: 1 },
  ],
};

try {
  const status = rs.status();
  print(`Replica set ${status.set} already exists, nothing to do`);
} catch (error) {
  // 94 = NotYetInitialized: the very first start.
  if (error.codeName !== 'NotYetInitialized') {
    throw error;
  }
  printjson(rs.initiate(config));
}

// Wait until one member is elected primary, so that the next command can already write.
for (let attempt = 0; attempt < 30; attempt++) {
  if (db.hello().isWritablePrimary) {
    print(`Primary elected: ${db.hello().me}`);
    quit(0);
  }
  sleep(1000);
}
print('No primary after 30 seconds');
quit(1);
