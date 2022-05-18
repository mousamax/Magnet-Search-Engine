DROP DATABASE IF EXISTS MagnetSG;

CREATE DATABASE MagnetSG;

USE MagnetSG;
DROP TABLE IF EXISTS CrawlerData;
DROP TABLE IF EXISTS UrlsToBeCrawled;
DROP TABLE IF EXISTS SearchData;
DROP TABLE IF EXISTS StemWords;
DROP TABLE IF EXISTS OriginalWords;
DROP TABLE IF EXISTS FilesAndScores;



CREATE TABLE CrawlerData (
Urls nvarchar(300) COLLATE Arabic_CI_AI_KS_WS NOT NULL  PRIMARY KEY,
CompactPages nvarchar(300) COLLATE Arabic_CI_AI_KS_WS,
Filename nvarchar(55),
UNIQUE (CompactPages)
);

CREATE TABLE UrlsToBeCrawled (
Urls nvarchar(300) COLLATE Arabic_CI_AI_KS_WS NOT NULL PRIMARY KEY
);

CREATE TABLE SearchData (
Query nvarchar(300) COLLATE Arabic_CI_AI_KS_WS NOT NULL PRIMARY KEY
);



CREATE TABLE StemWords(
Id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
StemTerm nvarchar(300) COLLATE Arabic_CI_AI_KS_WS NOT NULL,
numberOfDocuments int DEFAULT 0
);

CREATE TABLE OriginalWords(
Id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
originalWord nvarchar(300) COLLATE Arabic_CI_AI_KS_WS NOT NULL,
stemId int NOT NULL,
FOREIGN KEY (stemId) REFERENCES StemWords(id)
);


CREATE TABLE FilesAndScores(
Id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
Filename nvarchar(55),
Score nvarchar(300),
originalWordId int NOT NULL,
FOREIGN KEY (originalWordId) REFERENCES OriginalWords(id)
);
