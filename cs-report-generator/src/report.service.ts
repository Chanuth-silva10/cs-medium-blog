import puppeteer, { type Browser } from 'puppeteer';
import type { ReportInput } from './report.model.js';

// User text goes into an HTML page that a real browser renders, so it must be escaped first.
export const escapeHtml = (value: string): string =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');

export const renderReportHtml = (report: ReportInput & { generatedAt: Date }): string => {
  return `<!doctype html>
<html>
  <head>
    <meta charset="utf-8" />
    <style>
      body { font-family: Arial, sans-serif; padding: 40px; color: #1a1a1a; }
      h1 { color: #2aa4e0; }
      .meta { color: #666; margin-bottom: 24px; }
      .summary { line-height: 1.6; white-space: pre-wrap; }
    </style>
  </head>
  <body>
    <h1>${escapeHtml(report.projectName)} - Project Report</h1>
    <div class="meta">Prepared by ${escapeHtml(report.author)} on ${report.generatedAt.toDateString()}</div>
    <div class="summary">${escapeHtml(report.summary)}</div>
  </body>
</html>`;
};

// Starting Chromium is slow, so we start it once and reuse it for every request.
let browserPromise: Promise<Browser> | undefined;

const getBrowser = (): Promise<Browser> => {
  if (!browserPromise) {
    // --no-sandbox is needed because Chromium's sandbox cannot start inside a default Docker container.
    browserPromise = puppeteer.launch({ args: ['--no-sandbox'] }).then((browser) => {
      browser.on('disconnected', () => {
        browserPromise = undefined;
      });
      return browser;
    });
    // If launching fails, forget the failed promise so the next request can try again.
    browserPromise.catch(() => {
      browserPromise = undefined;
    });
  }
  return browserPromise;
};

export const closeBrowser = async (): Promise<void> => {
  if (browserPromise) {
    const browser = await browserPromise;
    browserPromise = undefined;
    await browser.close();
  }
};

// Uses a headless browser to turn the HTML report into a PDF buffer.
export const renderReportPdf = async (html: string): Promise<Buffer> => {
  const browser = await getBrowser();
  const page = await browser.newPage();
  try {
    // Defence in depth: the page needs no scripts and no network, so switch both off.
    await page.setJavaScriptEnabled(false);
    await page.setRequestInterception(true);
    page.on('request', (request) => {
      if (request.url().startsWith('data:') || request.url() === 'about:blank') {
        void request.continue();
      } else {
        void request.abort();
      }
    });
    await page.setContent(html, { waitUntil: 'load' });
    const pdf = await page.pdf({ format: 'A4', printBackground: true });
    return Buffer.from(pdf);
  } finally {
    await page.close();
  }
};
