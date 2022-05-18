package com.magnet.Magnet.Ranker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.magnet.Magnet.DataAccess;

import org.json.JSONException;

public class RankerPopularity {
    public static void main(String[] args) throws JSONException {
        System.out.println("Enter the number of threads: ");
        // Get num of threads from user
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            int threadsNum = Math.abs(scanner.nextInt());
            calculatePopularity(threadsNum);
        }
        System.out.println("El7amdullah");
    }

    public static void calculatePopularity(int threadsNum) {
        // DataAccess object to access database
        DataAccess dataAccess = new DataAccess();
        System.out.println("Besmellah");

        // Getting my data ready
        int totalLinksNum = dataAccess.getCountOfUrls();
        // List of urls pointing to me
        ConcurrentHashMap<String, List<String>> urlsPointingToMe = new ConcurrentHashMap<String, List<String>>();
        // How many links I'm pointing to
        ConcurrentHashMap<String, Integer> urlsCountMap = new ConcurrentHashMap<String, Integer>();
        // Popularity of each url
        ConcurrentHashMap<String, Double> urlPopularityMap = new ConcurrentHashMap<String, Double>();
        dataAccess.getRelatedUrls(urlsPointingToMe, urlsCountMap, urlPopularityMap, totalLinksNum);
        Double dampingFactor = 0.85; // which is the click-through probability
        Integer constIteration = 2; // ? Check the constant number of iteration on pageRank algorithm

        // TODO Remove printing below
        // System.out.println(urlsPointingToMe);
        // System.out.println(urlsCountMap);
        // System.out.println("Before algorithm: " + urlPopularityMap);

        // Making a temporary map
        ConcurrentHashMap<String, Double> tempMap = new ConcurrentHashMap<String, Double>();
        tempMap.putAll(urlPopularityMap);

        for (int i = 0; i < constIteration; i++) {
            // Loop on all urls
            for (Map.Entry<String, List<String>> url : urlsPointingToMe.entrySet()) {
                // System.out.println("key " + url.getKey());
                // System.out.println("value" + url.getValue());
                // System.out.println("\nThese urls " + url.getValue()+ " are pointing to "+
                // url.getKey());

                List<String> linkedUrls = url.getValue();
                // System.out.println("List = "+ linkedUrls);
                Double calculatedPopu = 0.0;
                // Loop on all urls pointing to me
                for (int j = 0; j < linkedUrls.size(); j++) {
                    // System.out.println("popularity of "+linkedUrls.get(j)+" is
                    // "+urlPopularityMap.get(linkedUrls.get(j)));
                    // System.out.println("Count "+urlsCountMap.get(linkedUrls.get(j)));
                    // Check if this link has a popularity
                    if (urlPopularityMap.get(linkedUrls.get(j)) != null) {
                        calculatedPopu += urlPopularityMap.get(linkedUrls.get(j)) / urlsCountMap.get(linkedUrls.get(j));
                    }
                }
                // System.out.println("New popu" + calculatedPopu);
                // System.out.println("\nEnd of inner loop");
                // Add the damping factor add the end
                if (constIteration - 1 == i) {
                    calculatedPopu = (((1 - dampingFactor) / totalLinksNum) + dampingFactor * calculatedPopu)
                            * totalLinksNum * 10;
                }
                tempMap.put(url.getKey(), calculatedPopu);
            }
            // System.out.println(tempMap);
            urlPopularityMap.putAll(tempMap);
            // System.out.println("\n End of " + i + " iteration");
        }
        // System.out.println("After algorithm: " + urlPopularityMap);

        // Adding the popularity in the database
        System.out.println("Threading will start\n");
        // int threadsNum = 2;
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
    }

}
