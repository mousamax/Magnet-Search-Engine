package com.magnet.Magnet;

import java.util.HashMap;
import java.util.Map;
import org.tartarus.snowball.ext.PorterStemmer;
import org.jsoup.nodes.Document;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;

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

        Set<String> stopWords = loadStopwords();
        if (choice == 1) {

            // Key: Term
            // Value: Map with: Key: filename , Value: Term Frequency
            Map<String, Map<String, Double>> mp = new HashMap<String, Map<String, Double>>();
            ArrayList<String> files = getHTMLFiles(new File("./"));

            int numberOfDocuments = files.size();
            System.out.println("\u001B[34m" + "Number of documents: " + numberOfDocuments + "\u001B[0m");

            for (String fileName : files) {
                mp = readHTMLFile(fileName, stopWords, mp);
            }

            writeToFile(convertToJSON(mp).toString(), "./index.json");
            System.out.println("--------------------------------");
            System.out.println(
                    "\u001B[32m" + "Finished indexing all files\nOutput of the indexer is written in index.json"
                            + "\u001B[0m");
        } else {
            String HTMLFileName;
            Scanner sc1 = new Scanner(System.in);
            System.out.println("Enter the file name:");
            HTMLFileName = sc1.nextLine();
            
            sc1.close();
            String indexFileString = readFile("index.json");
            sc1.close();
            Map<String, Map<String, Double>> mp = parseJSON(indexFileString);

        
            mp = readHTMLFile(HTMLFileName, stopWords, mp);
            writeToFile(convertToJSON(mp).toString(), "./index.json");
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

    public static Map<String, Map<String, Double>> readHTMLFile(String fileName, Set<String> stopWords,
            Map<String, Map<String, Double>> mp) throws IOException {

        // Read HTML file
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder content = new StringBuilder(1024);
        String s = "";
        // First line is url
        //String url = br.readLine();
        //Document document = Jsoup.connect(url).get();
        //String title = document.title();
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
                            //Double dblVal = (Double) tf;
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
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        bw.write(str);
        bw.close();
    }

    public static Set<String> loadStopwords() throws IOException {

        // read stopwords from file
        FileReader fr = new FileReader("./english_stopwords.txt");
        BufferedReader br = new BufferedReader(fr);
        String s = "";
        Set<String> stopwordsSet = new HashSet<String>();
        while ((s = br.readLine()) != null) {
            stopwordsSet.add(s);
        }
        br.close();
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
