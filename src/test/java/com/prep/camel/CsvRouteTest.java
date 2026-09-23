package com.prep.camel;

import com.prep.camel.entities.BookEntity;
import com.prep.camel.entities.RejectedImportRowEntity;
import com.prep.camel.models.BookImportRow;
import com.prep.camel.repositories.BookRepository;
import com.prep.camel.repositories.RejectedImportRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.test.spring.junit6.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Module 7 skeleton — fill in the TODOs.
 * <p>
 * Sends CSV content directly to "direct:processCsvFile" rather than dropping a real
 * file on disk — this is why CsvRoute needed splitting into a file-trigger route and
 * a separate direct:-triggered processing route.
 */
@CamelSpringBootTest
@SpringBootTest
class CsvRouteTest {

    @Autowired
    ProducerTemplate producerTemplate;

    @Autowired
    CamelContext camelContext;

    @MockitoBean
    BookRepository bookRepository;

    @MockitoBean
    RejectedImportRepository rejectedImportRepository;

    @Autowired
    IdempotentRepository bookIsbnIdempotentRepository;

    @BeforeEach
    void resetIdempotentState() {
        bookIsbnIdempotentRepository.clear();
    }

    @Test
    void validRowsAreSavedAsBooks() {
        String csv = """
                isbn,title,author,genre
                9780140449136,The Odyssey,Homer,Classic
                9780743273565,The Great Gatsby,F. Scott Fitzgerald,Fiction
                """;

        producerTemplate.sendBody("direct:processCsvFile", csv);

        verify(bookRepository, times(2)).save(any(BookEntity.class));
        verify(rejectedImportRepository, never()).save(any());
    }

    @Test
    void invalidRowIsRejectedNotSaved() {
        String csv = """
                isbn,title,author,genre
                ,The Odyssey,Homer,Classic
                """; // blank isbn — should be rejected

        producerTemplate.sendBody("direct:processCsvFile", csv);

        verify(bookRepository, never()).save(any());
        verify(rejectedImportRepository, times(1)).save(any(RejectedImportRowEntity.class));

        ArgumentCaptor<RejectedImportRowEntity> captor =
                ArgumentCaptor.forClass(RejectedImportRowEntity.class);

        verify(rejectedImportRepository).save(captor.capture());

        RejectedImportRowEntity rejected = captor.getValue();

        assertThat(rejected.getReason())
                .containsIgnoringCase("isbn")
                .containsIgnoringCase("blank");
    }

    @Test
    void duplicateIsbnInSameFileIsDedupedByIdempotentConsumer() {
        String csv = """
                isbn,title,author,genre
                9780140449136,The Odyssey,Homer,Classic
                9780140449136,The Odyssey,Homer,Classic
                """; // same ISBN twice

        producerTemplate.sendBody("direct:processCsvFile", csv);

        verify(bookRepository, times(1)).save(any(BookEntity.class));
    }

    @Test
    void adviceWithReplacesSaveStepForPureRouteAssertion() throws Exception {
        // Weaves out the real save call entirely, replacing it with a mock endpoint —
        // this tests "did the route correctly decide to reach the save step",
        // independent of whether BookRepository/mapper work correctly at all.
        AdviceWith.adviceWith(camelContext, "csv-handler", route ->
                route.weaveById("saveValidBook")
                        .replace()
                        .to("mock:savedBook")
        );

        MockEndpoint mock = camelContext.getEndpoint("mock:savedBook", MockEndpoint.class);
        mock.expectedMessageCount(1);

        String csv = """
                isbn,title,author,genre
                9780140449136,The Odyssey,Homer,Classic
                """;

        producerTemplate.sendBody("direct:processCsvFile", csv);

        mock.assertIsSatisfied();

        Exchange exchange = mock.getReceivedExchanges().getFirst();

        BookImportRow book = exchange.getIn().getBody(BookImportRow.class);

        assertThat(book.isbn()).isEqualTo("9780140449136");
        assertThat(book.title()).isEqualTo("The Odyssey");
        assertThat(book.author()).isEqualTo("Homer");
        assertThat(book.genre()).isEqualTo("Classic");

    }
}