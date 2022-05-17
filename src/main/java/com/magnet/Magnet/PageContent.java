package com.magnet.Magnet;

public class PageContent {
    //page title
    public String title;
    //description
    public String description;
    //keywords
    public String keywords;
    //body
    public String body;

    //print page content
    public void print() {
        System.out.println("Page content:");
        System.out.println("Title: " + title);
        System.out.println("Description: " + description);
        System.out.println("Keywords: " + keywords);
        System.out.println("Body: " + body);
    }
}
