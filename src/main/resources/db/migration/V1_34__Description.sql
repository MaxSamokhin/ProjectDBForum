CREATE EXTENSION IF NOT EXISTS CITEXT;

DROP TABLE IF EXISTS Forum CASCADE;
DROP TABLE IF EXISTS Posts CASCADE;
DROP TABLE IF EXISTS Thread CASCADE;
DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Vote CASCADE;
DROP TABLE IF EXISTS Votes CASCADE;

CREATE TABLE IF NOT EXISTS Users (
  id       SERIAL PRIMARY KEY,
  nickname CITEXT      NOT NULL UNIQUE,
  fullname VARCHAR(50) NOT NULL,
  email    CITEXT      NOT NULL UNIQUE,
  about    TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS user_nickname_index
  ON Users (nickname);
CREATE UNIQUE INDEX IF NOT EXISTS user_email_index
  ON Users (email);

CREATE TABLE IF NOT EXISTS Forum (
  id      SERIAL PRIMARY KEY,
  title   VARCHAR(100) NOT NULL,
  slug    CITEXT       NOT NULL UNIQUE,
  user_id INTEGER      NOT NULL REFERENCES Users (id),
  posts   INTEGER,
  threads INTEGER
);


CREATE UNIQUE INDEX IF NOT EXISTS forum_slug_index
  ON Forum (slug);
CREATE INDEX IF NOT EXISTS forum_user_id_index
  ON Forum (user_id);

CREATE TABLE IF NOT EXISTS Thread (
  id        SERIAL PRIMARY KEY,
  title     CITEXT  NOT NULL,
  author_id INTEGER NOT NULL REFERENCES Users (id),
  forum_id  INTEGER NOT NULL REFERENCES Forum (id),
  message   TEXT    NOT NULL,
  votes     INTEGER        DEFAULT 0,
  slug      CITEXT UNIQUE,
  created   TIMESTAMPTZ(6) DEFAULT now()
);


CREATE INDEX IF NOT EXISTS thread_author_id_index
  ON Thread (author_id);
CREATE INDEX IF NOT EXISTS thread_forum_id_index
  ON Thread (forum_id);
CREATE UNIQUE INDEX IF NOT EXISTS thread_slug_index
  ON Thread (slug);

CREATE TABLE IF NOT EXISTS Posts (
  id        SERIAL PRIMARY KEY,
  parent    INTEGER NOT NULL         DEFAULT 0,
  author    INTEGER NOT NULL REFERENCES Users (id),
  message   TEXT    NOT NULL,
  is_edited BOOLEAN                  DEFAULT FALSE,
  forum_id  INTEGER NOT NULL REFERENCES Forum (id),
  thread_id INTEGER NOT NULL REFERENCES Thread (id),
  created   TIMESTAMPTZ(6)           DEFAULT now(),
  path      INTEGER []
);


CREATE INDEX IF NOT EXISTS post_author_index
  ON Posts (author);
CREATE INDEX IF NOT EXISTS post_forum_id_index
  ON Posts (forum_id);
CREATE INDEX IF NOT EXISTS post_thread_id_index
  ON Posts (thread_id);
CREATE INDEX IF NOT EXISTS post_parent_index
  ON Posts (parent);
CREATE INDEX IF NOT EXISTS post_path_index
  ON Posts ((path [1]));
CREATE INDEX IF NOT EXISTS post_thread_id_parent on posts(thread_id, parent);
CREATE INDEX if NOT EXISTS post_id_thread_id_index on posts(id, thread_id);

CREATE TABLE IF NOT EXISTS Vote (
  user_id   INTEGER NOT NULL REFERENCES Users (id),
  thread_id INTEGER NOT NULL REFERENCES Thread (id),
  voice     INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS vote_user_id_index
  ON Vote (user_id);
CREATE INDEX IF NOT EXISTS vote_thread_id_index
  ON Vote (thread_id);
