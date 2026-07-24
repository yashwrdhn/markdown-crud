# Alexandria — Project Vision

## Vision

Alexandria is a **local-first personal knowledge platform** whose purpose is to organize, understand, and reason over personal knowledge.

The project is built around three independent domains:

* **Alexandria** — Owns knowledge.
* **ThinkFlow** — Owns execution.
* **AI** — Owns reasoning.

Each domain has a single responsibility and communicates through well-defined APIs.

---

# Domain 1 — Alexandria (Knowledge)

Alexandria is responsible for storing and retrieving knowledge.

Responsibilities:

* Markdown CRUD
* Git versioning
* Markdown parsing
* Wiki link resolution
* Backlink generation
* Search
* Semantic indexing
* Knowledge retrieval

Core Principles:

* Markdown is the source of truth.
* PostgreSQL stores only derived state.
* Git owns version history.
* UUIDs provide stable identity.

Alexandria answers questions like:

* What do I know?
* Where is this information?
* What is related to this topic?

---

# Domain 2 — ThinkFlow (Execution)

ThinkFlow is a **general-purpose workflow engine**.

Productivity features are simply predefined workflows.

Examples include:

* Tasks
* Habits
* Journal entries
* Weekly reviews
* Recurring reminders

Over time, ThinkFlow evolves into a customizable workflow platform where users can define their own workflows and automations.

ThinkFlow answers questions like:

* What should happen?
* What is currently running?
* What has completed?
* What should execute next?

---

# Domain 3 — AI (Reasoning)

AI never owns data.

AI never talks directly to databases.

AI reasons over the capabilities exposed by Alexandria and ThinkFlow.

Responsibilities:

* Tool calling
* Retrieval
* Summarization
* Planning
* Reasoning

AI answers questions like:

* What did I learn this week?
* Summarize my notes.
* Which unfinished tasks relate to networking?
* What should I study next?

---

# Development Roadmap

## Phase 1 — Alexandria Foundation

* Markdown CRUD
* Git integration
* Markdown parser
* Wiki links
* Backlinks
* PostgreSQL metadata
* PostgreSQL Full Text Search
* Rename support
* Reindex API

## Phase 2 — Semantic Retrieval

* Markdown chunking
* Embeddings
* pgvector
* Hybrid search (FTS + Vector Search)

## Phase 3 — AI

* AI service
* Tool registry
* Retrieval pipeline (RAG)
* Knowledge reasoning

## Phase 4 — ThinkFlow

* Workflow engine
* Built-in workflows

    * Tasks
    * Habits
    * Journals
    * Weekly reviews
* Custom workflow definitions
* Workflow execution
* AI-assisted workflow creation

---

# Architectural Principles

* Markdown is permanent.
* PostgreSQL is disposable.
* Git owns history.
* UUIDs own identity.
* AI consumes APIs, never databases.
* Every piece of derived data must be reproducible from the Markdown vault.
* Alexandria owns knowledge.
* ThinkFlow owns execution.
* AI owns reasoning.
* Prefer existing, battle-tested technologies over rebuilding solved problems.
