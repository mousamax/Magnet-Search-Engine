package com.magnet.Magnet;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataAccess {
    private Connection connection;
    // microsoft driverClassName for sql server
    private static final String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    String hostName = "localhost";
    String database = "MagnetSG";

    // obtain a connection to the database "MagnetSG"
    public DataAccess() {
        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection("jdbc:sqlserver://" + hostName + ";databaseName=" + database
                    + ";encrypt=true;trustServerCertificate=true;hostNameInCertificate=*.database.windows.net;loginTimeout=60;",
                    "mousamax", "fastopen");
            System.out.println("Connection successful");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // add visited url to the database table "CrawlerData"
    public void addVisitedUrl(String url) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO CrawlerData (Urls) Values(?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, url);
            statement.executeUpdate();
            // Execute the query
            // int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // addVisitedUrlandCompactPages to the database table "CrawlerData"
    public void addVisitedUrlandCompactPagesFilename(String url, String compactPages, String filename) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO CrawlerData (Urls, CompactPages, Filename) Values(?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, url);
            statement.setString(2, compactPages);
            statement.setString(3, filename);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // retrieve all the visited urls from the database table "CrawlerData"
    public void getVisitedUrls(ConcurrentHashMap<String, Boolean> visitedUrls) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Urls FROM CrawlerData";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            int i = 0;
            // Store the visited urls in the ConcurrentHashMap
            while (rs.next()) {
                // add to the visited urls hashmap
                visitedUrls.put(rs.getString("Urls"), true);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // add UrlsToBeCrawled array to the database table "UrlsToBeCrawled"
    public void addUrlsToBeCrawled(ConcurrentHashMap<String, Boolean> urlsToBeCrawled) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            // insert the map of urls to be crawled into the database table
            // "UrlsToBeCrawled"
            String query = "";
            for (String url : urlsToBeCrawled.keySet()) {
                query += "INSERT INTO UrlsToBeCrawled (Urls) Select N'" + url + "' ;";
            }
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("ignored duplicate url");
        }
    }

    // retrieve all the urls to be crawled from the database table "UrlsToBeCrawled"
    public void getUrlsToBeCrawled(ConcurrentHashMap<String, Boolean> urlsToBeCrawled) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Urls FROM UrlsToBeCrawled";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            int i = 0;
            // Store the urls to be crawled in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                String url = rs.getString("Urls").replaceAll("\\s+", "");
                urlsToBeCrawled.put(url, true);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // delete all the urls to be crawled from the database table "UrlsToBeCrawled"
    public void deleteUrlsToBeCrawled() {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "DELETE FROM UrlsToBeCrawled;";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // add compactPage to the database table "CrawlerData" for a given url
    public void addCompactPage(String url, String compactPage) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "UPDATE CrawlerData SET CompactPages = N'" + compactPage + "' WHERE Urls = N'" + url + "';";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // retrieve all the compactPages from the database table "CrawlerData" into a
    // ConcurrentHashMap
    public void getCompactPages(ConcurrentHashMap<String, String> compactPages) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT * FROM CrawlerData";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            int i = 0;
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // add to the compactPages hashmap
                compactPages.put(rs.getString("CompactPages"), rs.getString("Urls"));
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // retrieve all the compactPages from the database table "CrawlerData" into a
    // ConcurrentHashMap
    public void getUrlsAndCompactPages(ConcurrentHashMap<String, Boolean> visitedUrls,
            ConcurrentHashMap<String, String> compactPages) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT * FROM CrawlerData;";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            int i = 0;
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // add to the compactPages hashmap
                // remove spaces from the url
                String url = rs.getString("Urls").replaceAll("\\s+", "");
                if (rs.getString("CompactPages") != null) {
                    String compactPage = rs.getString("CompactPages").replaceAll("\\s+", "");
                    compactPages.put(compactPage, url);
                } else
                    compactPages.put("state:" + i, url);
                visitedUrls.put(url, true);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // add query to the database table "SearchData" for a given query
    public void addQuery(String query) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query1 = "INSERT INTO SearchData (Query) VALUES (N'" + query + "');";
            // Execute the query
            int count = st.executeUpdate(query1);
        } catch (SQLException e) {
        }
    }

    // getUrl from the database table "CrawlerData" for a file name
    public String getUrl(String fileName) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Urls FROM CrawlerData WHERE FileName = N'" + fileName + "';";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                String url = rs.getString("Urls").replaceAll("\\s+", "");
                return url;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    // --------------------------------------------------------------

    // Check if StemTerm exists in database table "StemWords"
    // and return its id
    public int getStemTermId(String stemTerm) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Id FROM StemWords WHERE StemTerm = N'" + stemTerm + "';";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Check if originalWord exists in table "OriginalWords"
    // and return its id
    public int getOriginalWordId(String originalWord) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Id FROM OriginalWords WHERE OriginalWord = N'" + originalWord + "';";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Check if file exists in table "FilesAndScores"
    // given a filename and originalWordId
    // and return its id
    public int getFileId(String fileName, int originalWordId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Id FROM FilesAndScores WHERE FileName = N'" + fileName + "' AND OriginalWordId = "
                    + originalWordId + ";";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Add StemTerm to table "StemWords" and return its id
    public int addStemTerm(String stemTerm) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO StemWords (StemTerm) VALUES (N'" + stemTerm + "');";
            // Execute the query
            int count = st.executeUpdate(query);
            // Obtain a statement
            Statement st1 = connection.createStatement();
            String query1 = "SELECT Id FROM StemWords WHERE StemTerm = N'" + stemTerm + "';";
            // Execute the query
            ResultSet rs = st1.executeQuery(query1);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Add originalWord to table "OriginalWords" and return its id
    // given a stem id
    public int addOriginalWord(String originalWord, int stemId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO OriginalWords (OriginalWord, StemId) VALUES (N'" + originalWord + "', " + stemId
                    + ");";
            // Execute the query
            int count = st.executeUpdate(query);
            // Obtain a statement
            Statement st1 = connection.createStatement();
            String query1 = "SELECT Id FROM OriginalWords WHERE OriginalWord = N'" + originalWord + "';";
            // Execute the query
            ResultSet rs = st1.executeQuery(query1);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Add fileName and score to table "FilesAndScores" and return its id
    // given an originalWordId
    public int addFile(String fileName, double score, int originalWordId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO FilesAndScores (FileName, Score, OriginalWordId) VALUES (N'" + fileName + "', "
                    + score + ", " + originalWordId + ");";
            // Execute the query
            int count = st.executeUpdate(query);
            // Obtain a statement
            Statement st1 = connection.createStatement();
            String query1 = "SELECT Id FROM FilesAndScores WHERE FileName = N'" + fileName + "';";
            // Execute the query
            ResultSet rs = st1.executeQuery(query1);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Get score from table "FilesAndScores"
    // given a fileId
    public double getScore(int fileId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Score FROM FilesAndScores WHERE Id = " + fileId + ";";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                double score = rs.getDouble("Score");
                return score;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Update the score of a file in table "FilesAndScores"
    // given a fileId and a score
    public void updateFileScore(int fileId, double score) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "UPDATE FilesAndScores SET Score = " + score + " WHERE Id = " + fileId + ";";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // close the connection
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
