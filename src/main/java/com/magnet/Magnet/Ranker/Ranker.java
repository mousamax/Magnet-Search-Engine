package com.magnet.Magnet.Ranker;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.magnet.Magnet.DataAccess;
// import com.magnet.Magnet.UrlUtils;

// import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
// import org.jsoup.nodes.Element;
// import org.jsoup.select.Elements;

import org.json.JSONException;

public class Ranker {
    public static void main(String[] args) throws JSONException {

        // * It should be LinkedHashMap
        // Map<String, Map<String, Double>> mp = new LinkedHashMap<String, Map<String,
        // Double>>();
        // relevanceRanking(mp);
        calculatePopularity();
        System.out.println("El7amdullah");
    }

    public static Object[] relevanceRanking(Map<String, Map<String, Double>> mp) {
        // * For testing purposes
        Map<String, Double> temp = new LinkedHashMap<String, Double>();
        temp.put("Example3.html", 0.2);
        temp.put("Example2.html", 1.2);
        temp.put("Example.html", 1.1);
        mp.put("engin", temp);

        Map<String, Double> temp2 = new LinkedHashMap<String, Double>();
        temp.put("Example4.html", 1.2);
        temp2.put("Example2.html", 2.2);
        temp2.put("Example3.html", 2.1);
        mp.put("Dept", temp2);

        // Create a map of fileNames and list of words inside it
        Map<String, List<String>> fileNamesMap = new LinkedHashMap<String, List<String>>();
        Map<String, Double> fileNameRelevanceMap = new LinkedHashMap<String, Double>();
        // Loop on the given map
        for (Map.Entry<String, Map<String, Double>> word : mp.entrySet()) {
            for (Map.Entry<String, Double> file : word.getValue().entrySet()) {
                // System.out.println(file);
                // Getting a list of words in each file
                List<String> wordsList = fileNamesMap.get(file.getKey());
                if (wordsList == null) {
                    // Did not find the fileName
                    // * Stream.collect(Collectors.toList()); This makes the list modifiable
                    // Create a list and add the to the map with fileName as the key
                    wordsList = Stream.of(word.getKey()).collect(Collectors.toList());
                    fileNamesMap.put(file.getKey(), wordsList);
                    // Add the TF/IDF to the fileNameRelevanceMap
                    fileNameRelevanceMap.put(file.getKey(), file.getValue());
                } else {
                    // Found the fileName
                    // So add the word to its wordsList
                    wordsList.add(word.getKey());
                    Double previousRelevance = fileNameRelevanceMap.get(file.getKey());
                    fileNameRelevanceMap.put(file.getKey(), previousRelevance + file.getValue());
                }
            }
        }

        // System.out.println("\n----------PRINTING MAP OF FILE NAMES-------\n");
        // System.out.println(fileNamesMap);
        // System.out.println("\n----------PRINTING fileNameRelevanceMap-------");
        Map<String, Double> reverseSortedMap = new LinkedHashMap<String, Double>();
        fileNameRelevanceMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        // System.out.println(fileNameRelevanceMap);
        // System.out.println(reverseSortedMap);
        Object[] resultList = reverseSortedMap.keySet().toArray();
        // System.out.println("\n----------PRINTING ARRAY-------\n");
        // for (int i = 0; i < resultList.length; i++) {
        // System.out.println(resultList[i]);
        // }
        return resultList;
    }

    public static void calculatePopularity() {
        // DataAccess object to access database
        DataAccess dataAccess = new DataAccess();
        System.out.println("Besmellah");

        // Getting my data ready
        int totalLinksNum = dataAccess.getCountOfUrls();
        ConcurrentHashMap<String, List<String>> urlsPointingToMe = new ConcurrentHashMap<String, List<String>>();
        ConcurrentHashMap<String, Integer> urlsCountMap = new ConcurrentHashMap<String, Integer>();
        ConcurrentHashMap<String, Double> urlPopularityMap = new ConcurrentHashMap<String, Double>();
        dataAccess.getRelatedUrls(urlsPointingToMe, urlsCountMap, urlPopularityMap, totalLinksNum);
        Double dampingFactor = 0.85; // which is the click-through probability
        Integer constIteration = 2; // ? Check the constant number of iteration on pageRank algorithm
        // TODO Remove printing below
        // System.out.println(urlsPointingToMe);
        // System.out.println(urlsCountMap);
        System.out.println("Before algorithm: " + urlPopularityMap);
        // Making a temporary map
        ConcurrentHashMap<String, Double> tempMap = new ConcurrentHashMap<String, Double>();
        tempMap.putAll(urlPopularityMap);

        for (int i = 0; i < constIteration; i++) {
            for (Map.Entry<String, List<String>> url : urlsPointingToMe.entrySet()) {
                // System.out.println("key " + url.getKey());
                // System.out.println("value" + url.getValue());
                List<String> linkedUrls = url.getValue();
                Double calculatedPopu = 0.0;
                for (int j = 0; j < linkedUrls.size(); j++) {
                    // System.out.println(urlPopularityMap.get(linkedUrls.get(j)));
                    // System.out.println(urlsCountMap.get(linkedUrls.get(j)));
                    calculatedPopu += urlPopularityMap.get(linkedUrls.get(j)) / urlsCountMap.get(linkedUrls.get(j));

                }
                // System.out.println("New popu" + calculatedPopu);
                // System.out.println("\nEnd of inner loop");
                // Add the damping factor add the end
                if (constIteration - 1 == i) {
                    // System.out.println("key " + url.getKey());
                    // System.out.println("calc " + calculatedPopu + "\n");

                    // Setting the total probability of links
                    calculatedPopu = ((1 - dampingFactor) / totalLinksNum) + dampingFactor * calculatedPopu;
                }
                tempMap.put(url.getKey(), calculatedPopu);
            }
            // System.out.println(tempMap);
            urlPopularityMap.putAll(tempMap);
            // System.out.println("\n End of " + i + " iteration");

        }
        System.out.println("After algorithm: " + urlPopularityMap);
        // TODO use the urlPopularityMap to input the popularity in the database
        // dataAccess.addUrlsPopularity(urlPopularityMap);
        System.out.println("Threading will start\n");
        int threadsNum = 2;
        // create number of threads
        Thread[] threads = new Thread[threadsNum];
        for (int i = 0; i < threadsNum; i++) {
            threads[i] = new Thread(new RankerRunner(threadsNum, totalLinksNum, urlPopularityMap));
            threads[i].setName(i + "");
        }
        // start threads
        for (int i = 0; i < threadsNum; i++) {
            threads[i].start();
        }
        // wait for all threads to finish
        for (int i = 0; i < threadsNum; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\nThreading ended");

        // Thread t1 = new Thread(mult);
        // t1.setName("1");
        // Thread t2 = new Thread(mult);
        // t2.setName("2");
        // ConcurrentHashMap<String, String> urlsMap = new ConcurrentHashMap<>();
        // // get the map of urls and filenames
        // System.out.println("call db");
        // dataAccess.getUrlsAndFilenames(urlsMap);
        // System.out.println(urlsMap);
        // // int result = dataAccess.getUrlsCount();
        // int result = 3;
        // System.out.print(result);
    }

}
