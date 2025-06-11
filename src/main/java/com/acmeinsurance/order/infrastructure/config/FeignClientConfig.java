package com.acmeinsurance.order.infrastructure.config;

import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters; // Não é o que queremos injetar, mas necessário para o wrapper
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter; // Para JSON

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FeignClientConfig {

    @Bean
    public Decoder feignDecoder() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        converters.add(new MappingJackson2HttpMessageConverter());

        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(converters);

        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }
}