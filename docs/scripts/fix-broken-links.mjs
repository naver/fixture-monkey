#!/usr/bin/env node

/**
 * Post-migration link fixer for Docusaurus.
 *
 * Fixes relative links that break due to Hugo vs Docusaurus routing differences:
 * 1. Same-directory sibling links: ../sibling → ./sibling
 * 2. Cross-directory links going one level too deep: ../../cat/page → ../cat/page
 * 3. Specific renamed/moved page links
 */

import fs from 'fs';
import path from 'path';

const BASE = path.resolve(import.meta.dirname, '..');

// All doc root directories to process
const DOC_ROOTS = [
  'docs',
  'versioned_docs/version-1.0.x',
  'versioned_docs/version-0.6.x',
  'i18n/ko/docusaurus-plugin-content-docs/current',
  'i18n/ko/docusaurus-plugin-content-docs/version-1.0.x',
];

let fixCount = 0;

function getAllMarkdownFiles(dir) {
  const results = [];
  if (!fs.existsSync(dir)) return results;

  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      results.push(...getAllMarkdownFiles(fullPath));
    } else if (entry.name.endsWith('.md') || entry.name.endsWith('.mdx')) {
      results.push(fullPath);
    }
  }
  return results;
}

function fileExistsWithAnyExt(dir, baseName) {
  if (!fs.existsSync(dir)) return false;
  const entries = fs.readdirSync(dir);
  // Strip anchor/hash from baseName
  const cleanName = baseName.split('#')[0].replace(/\/$/, '');
  if (!cleanName) return false;

  return entries.some(e => {
    const nameNoExt = e.replace(/\.(md|mdx)$/, '');
    return nameNoExt === cleanName;
  }) || entries.some(e => e === cleanName); // Also check for directory
}

function dirExists(dir, name) {
  const cleanName = name.split('#')[0].replace(/\/$/, '');
  if (!cleanName) return false;
  const target = path.join(dir, cleanName);
  return fs.existsSync(target) && fs.statSync(target).isDirectory();
}

function fixLinksInFile(filePath, docRoot) {
  let content = fs.readFileSync(filePath, 'utf-8');
  const fileDir = path.dirname(filePath);
  const original = content;

  // Match markdown links: [text](relative-path) or [text](relative-path#anchor)
  // Only match relative paths (not http://, https://, /, #)
  content = content.replace(
    /\[([^\]]*)\]\((\.\.[^\)]*)\)/g,
    (match, text, linkPath) => {
      // Skip absolute URLs, anchors-only, and already-correct links
      if (linkPath.startsWith('http') || linkPath.startsWith('#')) return match;

      const [pathPart, anchor] = linkPath.split('#');
      const anchorStr = anchor ? `#${anchor}` : '';

      // Try to resolve the link
      const resolvedPath = path.resolve(fileDir, pathPart);
      const resolvedDir = path.dirname(resolvedPath);
      const resolvedName = path.basename(resolvedPath);

      // Check if the resolved target exists
      if (fileExistsWithAnyExt(resolvedDir, resolvedName) || dirExists(resolvedDir, resolvedName)) {
        return match; // Link is valid, keep it
      }

      // ── Fix Pattern 1: Same-directory sibling ──
      // ../sibling → ./sibling (when sibling exists in same directory)
      if (pathPart.startsWith('../') && !pathPart.startsWith('../../')) {
        const siblingName = pathPart.slice(3); // Remove ../
        if (fileExistsWithAnyExt(fileDir, siblingName) || dirExists(fileDir, siblingName)) {
          fixCount++;
          return `[${text}](./${siblingName}${anchorStr})`;
        }
      }

      // ── Fix Pattern 2: Cross-directory one level too deep ──
      // ../../category/page → ../category/page
      if (pathPart.startsWith('../../')) {
        const shortened = pathPart.replace('../../', '../');
        const shortenedResolved = path.resolve(fileDir, shortened);
        const shortenedDir = path.dirname(shortenedResolved);
        const shortenedName = path.basename(shortenedResolved);
        if (fileExistsWithAnyExt(shortenedDir, shortenedName) || dirExists(shortenedDir, shortenedName)) {
          fixCount++;
          return `[${text}](${shortened}${anchorStr})`;
        }
      }

      // ── Fix Pattern 3: Cross-directory two levels too deep ──
      // ../../../category/page → ../../category/page
      if (pathPart.startsWith('../../../')) {
        const shortened = pathPart.replace('../../../', '../../');
        const shortenedResolved = path.resolve(fileDir, shortened);
        const shortenedDir = path.dirname(shortenedResolved);
        const shortenedName = path.basename(shortenedResolved);
        if (fileExistsWithAnyExt(shortenedDir, shortenedName) || dirExists(shortenedDir, shortenedName)) {
          fixCount++;
          return `[${text}](${shortened}${anchorStr})`;
        }
      }

      return match; // Can't fix, leave as-is
    }
  );

  if (content !== original) {
    fs.writeFileSync(filePath, content);
  }
}

function main() {
  console.log('🔗 Fixing broken links...\n');

  for (const root of DOC_ROOTS) {
    const absRoot = path.join(BASE, root);
    if (!fs.existsSync(absRoot)) {
      console.log(`⚠️  Skipping ${root} (not found)`);
      continue;
    }

    console.log(`📁 Processing ${root}`);
    const files = getAllMarkdownFiles(absRoot);
    for (const file of files) {
      fixLinksInFile(file, absRoot);
    }
  }

  console.log(`\n✅ Fixed ${fixCount} broken links`);
}

main();
