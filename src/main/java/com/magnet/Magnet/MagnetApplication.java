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
	public String result(@RequestParam(name = "query", defaultValue = "") String query,
						 @RequestParam(name = "pageNum", defaultValue = "1") int pageNum, Model model) {
		//TODO Submit query to the database
		DataAccess dataAccess = new DataAccess();
		dataAccess.addQuery(query);
		// TODO send query to query processor
		// query processor return list of urls or filenames
		ArrayList<String> fileNames = new ArrayList<>();
		fileNames.add("00.html");
		fileNames.add("10.html");
		fileNames.add("20.html");
		fileNames.add("31.html");
		fileNames.add("312.html");
		fileNames.add("436.html");
		fileNames.add("572.html");
		fileNames.add("601.html");
		fileNames.add("692.html");
		fileNames.add("711.html");
		fileNames.add("771.html");
		fileNames.add("875.html");
		fileNames.add("913.html");
		fileNames.add("972.html");
		fileNames.add("1029.html");
		// TODO parse list of filenames to list of magnet links
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		SearchController searchController = new SearchController();
		searchController.fillSearchResultList(results, fileNames);
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

