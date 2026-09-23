package com.prep.camel.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.camel.spi.IdempotentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/debug")
public class DebugController {

    private final IdempotentRepository bookIsbnIdempotentRepository;

    @GetMapping("/{isbn}")
    Boolean findBookById(@PathVariable String isbn) {
        return bookIsbnIdempotentRepository.contains(isbn);
    }
}
