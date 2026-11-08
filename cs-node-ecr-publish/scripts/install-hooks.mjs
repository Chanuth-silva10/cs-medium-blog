// Installs the git hooks with husky. Husky must run from the git root, but this project can be
// the root of its own repo or a subfolder of a bigger one (like the cs-medium-blog monorepo).
import { execSync } from 'node:child_process';
import { relative } from 'node:path';
import husky from 'husky';

let root;
try {
  root = execSync('git rev-parse --show-toplevel', { stdio: ['ignore', 'pipe', 'ignore'] }).toString().trim();
} catch {
  // Not a git checkout (for example inside a Docker build), so there is nothing to install.
  process.exit(0);
}

const projectDir = relative(root, process.cwd()).replaceAll('\\', '/');
process.chdir(root);
console.log(husky(projectDir ? `${projectDir}/.husky` : '.husky') || 'Git hooks installed');
