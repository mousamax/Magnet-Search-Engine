package com.magnet.Magnet;

import com.magnet.Magnet.Ranker.RankerRelevance;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.text.ParseException;
import java.util.*;


public class QueryProcessor{

    public static Map<String, Map<String, Map<String, Double>>> mp;
    public static Set<String> stopWords;

    public static void main(String[] args) throws JSONException, IOException, FileNotFoundException, ParseException, org.json.simple.parser.ParseException  {
        String query = "computer the Universe in engineer";
        QueryProcessor.mp = QueryProcessor.parseJSON();
		QueryProcessor.stopWords = QueryProcessor.loadStopwords();
        QueryProcessing(query);
    }
    public static Object[] QueryProcessing(String query) throws IOException, JSONException, org.json.simple.parser.ParseException{
                query = query.toLowerCase();
                boolean isQuerySearching = true;
                //ArrayList<String> files = getHTMLFiles(new File("./"));
                //final map containing the needed stemmed files that will be sent to the ranker
                Map<String, Map<String, Map<String, Double>>> processedMap = new HashMap<String, Map<String, Map<String, Double>>>();
                
                if(query.charAt(0) == '"' || query.charAt(query.length()-1) == '"'){
                    isQuerySearching = false;
                    query = query.substring(1, query.length()-1);
                }
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
                Map<String, Integer> DocumentsContainingPhrase = new HashMap<String, Integer>();
                Map<String,Double> HTMLdocuments_scores = new HashMap<String,Double>();
                for(int i = 0; i < queryArrayStemmed.size();i++){
                    
                    if(mp.containsKey(queryArrayStemmed.get(i))){
                        
                        processedMap.put(queryArrayStemmed.get(i), mp.get(queryArrayStemmed.get(i)));
                        if(mp.get(queryArrayStemmed.get(i)).containsKey(originalQueryArray.get(i))){
                            for(Map.Entry<String, Double> HTMLdoc : mp.get(queryArrayStemmed.get(i)).get(originalQueryArray.get(i)).entrySet())
                                {
                                    //phraseSearchingMap.put(queryArrayStemmed.get(i), mp.get(originalQueryArray.get(i)));
                                    //------------------phrase searching-------------------------------------
                                    if(DocumentsContainingPhrase.containsKey(HTMLdoc.getKey()) && !isQuerySearching)
                                    {
                                        DocumentsContainingPhrase.replace(HTMLdoc.getKey(), DocumentsContainingPhrase.get(HTMLdoc.getKey()) + 1);
                                    }
                                    else
                                    {
                                        DocumentsContainingPhrase.put(HTMLdoc.getKey(), 1);
                                    }
                                    //------------------end phrase searching-------------------------------------
                                    //files.add(HTMLdoc.getKey());
                                    // if(HTMLdocuments_scores.containsKey(HTMLdoc.getKey()))
                                    // {
                                    //     HTMLdocuments_scores.replace(HTMLdoc.getKey(), HTMLdocuments_scores.get(HTMLdoc.getKey()) + HTMLdoc.getValue());
                                    // }
                                    // else
                                    // {
                                    //     HTMLdocuments_scores.put(HTMLdoc.getKey(), HTMLdoc.getValue());
                                    // }
                                    processedMap.get(queryArrayStemmed.get(i)).get(originalQueryArray.get(i)).replace(HTMLdoc.getKey(), HTMLdoc.getValue() + 0.500);
                                }
                        }
                        // for(int j = 0; j < originalQueryArray.size();j++){
                            
                        // }

                    }
                }
                if(!isQuerySearching)
                {

                    for(int i = 0; i < queryArrayStemmed.size();i++){
                        Map<String, Double> temp = new HashMap<String, Double>();
                        Map<String, Map<String, Double>> temp2 = new HashMap<String, Map<String, Double>>();
                        temp2.put(originalQueryArray.get(i),temp);
                        phraseSearchingMap.put(queryArrayStemmed.get(i), temp2);
                        for(Map.Entry<String, Integer> HTMLdoc : DocumentsContainingPhrase.entrySet())
                        {
                            if(HTMLdoc.getValue() == queryArrayStemmed.size())
                            {
                                // Read file from given filename
                                File input = new File("./html_files/" + HTMLdoc.getKey() + ".html");
                                // Use Jsoup to parse the file
                                Document doc = Jsoup.parse(input, "UTF-8", "");
                                System.out.println(doc.body().text().equals(query));
                                if(doc.body().text().contains(query) || doc.title().contains(query)){
    
                                    Double val = mp.get(queryArrayStemmed.get(i)).get(originalQueryArray.get(i)).get(HTMLdoc.getKey());
                                    phraseSearchingMap.get(queryArrayStemmed.get(i)).get(originalQueryArray.get(i)).put(HTMLdoc.getKey(), val);
                                }
                            }
                        }
                    }
                    return RankerRelevance.relevanceRanking(phraseSearchingMap);
                }
                else {
                    return RankerRelevance.relevanceRanking(processedMap);
                }
                


                //writeToFile(convertToJSON(processedMap).toString(), "processed.json");
               //call ranker
       
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