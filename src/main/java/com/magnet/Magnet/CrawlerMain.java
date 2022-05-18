package com.magnet.Magnet;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlerMain {

    public static void main(String[] args)  {
        //ConcurrentHashMap to store normalizeUrl as key and page content as  value
        //ConcurrentHashMap is more efficient than hashtable but not thread safe as it is not synchronized
        //,but we don't care if two threads access the same key at the same time
        //ConcurrentHashMap for visited urls
        ConcurrentHashMap<String, Boolean> visitedUrls = new ConcurrentHashMap<>();
        //ConcurrentHashMap for urls to be crawled
        ConcurrentHashMap<String, Boolean> urlsToBeCrawled = new ConcurrentHashMap<>();
        // store compact version of the crawled pages to avoid storing the whole page content again
        ConcurrentHashMap<String, String> compactPages = new ConcurrentHashMap<>();
        // dataAccess object to access the database
        DataAccess dataAccess = new DataAccess();
        //  -------- load visitedUrls and compactPages from database --------
        dataAccess.getUrlsAndCompactPages(visitedUrls, compactPages);
        // -------- load urlsToBeCrawled from database --------
        dataAccess.getUrlsToBeCrawled(urlsToBeCrawled);
        // get number of crawled files
        int numOfCrawledFiles = dataAccess.getNumOfCrawledFiles();
        //print the number of crawled files
        System.out.println("Number of crawled files: " + numOfCrawledFiles);
        //create array list of urls to be crawled to divide work among threads
        String[] urlsToBeCrawledArray = {"https://edition.cnn.com","https://www.goal.com/en","https://www.bbc.com","https://www.encyclopedia.com"};
        if (urlsToBeCrawled.size() == 0) {//if cold start load the seed url
            for (String url : urlsToBeCrawledArray) {
                urlsToBeCrawled.put(url, true);
            }
        }
        // prompt the user to enter the number of threads
        System.out.println("Enter the number of threads: ");
        //scan user input
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        //get the number of threads
        int numOfThreads = Math.abs(scanner.nextInt());
        System.out.println("How many pages to crawl: ");
        java.util.Scanner scanner1 = new java.util.Scanner(System.in);
        int requestedPages = scanner1.nextInt();
        numOfThreads = Math.min(numOfThreads, 10);//max is 10 threads
        while (numOfCrawledFiles < requestedPages) {
            numOfCrawledFiles = dataAccess.getNumOfCrawledFiles();
            int remaining = requestedPages - numOfCrawledFiles;
            if(urlsToBeCrawled.size() == 0) {
                System.out.println("No more urls to be crawled");
                break;
            }
            //send remaining urls to crawlWebPage function
            if(remaining < urlsToBeCrawled.size())
                urlsToBeCrawledArray = Arrays.copyOfRange(urlsToBeCrawled.keySet().toArray(urlsToBeCrawledArray),0,remaining);
            else    //send all urls to crawlWebPage function
                urlsToBeCrawledArray = urlsToBeCrawled.keySet().toArray(urlsToBeCrawledArray);
            urlsToBeCrawled.clear();
            dataAccess.deleteUrlsToBeCrawled();
            //print out the crawled pages
            System.out.println("crawledPages NEW wave MultiThreading");
            Crawler.numThreads = numOfThreads;
            Crawler.urls = urlsToBeCrawledArray;
            System.out.println("remaining: " + remaining + ", urlsToBeCrawledArray.length: " + urlsToBeCrawledArray.length);
            Crawler.numUrls = Math.min(remaining, urlsToBeCrawledArray.length);
            //print numUrls
            System.out.println("numUrls: " + Crawler.numUrls);
            Crawler.urlsToBeCrawled = urlsToBeCrawled;
            Crawler.visitedUrls = visitedUrls;
            Crawler.compactPages = compactPages;
            Crawler.dataAccess = dataAccess;
            //create number of threads
            Thread[] threads = new Thread[Crawler.numThreads];
            for (int i = 0; i < Crawler.numThreads; i++) {
                threads[i] = new Thread(new Crawler());
                threads[i].setName(i + "");
            }
            //start threads
            for (int i = 0; i < Crawler.numThreads; i++) {
                threads[i].start();
            }
            //wait for all threads to finish
            for (int i = 0; i < Crawler.numThreads; i++) {
                try {
                    threads[i].join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Threads Finished ------------------------------------------------");
            //print urlsToBeCrawled
            System.out.println("urlsToBeCrawled in NEXT wave");
            //print size of urlsToBeCrawled
            System.out.println( "total hyperlinks(Normalized) found: " + urlsToBeCrawled.size());
        }
    }
}
