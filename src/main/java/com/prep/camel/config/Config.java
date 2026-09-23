package com.prep.camel.config;

import com.prep.camel.models.ImportSummary;
import org.apache.camel.component.jackson3.JacksonDataFormat;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class Config {
    @Bean
    public IdempotentRepository bookIsbnIdempotentRepository() {
        return MemoryIdempotentRepository.memoryIdempotentRepository(200);
    }

    @Bean
    public JacksonDataFormat importSummaryJsonFormat(JsonMapper mapper) {
        return new JacksonDataFormat(mapper, ImportSummary.class);
    }
}
