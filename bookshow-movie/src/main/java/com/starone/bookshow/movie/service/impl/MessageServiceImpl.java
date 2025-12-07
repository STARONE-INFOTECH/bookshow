package com.starone.bookshow.movie.service.impl;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.starone.bookshow.movie.service.IMessageService;

public class MessageServiceImpl implements IMessageService{

    private final MessageSource messageSource;
    
    public MessageServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(String messageKey, Object... obj) {
        return messageSource.getMessage(messageKey, obj, LocaleContextHolder.getLocale());
    }
}
