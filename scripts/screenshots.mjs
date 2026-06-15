// Captures UI screenshots of the running FERKO app for the README/docs.
//
// Usage (against a running instance on http://localhost:8080):
//   docker run --rm -v "$PWD":/work -w /work \
//     mcr.microsoft.com/playwright:v1.49.0-jammy \
//     bash -lc 'npm i -D playwright@1.49.0 >/dev/null 2>&1 && \
//       FERKO_BASE_URL=http://host.docker.internal:8080 node scripts/screenshots.mjs'
//
// Output: docs/images/*.png
import { chromium } from 'playwright';
import { mkdirSync } from 'node:fs';

const baseUrl = process.env.FERKO_BASE_URL ?? 'http://localhost:8080';
const outDir = 'docs/images';
const user = process.env.FERKO_SCREENSHOT_USER ?? 'admin.ferko';
const password = process.env.FERKO_SCREENSHOT_PASSWORD ?? 'ferko123';

const shots = [
  { name: 'login', path: '/login', auth: false },
  { name: 'dashboard', path: '/' },
  { name: 'timetable', path: '/raspored' },
  { name: 'courses', path: '/kolegiji' },
  { name: 'rooms', path: '/prostorije' },
  { name: 'admin', path: '/admin' },
];

mkdirSync(outDir, { recursive: true });

const browser = await chromium.launch();
const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
const page = await context.newPage();

async function login() {
  await page.goto(`${baseUrl}/login`, { waitUntil: 'networkidle' });
  await page.fill('#username', user);
  await page.fill('#password', password);
  await Promise.all([
    page.waitForURL((url) => !url.pathname.endsWith('/login'), { timeout: 15000 }),
    page.click('button[type=submit]'),
  ]);
}

for (const shot of shots) {
  if (shot.auth === false) {
    await page.goto(`${baseUrl}${shot.path}`, { waitUntil: 'networkidle' });
  } else {
    await login().catch(() => {});
    await page.goto(`${baseUrl}${shot.path}`, { waitUntil: 'networkidle' });
  }
  await page.waitForTimeout(800);
  await page.screenshot({ path: `${outDir}/${shot.name}.png`, fullPage: true });
  console.log(`captured ${shot.name}`);
}

await browser.close();
