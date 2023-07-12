package org.dulci.challenge;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.postgresql.ds.PGSimpleDataSource;


public class CloudonixService {

    private static final Logger LOGGER = Logger.getLogger(CloudonixService.class.getName());


    private static final String DB_URL = "db.url";
    private static final String DB_USER = "db.user";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB_POOL_MAX = "db.pool.max";

    private static PgPool getPgPool(Vertx vertx, JsonObject config) {
        PgConnectOptions connectOptions = PgConnectOptions.fromUri("postgresql://localhost:5432/cloudonix");
        connectOptions.setUser("cloudonix");
        connectOptions.setPassword("Cl0udon!x");
        return PgPool.pool(vertx, connectOptions, new PoolOptions().setMaxSize(20));
    }

    private static ConfigRetriever getConfigRetriever(Vertx vertx) {
        var config = new ConfigStoreOptions()
            .setType("file")
            .setFormat("properties")
            .setConfig(new JsonObject().put("path", "application.yaml"));
        return ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(config));
    }

    private static void runDbMigrations(DataSource dataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migrations")
            .load()
            .migrate();
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        getConfigRetriever(vertx).getConfig(json -> {
            JsonObject config = json.result();
            DeploymentOptions options = new DeploymentOptions()
                .setConfig(config);
            final Configuration dbConfig = setupDatabase(config);
            Injector injector = setupDependencies(vertx, config, dbConfig);
            vertx.deployVerticle(injector.getInstance(CloudonixVerticle.class), options);
        });
    }

    private static Injector setupDependencies(Vertx vertx,
                                              JsonObject config,
                                              Configuration dbConfig) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toInstance(vertx);
                bind(SqlClient.class).toInstance(getPgPool(vertx, config));
                bind(Configuration.class).toInstance(dbConfig);
                bind(CloudonixService.class);
                // Bind PgPool interface to the implementation
                bind(PgPool.class).toInstance(getPgPool(vertx, config));
            }
        });
    }

    private static Configuration setupDatabase(JsonObject config) {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/cloudonix");
        dataSource.setUser("cloudonix");
        dataSource.setPassword("Cl0udon!x");
        var defaultConfiguration = new DefaultConfiguration();
        defaultConfiguration.set(new DataSourceConnectionProvider(dataSource));
        defaultConfiguration.set(SQLDialect.POSTGRES);
        defaultConfiguration.set(new Settings().withRenderNameStyle(RenderNameStyle.AS_IS));
        runDbMigrations(dataSource);
        return defaultConfiguration;
    }
}
