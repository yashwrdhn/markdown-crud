## We are building Alexandria, a local-first knowledge management backend. 


Our next focus is to turn Alexandria into a usable backend rather than adding new architecture. The plan is to expose the existing functionality through REST APIs, ingest a realistic Markdown vault to populate PostgreSQL with real data, validate and refine full-text search, then implement an inverted index from scratch separately as a learning exercise to compare it with PostgreSQL FTS. After that, we'll add WikiLink parsing, link resolution, and backlinks so the knowledge graph formed by Markdown documents becomes navigable. This will give us a complete, practical Markdown backend before moving on to the AI layer.


storage/
Raw markdown persistence

parser/
Markdown → semantic model

index/
Semantic model → metadata

repository/
Metadata ↔ PostgreSQL

Part 1 – Markdown Backend, Step 2: Parser. Parser is minimal without AST, only fetches heading, codeblocks and links.

Part 1 – Markdown Backend, Step 1: Storage. Storage is now feature-complete with create, read, write (overwrite), delete, rename, exists, and recursive list. Markdown is the source of truth, UUIDs are stored in hidden sidecar .meta.json files (not frontmatter), Document contains UUID, relative Path, and raw markdown content, and storage has no knowledge of markdown syntax. We intentionally kept clear boundaries: storage manages files and metadata only; parsing begins next. In the next chat, we'll design and implement Step 2 – Parser, starting from a raw Document and extracting headings, wiki links, external links, and any future markdown-derived structure while keeping the parser completely independent of the filesystem and storage implementation.