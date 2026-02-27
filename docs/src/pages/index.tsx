import type {ReactNode} from 'react';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';

import styles from './index.module.css';

function GitHubIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
      <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z" />
    </svg>
  );
}

function MediumIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
      <path d="M13.54 12a6.8 6.8 0 01-6.77 6.82A6.8 6.8 0 010 12a6.8 6.8 0 016.77-6.82A6.8 6.8 0 0113.54 12zM20.96 12c0 3.54-1.51 6.42-3.38 6.42-1.87 0-3.39-2.88-3.39-6.42s1.52-6.42 3.39-6.42 3.38 2.88 3.38 6.42M24 12c0 3.17-.53 5.75-1.19 5.75-.66 0-1.19-2.58-1.19-5.75s.53-5.75 1.19-5.75C23.47 6.25 24 8.83 24 12z" />
    </svg>
  );
}

function ArrowRightIcon() {
  return (
    <svg viewBox="0 0 20 20" width="18" height="18" fill="currentColor" style={{marginLeft: '0.5rem'}}>
      <path fillRule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clipRule="evenodd" />
    </svg>
  );
}

const features = [
  {
    title: 'Simplicity',
    icon: '✨',
    description:
      'Generate fully-populated test objects with a single line of code. No boilerplate, no complex setup.',
  },
  {
    title: 'Reusability',
    icon: '♻️',
    description:
      'Define once, use everywhere. Share test fixtures across your entire test suite effortlessly.',
  },
  {
    title: 'Randomness',
    icon: '🎲',
    description:
      'Discover edge cases automatically with smart randomization. Focus on what matters in your tests.',
  },
];

const codeExample = `// Generate a random object
FixtureMonkey fm = FixtureMonkey.create();
Product product = fm.giveMeOne(Product.class);

// Customize specific fields
Product custom = fm.giveMeBuilder(Product.class)
    .set("name", "Fixture Monkey")
    .set("price", 29_900L)
    .sample();`;

function HeroSection(): ReactNode {
  return (
    <section className={styles.hero}>
      <div className={styles.heroOverlay} />
      <div className={styles.heroContent}>
        <div className={styles.socialLinks}>
          <a
            className={styles.socialLink}
            href="https://medium.com/naver-platform-labs"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Medium"
          >
            <MediumIcon />
          </a>
          <a
            className={styles.socialLink}
            href="https://github.com/naver/fixture-monkey"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="GitHub"
          >
            <GitHubIcon />
          </a>
        </div>
      </div>
    </section>
  );
}

function TaglineSection(): ReactNode {
  return (
    <section className={styles.taglineSection}>
      <div className={styles.taglineContainer}>
        <div className={styles.gradientCard}>
          <h1 className={styles.tagline}>
            The easiest way to generate fully&#8209;customizable,
            randomly&nbsp;populated instances
          </h1>
        </div>
        <p className={styles.subtitle}>
          Java &amp; Kotlin library for both{' '}
          <strong>DRY</strong> (Don&apos;t Repeat Yourself) and{' '}
          <strong>DAMP</strong> (Descriptive And Meaningful Phrases) testing
        </p>
        <div className={styles.ctaGroup}>
          <Link className={styles.ctaPrimary} to="/docs/get-started/requirements">
            Get Started <ArrowRightIcon />
          </Link>
          <a
            className={styles.ctaSecondary}
            href="https://github.com/naver/fixture-monkey"
            target="_blank"
            rel="noopener noreferrer"
          >
            <GitHubIcon /> GitHub
          </a>
        </div>
      </div>
    </section>
  );
}

function CodeShowcase(): ReactNode {
  return (
    <section className={styles.codeSection}>
      <div className={styles.codeContainer}>
        <div className={styles.codeHeader}>
          <div className={styles.codeDots}>
            <span className={styles.codeDotRed} />
            <span className={styles.codeDotYellow} />
            <span className={styles.codeDotGreen} />
          </div>
          <span className={styles.codeFileName}>ProductTest.java</span>
        </div>
        <pre className={styles.codeBlock}>
          <code>{codeExample}</code>
        </pre>
      </div>
    </section>
  );
}

function FeatureSection(): ReactNode {
  return (
    <section className={styles.featureSection}>
      <h2 className={styles.featureSectionTitle}>Why Fixture Monkey?</h2>
      <div className={styles.featureGrid}>
        {features.map((feature) => (
          <div key={feature.title} className={styles.featureCard}>
            <div className={styles.featureIcon}>{feature.icon}</div>
            <h3 className={styles.featureTitle}>{feature.title}</h3>
            <p className={styles.featureDesc}>{feature.description}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

export default function Home(): ReactNode {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={siteConfig.title}
      description="Java & Kotlin library for generating test fixtures"
    >
      <main className={styles.main}>
        <HeroSection />
        <TaglineSection />
        <CodeShowcase />
        <FeatureSection />
      </main>
    </Layout>
  );
}
