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
	public String result(@RequestParam(name = "query", defaultValue = "default") String query,
						 @RequestParam(name = "pageNum", defaultValue = "1") int pageNum, Model model) {
		// TO DO send query to query processor
		// query processor return list of urls or filenames

		SearchResult res1 = new SearchResult("https://www.google.com/",
		"Google", "Google Search Engine");
	
		SearchResult res2 = new SearchResult("https://www.youtube.com/",
		"Youtube", "Youtube");

		SearchResult res3 = new SearchResult("https://www.facebook.com/",
		"Facebook", "Facebook");
		SearchResult res4 = new SearchResult("https://www.facebook.com/",
				"LinkedIN", "750 million+ members | Manage your professional identity. Build and engage with your professional network. Access knowledge, insights and opportunities.");

		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		//loop 15 times
		for (int i = 0; i < 15; i++) {
			results.add(res1);
			results.add(res2);
			results.add(res3);
			if (i == 11) {
				results.add(res4);
			}
		}
		// return result page
		model.addAttribute("query", query);
		// create list of page numbers to be displayed in the pagination 10 per page
		int pageCount = results.size() / 10;
		if (results.size() % 10 != 0) {
			pageCount++;
		}
		ArrayList<Integer> pageNumbers = new ArrayList<Integer>();
		for (int i = 1; i <= pageCount; i++) {
			pageNumbers.add(i);
		}
		model.addAttribute("pageNumbers", pageNumbers);
		model.addAttribute("currentPageNum", pageNum);
		//send only 10 results per pageNum request
		int end = Math.min(pageNum * 10, results.size());
		model.addAttribute("results", results.subList(pageNum * 10 - 10, end));
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

