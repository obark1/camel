package com.prep.camel.parsers;

import com.prep.camel.models.BookImportRow;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class CsvRowParser {
    public CsvParseResult parse(Exchange exchange) {
        String[] contents = exchange.getIn().getBody(String.class).split(",");
        if (contents.length != 4) {
            return new CsvParseResult(null, "Line has incorrect format");
        } else if (contents[0].isBlank()) {
            return new CsvParseResult(null, "Isbn is blank");
        } else if (contents[1].isBlank()) {
            return new CsvParseResult(null, "Title is blank");
        } else if (contents[2].isBlank()) {
            return new CsvParseResult(null, "Author is blank");
        } else if (contents[3].isBlank()) {
            return new CsvParseResult(null, "Genre is blank");
        }
        return new CsvParseResult(new BookImportRow(contents[0], contents[1], contents[2], contents[3]), null);
    }
}