package org.dulci.challenge.handler.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import java.util.UUID;

public class TestUtils {

    public static Future<UUID> createFuture() {
        Promise<UUID> promise = Promise.promise();
        Vertx.vertx().setTimer(1000, timerId -> {
            UUID uuid = UUID.randomUUID();
            promise.complete(uuid);
        });
        return promise.future();
    }
}
