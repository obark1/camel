package com.prep.camel.processors;

import com.prep.camel.models.ImportSummary;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Component("wireTapPrepareProcessor")
public class WireTapPrepareProcessor implements Processor {

    public void process(Exchange exchange) {
        int rejectedCount = exchange.getProperty("rejectedCount", AtomicInteger.class).get();
        int processedCount = exchange.getProperty("processedCount", AtomicInteger.class).get();
        String filename = exchange.getIn().getHeader("CamelFileName", String.class);
        Long lastModified = exchange.getIn().getHeader("CamelFileLastModified", Long.class);
        Instant timestamp = lastModified != null ? Instant.ofEpochMilli(lastModified) : Instant.now();

        exchange.getIn().setBody(new ImportSummary(rejectedCount, processedCount, filename, timestamp));
    }
}