package com.magnet.Magnet;

import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public final class UrlUtils {
    // concurrent hash map for storing robots.txt for each host, it will speed up the crawling process
    private static final ConcurrentHashMap<String, SimpleRobotRules> robots = new ConcurrentHashMap<>();
    private UrlUtils() {
        // nothing here ...
    }
    public static boolean isDisallowedByRobots(String url) throws URISyntaxException, IOException {
        // get the host name from the link
        URI uri = new URI(url);
        String host = uri.getHost();
        // check if the host is already in the robots.txt map
        if (robots.containsKey(host)) {
            // if yes, get the rules from the map
            SimpleRobotRules rules = robots.get(host);
            // check if the link is disallowed by the rules
            return rules != null && !rules.isAllowed(url);
        }
        // get the robots.txt file
        String robotsURL = new URL("http://" + host + "/robots.txt").toString();
        // print the robots.txt URL
        //System.out.println("robots.txt URL: " + robotsURL);
        // download the robots.txt file with Jsoup with lines
        //create a document object
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() + ":Can't Crawl... " + url);
        }// catch malformed url
        catch (Exception e) {
            System.out.println(Thread.currentThread().getName() + ":Can't Crawl Malformed URL... " + url);
        }
        String robotsTxt = doc.body().text();
        // for each two white space in the robots.txt file add a new line
        String[] lines = robotsTxt.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i+=2) {
            if (i < lines.length - 1) {
                sb.append(lines[i] +" "+lines[i+1] + "\n");
            }
            else {
                break;
            }
        }
        robotsTxt = sb.toString();
        // convert the robots.txt file to raw bytes
        byte[] bytes = robotsTxt.getBytes();
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        // get Content-Type from robots.txt with Jsoup
        String contentType = "text/plain";
        SimpleRobotRules rules = parser.parseContent(robotsURL, bytes, contentType, "*");
        // add the robots.txt to the hash map
        robots.put(host, rules);
        return rules != null && !rules.isAllowed(url);
    }
    public static  String compactPage(String page) {
        // if body length less than 100 characters, get compact string and store in compactPages
        // compact strings are enabled by default in Java 9
        // remove white spaces from the page
        page = page.replaceAll("\\s+", "");
        String compactPage = page.trim().toLowerCase(Locale.ROOT);
        if (compactPage.length() > 200) {
            // get compact version of body text by picking out the chars at multiple positions
            StringBuilder sb = new StringBuilder();
            //for loop through all the chars in the body text
            int selector = 3;
            for (int i = 1; i < 200; i++) {
                //add the chars at multiple positions to the string builder
                sb.append(compactPage.charAt(Math.abs(selector+i) % compactPage.length()));
                selector = Math.abs(selector) % compactPage.length();
                selector *= i;
            }
            //add compact version of body text to compactPages
            compactPage = sb.toString();
        }
        return compactPage;
    }
    public static String normalizeUrl(String url) throws URISyntaxException {
        URI result1;
        try {
            URI uri = new URI(url);

            String scheme = uri.getScheme();

            if (scheme == null) {
                throw new RuntimeException("URL scheme is required.");
            }

            String user = uri.getUserInfo();
            String host = uri.getHost();

            int port = normalizePort(scheme, uri.getPort());
            String path = normalizePath(uri.getPath());
            String query = normalizeQuery(uri.getQuery());
            String fragment = normalizeFragment(uri.getFragment());

            result1 = new URI(scheme, user, host, port, path, query, fragment);
        } catch (Exception e) {
            //second layer of normalization if first layer failed
            Canonicalizer.WHATWG.canonicalize(ParsedUrl.parseUrl(url));
            return normalizeSectionsRef(url);
        }
        try {
            //second layer of normalization
            String resultString = result1.toString();
            Canonicalizer.WHATWG.canonicalize(ParsedUrl.parseUrl(resultString));
            return normalizeSectionsRef(resultString);
        }
        catch (Exception e) {
            //not possible to normalize
            return normalizeSectionsRef(url);
        }
    }

    private static String normalizeSectionsRef(String url) {
        // if url contains #, remove the #
        if (url.contains("#")) {
            if(url.lastIndexOf("#") > url.lastIndexOf("/")) {
                //if there is a # before last /, remove the #
                return url.substring(0, url.lastIndexOf("#"));
            }
        }
        return url;
    }
    private static int normalizePort(String scheme, int port) {
        switch (port) {
            case 80:
                if ("http".equals(scheme)) {
                    return -1;
                }
                break;

            case 443:
                if ("https".equals(scheme)) {
                    return -1;
                }
                break;
        }
        return port;
    }

    private static String normalizePath(String path) {
        String result = removeDuplicates(path, '/');
        if (result == null || result.isEmpty()) {
            return null;
        }
        int length = result.length();
        char value = result.charAt(length - 1);
        if (value == '/') {
            return result.substring(0, length - 1);
        }
        return result;
    }

    private static String normalizeQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String[] parts = query.split("&");
        if (parts.length > 1) {
            Arrays.sort(parts);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length; ++i) {
                String part = parts[i];
                if (part.isEmpty()) {
                    continue;
                }
                int length = builder.length();
                if (length > 0) {
                    builder.append("&");
                }
                builder.append(part);
            }
            return builder.toString();
        }
        return query;
    }

    private static String normalizeFragment(String fragment) {
        if (fragment == null || fragment.isEmpty()) {
            return null;
        }
        return fragment;
    }

    private static String removeDuplicates(String text, char character) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        int duplicatesCount = 0;
        int textLength = text.length();
        for (int i = 0; i < textLength; ++i) {
            char value = text.charAt(i);
            if (value == character) {
                duplicatesCount += 1;
                if (duplicatesCount > 1) {
                    continue;
                }
            } else {
                duplicatesCount = 0;
            }
            builder.append(value);
        }
        return builder.toString();
    }
}