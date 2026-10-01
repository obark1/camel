package com.prep.camel.controllers;

import com.prep.camel.dtos.BookDTO;
import com.prep.camel.services.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @GetMapping()
    List<BookDTO> getBooks() {
        log.info("returning book info");
        return bookService.findAllBooks();
    }

    @GetMapping("/{id}")
    ResponseEntity<BookDTO> findBookById(@PathVariable UUID id) {
        return bookService.findBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
