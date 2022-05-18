package com.magnet.Magnet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.text.ParseException;
import java.util.*;
//import class phraseprocessor


public class PhraseSearching {
    //main function
    public static void main(String[] args) throws JSONException, IOException, FileNotFoundException, ParseException, org.json.simple.parser.ParseException {
        String phrase = "computer";
        phraseSearching(phrase);
    }

    //function to search for a phrase
    public static void phraseSearching(String phrase) throws JSONException, IOException, FileNotFoundException, ParseException, org.json.simple.parser.ParseException {
        
        //stop words from
        // Set<String> stopWords = QueryProcessor.loadStopwords();
        // Map<String, Map<String, Map<String, Double>>> mp = QueryProcessor.ToPhraseSearching(phrase);

        // String[] phraseArray = phrase.split(" ");
        // ArrayList<String> phraseArrayNoStopWords = new ArrayList<String>();

        // for(int i = 0; i < phraseArray.length; i++){
        //     if(!stopWords.contains(phraseArray[i])){
        //         phraseArrayNoStopWords.add(phraseArray[i]);
        //     }
        // }

        //read json file
        Map<String, Map<String, Map<String, Double>>> mp = new HashMap<String, Map<String, Map<String, Double>>>();
        Set<String> stopWords = QueryProcessor.loadStopwords();
        //System.out.println(stopWords);
        Map<String, Map<String, Map<String, Double>>> processedMap = new HashMap<String, Map<String, Map<String, Double>>>();


        String[] phraseArray = phrase.split(" ");
        //each word in original phrase after stemming and removing stop words
        ArrayList<String> phraseArrayStemmed = new ArrayList<String>();
        //each word in original phrase after removing stop words
        ArrayList<String> originalPhraseArray = new ArrayList<String>();
        //remove stop words and stem the rest
        for(int i = 0; i < phraseArray.length; i++){
            if(!stopWords.contains(phraseArray[i])){
                //phraseArray[i] = stemming(phraseArray[i]);
                phraseArrayStemmed.add(QueryProcessor.stemming(phraseArray[i]));
                originalPhraseArray.add(phraseArray[i]);
            }   
        }
        System.out.println(phraseArrayStemmed);

        for(Map.Entry<String, Map<String, Map<String, Double>>> entry : mp.entrySet()){
        
            if(phraseArrayStemmed.contains(entry.getKey())){
                //processedMap.put(entry.getKey(), entry.getValue());
                for(Map.Entry<String, Map<String, Double>> originalWord : entry.getValue().entrySet())
                {
                    if(originalPhraseArray.contains(originalWord.getKey()))
                    {
                        for(Map.Entry<String, Double> HTMLdoc : originalWord.getValue().entrySet())
                        {
                            //processedMap.get(entry.getKey()).get(originalWord.getKey()).replace(HTMLdoc.getKey(), HTMLdoc.getValue() + 20);
                            
                        }
                    }
                }
                //System.out.println(entry.getKey());
                //System.out.println(entry.getValue());
                
            } 
        }

        
    }
}
