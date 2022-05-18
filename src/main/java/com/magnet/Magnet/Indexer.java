package com.magnet.Magnet;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.*;

public class Indexer {

    public static void main(String[] args) throws JSONException, IOException, ParseException {

        DataAccess dataAccess = new DataAccess();
        int choice;
        Scanner sc = new Scanner(System.in);
        System.out
                .println("\u001B[34m" + "Do you want to index all files or a single file?\n1.All files\n2.Single file");
        choice = sc.nextInt();
        while (choice != 1 && choice != 2) {
            System.out.println("\u001B[31m" + "Invalid choice. Please enter 1 or 2." + "\u001B[0m");
            choice = sc.nextInt();
        }
        System.out.println(
                "\u001B[35m" + "Are the HTML files in arabic or english ?\n1.Arabic\n2.English");
        Set<String> stopWords = loadStopwords();
        if (choice == 1) {

            // Key: Term
            // Value: Map with: Key: filename , Value: Term Frequency
            // Map<String, Map<String, Double>> mp = new HashMap<String, Map<String,
            // Double>>();
            ArrayList<String> files = getHTMLFiles(new File("./html_files"));

            int numberOfDocuments = files.size();
            System.out.println("\u001B[34m" + "Number of documents: " + numberOfDocuments + "\u001B[0m");

            for (String fileName : files) {
                // mp = readHTMLFile(fileName, stopWords, mp);
                readHTMLFileDB(fileName, stopWords, dataAccess);
            }

            // writeToFile(convertToJSON(mp).toString(), "index.json");
            System.out.println("--------------------------------");
            System.out.println(
                    "\u001B[32m" + "Finished indexing all files\nOutput of the indexer is in the DB"
                            + "\u001B[0m");
        } else {
            String HTMLFileName;

            // Read HTML file name from user
            Scanner sc1 = new Scanner(System.in);
            System.out.println("Enter the file name:");
            HTMLFileName = sc1.nextLine();
            sc1.close();

            // Read the index.json file and save it in a string
            String indexFileString = readFile("./indexer result/index.json");

            // Parse the json file and save it in a map
            Map<String, Map<String, Double>> mp = parseJSON(indexFileString);

            // Update the map
            mp = readHTMLFile(HTMLFileName, stopWords, mp);

            // Write the new index.json
            writeToFile(convertToJSON(mp).toString(), "index.json");
            System.out.println("--------------------------------");
            System.out.println(
                    "\u001B[32m" + "Finished indexing " + HTMLFileName
                            + "\nOutput of the indexer is written in index.json"
                            + "\u001B[0m");
        }
        sc.close();
    }

    public static Map<String, Map<String, Double>> parseJSON(String stringToParse) throws ParseException {

        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(stringToParse);
        Map<String, Map<String, Double>> mp = new HashMap<String, Map<String, Double>>();
        mp = (Map<String, Map<String, Double>>) obj;
        return mp;

    }

