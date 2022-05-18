DROP DATABASE IF EXISTS MagnetSG;

CREATE DATABASE MagnetSG;

USE MagnetSG;
DROP TABLE IF EXISTS CrawlerData;
DROP TABLE IF EXISTS HyperLinks;
DROP TABLE IF EXISTS UrlsToBeCrawled;
DROP TABLE IF EXISTS SearchData;
DROP TABLE IF EXISTS StemWords;
DROP TABLE IF EXISTS OriginalWords;
DROP TABLE IF EXISTS FilesAndScores;

CREATE TABLE CrawlerData (
Id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
Urls nvarchar(300) COLLATE Arabic_CI_AI_KS_WS NOT NULL,
CompactPages nvarchar(300) COLLATE Arabic_CI_AI_KS_WS,
Filename nvarchar(55),
indexed nvarchar(1) DEFAULT '0',
Popularity DOUBLE PRECISION DEFAULT 0.0,
Unique(Urls)
);

CREATE TABLE HyperLinks (
UrlId int NOT NULL,
InnerUrl nvarchar(300) COLLATE Arabic_CI_AI_KS_WS NOT NULL,
FOREIGN KEY (UrlId) REFERENCES CrawlerData(id),
Primary Key(UrlId, InnerUrl)
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
numberOfDocuments int DEFAULT 0,
IDF nvarchar(5)
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
Score nvarchar(15),
originalWordId int NOT NULL,
FOREIGN KEY (originalWordId) REFERENCES OriginalWords(id)
);
