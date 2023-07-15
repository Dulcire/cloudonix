package org.dulci.challenge.handler;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.dulci.challenge.handler.util.TestUtils;
import org.dulci.challenge.model.Resource;
import org.dulci.challenge.model.Response;
import org.dulci.challenge.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostsHandlerTest {

    @Mock
    private PostRepository postsRepository;
    @Mock
    private RequestBody requestBody;
    @Mock
    private HttpServerResponse response;
    @Mock
    private RoutingContext routingContext;

    @InjectMocks
    private PostsHandler postsHandler;

    @Captor
    private ArgumentCaptor<Resource> resourceCaptor;

    @Captor
    private ArgumentCaptor<String> responseCaptor;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        final JsonObject mockBody = new JsonObject()
            .put("text", "rock");

        when(requestBody.asJsonObject()).thenReturn(mockBody);
        doReturn(requestBody).when(routingContext).body();

        doReturn(response).when(routingContext).response();
        doReturn(response).when(response).setStatusCode(anyInt());
    }

    @Test
    void evaluateCreateWordTest() {

        final Set<Resource> wordList = new HashSet<>();
        wordList.add(new Resource(UUID.randomUUID().toString(), "existingWord"));

        final Future<UUID> feature = TestUtils.createFuture();
        when(postsRepository.findAll()).thenReturn(Future.succeededFuture(wordList));
        when(postsRepository.save(any(Resource.class))).thenReturn(feature);        // Act
        postsHandler.evaluateWord(routingContext);

        verify(postsRepository).findAll();
        verify(postsRepository).save(resourceCaptor.capture());
        final Resource savedResource = resourceCaptor.getValue();
        assertNotNull(savedResource);
        assertEquals("rock", savedResource.getText());

    }

    @Test
    void evaluateWordEmptyListTest() throws InterruptedException {

        final Set<Resource> wordList = new HashSet<>();

        final Future<UUID> feature = TestUtils.createFuture();
        when(postsRepository.findAll()).thenReturn(Future.succeededFuture(wordList));
        when(postsRepository.save(any(Resource.class))).thenReturn(feature);        // Act
        postsHandler.evaluateWord(routingContext);
        Thread.sleep(2000);
        verify(response).end(responseCaptor.capture());


        final JsonObject jsonObject = new JsonObject(responseCaptor.getValue());
        final Response result = jsonObject.getJsonObject("response").mapTo(Response.class);
        assertNull(result.getLexical());
        assertNull(result.getValue());


    }

    @Test
    void evaluateWordTest() throws InterruptedException {

        final Set<Resource> wordList = new HashSet<>();
        wordList.add(new Resource(UUID.randomUUID().toString(), "mom"));
        wordList.add(new Resource(UUID.randomUUID().toString(), "dad"));
        wordList.add(new Resource(UUID.randomUUID().toString(), "soup"));

        final Future<UUID> feature = TestUtils.createFuture();
        when(postsRepository.findAll()).thenReturn(Future.succeededFuture(wordList));
        when(postsRepository.save(any(Resource.class))).thenReturn(feature);        // Act
        postsHandler.evaluateWord(routingContext);
        Thread.sleep(2000);
        verify(response).end(responseCaptor.capture());


        final JsonObject jsonObject = new JsonObject(responseCaptor.getValue());
        Response result = jsonObject.getJsonObject("response").mapTo(Response.class);
        assertEquals("soup", result.getLexical());
        assertEquals("mom", result.getValue());

    }

    @Test
    void evaluateWordSameSizeTest() throws InterruptedException {
        final JsonObject mockBody = new JsonObject()
            .put("text", "abba");

        when(requestBody.asJsonObject()).thenReturn(mockBody);
        doReturn(requestBody).when(routingContext).body();

        doReturn(response).when(routingContext).response();
        doReturn(response).when(response).setStatusCode(anyInt());
        final Set<Resource> wordList = new HashSet<>();
        wordList.add(new Resource(UUID.randomUUID().toString(), "baba"));
        wordList.add(new Resource(UUID.randomUUID().toString(), "abab"));
        wordList.add(new Resource(UUID.randomUUID().toString(), "aabb"));

        final Future<UUID> feature = TestUtils.createFuture();
        when(postsRepository.findAll()).thenReturn(Future.succeededFuture(wordList));
        when(postsRepository.save(any(Resource.class))).thenReturn(feature);        // Act
        postsHandler.evaluateWord(routingContext);
        Thread.sleep(2000);
        verify(response).end(responseCaptor.capture());


        final JsonObject jsonObject = new JsonObject(responseCaptor.getValue());

        Response result = jsonObject.getJsonObject("response").mapTo(Response.class);
        assertEquals("aabb", result.getLexical());
        assertEquals("baba", result.getValue());

    }


    @Test
    void evaluateWord_shouldSendSuccessResponseWhenWordInWordList() throws InterruptedException {

        final String id = UUID.randomUUID().toString();

        final Set<Resource> wordList = new HashSet<>();
        wordList.add(new Resource(id, "existingWord"));
        final Future<UUID> feature = TestUtils.createFuture();
        when(postsRepository.findAll()).thenReturn(Future.succeededFuture(wordList));
        when(postsRepository.save(any(Resource.class))).thenReturn(feature);
        postsHandler.evaluateWord(routingContext);
        Thread.sleep(2000);
        verify(postsRepository).findAll();
        verify(routingContext).response();
    }

    @Test
    void evaluateWord_shouldHandleErrorWhenRepositoryFails() {

        final JsonObject mockBody = new JsonObject()
            .put("text", "rock");

        when(requestBody.asJsonObject()).thenReturn(mockBody);
        doReturn(requestBody).when(routingContext).body();

        doReturn(response).when(routingContext).response();
        doReturn(response).when(response).setStatusCode(anyInt());
        final Throwable error = new RuntimeException("Database error");
        when(postsRepository.findAll()).thenReturn(Future.failedFuture(error));

        postsHandler.evaluateWord(routingContext);

        verify(postsRepository).findAll();
        verify(routingContext).response();
        verify(routingContext.response()).setStatusCode(500);
        verify(routingContext.response()).end("Internal Server Error");
    }


}
