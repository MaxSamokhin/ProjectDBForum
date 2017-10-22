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
  title     citext NOT NULL,
  author_id INTEGER      NOT NULL REFERENCES Users (id),
  forum_id  INTEGER      NOT NULL REFERENCES Forum (id),
  message   TEXT         NOT NULL,
  votes     INTEGER                  DEFAULT 0,
  slug      CITEXT  UNIQUE,
  created   timestamptz(6) DEFAULT now()
);

CREATE TABLE IF NOT EXISTS Posts (
  id        SERIAL PRIMARY KEY,
  parent    INTEGER NOT NULL         DEFAULT 0,
  author    INTEGER NOT NULL REFERENCES Users (id),
  message   TEXT    NOT NULL,
  is_edited BOOLEAN                  DEFAULT FALSE,
  forum_id  INTEGER NOT NULL REFERENCES Forum (id),
  thread_id INTEGER NOT NULL REFERENCES Thread (id),
  created   timestamptz(6) DEFAULT now(),
  path      INTEGER []
);

CREATE TABLE IF NOT EXISTS Vote (
  user_id   INTEGER NOT NULL REFERENCES Users (id),
  thread_id INTEGER NOT NULL REFERENCES Thread (id),
  voice     INTEGER NOT NULL
);