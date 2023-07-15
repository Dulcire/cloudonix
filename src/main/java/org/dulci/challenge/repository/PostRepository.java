package org.dulci.challenge.repository;

import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import org.dulci.challenge.model.Resource;

public class PostRepository {

    private final PgPool client;

    @Inject
    public PostRepository(final PgPool pgPool) {
        this.client = pgPool;
    }

    //factory method
    public static PostRepository create(final PgPool client) {
        return new PostRepository(client);
    }

    private static final Function<Row, Resource> MAPPER = (row) ->
        new Resource(
            row.getString("id"),
            row.getString("text")
        );

    /**
     * Method to get a list of resources in DB.
     * @return Feature Set list of all words saved in resource table.
     */
    public Future<Set<Resource>> findAll() {
        return client.query("SELECT * FROM resource ORDER BY id ASC")
            .execute()
            .compose(rs -> {
                Set<Resource> resources = StreamSupport.stream(rs.spliterator(), false)
                    .map(MAPPER)
                    .collect(Collectors.toSet());
                return Future.succeededFuture(resources);
            })
            .onFailure(Throwable::printStackTrace);
    }

    /**
     * Method to save in a DB a new word in table resource.
     * @param data  Resource object with the request data.
     * @return Feature value of UUID get from DB.
     */
    public Future<UUID> save(final Resource data) {
        final Tuple params = Tuple.of(data.getUuid(), data.getText());
        return client.preparedQuery("INSERT INTO resource (id, text) VALUES ($1, $2) RETURNING id")
            .execute(params)
            .compose(result -> {
                Row row = result.iterator().next();
                UUID savedId = row.getUUID("id");
                return Future.succeededFuture(savedId);
            })
            .onFailure(Throwable::printStackTrace);
    }


}
