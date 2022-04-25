package com.magnet.Magnet;

import org.netpreserve.urlcanon.Canonicalizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

	@GetMapping("/normalize")
	public String hello(@RequestParam(value = "url", defaultValue = "https://mawdoo3.com/") String url , Model model) throws URISyntaxException {
		url = UrlUtils.normalizeUrl(url);
		//add model attribute
		model.addAttribute("url",url);
		return "result";
	}
}
