package com.SE3.WLSB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class WlsbApplication {

	public static void main(String[] args) {
		SpringApplication.run(WlsbApplication.class, args);
	}

	@GetMapping("/api")
	public String api() {
		return "{\"_links\": {\"self\": \"/api\",\"schedule\": \"/api/schedule\"}}";
	}
	@GetMapping("/api/schedule")
	public String schedule(@RequestParam(value = "nap") boolean nap, @RequestParam(value = "age") String age) {
		
		String schedule = "";
		return String.format("{\"schedule\":[%s],\"_links\":{\"self\":\"/api/schedule\"}}", schedule);
	}
}
