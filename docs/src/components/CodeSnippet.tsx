import React, {useMemo} from 'react';
import CodeBlock from '@theme/CodeBlock';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './CodeSnippet.module.css';

interface Props {
  src: string;
  language?: 'java' | 'kotlin';
  method?: string;
  showClass?: boolean;
  title?: string;
}

function extractMethodBody(source: string, methodName?: string): string {
  const lines = source.split('\n');
  let startIdx = -1;
  let braceDepth = 0;
  let endIdx = -1;

  const pattern = methodName
    ? new RegExp(`\\b${methodName}\\s*\\(`)
    : /\bvoid\s+\w+\s*\(|fun\s+\w+\s*\(/;

  for (let i = 0; i < lines.length; i++) {
    if (startIdx === -1) {
      if (pattern.test(lines[i])) {
        for (let j = i; j < lines.length; j++) {
          if (lines[j].includes('{')) {
            startIdx = j + 1;
            braceDepth = 1;
            break;
          }
        }
      }
    } else {
      for (const ch of lines[i]) {
        if (ch === '{') braceDepth++;
        if (ch === '}') braceDepth--;
      }
      if (braceDepth === 0) {
        endIdx = i;
        break;
      }
    }
  }

  if (startIdx === -1 || endIdx === -1) return source;

  const body = lines.slice(startIdx, endIdx);
  const indent = body
    .filter(l => l.trim().length > 0)
    .reduce((min, l) => Math.min(min, l.search(/\S/)), Infinity);
  return body.map(l => l.slice(indent)).join('\n').trim();
}

function extractClassBody(source: string): string {
  return source
    .split('\n')
    .filter(l => !l.startsWith('package ') && !l.startsWith('import '))
    .join('\n')
    .trim();
}

const LANG = {
  java: {label: 'Java', color: '#f59e0b'},
  kotlin: {label: 'Kotlin', color: '#8b5cf6'},
} as const;

function FileIcon({className}: {className?: string}) {
  return (
    <svg className={className} viewBox="0 0 16 16" fill="currentColor">
      <path d="M3.75 1.5a.25.25 0 0 0-.25.25v12.5c0 .138.112.25.25.25h8.5a.25.25 0 0 0 .25-.25V6H9.75A1.75 1.75 0 0 1 8 4.25V1.5ZM10 4.25v-2.5l3.5 3.5H10.5a.25.25 0 0 1-.25-.25ZM3.75 0h5.086c.464 0 .909.184 1.237.513l3.414 3.414c.329.328.513.773.513 1.237v9.086A1.75 1.75 0 0 1 12.25 16h-8.5A1.75 1.75 0 0 1 2 14.25V1.75C2 .784 2.784 0 3.75 0Z" />
    </svg>
  );
}

function buildSourceUrl(src: string, organizationName?: string, projectName?: string): {url: string; label: string; isExternal: boolean} | undefined {
  const sourcePath: string | undefined = (src as any).__sourcePath;
  if (!sourcePath) return undefined;

  const fileName = sourcePath.split('/').pop() ?? sourcePath;
  const isDev = process.env.NODE_ENV === 'development';

  if (isDev) {
    const absolutePath: string | undefined = (src as any).__absolutePath;
    if (absolutePath) {
      return {
        url: `idea://open?file=${absolutePath}`,
        label: fileName,
        isExternal: false,
      };
    }
  }

  if (organizationName && projectName) {
    return {
      url: `https://github.com/${organizationName}/${projectName}/blob/main/${sourcePath}`,
      label: fileName,
      isExternal: true,
    };
  }

  return undefined;
}

export default function CodeSnippet({src, language = 'java', method, showClass, title}: Props) {
  const {siteConfig} = useDocusaurusContext();

  const code = useMemo(() => {
    if (showClass) return extractClassBody(src);
    return extractMethodBody(src, method);
  }, [src, method, showClass]);

  const displayTitle = title || (method ? `${method}()` : undefined);
  const lineCount = code.split('\n').length;
  const lang = LANG[language];
  const source = buildSourceUrl(
    src,
    siteConfig.organizationName,
    siteConfig.projectName,
  );

  return (
    <div className={styles.snippet}>
      <div className={styles.header}>
        <div className={styles.headerLeft}>
          <span
            className={styles.lang}
            style={{'--badge-color': lang.color} as React.CSSProperties}
          >
            {lang.label}
          </span>
          {displayTitle && (
            <span className={styles.title}>{displayTitle}</span>
          )}
        </div>
        {source && (
          <a
            className={styles.sourceLink}
            href={source.url}
            title={(src as any).__sourcePath}
            {...(source.isExternal ? {target: '_blank', rel: 'noopener noreferrer'} : {})}
          >
            <FileIcon className={styles.sourceIcon} />
            {source.label}
          </a>
        )}
      </div>
      <CodeBlock language={language} showLineNumbers={lineCount >= 5}>
        {code}
      </CodeBlock>
    </div>
  );
}
