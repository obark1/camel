package com.prep.camel.mappers;

import com.prep.camel.dtos.BookDTO;
import com.prep.camel.entities.BookEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class BookEntityToBookDtoMapper implements Function<BookEntity, BookDTO> {
    @Override
    public BookDTO apply(BookEntity bookEntity) {
        return BookDTO.builder()
                .id(bookEntity.getId())
                .isbn(bookEntity.getIsbn())
                .title(bookEntity.getTitle())
                .author(bookEntity.getAuthor())
                .genre(bookEntity.getGenre())
                .build();
    }
}
