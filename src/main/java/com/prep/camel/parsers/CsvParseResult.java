package com.prep.camel.parsers;

import com.prep.camel.models.BookImportRow;

public record CsvParseResult(BookImportRow row, String reason) {
    public boolean isValid() {
        return row != null;
    }
}
