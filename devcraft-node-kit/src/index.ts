type AppInfo = {
  name: string;
  version: string;
  startedAt: Date;
};

const readAppInfo = (): AppInfo => {
  return {
    name: process.env.APP_NAME ?? 'unknown-app',
    version: process.env.APP_VERSION ?? '0.0.0',
    startedAt: new Date(),
  };
};

const printBanner = (app: AppInfo): void => {
  console.log('--------------------------------------');
  console.log(`  ${app.name} v${app.version}`);
  console.log(`  Booted at ${app.startedAt.toISOString()}`);
  console.log('--------------------------------------');
};

const app = readAppInfo();
printBanner(app);
