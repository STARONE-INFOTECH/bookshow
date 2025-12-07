package com.starone.bookshow.movie.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MovieConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT) // Exact field mapping
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true); // Ignores null values

        return mapper;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Add base names for all language files (without the locale suffix)
        messageSource.addBasenames("classpath:message");
        // Set default encoding
        messageSource.setDefaultEncoding("UTF-8");
        // Use the message code as the default message if a key is not found
        messageSource.setUseCodeAsDefaultMessage(true); // Use with caution in production
        
        return messageSource;
    }

}
