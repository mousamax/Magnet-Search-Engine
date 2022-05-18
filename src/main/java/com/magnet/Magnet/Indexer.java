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
                "\u001B[35m" + "Are the HTML files in arabic or english ?\n1.Arabic\n2.English\n3.Both");
        sc.nextInt();
        Set<String> stopWords = loadStopwords();
        if (choice == 1) {

            // Key: Stemmed Term
            // Value: Map with: Key: Original Word, Value: Map with:
            // Key: File name, Value: Normalized TF * IDF
            Map<String, Map<String, Map<String, Double>>> mp = new HashMap<String, Map<String, Map<String, Double>>>();
            ArrayList<String> files = getHTMLFiles(new File("./html_files"));

            int numberOfDocuments = files.size();
            System.out.println("\u001B[34m" + "Number of documents: " + numberOfDocuments + "\u001B[0m");

            for (String fileName : files) {
                mp = readHTMLFile(fileName, stopWords, mp);
            }

            writeToFile(convertToJSON(mp).toString(), "index.json");
            System.out.println("--------------------------------");
            System.out.println(
                    "\u001B[32m"
                            + "Finished indexing all files\nOutput of the indexer is written in ./indexer/result/index.json"
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
            Map<String, Map<String, Map<String, Double>>> mp = parseJSON(indexFileString);

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

    public static Map<String, Map<String, Map<String, Double>>> parseJSON(String stringToParse) throws ParseException {

        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(stringToParse);
        Map<String, Map<String, Map<String, Double>>> mp = new HashMap<String, Map<String, Map<String, Double>>>();
        mp = (Map<String, Map<String, Map<String, Double>>>) obj;
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

    public static Map<String, Map<String, Map<String, Double>>> readHTMLFile(String fileName, Set<String> stopWords,
            Map<String, Map<String, Map<String, Double>>> mp) throws IOException {

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


        //Remove .html from file name
        fileName = fileName.substring(0, fileName.length() - 5);

        // Loop on title words and add them to the map
        for (String word : titleWords) {

            // Convert word to lowercase
            word = word.toLowerCase();

            // Stemming
            String stemmedWord = stemming(word);

            // Remove stop words
            if (stopWords.contains(word)) {
                continue;
            }

            // Add to map
            // If the map already contains the stemmed term
            if (mp.containsKey(stemmedWord)) {
                Map<String, Map<String, Double>> originalWordMap = mp.get(stemmedWord);

                // If the map already contains the original term
                if (originalWordMap.containsKey(word)) {
                    Map<String, Double> fileMap = originalWordMap.get(word);

                    // If the map already contains the filename
                    if (fileMap.containsKey(fileName)) {
                        // Update the score
                        // Update the score
                        // Normalized TF * docLength = TF
                        // TF++
                        // Normalized TF = TF / docLength
                        Object tf = fileMap.get(fileName);
                        Double tf2 = (Double) tf;
                        Double normalizedTF = (Double) (((tf2 * docLength) + titleScore)
                                / docLength);
                        fileMap.put(fileName, normalizedTF);
                        fileMap.put(fileName, normalizedTF);
                    } else {
                        // Add the filename and term frequency
                        fileMap.put(fileName, titleScore / docLength);
                    }
                } else {
                    // Original word map does not contain the original term
                    // Add the original term and filename and term frequency
                    Map<String, Double> fileMap = new HashMap<String, Double>();
                    fileMap.put(fileName, titleScore / docLength);
                    originalWordMap.put(word, fileMap);
                }
            } else {
                
                // mp does not contain the stemmed term
                // Add the stemmed term and original term and filename and term frequency
                Map<String, Double> fileMap = new HashMap<String, Double>();
                fileMap.put(fileName, titleScore / docLength);
                Map<String, Map<String, Double>> originalWordMap = new HashMap<String, Map<String, Double>>();
                originalWordMap.put(word, fileMap);
                mp.put(stemmedWord, originalWordMap);
            }

        }

        // Loop on body words and add them to the map
        for (String word : bodyWords) {

            // Convert word to lowercase
            word = word.toLowerCase();

            // Stemming
            String stemmedWord = stemming(word);

            // Remove stop words
            if (stopWords.contains(word)) {
                continue;
            }

            // Add to map

            // If the map already contains the stemmed term
            if (mp.containsKey(stemmedWord)) {
                Map<String, Map<String, Double>> originalWordMap = mp.get(stemmedWord);

                // If the map already contains the original term
                if (originalWordMap.containsKey(word)) {
                    Map<String, Double> fileMap = originalWordMap.get(word);

                    // If the map already contains the filename
                    if (fileMap.containsKey(fileName)) {
                        // Update the score
                        // Normalized TF * docLength = TF
                        // TF++
                        // Normalized TF = TF / docLength
                        Object tf = fileMap.get(fileName);
                        Double tf2 = (Double) tf;

                        Double normalizedTF = (Double) (((tf2 * docLength) + bodyScore)
                                / docLength);
                        fileMap.put(fileName, normalizedTF);
                    } else {
                        // Add the filename and term frequency
                        fileMap.put(fileName, bodyScore / docLength);
                    }
                } else {
                    // Original word map does not contain the original term
                    // Add the original term and filename and term frequency
                    Map<String, Double> fileMap = new HashMap<String, Double>();
                    fileMap.put(fileName, bodyScore / docLength);
                    originalWordMap.put(word, fileMap);
                }
            } else {
                // mp does not contain the stemmed term
                // Add the stemmed term and original term and filename and term frequency
                Map<String, Double> fileMap = new HashMap<String, Double>();
                fileMap.put(fileName, bodyScore / docLength);
                Map<String, Map<String, Double>> originalWordMap = new HashMap<String, Map<String, Double>>();
                originalWordMap.put(word, fileMap);
                mp.put(stemmedWord, originalWordMap);
            }

        }

        return mp;
    }

    public static JSONObject convertToJSON(Map<String, Map<String, Map<String, Double>>> mp) throws JSONException {

        // convert to JSON
        JSONObject json = new JSONObject();
        for (String stemmedWord : mp.keySet()) {
            JSONObject jsonObject = new JSONObject();
            Map<String, Map<String, Double>> originalWordMap = mp.get(stemmedWord);
            for (String originalWord : originalWordMap.keySet()) {
                JSONObject jsonObject2 = new JSONObject();
                Map<String, Double> fileMap = originalWordMap.get(originalWord);
                for (String fileName : fileMap.keySet()) {
                    jsonObject2.put(fileName, fileMap.get(fileName));
                }
                jsonObject.put(originalWord, jsonObject2);
            }
            json.put(stemmedWord, jsonObject);
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
