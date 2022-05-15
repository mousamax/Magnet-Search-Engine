package com.magnet.Magnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URISyntaxException;

@SpringBootApplication
@Controller
public class MagnetApplication {

	public static void main(String[] args) {
		SpringApplication.run(MagnetApplication.class, args);
	}

	//home page for the app with search bar
	@GetMapping("/")
	public String home() {
		//return greeting page
		return "greeting";
	}

	//result page from search
	@GetMapping("/result")
	public String result(@RequestParam(name="query", defaultValue="default") String query, Model model) {
		//TO DO send query to query processor
		//query processor return list of urls or filenames
		
		//return result page
		model.addAttribute("query", query);
		return "result";
	}

	@GetMapping("/normalize")
	public String hello(@RequestParam(value = "url", defaultValue = "https://mawdoo3.com/") String url , Model model) throws URISyntaxException {
		url = UrlUtils.normalizeUrl(url);
		//add model attribute
		model.addAttribute("url",url);
		return "result";
	}
}
