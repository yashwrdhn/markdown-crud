Project: Alexandria

Goal:
Build a local-first knowledge management backend.

Architecture decisions:
- Markdown is the source of truth.
- PostgreSQL stores only derived state.
- Git manages version history.
- UUIDs identify documents.
- AI is completely out of scope.
- Workflow is completely out of scope.
- UI is completely out of scope.

Scope:

Storage
- Markdown CRUD
- FileSystem abstraction
- UUID generation
- Metadata extraction

Parser
- Markdown parser
- Frontmatter parsing
- WikiLink parsing ([[Document]])
- External link parsing
- Heading extraction

Database
documents
- uuid
- path
- title
- created_at
- modified_at
- hash
- word_count
- reading_time
- tsvector

document_links
- source_uuid
- target_uuid

Search
- PostgreSQL Full Text Search
- Store only tsvector
- Markdown remains source of truth
- No search_content stored

Git
- Commit changes
- Restore
- History
- Git is implementation detail

Features
- Reindex endpoint
- Rename support
- Link resolution
- Backlinks

Out of scope
- Embeddings
- AI
- Semantic search
- Rich editor
- Images
- PDFs
- Tags

Alexandria Foundation is complete.

Goal:
Add semantic retrieval without changing existing APIs.

Architecture:
Markdown

↓

Parser

↓

Chunking

↓

Embedding Model

↓

pgvector

↓

Hybrid Search

↓

UUIDs

Existing keyword search remains untouched.

Need to design:
- Chunking strategy
- Chunk metadata
- Embedding model
- pgvector schema
- Hybrid ranking
- Reindex embeddings
- Embedding versioning

Out of scope:
LLM
Tool calling
ThinkFlow
Workflow



Foundation and semantic search are complete.

Goal:
Build an AI service.

Responsibilities:
- Tool calling
- RAG
- Conversation memory
- Prompt management

AI owns no data.

AI only calls tools.

Initial tools:
searchKnowledge()
readDocument()
getLinkedDocuments()
getRecentDocuments()

Need to discuss:
- Tool registry
- Prompt templates
- Context window management
- Citation strategy
- RAG pipeline
- Conversation memory
- Hallucination prevention

Out of scope:
Workflow
Task execution
Automation


Alexandria and AI are complete.

Goal:
Build ThinkFlow.

ThinkFlow is NOT a productivity app.

ThinkFlow is a workflow engine.

Tasks, Habits, Journals are predefined workflows.

Need to design:
Workflow model
Execution engine
Scheduler
State machine
Variables
Triggers
Persistence

Initial built-in workflows:
Task
Habit
Journal
Reminder
Weekly Review

Future:
Custom workflows
Visual designer
Camunda-style execution

AI interacts through APIs.



Alexandria
Knowledge

ThinkFlow
Execution

AI
Reasoning

Goal:
Teach AI to orchestrate ThinkFlow.

Example:

User:
Summarize everything I learned this week.

AI

↓

getKnowledgeActivity()

↓

getJournalEntries()

↓

LLM Summary

Later:

User:
Every Friday summarize my learning.

↓

AI

↓

createWorkflow()

↓

ThinkFlow executes automatically.

Need to design:
Intent extraction
Workflow generation
Planning
Tool orchestration
Approval flow