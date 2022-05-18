package com.magnet.Magnet;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.text.ParseException;
import java.util.*;


public class QueryProcessor{

    public static Map<String, Map<String, Map<String, Double>>> mp;
    public static Set<String> stopWords;

    public static void main(String[] args) throws JSONException, IOException, FileNotFoundException, ParseException, org.json.simple.parser.ParseException  {
        String query = "computer the Universe in engineer department";
        QueryProcessing(query);
    }
    public static ArrayList<String> QueryProcessing(String query) throws IOException, JSONException, org.json.simple.parser.ParseException{
        query = query.toLowerCase();
                //ArrayList<String> files = getHTMLFiles(new File("./"));
                //final map containing the needed stemmed files that will be sent to the ranker
                Map<String, Map<String, Map<String, Double>>> processedMap = new HashMap<String, Map<String, Map<String, Double>>>();
                

                //------------------phrase searching-------------------------------------
                Map<String, Map<String, Map<String, Double>>> phraseSearchingMap = new HashMap<String, Map<String, Map<String, Double>>>();
               

                //------------------end phrase searching-------------------------------------

                System.out.println(query);
                //each word in original query splitted 
                String[] queryArray = query.split(" ");
                //each word in original query after stemming and removing stop words
                ArrayList<String> queryArrayStemmed = new ArrayList<String>();
                //each word in original query after removing stop words
                ArrayList<String> originalQueryArray = new ArrayList<String>();
                //remove stop words and stem the rest
                for(int i = 0; i < queryArray.length; i++){
                    if(!stopWords.contains(queryArray[i])){
                        //queryArray[i] = stemming(queryArray[i]);
                        queryArrayStemmed.add(stemming(queryArray[i]));
                        originalQueryArray.add(queryArray[i]);
                    }   
                }
                //System.out.println(queryArrayStemmed);
        ArrayList<String> files = new ArrayList<String>();
                Map<String, Integer> phraseDocuments = new HashMap<String, Integer>();
                for(int i = 0; i < queryArrayStemmed.size();i++){
                    
                    if(mp.containsKey(queryArrayStemmed.get(i))){
                        
                        processedMap.put(queryArrayStemmed.get(i), mp.get(queryArrayStemmed.get(i)));
                        if(mp.get(queryArrayStemmed.get(i)).containsKey(originalQueryArray.get(i))){
                            for(Map.Entry<String, Double> HTMLdoc : mp.get(queryArrayStemmed.get(i)).get(originalQueryArray.get(i)).entrySet())
                                {
                                    //------------------phrase searching-------------------------------------
                                    if(phraseDocuments.containsKey(HTMLdoc.getKey()))
                                    {
                                        phraseDocuments.replace(HTMLdoc.getKey(), phraseDocuments.get(HTMLdoc.getKey()) + 1);
                                    }
                                    else
                                    {
                                        phraseDocuments.put(HTMLdoc.getKey(), 1);
                                    }
                                    //------------------end phrase searching-------------------------------------
                                    files.add(HTMLdoc.getKey());
                                    
                                    //processedMap.get(queryArrayStemmed.get(i)).get(originalQueryArray.get(i)).replace(HTMLdoc.getKey(), HTMLdoc.getValue() + 20);
                                }
                        }
                        // for(int j = 0; j < originalQueryArray.size();j++){
                            
                        // }
                    }
                }
                //System.out.println(processedMap);
                //System.out.println(phraseDocuments);
        
                //writeToFile(convertToJSON(processedMap).toString(), "processed.json");
               
                return files;
    };

    public static Map<String, Map<String, Map<String, Double>>> ToPhraseSearching(String query) throws IOException, JSONException, org.json.simple.parser.ParseException{
        //read json file
        Map<String, Map<String, Map<String, Double>>> mp = new HashMap<String, Map<String, Map<String, Double>>>();
        //ArrayList<String> files = getHTMLFiles(new File("./"));
        Set<String> stopWords = loadStopwords();

        mp = parseJSON();
        
        //final map containing the needed stemmed files that will be sent to the ranker
        Map<String, Map<String, Map<String, Double>>> processedMap = new HashMap<String, Map<String, Map<String, Double>>>();


        //System.out.println(query);
        //each word in original query splitted 
        String[] queryArray = query.split(" ");
        //each word in original query after stemming and removing stop words
        ArrayList<String> queryArrayStemmed = new ArrayList<String>();
        //each word in original query after removing stop words
        ArrayList<String> originalQueryArray = new ArrayList<String>();
        //remove stop words and stem the rest
        for(int i = 0; i < queryArray.length; i++){
            if(!stopWords.contains(queryArray[i])){
                //queryArray[i] = stemming(queryArray[i]);
                queryArrayStemmed.add(stemming(queryArray[i]));
                originalQueryArray.add(queryArray[i]);
            }   
        }
        //System.out.println(queryArrayStemmed);


        for(Map.Entry<String, Map<String, Map<String, Double>>> entry : mp.entrySet()){

            if(queryArrayStemmed.contains(entry.getKey())){
                processedMap.put(entry.getKey(), entry.getValue());
                for(Map.Entry<String, Map<String, Double>> originalWord : entry.getValue().entrySet())
                {
                    if(originalQueryArray.contains(originalWord.getKey()))
                    {
                        for(Map.Entry<String, Double> HTMLdoc : originalWord.getValue().entrySet())
                        {
                            processedMap.get(entry.getKey()).get(originalWord.getKey()).replace(HTMLdoc.getKey(), HTMLdoc.getValue() + 20);
                        }
                    }
                }
                //System.out.println(entry.getKey());
                //System.out.println(entry.getValue());
                
            } 
        }
        //System.out.println(processedMap);
        

        writeToFile(convertToJSON(processedMap).toString(), "processed.json");
        //ArrayList<String> files = new ArrayList<String>();
        return processedMap;
};

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
        BufferedWriter bw = new BufferedWriter(new FileWriter("./Query Processor Result/" + filename));
        bw.write(str);
        bw.close();
    }

    public static Map<String, Map<String, Map<String, Double>>> parseJSON() throws IOException, org.json.simple.parser.ParseException{
        //for each file
        JSONParser jsonParser = new JSONParser();
                 
        FileReader reader = new FileReader("./indexer result/index.json");
        //Read JSON file
        Object obj = jsonParser.parse(reader);
        
        //store the json object in hash map
        return (Map<String, Map<String, Map<String, Double>>>) obj;

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
    
}