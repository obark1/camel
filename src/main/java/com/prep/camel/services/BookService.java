package com.prep.camel.services;

import com.prep.camel.dtos.BookDTO;
import com.prep.camel.mappers.BookEntityToBookDtoMapper;
import com.prep.camel.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookEntityToBookDtoMapper bookEntityToBookDtoMapper;

    public List<BookDTO> findAllBooks() {
        return bookRepository.findAll().stream()
                .map(bookEntityToBookDtoMapper)
                .toList();
    }

    public Optional<BookDTO> findBookById(final UUID id) {
        return bookRepository.findById(id).map(bookEntityToBookDtoMapper);
    }

}
