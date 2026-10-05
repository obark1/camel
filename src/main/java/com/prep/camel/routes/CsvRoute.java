package com.prep.camel.routes;

import com.prep.camel.entities.ImportEventLogEntity;
import com.prep.camel.entities.RejectedImportRowEntity;
import com.prep.camel.mappers.BookImportRowToBookEntityMapper;
import com.prep.camel.models.BookImportRow;
import com.prep.camel.parsers.CsvParseResult;
import com.prep.camel.parsers.CsvRowParser;
import com.prep.camel.processors.WireTapPrepareProcessor;
import com.prep.camel.repositories.BookRepository;
import com.prep.camel.repositories.ImportEventLogRepository;
import com.prep.camel.repositories.RejectedImportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson3.JacksonDataFormat;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvRoute extends RouteBuilder {

    private final BookImportRowToBookEntityMapper mapper;
    private final CsvRowParser csvRowParser;
    private final BookRepository bookRepository;
    private final RejectedImportRepository rejectedImportRepository;
    private final ImportEventLogRepository importEventLogRepository;
    private final IdempotentRepository bookIsbnIdempotentRepository;
    private final WireTapPrepareProcessor wireTapPrepareProcessor;
    private final JacksonDataFormat jacksonDataFormat;

    @Override
    public void configure() {

        from("file:./import-inbox?noop=true")
                .routeId("csv-file-trigger")
                .to("direct:processCsvFile");

        from("direct:processCsvFile")
                .routeId("csv-handler")
                .onException(ConstraintViolationException.class)
                    .handled(true)
                    .log("Error occurred in this route: ${exception.message}")
                .end()

                .process(e -> {
                    e.setProperty("rejectedCount", new AtomicInteger(0));
                    e.setProperty("processedCount", new AtomicInteger(0));
                })
                .split(body().tokenize("\n", 1, true))
                .process(e -> {
                    CsvParseResult result = csvRowParser.parse(e);
                    e.setProperty("parseResult", result);
                    if (result.isValid()) {
                        e.getIn().setBody(result.row());
                    }
                })
                .choice()
                    .when(e -> e.getProperty("parseResult", CsvParseResult.class).isValid())
                        .idempotentConsumer(simple("${body.isbn}"), bookIsbnIdempotentRepository)
                            .process(this::handleMessage).id("saveValidBook")
                        .end()
                    .endChoice()
                    .otherwise()
                        .process(this::handleInvalidMessage)
                    .end()
                .end()
                .wireTap("direct:importSummary")
                .onPrepare(wireTapPrepareProcessor);

         from("direct:importSummary")
                 .log("Import Summary: ${body}")
                 .setHeader(KafkaConstants.KEY, simple("${body.filename}-${body.timestamp}"))
                 .marshal(jacksonDataFormat)
                 .to("kafka:book-library.import-events?brokers={{spring.kafka.bootstrap-servers}}");

        from("kafka:book-library.import-events?brokers={{spring.kafka.bootstrap-servers}}")
                .routeId("book-library.import-events-consume")
                .log("Body: ${body}")
                .idempotentConsumer(header(KafkaConstants.KEY), new MemoryIdempotentRepository())
                .process(exchange -> {
                    ImportEventLogEntity entity = new ImportEventLogEntity();
                    entity.setPayload(exchange.getIn().getBody(String.class));
                    entity.setReceivedAt(Instant.now());
                    exchange.getIn().setBody(entity);
                })
                .bean(importEventLogRepository, "save");
        }

    private void handleMessage(Exchange exchange) {
        bookRepository.save(mapper.apply(exchange.getIn().getBody(BookImportRow.class)));
        exchange.getProperty("processedCount", AtomicInteger.class).incrementAndGet();
    }

    private void handleInvalidMessage(Exchange exchange) {
        CsvParseResult result = exchange.getProperty("parseResult",  CsvParseResult.class);
        String reason = result.reason();
        String rawLine = exchange.getIn().getBody(String.class);
        RejectedImportRowEntity row = new RejectedImportRowEntity();
        row.setRawLine(rawLine);
        row.setReason(reason);
        row.setCreated(Instant.now());
        rejectedImportRepository.save(row);
        exchange.getProperty("rejectedCount", AtomicInteger.class).incrementAndGet();
    }

}
