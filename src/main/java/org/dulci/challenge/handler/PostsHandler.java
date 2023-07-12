package org.dulci.challenge.handler;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.dulci.challenge.model.Resource;
import org.dulci.challenge.model.Response;
import org.dulci.challenge.repository.PostRepository;
import org.dulci.challenge.utils.CloudonixConstants;
import org.dulci.challenge.utils.UtilHelper;

public class PostsHandler {
    private final Set<String> wordList;
    private static final Logger LOGGER = Logger.getLogger(PostsHandler.class.getName());

    PostRepository posts;

    private PostsHandler(PostRepository _posts) {
        wordList = loadWordsFromFile();
        this.posts = _posts;
    }

    //factory method
    public static PostsHandler create(PostRepository posts) {
        return new PostsHandler(posts);
    }

    public void createResource(final RoutingContext routingContext) {

        final Resource resource = routingContext.body().asJsonObject().mapTo(Resource.class);
        final String text = resource.getText();
        final Response responseValue = Response.builder()
            .value(UtilHelper.findClosestByValue(text, wordList))
            .lexical(UtilHelper.findClosestByLexical(text, wordList))
            .build();

        final JsonObject responseJson = new JsonObject();
        responseJson.put(CloudonixConstants.VALUE_RESPONSE, responseValue);
        storeWordInFile(text);

        final HttpServerResponse response = routingContext.response();
        response.putHeader("Content-Type", "application/json");
        response.setStatusCode(200).end(responseJson.encode());

    }

    private Set<String> loadWordsFromFile() {
        try {
            return new HashSet<>(Files.readAllLines(Path.of(CloudonixConstants.WORDS_FILE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    private void storeWordInFile(final String word) {

        try {
            if (!wordList.contains(word)) {
                wordList.add(word);
                Files.write(Path.of(CloudonixConstants.WORDS_FILE), (word + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
