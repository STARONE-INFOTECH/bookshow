package com.starone.bookshow.person;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.starone.bookshow.person",
	"com.starone.springcommon"
})
public class BookshowPersonServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookshowPersonServiceApplication.class, args);
	}

}
