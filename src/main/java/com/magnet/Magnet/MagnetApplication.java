package com.magnet.Magnet;

import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;

@SpringBootApplication
@Controller
public class MagnetApplication {
	ArrayList<String> fileNames;
	ArrayList<SearchResult> results;
	boolean loaded = false;
	public static void main(String[] args) {
		SpringApplication.run(MagnetApplication.class, args);
	}

	// home page for the app with search bar
	@GetMapping("/")
	public String home() throws IOException, ParseException {
		if (!loaded) {
		//load query processing data
		QueryProcessor.mp = QueryProcessor.parseJSON();
		QueryProcessor.stopWords = QueryProcessor.loadStopwords();
		loaded = true;
		}
		// return greeting page
		return "greeting";
	}

	// result page from search
	@GetMapping("/result")
	public String result(@RequestParam(name = "query", defaultValue = "") String query,
						 @RequestParam(name = "pageNum", defaultValue = "1") int pageNum, Model model) throws IOException, ParseException {
		if(pageNum == 1)
		{
			//TODO Submit query to the database
			DataAccess dataAccess = new DataAccess();
			dataAccess.addQuery(query);
			//Get all queries
			ArrayList<String> pastQueries = dataAccess.getQueries();
			// TODO send query to query processor
			fileNames = QueryProcessor.QueryProcessing(query);
			// query processor return list of urls or filenames
			// TODO parse list of filenames to list of magnet links
			SearchController searchController = new SearchController();
			results = new ArrayList<>();
			searchController.fillSearchResultList(results, fileNames);
		}
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

