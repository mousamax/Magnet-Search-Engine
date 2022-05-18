package com.magnet.Magnet;

import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class Crawler implements Runnable {

    public static int numThreads;
    public static int numUrls;
    public static String[] urls;
    public static ConcurrentHashMap<String, Boolean> visitedUrls;
    public static ConcurrentHashMap<String, Boolean> urlsToBeCrawled;
    // store compact version of the crawled pages to avoid storing the whole page content again
    public static ConcurrentHashMap<String, String> compactPages;
    // dataaccess object to access the database
    public static DataAccess dataAccess;

    LanguageDetector detector;
    public void run() {
        detector = new OptimaizeLangDetector().loadModels();
        // convert thread name to int
        int threadNum = Character.getNumericValue(Thread.currentThread().getName().charAt(0));
        // print out the thread name
        System.out.println("Thread " + threadNum + " is running.");
        // divide the urls into chunks and run the threads
        int chunkSize = numUrls / numThreads;
        int start = chunkSize * threadNum;
        //shift start by modulus for other threads
        if (threadNum != 0)
            start += numUrls % numThreads;
            int end = start + chunkSize;
            // shift end by modulus for first thread
            if (threadNum == 0)
                end += numUrls % numThreads;
            // print start and end of each thread
            System.out.println("Thread " + threadNum + ": " + start + " to " + end);
            for (int i = 0; i < numThreads; i++) {
                try {
                    if(start < end)
                     crawlWebPage(Arrays.copyOfRange(urls, start, end), visitedUrls, urlsToBeCrawled, compactPages);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

    //function to crawl a webpage pass visited urls and urls to be crawled
    public void crawlWebPage(String[] urls,
                                    ConcurrentHashMap<String, Boolean> visitedUrls,
                                    ConcurrentHashMap<String, Boolean> urlsToBeCrawled,
                                    ConcurrentHashMap<String,String> compactPages) throws IOException, URISyntaxException {
        //foreach url in urls to be crawled
        for (String url : urls) {
            //if url visited, this may happen if the program interrupted and started the crawling list again
            if (!visitedUrls.containsKey(url)) {
                System.out.println(Thread.currentThread().getName() + ":Crawling... " + url);
                // check if url is allowed to be crawled by robots.txt
                if (UrlUtils.isDisallowedByRobots(url)) {
                    System.out.println(Thread.currentThread().getName() + ":Disallowed by robots.txt: " + url);
                    //mark url as visited
                    visitedUrls.put(url, true);
                    // add url to database
                    dataAccess.addVisitedUrl(url);
                    continue;
                }
                //create a document object
                Document doc = null;
                try {
                    doc = Jsoup.connect(url).get();
                } catch (IOException e) {
                    System.out.println(Thread.currentThread().getName() + ":Can't Crawl... " + url);
                    //mark url as visited
                    visitedUrls.put(url, true);
                    // add url to database
                    dataAccess.addVisitedUrl(url);
                    continue;
                }// catch malformed url
                catch (Exception e) {
                    System.out.println(Thread.currentThread().getName() + ":Can't Crawl Malformed URL... " + url);
                    //mark url as visited
                    visitedUrls.put(url, true);
                    // add url to database
                    dataAccess.addVisitedUrl(url);
                    continue;
                }
                //chack doc html lang attribute with jsoup and if not english, skip
                if(!languageDetection(doc)) {
                    // print not english
                    System.out.println(Thread.currentThread().getName() + ":Not English... " + url);
                    //mark url as visited
                    visitedUrls.put(url, true);
                    // add url to database
                    dataAccess.addVisitedUrl(url);
                    continue;
                }
                String compactPage = UrlUtils.compactPage(doc.body().text());
                // check if compact version of body text is already in the compactPages
                if (compactPages != null && compactPages.containsKey(compactPage)) {
                    //mark url as visited
                    visitedUrls.put(url, true);
                    // add url to database
                    dataAccess.addVisitedUrl(url);
                    continue;
                }
                //get working directory
                String workingDir = System.getProperty("user.dir");
                String fileName = visitedUrls.size() + Thread.currentThread().getName();
                //create file if not exists to store the html
                File file = new File(workingDir+"\\html_files\\" + fileName + ".html");
                file.createNewFile();
                //create file writer
                FileWriter fw = new FileWriter(file);
                //write the html to the file
                fw.write(doc.html());
                //close the file writer
                fw.close();
                //add compact version of body text to compactPages
                compactPages.put(compactPage, url);
                //mark url as visited
                visitedUrls.put(url, true);
                // add url to database
                dataAccess.addVisitedUrlandCompactPagesFilename(url, compactPage, fileName);
                //get all hyperlinks from Document
                Elements hyperlinks = doc.select("a[href]");
                System.out.println( Thread.currentThread().getName() + ": Found " + hyperlinks.size() + " hyperlinks");
                ConcurrentHashMap<String, Boolean> urlsTobeSentToDB = new ConcurrentHashMap<>();
                //iterate through all hyperlinks
                for (Element link : hyperlinks) {
                    //normalize link
                    String linkUrl = link.attr("abs:href");
                    try {
                        linkUrl = UrlUtils.normalizeUrl(linkUrl);
                    } catch (URISyntaxException e) { }
                    //check if link is not visited
                    if (!visitedUrls.containsKey(linkUrl)) {
                            //add link to urls to be crawled if not exists
                        if(!urlsToBeCrawled.containsKey(linkUrl)) {
                            urlsToBeCrawled.put(linkUrl, true);
                            urlsTobeSentToDB.put(linkUrl, true);
                        }
                    }
                }
                //add urls to database if size is greater than 0
                if(urlsTobeSentToDB.size() > 0)
                    dataAccess.addUrlsToBeCrawled(urlsTobeSentToDB, url);
            }
        }
    }
    // language detection method for given Document
    public boolean languageDetection(Document doc) {
        Elements lang = doc.select("html[lang]");
        if(lang.size() > 0) {
            String langCode = lang.attr("lang");
            if(!langCode.equals("en")) {
                return false;
            }
            return true;
        }
        LanguageResult result = detector.detect(doc.body().text().substring(0, Math.min(doc.body().text().length(), 100)));
        if(!result.getLanguage().equals("en")) {
            return false;
        }
        return true;
    }
}
