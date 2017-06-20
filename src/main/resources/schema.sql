SET SYNCHRONOUS_COMMIT = 'off';


DROP TABLE IF EXISTS Users;
CREATE TABLE IF NOT EXISTS Users (
  nickname TEXT,
  fullname TEXT,
  email TEXT UNIQUE,
  about TEXT
);


DROP INDEX IF EXISTS UsersNickname;
CREATE UNIQUE INDEX UsersNickname ON Users (lower(nickname));

DROP INDEX IF EXISTS UsersEmail;
CREATE UNIQUE INDEX UsersEmail ON Users (lower(email));



DROP TABLE IF EXISTS Forums;
CREATE TABLE IF NOT EXISTS Forums (
  slug   TEXT UNIQUE,
  title  TEXT,
  author TEXT,
  posts INT DEFAULT 0,
  threads INT DEFAULT 0
);

DROP INDEX IF EXISTS ForumsSlug;
CREATE UNIQUE INDEX ForumsSlug ON Forums (lower(slug));


DROP TABLE IF EXISTS Threads;
CREATE TABLE IF NOT EXISTS Threads (
  id SERIAL,
  slug TEXT UNIQUE,
  author TEXT,
  title TEXT,
  created TIMESTAMPTZ,
  forum TEXT,
  message TEXT,
  votes INT
);


DROP INDEX IF EXISTS ThreadsId;
CREATE UNIQUE INDEX ThreadsId ON Threads (id);


DROP INDEX IF EXISTS ThreadsSlug;
CREATE UNIQUE INDEX ThreadsSlug ON Threads (lower(slug));


DROP INDEX IF EXISTS ThreadsCreated;
CREATE INDEX ThreadsCreated ON Threads (created);


DROP TABLE IF EXISTS Posts;
CREATE TABLE IF NOT EXISTS Posts (
  id SERIAL,
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
CREATE UNIQUE INDEX PostsId ON Posts (id);


DROP INDEX IF EXISTS PostsPath;
CREATE INDEX PostsPath ON Posts USING GIN (path);


DROP INDEX IF EXISTS PostsParent;
CREATE INDEX PostsParent ON Posts (parent);


DROP INDEX IF EXISTS PostsThread;
CREATE INDEX PostsThread ON Posts (thread);


DROP INDEX IF EXISTS PostsThreadId;
CREATE INDEX PostsThreadId ON Posts (thread, id);


DROP INDEX IF EXISTS PostsThreadParentId;
CREATE INDEX PostsThreadParentId ON Posts (thread, parent, id);


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


DROP INDEX IF EXISTS MembersForum;
CREATE INDEX MembersForum ON Members (lower(forum));


DROP INDEX IF EXISTS MembersForumAuthor;
CREATE INDEX MembersForumAuthor ON Members (lower(forum), lower(author));
