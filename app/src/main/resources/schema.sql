CREATE TABLE IF NOT EXISTS documents (
                uuid UUID PRIMARY KEY,
                path TEXT NOT NULL,
                title TEXT NOT NULL,
                word_count INTEGER NOT NULL,
                search_text TEXT NOT NULL,
                search_vector TSVECTOR
            );