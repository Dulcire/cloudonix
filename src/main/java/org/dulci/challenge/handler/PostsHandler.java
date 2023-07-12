package org.dulci.challenge.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.dulci.challenge.model.Resource;
import org.dulci.challenge.model.Response;
import org.dulci.challenge.repository.PostRepository;
import org.dulci.challenge.utils.CloudonixConstants;
import org.dulci.challenge.utils.UtilHelper;

public class PostsHandler {
    private static final Logger LOGGER = Logger.getLogger(PostsHandler.class.getName());
    private final PostRepository posts;
    @Inject
    public PostsHandler(PostRepository postRepository) {
        this.posts = postRepository;
    }


    /**
     * Method to get a value and lexical calculation of a word sent as a request. And save this new word in BD.
     * @param routingContext routingContext.
     */
    public void createResource(final RoutingContext routingContext) {
        LOGGER.info("Getting word from request.");
        final Resource resource = routingContext.body().asJsonObject().mapTo(Resource.class);
        LOGGER.info("Getting wordlist from .");
        final Future<Set<Resource>> wordlistDB = this.posts.findAll();
        wordlistDB.onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
                final Set<String> wordList = getWordList(asyncResult);
                LOGGER.info("Preparing response to be sent.");
                final JsonObject responseJson = prepareResponse(resource, wordList);
                if (!wordList.contains(resource.getText())) {
                    LOGGER.info("Persisting new word in database.");
                    saveResourceAndHandleResponse(resource, responseJson, routingContext);
                } else {
                    sendSuccessResponse(responseJson, routingContext);
                }
            } else {
                Throwable error = asyncResult.cause();
                error.printStackTrace();
                routingContext.response().setStatusCode(500).end("Internal Server Error");
            }
        });
    }

    /**
     * Method to save a new word in the DB and handled a response.
     *
     * @param resource       data from request.
     * @param responseJson   response to be sent.
     * @param routingContext routingContext.
     */
    private void saveResourceAndHandleResponse(Resource resource, JsonObject responseJson, RoutingContext routingContext) {
        LOGGER.info("Saving data in DB....");
        this.posts.save(Resource.builder().uuid(UUID.randomUUID().toString()).text(resource.getText()).build())
            .onSuccess(savedId -> {
                sendSuccessResponse(responseJson, routingContext);
            })
            .onFailure(throwable -> {
                throwable.printStackTrace();
                routingContext.response().setStatusCode(500).end("Internal Server Error");
            });
    }

    /**
     * Method to send a success response.
     *
     * @param responseJson   json with data response.
     * @param routingContext routingContext.
     */
    private void sendSuccessResponse(JsonObject responseJson, RoutingContext routingContext) {
        LOGGER.info("sending response back.");
        final HttpServerResponse response = routingContext.response();
        response.putHeader("Content-Type", "application/json");
        response.setStatusCode(200).end(responseJson.encode());
    }

    /**
     * Method to transform async result to a Set list of words.
     *
     * @param asyncResult async list result.
     * @return set list of strings.
     */
    private Set<String> getWordList(final AsyncResult<Set<Resource>> asyncResult) {
        LOGGER.info("Preparing word list.");
        final Set<Resource> result = asyncResult.result();
        return result.stream().map(Resource::getText).collect(Collectors.toSet());
    }

    /**
     * Method to prepare response to be sent.
     *
     * @param resource data request.
     * @param wordList list of words in DB.
     * @return Json to be sent as a request response.
     */
    private JsonObject prepareResponse(final Resource resource, final Set<String> wordList) {
        final String text = resource.getText();
        final Response responseValue = Response.builder()
            .value(UtilHelper.findClosestByValue(text, wordList))
            .lexical(UtilHelper.findClosestByLexical(text, wordList))
            .build();

        final JsonObject responseJson = new JsonObject();
        responseJson.put(CloudonixConstants.VALUE_RESPONSE, responseValue);
        LOGGER.info("Response prepared.");
        return responseJson;
    }


}