    public static ArrayList<String> getHTMLFiles(final File folder) {
        ArrayList<String> files = new ArrayList<String>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getHTMLFiles(fileEntry);
            } else {
                if (fileEntry.getName().endsWith(".html")) {
                    files.add(fileEntry.getName());
                }
            }
        }
        return files;
    }

    public static String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    public static void readHTMLFileDB(String fileName, Set<String> stopWords, DataAccess dataAccess)
            throws IOException {

        // Define scores for terms in title and in body
        Double titleScore = 2.0;
        Double bodyScore = 1.0;
        // Read file from given filename
        File input = new File("./html_files/" + fileName);
        // Use Jsoup to parse the file
        Document doc = Jsoup.parse(input, "UTF-8", "");
        // Remove .html from the file name
        fileName = fileName.substring(0, fileName.length() - 5);
        // Save the title in a string
        String title = doc.title();
        // Save the body text in a string
        String body = doc.body().text();
        // Get an array of body words
        String[] bodyWords = body.split(" ");
        // Get an array of title words
        String[] titleWords = title.split(" ");
        // Calculate docLength, will be used to calculate the normalized TF
        Integer docLength = bodyWords.length + titleWords.length;

        // Loop on title words and add them to the map
        for (String word : titleWords) {

            // Convert word to lowercase
            word = word.toLowerCase();

            // Remove stop words
            if (stopWords.contains(word)) {
                continue;
            }

            // Stemming
            String stemmedWord = stemming(word);

            // Check if the stemmed term is in the DB
            // Get the stem id
            int stemId = dataAccess.getStemTermId(stemmedWord);
            if (stemId == -1) {
                // Stemmed Term is not in DB

                // Add the stemmed term to the DB
                stemId = dataAccess.addStemTerm(stemmedWord);

                // Add original word to DB
                int originalWordId = dataAccess.addOriginalWord(word, stemId);

                // Add file to DB

                dataAccess.addFile(fileName, titleScore / docLength, originalWordId);

                // Increment the number of documents for the stemmed term
                dataAccess.incrementNumberOfDocuments(stemId);

            } else {

                // Stemmed word is in DB

                // Check if the original term is in the DB
                // Get the original word id
                int originalWordId = dataAccess.getOriginalWordId(word);
                if (originalWordId == -1) {
                    // Original Term is not in DB

                    // Get the stem id
                    stemId = dataAccess.getStemTermId(stemmedWord);

                    // Add original word to DB
                    originalWordId = dataAccess.addOriginalWord(word, stemId);

                    // Add file to DB
                    dataAccess.addFile(fileName, titleScore / docLength, originalWordId);

                    // Increment the number of documents for the stemmed term
                    dataAccess.incrementNumberOfDocuments(stemId);

                } else {
                    // Original Term is in DB

                    // Get the original word id
                    originalWordId = dataAccess.getOriginalWordId(word);

                    // Get the file id
                    int fileId = dataAccess.getFileId(fileName, originalWordId);

                    // Check if the file is in the DB
                    if (fileId == -1) {
                        // File is not in DB

                        // Add file to DB
                        fileId = dataAccess.addFile(fileName, titleScore / docLength, originalWordId);

                        // Increment the number of documents for the stemmed term
                        dataAccess.incrementNumberOfDocuments(stemId);

                    } else {
                        // File is in DB

                        // Get the file TF
                        Double score = dataAccess.getScore(fileId);

                        // Update the file TF
                        dataAccess.updateFileScore(fileId, score + titleScore / docLength);

                    }

                }

            }

        }

        // Loop on title words and add them to the map
        for (String word : bodyWords) {

            // Convert word to lowercase
            word = word.toLowerCase();

            // Remove stop words
            if (stopWords.contains(word)) {
                continue;
            }

            // Stemming
            String stemmedWord = stemming(word);

            // Check if the stemmed term is in the DB
            // Get the stem id
            int stemId = dataAccess.getStemTermId(stemmedWord);
            if (stemId == -1) {
                // Stemmed Term is not in DB

                // Add the stemmed term to the DB
                stemId = dataAccess.addStemTerm(stemmedWord);

                // Add original word to DB
                int originalWordId = dataAccess.addOriginalWord(word, stemId);

                // Add file to DB

                dataAccess.addFile(fileName, bodyScore / docLength, originalWordId);

                // Increment the number of documents for the stemmed term
                dataAccess.incrementNumberOfDocuments(stemId);

            } else {

                // Stemmed word is in DB

                // Check if the original term is in the DB
                // Get the original word id
                int originalWordId = dataAccess.getOriginalWordId(word);
                if (originalWordId == -1) {
                    // Original Term is not in DB

                    // Get the stem id
                    stemId = dataAccess.getStemTermId(stemmedWord);

                    // Add original word to DB
                    originalWordId = dataAccess.addOriginalWord(word, stemId);

                    // Add file to DB
                    dataAccess.addFile(fileName, bodyScore / docLength, originalWordId);

                    // Increment the number of documents for the stemmed term
                    dataAccess.incrementNumberOfDocuments(stemId);

                } else {
                    // Original Term is in DB

                    // Get the original word id
                    originalWordId = dataAccess.getOriginalWordId(word);

                    // Get the file id
                    int fileId = dataAccess.getFileId(fileName, originalWordId);

                    // Check if the file is in the DB
                    if (fileId == -1) {
                        // File is not in DB

                        // Add file to DB
                        fileId = dataAccess.addFile(fileName, bodyScore / docLength, originalWordId);

                        // Increment the number of documents for the stemmed term
                        dataAccess.incrementNumberOfDocuments(stemId);

                    } else {
                        // File is in DB

                        // Get the file TF
                        Double score = dataAccess.getScore(fileId);

                        // Update the file TF
                        dataAccess.updateFileScore(fileId, score + bodyScore / docLength);

                    }

                }

            }

        }

    }

    public static Map<String, Map<String, Double>> readHTMLFile(String fileName, Set<String> stopWords,
            Map<String, Map<String, Double>> mp) throws IOException {

        // Define scores for terms in title and in body
        Double titleScore = 2.0;
        Double bodyScore = 1.0;
        // Read file from given filename
        File input = new File("./html_files/" + fileName);
        // Use Jsoup to parse the file
        Document doc = Jsoup.parse(input, "UTF-8", "");
        // Save the title in a string
        String title = doc.title();
        // Save the body text in a string
        String body = doc.body().text();
        // Get an array of body words
        String[] bodyWords = body.split(" ");
        // Get an array of title words
        String[] titleWords = title.split(" ");
        // Calculate docLength, will be used to calculate the normalized TF
        Integer docLength = bodyWords.length + titleWords.length;

        // Loop on title words and add them to the map
        for (String word : titleWords) {

            // Convert word to lowercase
            word = word.toLowerCase();

            // Stemming
            word = stemming(word);

            // Remove stop words
            if (stopWords.contains(word)) {
                continue;
            }

            // Add to map

            // If the map already contains the term
            if (mp.containsKey(word)) {
                Map<String, Double> mpTemp = mp.get(word);

                // If the map already contains the filename
                if (mpTemp.containsKey(fileName)) {
                    // Normalized TF * docLength = TF
                    // TF++
                    // Normalized TF = TF / docLength
                    Object tf = mpTemp.get(fileName);
                    Double tf2 = (Double) tf;

                    Double normalizedTF = (Double) (((tf2 * docLength) + titleScore)
                            / docLength);

                    if (normalizedTF < 0.5f) {
                        mpTemp.put(fileName, normalizedTF);
                    } else {
                        // Spam
                        mpTemp.put(fileName, 0.0);
                    }
                } else {
                    mpTemp.put(fileName, (Double) (titleScore / docLength));
                }
                mp.put(word, mpTemp);

            } else {
                Map<String, Double> mpTemp = new HashMap<String, Double>();
                mpTemp.put(fileName, (Double) (titleScore / docLength));
                mp.put(word, mpTemp);
            }
        }

        // Loop on body words and add them to the map
        for (String word : bodyWords) {

            // Convert word to lowercase
            word = word.toLowerCase();

            // Stemming
            word = stemming(word);

            // Remove stop words
            if (stopWords.contains(word)) {
                continue;
            }

            // Add to map
            if (mp.containsKey(word)) {
                Map<String, Double> mpTemp = mp.get(word);

                if (mpTemp.containsKey(fileName)) {
                    // Normalized TF * docLength = TF
                    // TF++
                    // Normalized TF = TF / docLength
                    Object tf = mpTemp.get(fileName);
                    Double tf2 = (Double) tf;

                    Double normalizedTF = (Double) (((tf2 * docLength) + bodyScore)
                            / docLength);

                    if (normalizedTF < 0.5f) {
                        mpTemp.put(fileName, normalizedTF);
                    } else {
                        // Spam
                        mpTemp.put(fileName, 0.0);
                    }
                } else {
                    mpTemp.put(fileName, (Double) (bodyScore / docLength));
                }
                mp.put(word, mpTemp);

            } else {
                Map<String, Double> mpTemp = new HashMap<String, Double>();
                mpTemp.put(fileName, (Double) (bodyScore / docLength));
                mp.put(word, mpTemp);
            }
        }

        return mp;
    }

    public static Map<String, Map<String, Double>> readHTMLFileOld(String fileName, Set<String> stopWords,
            Map<String, Map<String, Double>> mp) throws IOException {

        // Read HTML file
        FileReader fr = new FileReader("./html_files/" + fileName);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder content = new StringBuilder(1024);
        String s = "";
        // First line is url
        // String url = br.readLine();
        // Document document = Jsoup.connect(url).get();
        // String title = document.title();
        String title = "TEMP";

        while ((s = br.readLine()) != null) {
            content.append(s.toLowerCase());
        }
        br.close();
        fr.close();

        // Remove white space and tags
        // And save the words in a hash map
        String words = content.toString();
        String currentWord = "";

        // Calculate length of document (without HTML tags and spaces)
        Integer docLength = 0;
        for (Integer i = 0; i < words.length(); i++) {
            if (words.charAt(i) == ' ') {
                if (currentWord.length() > 0) {

                    // Don't count stop words
                    if (stopWords.contains(currentWord)) {
                        currentWord = "";
                        continue;
                    }
                    docLength++;
                    currentWord = "";
                }
                continue;
            } else if (words.charAt(i) == '<') {
                while (words.charAt(i) != '>') {
                    i++;
                }

            } else {
                currentWord += words.charAt(i);

            }
        }

        // For each word (don't include HTML tags + spaces),
        // Save the word in a hash map with the filename as the key and the normalized
        // term frequency as the value
        for (Integer i = 0; i < words.length(); i++) {
            if (words.charAt(i) == ' ') {
                if (currentWord.length() > 0) {

                    // Stemming
                    currentWord = stemming(currentWord);

                    // Remove stop words
                    if (stopWords.contains(currentWord)) {
                        currentWord = "";
                        continue;
                    }

                    if (mp.containsKey(currentWord)) {
                        Map<String, Double> mpTemp = mp.get(currentWord);

                        if (mpTemp.containsKey(fileName)) {
                            // Normalized TF * docLength = TF
                            // TF++
                            // Normalized TF = TF / docLength

                            Object tf = mpTemp.get(fileName);
                            // Double dblVal = (Double) tf;
                            Double tf2 = (Double) tf;

                            Double normalizedTF = (Double) (((tf2 * docLength) + 1.0)
                                    / docLength);

                            // If the term is in the title, add 0.3
                            if (title.contains(currentWord)) {
                                normalizedTF += 0.3f;
                            }
                            if (normalizedTF < 0.5f) {
                                mpTemp.put(fileName, normalizedTF);
                            } else {
                                // Spam
                                mpTemp.put(fileName, 0.0);
                            }
                        } else {
                            // If the term is in the title, add 0.3
                            if (title.contains(currentWord)) {
                                mpTemp.put(fileName, (Double) (1.0 / docLength) + 0.3);
                            } else {
                                mpTemp.put(fileName, (Double) (1.0 / docLength));
                            }
                        }
                        mp.put(currentWord, mpTemp);

                    } else {
                        Map<String, Double> mpTemp = new HashMap<String, Double>();
                        // If the term is in the title, add 0.3
                        if (title.contains(currentWord)) {
                            mpTemp.put(fileName, (Double) (1.0 / docLength) + 0.3);
                        } else {
                            mpTemp.put(fileName, (Double) (1.0 / docLength));
                        }
                        mp.put(currentWord, mpTemp);
                    }
                    currentWord = "";
                }
                continue;
            } else if (words.charAt(i) == '<') {
                while (words.charAt(i) != '>') {
                    i++;
                }

            } else {
                currentWord += words.charAt(i);

            }
        }
        return mp;
    }

    public static JSONObject convertToJSON(Map<String, Map<String, Double>> mp) throws JSONException {

        // convert to JSON
        JSONObject json = new JSONObject();
        for (String term : mp.keySet()) {
            JSONObject jsonTerm = new JSONObject();
            for (String file : mp.get(term).keySet()) {
                jsonTerm.put(file, mp.get(term).get(file));
            }
            json.put(term, jsonTerm);
        }

        return json;
    }

    public static String stemming(String word) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public static void writeToFile(String str, String filename) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("./indexer result/" + filename));
        bw.write(str);
        bw.close();
    }

    public static Set<String> loadStopwords() throws IOException {

        // Read stopwords from file
        FileReader frAr = new FileReader("./arabic stopwords/arabic_stopwords.txt");

        BufferedReader brAr = new BufferedReader(frAr);
        String s = "";
        Set<String> stopwordsSet = new HashSet<String>();
        while ((s = brAr.readLine()) != null) {
            stopwordsSet.add(s);
        }

        FileReader frEn = new FileReader("./english stopwords/english_stopwords.txt");
        BufferedReader brEn = new BufferedReader(frEn);

        while ((s = brEn.readLine()) != null) {
            stopwordsSet.add(s);
        }

        brAr.close();
        brEn.close();

        stopwordsSet.add("*");
        stopwordsSet.add("/");
        stopwordsSet.add("\\");
        stopwordsSet.add("$");
        stopwordsSet.add("^");
        stopwordsSet.add("#");
        stopwordsSet.add("@");
        stopwordsSet.add("!");
        stopwordsSet.add("&");
        stopwordsSet.add("-");

        return stopwordsSet;
    }

    public static void calculateIDF_DB(DataAccess dataAccess, int numbeOfDocuments) {
        // Calculate IDF

        //Get all terms
        HashMap<Integer, Integer> terms = dataAccess.getAllTermsAndNumberOfDocuments();

        //for each term
        for (Integer termId: terms.keySet())
        {
            
            //calculate the idf
            double idf = Math.log10((double)numbeOfDocuments / (double)terms.get(termId));

            //update the idf in the database
            dataAccess.setIDF(termId, idf);
        }
        
    }

    public static Map<String, Map<String, Double>> calculateIDF(Map<String, Map<String, Double>> mp,
            int numberOfDocuments)
            throws IOException {

        // Calculate IDF
        for (String term : mp.keySet()) {
            Map<String, Double> mpTemp = mp.get(term);
            // Count Number of documents containing the term
            Integer numberOfDocumentsContainingTerm = 0;
            for (String file : mpTemp.keySet()) {
                if (mpTemp.get(file) > 0) {
                    numberOfDocumentsContainingTerm++;
                }
            }

            // Calculate IDF for each document
            for (String file : mpTemp.keySet()) {
                Double tf = mpTemp.get(file);

                Double idf = (Double) Math.log10((float) numberOfDocuments / mpTemp.get(file));
                mpTemp.put(file, tf * idf);
            }
            mp.put(term, mpTemp);
        }
        return mp;
    }
}
