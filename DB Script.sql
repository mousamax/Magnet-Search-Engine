DROP DATABASE IF EXISTS MagnetSG;

CREATE DATABASE MagnetSG;

USE MagnetSG;
DROP TABLE IF EXISTS CrawlerData;
DROP TABLE IF EXISTS UrlsToBeCrawled;


CREATE TABLE CrawlerData (
id int IDENTITY(1,1) PRIMARY KEY,
name nvarchar(255) COLLATE Arabic_CI_AI_KS_WS NOT NULL,
color INT NOT NULL,
kind nvarchar(55) COLLATE Arabic_CI_AI_KS_WS NOT NULL,
size nVARCHAR(55) COLLATE Arabic_CI_AI_KS_WS  NOT NULL,
price int NOT NULL,
quantity INT NOT NULL,
image image,
imgLoc text,
UNIQUE (name,color)
);