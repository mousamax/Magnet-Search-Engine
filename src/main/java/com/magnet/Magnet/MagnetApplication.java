package com.magnet.Magnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;

@SpringBootApplication
@Controller
public class MagnetApplication {

	public static void main(String[] args) {
		SpringApplication.run(MagnetApplication.class, args);
	}

	// home page for the app with search bar
	@GetMapping("/")
	public String home() {
		// return greeting page
		return "greeting";
	}

	// result page from search
	@GetMapping("/result")
	public String result(@RequestParam(name = "query", defaultValue = "default") String query, Model model) {
		// TO DO send query to query processor
		// query processor return list of urls or filenames

		SearchResult res1 = new SearchResult("https://www.google.com/",
		"Google", "Google Search Engine");
	
		SearchResult res2 = new SearchResult("https://www.youtube.com/",
		"Youtube", "Youtube");

		SearchResult res3 = new SearchResult("https://www.facebook.com/",
		"Facebook", "Facebook");

		ArrayList<SearchResult> results = new ArrayList<SearchResult>();

		results.add(res1);
		results.add(res2);
		results.add(res3);

		
		// return result page
		model.addAttribute("query", query);
		model.addAttribute("res", res1);
		model.addAttribute("size", results.size());
		model.addAttribute("results", results);

		return "result";
	}


	@GetMapping("/normalize")
	public String hello(@RequestParam(value = "url", defaultValue = "https://mawdoo3.com/") String url, Model model)
			throws URISyntaxException {
		url = UrlUtils.normalizeUrl(url);
		// add model attribute
		model.addAttribute("url", url);
		return "result";
	}

}

class SearchResult {
	public String url, title, description;

	public SearchResult(String url, String title, String description) {
		this.url = url;
		this.title = title;
		this.description = description;
	}
}

