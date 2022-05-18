package com.magnet.Magnet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;

public class SearchController {
    //function take arraylist of html filename and fill up the arraylist of search result objects
    public void fillSearchResultList(ArrayList<SearchResult> searchResultList, ArrayList<String> htmlFileNameList) {
        DataAccess dataAccess = new DataAccess();
        for (int i = 0; i < htmlFileNameList.size(); i++) {
            String htmlFileName = htmlFileNameList.get(i);
            //get url from database where html filename is equal to the html filename column
            //remove the html extension from the filename
            String url = dataAccess.getUrl(htmlFileName.substring(0, htmlFileName.length() - 5));
            String title = "", description = "";
            try {
                // Read file from given filename
                File input = new File("./html_files/" + htmlFileName);
                // Use Jsoup to parse the file
                Document doc = Jsoup.parse(input, "UTF-8", "");
                // Save the title in a string
                title = doc.title();
                //add description from document object.
                Elements meta = doc.select("meta[name=description]");
                //add description
                description = meta.attr("content");
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchResultList.add(new SearchResult(url, title, description));
        }
    }
}
