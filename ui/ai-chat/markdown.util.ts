// Minimal markdown -> HTML renderer, no dependencies. Covers what shows
// up in everyday LLM answers: headings, bold/italic, strikethrough,
// inline code, fenced code blocks, links, lists, blockquotes, rules,
// and paragraphs.
//
// Input is HTML-escaped first, so nothing the model outputs — including
// stray HTML or script tags — reaches the DOM unescaped. Only the tags
// this file explicitly emits ever render, which is what makes it safe
// to trust with [innerHTML] on the component side.

const escapeHtml = (input: string): string =>
  input
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');

const inline = (text: string): string =>
  text
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/__([^_]+)__/g, '<strong>$1</strong>')
    .replace(/~~([^~]+)~~/g, '<del>$1</del>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/_([^_]+)_/g, '<em>$1</em>')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');

export function renderMarkdown(raw: string): string {
  const escaped = escapeHtml(raw);

  // Pull fenced code blocks out first so nothing inside them gets
  // touched by the inline rules or line-based rules below.
  const blocks: string[] = [];
  const withoutCode = escaped.replace(/```(\w*)\n?([\s\S]*?)```/g, (_m, lang, code) => {
    const index = blocks.length;
    blocks.push(`<pre${lang ? ` data-lang="${lang}"` : ''}><code>${code.replace(/\n$/, '')}</code></pre>`);
    return `\u0000${index}\u0000`;
  });

  const html: string[] = [];
  let listType: 'ul' | 'ol' | null = null;
  let paragraph: string[] = [];

  const closeList = () => {
    if (listType) {
      html.push(`</${listType}>`);
      listType = null;
    }
  };

  const flushParagraph = () => {
    if (paragraph.length) {
      html.push(`<p>${paragraph.join(' ')}</p>`);
      paragraph = [];
    }
  };

  for (const rawLine of withoutCode.split('\n')) {
    const line = rawLine.trimEnd();
    const trimmed = line.trim();

    const codePlaceholder = trimmed.match(/^\u0000(\d+)\u0000$/);
    if (codePlaceholder) {
      closeList();
      flushParagraph();
      html.push(blocks[Number(codePlaceholder[1])]);
      continue;
    }

    if (!trimmed) {
      closeList();
      flushParagraph();
      continue;
    }

    const heading = line.match(/^(#{1,4})\s+(.*)$/);
    if (heading) {
      closeList();
      flushParagraph();
      const level = Math.min(heading[1].length + 2, 6); // h3..h6 — modest sizing inside a chat bubble
      html.push(`<h${level}>${inline(heading[2])}</h${level}>`);
      continue;
    }

    const quote = line.match(/^>\s?(.*)$/);
    if (quote) {
      closeList();
      flushParagraph();
      html.push(`<blockquote>${inline(quote[1])}</blockquote>`);
      continue;
    }

    if (/^(-{3,}|\*{3,})$/.test(trimmed)) {
      closeList();
      flushParagraph();
      html.push('<hr>');
      continue;
    }

    const unordered = line.match(/^[-*]\s+(.*)$/);
    if (unordered) {
      flushParagraph();
      if (listType !== 'ul') { closeList(); html.push('<ul>'); listType = 'ul'; }
      html.push(`<li>${inline(unordered[1])}</li>`);
      continue;
    }

    const ordered = line.match(/^\d+\.\s+(.*)$/);
    if (ordered) {
      flushParagraph();
      if (listType !== 'ol') { closeList(); html.push('<ol>'); listType = 'ol'; }
      html.push(`<li>${inline(ordered[1])}</li>`);
      continue;
    }

    closeList();
    paragraph.push(inline(line));
  }

  closeList();
  flushParagraph();

  return html.join('\n');
}