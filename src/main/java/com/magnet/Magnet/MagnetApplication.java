package com.magnet.Magnet;

import org.netpreserve.urlcanon.Canonicalizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@SpringBootApplication
@RestController
public class MagnetApplication {

	public static void main(String[] args) {
		SpringApplication.run(MagnetApplication.class, args);
	}

	@GetMapping("/normalize")
	public String hello(@RequestParam(value = "url", defaultValue = "https://mawdoo3.com/") String url) throws URISyntaxException {
		url = UrlUtils.normalizeUrl(url);
		return String.format("<h1>Normalized URL:  <a href=%s>%s</a></h1>", url, url);
	}


}
