package com.prep.camel.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class BookDTO {
    private UUID id;
    private String isbn;
    private String title;
    private String author;
    private String genre;
}
