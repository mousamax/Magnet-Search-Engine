package com.magnet.Magnet.Ranker;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RankerRelevance {
  public static void main(String[] args) {
    // * It should be LinkedHashMap
    Map<String, Map<String, Double>> mp = new LinkedHashMap<String, Map<String, Double>>();
    relevanceRanking(mp);
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
    // * For testing purposes

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
    // TODO Loop on relevanceMap and add url popularity

    
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
