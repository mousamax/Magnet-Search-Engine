package com.magnet.Magnet.Ranker;

import java.util.concurrent.ConcurrentHashMap;

import com.magnet.Magnet.DataAccess;

public class RankerRunner implements Runnable {
  private int numThreads;
  private int numUrls;
  private String[] urlsArray;
  private Double[] popularitiesArray;

  public RankerRunner(int threadsNum, int urlsNum, ConcurrentHashMap<String, Double> urlPopularityMap) {
    this.numThreads = threadsNum;
    this.numUrls = urlsNum;
    urlsArray = urlPopularityMap.keySet().toArray(new String[0]);
    popularitiesArray = urlPopularityMap.values().toArray(new Double[0]);
  }

  @Override
  public void run() {
    // Auto-generated method stub
    DataAccess dataAccess = new DataAccess();
    // convert thread name to int
    int threadNum = Character.getNumericValue(Thread.currentThread().getName().charAt(0));
    // print out the thread name
    System.out.println("Thread " + threadNum + " is running.");
    // divide the urls into chunks and run the threads
    int chunkSize = numUrls / numThreads;
    int start = chunkSize * threadNum;
    // shift start by modulus for other threads
    if (threadNum != 0) {
      start += numUrls % numThreads;
    }
    int end = start + chunkSize;
    // shift end by modulus for first thread
    if (threadNum == 0) {
      end += numUrls % numThreads;
    }
    // print start and end of each thread
    System.out.println("Thread " + threadNum + ": " + start + " to " + end);
    if (start < end) {
      dataAccess.addUrlsPopularity(urlsArray, popularitiesArray, start, end);
    }
  }

}
