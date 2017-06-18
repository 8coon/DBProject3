
DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
  nickname TEXT,
  fullname TEXT,
  email TEXT UNIQUE,
  about TEXT
);


DROP TABLE IF EXISTS Forums;
CREATE TABLE Forums (
  slug   TEXT UNIQUE,
  title  TEXT,
  author TEXT,
  posts INT,
  threads INT
);


DROP TABLE IF EXISTS Threads;
CREATE TABLE Threads (
  id SERIAL,
  slug TEXT UNIQUE,
  author TEXT,
  title TEXT,
  created TIMESTAMPTZ,
  forum TEXT,
  message TEXT,
  votes INT
);


DROP TABLE IF EXISTS Posts;
CREATE TABLE Posts (
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


DROP TABLE IF EXISTS Votes;
CREATE TABLE Votes (
  thread INT,
  author TEXT,
  voice INT
);


DROP TABLE IF EXISTS Members;
CREATE TABLE Members (
  forum TEXT,
  author TEXT,
  fullname TEXT,
  email TEXT,
  about TEXT
);
