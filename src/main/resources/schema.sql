SET SYNCHRONOUS_COMMIT = 'off';


DROP TABLE IF EXISTS Users;
CREATE TABLE IF NOT EXISTS Users (
  nickname TEXT UNIQUE PRIMARY KEY,
  fullname TEXT,
  email TEXT UNIQUE,
  about TEXT
);


DROP INDEX IF EXISTS UsersNickname;
CREATE UNIQUE INDEX UsersNickname ON Users (lower(nickname));

--DROP INDEX IF EXISTS UsersEmail;
--CREATE UNIQUE INDEX UsersEmail ON Users (lower(email));



DROP TABLE IF EXISTS Forums;
CREATE TABLE IF NOT EXISTS Forums (
  slug   TEXT UNIQUE PRIMARY KEY,
  title  TEXT,
  author TEXT,
  posts INT DEFAULT 0,
  threads INT DEFAULT 0
);

--DROP INDEX IF EXISTS ForumsSlug;
--CREATE UNIQUE INDEX ForumsSlug ON Forums (lower(slug));


DROP TABLE IF EXISTS Threads;
CREATE TABLE IF NOT EXISTS Threads (
  id SERIAL PRIMARY KEY,
  slug TEXT UNIQUE,
  author TEXT,
  title TEXT,
  created TIMESTAMPTZ,
  forum TEXT,
  message TEXT,
  votes INT
);


DROP INDEX IF EXISTS ThreadsId;
CREATE INDEX ThreadsId ON Threads (id);


--DROP INDEX IF EXISTS ThreadsSlug;
--CREATE UNIQUE INDEX ThreadsSlug ON Threads (lower(slug));


DROP TABLE IF EXISTS Posts;
CREATE TABLE IF NOT EXISTS Posts (
  id SERIAL PRIMARY KEY,
  author TEXT,
  forum TEXT,
  thread INT,
  message TEXT,
  created TIMESTAMPTZ,
  isEdited BOOLEAN,
  parent INT DEFAULT 0,
  path INT[]
);


DROP INDEX IF EXISTS PostsId;
CREATE INDEX PostsId ON Posts (id);


DROP INDEX IF EXISTS PostsPath;
CREATE INDEX PostsPath ON Posts USING GIN (path);


DROP INDEX IF EXISTS PostsParent;
CREATE INDEX PostsParent ON Posts (parent);


DROP TABLE IF EXISTS Votes;
CREATE TABLE IF NOT EXISTS Votes (
  thread INT,
  author TEXT,
  voice INT
);


DROP INDEX IF EXISTS VotesThreadAuthor;
CREATE UNIQUE INDEX VotesThreadAuthor ON Votes (thread, lower(author));


DROP TABLE IF EXISTS Members;
CREATE TABLE IF NOT EXISTS Members (
  forum TEXT,
  author TEXT
);


DROP INDEX IF EXISTS MembersForumAuthor;
CREATE INDEX MembersForumAuthor ON Members (lower(forum), lower(author));



CREATE OR REPLACE FUNCTION onPostInsert() RETURNS trigger AS '
BEGIN
  IF NEW.parent > 0 THEN
    CREATE TEMPORARY TABLE parent AS SELECT thread, path FROM Posts WHERE id = NEW.parent LIMIT 1;

    IF (SELECT count(*) FROM parent) = 0 THEN
      DROP TABLE parent;
      RAISE EXCEPTION ''Parent post does not exist!'';
    END IF;

    NEW.path = (SELECT path FROM parent) || NEW.id;

    IF NEW.thread <> (SELECT thread FROM parent) THEN
      DROP TABLE parent;
      RAISE EXCEPTION ''Parent post belongs to another thread!'';
    END IF;

    DROP TABLE parent;
  ELSE
    NEW.path = array_agg(NEW.id);
  END IF;

  --IF NOT EXISTS(SELECT author FROM Members WHERE forum = NEW.forum AND author = NEW.author LIMIT 1) THEN
  --  INSERT INTO Members (forum, author) VALUES (NEW.forum, NEW.author);
  --END IF;

  --UPDATE Forums SET posts = posts + 1 WHERE lower(slug) = lower(NEW.forum);

  RETURN NEW;
END;' LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS postCheck ON Posts;
CREATE TRIGGER postCheck BEFORE INSERT OR UPDATE ON Posts FOR EACH ROW EXECUTE PROCEDURE onPostInsert();



--CREATE OR REPLACE FUNCTION onThreadInsert() RETURNS trigger AS '
--BEGIN
--  IF NOT EXISTS(SELECT author FROM Members WHERE forum = NEW.forum AND author = NEW.author LIMIT 1) THEN
--    INSERT INTO Members (forum, author) VALUES (NEW.forum, NEW.author);
--  END IF;
--
--  UPDATE Forums SET threads = threads + 1 WHERE lower(slug) = lower(NEW.forum);
--
--  RETURN NEW;
--END;' LANGUAGE plpgsql;


--DROP TRIGGER IF EXISTS threadCheck ON Threads;
--CREATE TRIGGER threadCheck AFTER INSERT ON Threads FOR EACH ROW EXECUTE PROCEDURE onThreadInsert();

