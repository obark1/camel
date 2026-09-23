package com.prep.camel.mappers;

import com.prep.camel.entities.BookEntity;
import com.prep.camel.models.BookImportRow;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class BookImportRowToBookEntityMapper implements Function<BookImportRow, BookEntity> {
    @Override
    public BookEntity apply(BookImportRow bookImportRow) {
        BookEntity book = new BookEntity();
        book.setIsbn(bookImportRow.isbn());
        book.setTitle(bookImportRow.title());
        book.setAuthor(bookImportRow.author());
        book.setGenre(bookImportRow.genre());
        return book;
    }
}
