package org.dulci.challenge;

import com.google.inject.Injector;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.dulci.challenge.verticle.CloudonixVerticle;
import org.jooq.Configuration;


public class CloudonixService extends ServiceConfig {


    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();

        getConfigRetriever(vertx).getConfig(json -> {
            JsonObject config = json.result();
            DeploymentOptions options = new DeploymentOptions()
                .setConfig(config);
            final Configuration dbConfig = setupDatabase(config);
            Injector injector = setupDependencies(vertx, config, dbConfig);
            vertx.deployVerticle(injector.getInstance(CloudonixVerticle.class), options);
        });
    }
}
