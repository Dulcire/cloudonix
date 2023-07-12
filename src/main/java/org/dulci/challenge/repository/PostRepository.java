package org.dulci.challenge.repository;

import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.dulci.challenge.model.Resource;

public class PostRepository {

    private static final Logger LOGGER = Logger.getLogger(PostRepository.class.getName());
    private final PgPool client;

    private PostRepository(PgPool _client) {
        this.client = _client;
    }

    //factory method
    public static PostRepository create(PgPool client) {
        return new PostRepository(client);
    }

    private static final Function<Row, Resource> MAPPER = (row) ->
        new Resource(
            row.getString("id"),
            row.getString("text")
        );

    public Future<Set<Resource>> findAll() {
        return client.query("SELECT * FROM resource ORDER BY id ASC")
            .execute()
            .map(rs -> StreamSupport.stream(rs.spliterator(), false)
                .map(MAPPER)
                .collect(Collectors.toSet())
            );
    }

    public Future<UUID> save(Resource data) {
        return client.preparedQuery("INSERT INTO resource(text) VALUES ($1) RETURNING (id)").execute(Tuple.of(data.getText()))
            .map(rs -> rs.iterator().next().getUUID("id"));
    }


}
