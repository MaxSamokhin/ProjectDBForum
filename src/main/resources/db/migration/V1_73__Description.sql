CREATE EXTENSION IF NOT EXISTS CITEXT;


DROP TABLE IF EXISTS Forum CASCADE;
DROP TABLE IF EXISTS Posts CASCADE;
DROP TABLE IF EXISTS Thread CASCADE;
DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Vote CASCADE;
DROP TABLE IF EXISTS Votes CASCADE;
DROP TABLE IF EXISTS Forum_User CASCADE;


CREATE TABLE IF NOT EXISTS Users (
  id       SERIAL PRIMARY KEY,
  nickname CITEXT  COLLATE ucs_basic  NOT NULL UNIQUE,
  fullname VARCHAR(50) NOT NULL,
  email    CITEXT      NOT NULL UNIQUE,
  about    TEXT
);


CREATE TABLE IF NOT EXISTS Forum (
  id      SERIAL PRIMARY KEY,
  title   VARCHAR(100) NOT NULL,
  slug    CITEXT       NOT NULL UNIQUE,
  user_id INTEGER      NOT NULL REFERENCES Users (id),
  posts   INTEGER,
  threads INTEGER
);


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


CREATE TABLE IF NOT EXISTS Posts (
  id        SERIAL PRIMARY KEY,
  parent    INTEGER NOT NULL         DEFAULT 0,
  author_id INTEGER NOT NULL REFERENCES Users (id),
  nickname  CITEXT  NOT NULL,
  message   TEXT    NOT NULL,
  is_edited BOOLEAN                  DEFAULT FALSE,
  forum_id  INTEGER NOT NULL REFERENCES Forum (id),
  thread_id INTEGER NOT NULL REFERENCES Thread (id),
  created   TIMESTAMPTZ(6)           DEFAULT now(),
  path      INTEGER []
);


CREATE TABLE IF NOT EXISTS Forum_User (
  user_id  INTEGER REFERENCES Users (id),
  forum_id INTEGER REFERENCES Forum (id)
);


CREATE TABLE IF NOT EXISTS Vote (
  user_id   INTEGER NOT NULL REFERENCES Users (id),
  thread_id INTEGER NOT NULL REFERENCES Thread (id),
  voice     INTEGER NOT NULL
);


DROP INDEX IF EXISTS forum_user_id_index;
CREATE INDEX IF NOT EXISTS forum_user_id_index
  ON forum (user_id);


DROP INDEX IF EXISTS forum_slug_index;
CREATE UNIQUE INDEX IF NOT EXISTS forum_slug_index
  ON forum (slug);


DROP INDEX IF EXISTS user_nickname_index;
CREATE UNIQUE INDEX IF NOT EXISTS user_nickname_index
  ON users (nickname);


DROP INDEX IF EXISTS user_email_index;
CREATE UNIQUE INDEX IF NOT EXISTS user_email_index
  ON users (email);


DROP INDEX IF EXISTS fu_forum_user_id_index;
CREATE INDEX IF NOT EXISTS fu_forum_user_id_index
  ON forum_user (forum_id, user_id);


DROP INDEX IF EXISTS posts_forum_id;
CREATE INDEX IF NOT EXISTS posts_forum_id_index
  ON posts (forum_id);


DROP INDEX IF EXISTS posts_id_thread_id;
CREATE INDEX IF NOT EXISTS posts_id_thread_id
  ON posts (thread_id, id);


DROP INDEX IF EXISTS vote_user_thread_index;
CREATE INDEX IF NOT EXISTS vote_user_thread_index
  ON vote (user_id, thread_id);


DROP INDEX IF EXISTS thread_forum_id_index;
CREATE INDEX IF NOT EXISTS thread_forum_id_index
  ON thread (forum_id);


DROP INDEX IF EXISTS thread_author_id_index;
CREATE INDEX IF NOT EXISTS thread_author_id_index
  ON thread (author_id);


DROP INDEX IF EXISTS thread_slug_index;
CREATE UNIQUE INDEX IF NOT EXISTS thread_slug_index
  ON thread (slug);


DROP INDEX IF EXISTS thread_forum_created;
CREATE INDEX thread_forum_created
  ON thread (forum_id, created);


DROP INDEX IF EXISTS post_thread_id_index;
CREATE INDEX post_thread_id_index
  ON posts (thread_id);


DROP INDEX IF EXISTS post_thread_forum_id;
CREATE INDEX post_thread_forum_id
  ON posts (thread_id, forum_id, id);


DROP INDEX IF EXISTS post_created_index;
CREATE INDEX post_created_index
  ON posts (created);


DROP INDEX IF EXISTS post_id_path_index;
CREATE INDEX post_id_path_index
  ON posts (id, path);


DROP INDEX IF EXISTS post_multi_index;
CREATE INDEX post_multi_index
  ON posts (thread_id, forum_id, path, id);


DROP INDEX IF EXISTS post_thread_path_one_index;
CREATE INDEX post_thread_path_one_index
  ON posts (thread_id, (path [1]));


DROP INDEX IF EXISTS post_thread_parent_index;
CREATE INDEX post_thread_parent_index
  ON posts (thread_id, parent, path);
