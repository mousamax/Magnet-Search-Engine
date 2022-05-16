import java.util.HashMap;
import java.util.Map;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import org.*;
import org.json.JSONArray;
import org.json.JSONException;
//import org.javatuples.Pair;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;


public class QueryProcessor{

 
    public static void main(String[] args) throws JSONException, IOException, FileNotFoundException, ParseException, org.json.simple.parser.ParseException  {

        QueryProcessing();
    }
    public static ArrayList<String> QueryProcessing() throws IOException, JSONException, org.json.simple.parser.ParseException{
                //read json file
                Map<String, Map<String, Double>> mp = new HashMap<String, Map<String, Double>>();
                //ArrayList<String> files = getHTMLFiles(new File("./"));
                Set<String> stopWords = loadStopwords();
        
                //for each file
                JSONParser jsonParser = new JSONParser();
                 
                FileReader reader = new FileReader("index.json");
                //Read JSON file
                Object obj = jsonParser.parse(reader);
                
                //store the json object in hash map
                mp = (Map<String, Map<String, Double>>) obj;
                Map<String, Map<String, Double>> processedMap = new HashMap<String, Map<String, Double>>();
        
                String query = "Computer the Universe in engineer Department";
                System.out.println(query);
                String[] queryArray = query.split(" ");
                ArrayList<String> queryArrayStemmed = new ArrayList<String>();
        
                //remove stop words and stem the rest
                for(int i = 0; i < queryArray.length; i++){
                    if(!stopWords.contains(queryArray[i])){
                        queryArray[i] = stemming(queryArray[i]);
                        queryArrayStemmed.add(queryArray[i]);
                    }   
                }
                System.out.println(queryArrayStemmed);
                //System.out.println(queryArray);
                //loop through mp
                for(Map.Entry<String, Map<String, Double>> entry : mp.entrySet()){
        
                    if(queryArrayStemmed.contains(entry.getKey())){
                        System.out.println(entry.getKey());
                        System.out.println(entry.getValue());
                        processedMap.put(entry.getKey(), entry.getValue());
                    } 
                }
                System.out.println(processedMap);
                
        
                writeToFile(convertToJSON(processedMap).toString(), "./processed.json");
                ArrayList<String> files = new ArrayList<String>();
                return files;
    };
    public static JSONObject convertToJSON(Map<String, Map<String, Double>> mp) throws JSONException {
        
        //convert to JSON
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
    
}