package com.starone.bookshow.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {
	"com.starone.bookshow.movie",
	"com.starone.springcommon"
})
public class BookshowMovieApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookshowMovieApplication.class, args);
	}

}
