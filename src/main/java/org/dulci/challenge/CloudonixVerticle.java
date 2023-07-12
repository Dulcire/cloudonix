package org.dulci.challenge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgPool;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.dulci.challenge.handler.PostsHandler;
import org.dulci.challenge.repository.PostRepository;
import org.dulci.challenge.utils.CloudonixConstants;

public class CloudonixVerticle  extends AbstractVerticle {
    private static final Logger LOGGER = Logger.getLogger(CloudonixVerticle.class.getName());
    private final PgPool pgPool;

    @Inject
    public CloudonixVerticle(PgPool pgPool) {
        this.pgPool = pgPool;
    }

    @Override
    public void start(final Promise<Void> startPromise) {

        LOGGER.info("Starting HTTP server...");
        //Create a PgPool instance
        PostRepository postRepository = PostRepository.create(pgPool);
        final PostsHandler postHandlers = PostsHandler.create(postRepository);
        final Router router = routes(postHandlers);

        vertx.createHttpServer().requestHandler(router).listen(CloudonixConstants.HTTP_PORT);

        // Create the HTTP server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(CloudonixConstants.HTTP_PORT)
            .onSuccess(server -> {
                startPromise.complete();
                LOGGER.info(("HTTP server started on port " + server.actualPort()));
            })
            .onFailure(event -> {
                startPromise.fail(event);
                LOGGER.info(("Failed to start HTTP server:" + event.getMessage()));
            });
    }

    //create routes
    private Router routes(final PostsHandler handlers) {
        // Create a Router
        final Router router = Router.router(vertx);
        router.post(CloudonixConstants.BASE_URL).consumes("application/json").handler(BodyHandler.create()).handler(handlers::createResource);
        return router;
    }
}
