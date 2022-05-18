package com.magnet.Magnet.Ranker;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.magnet.Magnet.DataAccess;

public class RankerRelevance {
  public static void main(String[] args) {
    // * It should be LinkedHashMap
    Map<String, Map<String, Map<String, Double>>> inputMap = new LinkedHashMap<String, Map<String, Map<String, Double>>>();
    relevanceRanking(inputMap);
  }

  public static Object[] relevanceRanking(Map<String, Map<String, Map<String, Double>>> inputMap) {
    // Map<String, Map<String, Map<String, Double>>>

    // Map<String, Map<String, Double>> mp
    // * For testing purposes
    Map<String, Map<String, Double>> mp = new LinkedHashMap<String, Map<String, Double>>();
    Map<String, Double> temp = new LinkedHashMap<String, Double>();
    temp.put("Example2.html", 0.2857142857142857);
    temp.put("Example3.html", 1.25);
    temp.put("Example.html", 2.2222222222222223);

    mp.put("engine", temp);

    Map<String, Double> temp2 = new LinkedHashMap<String, Double>();
    temp2.put("Example.html", 2.0);
    mp.put("engineer", temp2);
    inputMap.put("engin", mp);
    // * End of test

    DataAccess dataAccess = new DataAccess();
    // Create a map of fileNames and popularity
    LinkedHashMap<String, Double> filenamePopularityMap = new LinkedHashMap<String, Double>();
    dataAccess.getFilenameWithPopularity(filenamePopularityMap);
    // Create a map of fileNames and list of words inside it
    // Map<String, List<String>> fileNamesMap = new LinkedHashMap<String,
    // List<String>>();
    Map<String, Double> fileNameRelevanceMap = new LinkedHashMap<String, Double>();
    // Loop on the given map
    for (Map.Entry<String, Map<String, Map<String, Double>>> stemWord : inputMap.entrySet()) {
      // System.out.println(stemWord.getKey());
      // System.out.println(stemWord.getValue());
      for (Map.Entry<String, Map<String, Double>> unstemmedWord : stemWord.getValue().entrySet()) {
        for (Map.Entry<String, Double> file : unstemmedWord.getValue().entrySet()) {
          // System.out.println(file);
          // Getting a list of words in each file
          // List<String> wordsList = fileNamesMap.get(file.getKey());
          Double value = fileNameRelevanceMap.get(file.getKey());
          if (value == null) {
            // Did not find the fileName
            // * Stream.collect(Collectors.toList()); This makes the list modifiable
            // Add the TF/IDF to the fileNameRelevanceMap
            fileNameRelevanceMap.put(file.getKey(), file.getValue());
          } else {
            // Found the key
            Double previousRelevance = fileNameRelevanceMap.get(file.getKey());
            fileNameRelevanceMap.put(file.getKey(), previousRelevance + file.getValue());
          }
        }
      }
    }

    // Loop on relevanceMap and add url popularity
    for (Map.Entry<String, Double> file : fileNameRelevanceMap.entrySet()) {
      Double myPopularity = filenamePopularityMap.get(file.getKey());
      // It won't be null but to prevent
      if (myPopularity != null) {
        fileNameRelevanceMap.put(file.getKey(), file.getValue() + myPopularity);
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
}
