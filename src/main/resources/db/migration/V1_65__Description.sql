CREATE EXTENSION IF NOT EXISTS CITEXT;


DROP TABLE IF EXISTS Forum CASCADE;
DROP TABLE IF EXISTS Posts CASCADE;
DROP TABLE IF EXISTS Thread CASCADE;
DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Vote CASCADE;
DROP TABLE IF EXISTS Votes CASCADE;
DROP TABLE IF EXISTS Forum_User CASCADE;


DROP INDEX IF EXISTS user_nickname_index;
DROP INDEX IF EXISTS user_email_index;


DROP INDEX IF EXISTS forum_slug_index;
DROP INDEX IF EXISTS forum_user_id_index;


DROP INDEX IF EXISTS thread_author_id_index;
DROP INDEX IF EXISTS thread_forum_id_index;
DROP INDEX IF EXISTS thread_slug_index;


DROP INDEX IF EXISTS post_author_index;
DROP INDEX IF EXISTS post_thread_id_index;
DROP INDEX IF EXISTS post_parent_index;
DROP INDEX IF EXISTS post_path_index;
DROP INDEX IF EXISTS post_created_index;


DROP INDEX IF EXISTS vote_user_id_index;
DROP INDEX IF EXISTS vote_thread_id_index;


DROP INDEX IF EXISTS forum_user_user_id_index;
DROP INDEX IF EXISTS forum_user_forum_id_index;


DROP TRIGGER IF EXISTS trigger_post
ON Thread;
DROP TRIGGER IF EXISTS trigger_thread
ON Posts;


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
  ON Forum(slug);
CREATE INDEX IF NOT EXISTS forum_user_id_index
  ON Forum(user_id);


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

DROP INDEX IF EXISTS thread_forum_id_index;
CREATE INDEX IF NOT EXISTS thread_forum_id_index
  ON thread (forum_id);

DROP INDEX IF EXISTS thread_author_id_index;
CREATE INDEX IF NOT EXISTS thread_author_id_index
  ON thread (author_id);

-- CREATE INDEX IF NOT EXISTS thread_author_id_index
--   ON Thread (author_id);
-- CREATE INDEX IF NOT EXISTS thread_forum_id_index
--   ON Thread (forum_id);
CREATE UNIQUE INDEX IF NOT EXISTS thread_slug_index
  ON Thread (slug);
CREATE INDEX thread_created_index
  ON Thread (created);



CREATE TABLE IF NOT EXISTS Posts (
  id        SERIAL PRIMARY KEY,
  parent    INTEGER NOT NULL         DEFAULT 0,
  author_id INTEGER NOT NULL REFERENCES Users (id),
  message   TEXT    NOT NULL,
  is_edited BOOLEAN                  DEFAULT FALSE,
  forum_id  INTEGER NOT NULL REFERENCES Forum (id),
  thread_id INTEGER NOT NULL REFERENCES Thread (id),
  created   TIMESTAMPTZ(6)           DEFAULT now(),
  path      INTEGER [],
  nickname  CITEXT  NOT NULL
);


-- CREATE INDEX IF NOT EXISTS post_author_index
--   ON Posts (author_id);
-- CREATE INDEX IF NOT EXISTS post_forum_id_index
--   ON Posts (forum_id);
-- CREATE INDEX IF NOT EXISTS post_thread_id_index
--   ON Posts (thread_id);
-- CREATE INDEX IF NOT EXISTS post_parent_index
--   ON Posts (parent);
-- CREATE INDEX IF NOT EXISTS post_path_index
--   ON Posts ((path [1]));
-- CREATE INDEX IF NOT EXISTS post_created_index
--   ON Posts (created);
--

DROP INDEX IF EXISTS posts_forum_id_index;
CREATE INDEX IF NOT EXISTS posts_forum_id_index
  ON posts (forum_id);

DROP INDEX IF EXISTS posts_author_id_index;
CREATE INDEX IF NOT EXISTS posts_author_id_index
  ON posts (author_id);

DROP INDEX IF EXISTS post_parent_index;
CREATE INDEX IF NOT EXISTS post_parent_index
  ON Posts (parent);

DROP INDEX IF EXISTS post_path_index;
CREATE INDEX IF NOT EXISTS post_path_index
  ON Posts ((path [1]));

DROP INDEX IF EXISTS post_thread_id_created_id_index;
CREATE INDEX IF NOT EXISTS post_thread_id_created_id_index
  ON posts (thread_id, created, id);

DROP INDEX IF EXISTS post_thread_id_path_index;
CREATE INDEX IF NOT EXISTS post_thread_id_path_index
  ON posts (thread_id, path);

DROP INDEX IF EXISTS post_thread_id_parent_id_index;
CREATE INDEX IF NOT EXISTS post_thread_id_parent_id_index
  ON posts (thread_id, parent, id);




CREATE TABLE IF NOT EXISTS Forum_User (
  user_id  INTEGER REFERENCES Users (id),
  forum_id INTEGER REFERENCES Forum (id)
);


CREATE INDEX IF NOT EXISTS forum_user_user_id_index
  ON Forum_User (user_id);
CREATE INDEX IF NOT EXISTS forum_user_forum_id_index
  ON Forum_User (forum_id);



CREATE OR REPLACE FUNCTION forum_user()
  RETURNS TRIGGER AS $forum_user$

BEGIN
  INSERT INTO Forum_User (user_id, forum_id) VALUES (NEW.author_id, NEW.forum_id);
  RETURN NULL;
END;

$forum_user$ LANGUAGE plpgsql;


CREATE TRIGGER trigger_thread
AFTER INSERT ON Thread
FOR EACH ROW EXECUTE PROCEDURE forum_user();


CREATE TRIGGER trigger_post
AFTER INSERT ON Posts
FOR EACH ROW EXECUTE PROCEDURE forum_user();


CREATE TABLE IF NOT EXISTS Vote (
  user_id   INTEGER NOT NULL REFERENCES Users (id),
  thread_id INTEGER NOT NULL REFERENCES Thread (id),
  voice     INTEGER NOT NULL
);


CREATE INDEX IF NOT EXISTS vote_user_id_index
  ON Vote (user_id);
CREATE INDEX IF NOT EXISTS vote_thread_id_index
  ON Vote (thread_id);

