package com.starone.bookshow.show;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BookshowShowServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookshowShowServiceApplication.class, args);
	}

}
