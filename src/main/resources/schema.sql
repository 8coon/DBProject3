
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
  author TEXT
);


DROP TABLE IF EXISTS Threads;
CREATE TABLE Threads (
  id SERIAL,
  slug TEXT UNIQUE,
  author TEXT,
  title TEXT,
  created TIMESTAMPTZ
);


DROP TABLE IF EXISTS Posts;
CREATE TABLE Posts (
  id SERIAL,
  author TEXT,
  thread TEXT,
  forum TEXT,
  message TEXT,
  created TIMESTAMPTZ,
  isEdited BOOLEAN,
  parent INT DEFAULT 0
);


DROP TABLE IF EXISTS Votes;
CREATE TABLE Votes (
  post INT,
  voter TEXT,
  value INT
);

