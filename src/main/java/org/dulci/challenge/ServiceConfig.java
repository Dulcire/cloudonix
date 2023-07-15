package org.dulci.challenge;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import javax.sql.DataSource;
import org.apache.maven.model.Repository;
import org.flywaydb.core.Flyway;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.postgresql.ds.PGSimpleDataSource;

import static org.dulci.challenge.utils.CloudonixConstants.DB_PASSWORD;
import static org.dulci.challenge.utils.CloudonixConstants.DB_URL;
import static org.dulci.challenge.utils.CloudonixConstants.DB_USER;

public abstract class ServiceConfig {

    static PgPool getPgPool(final Vertx vertx, final JsonObject config) {
        final PgConnectOptions connectOptions = PgConnectOptions.fromUri(config.getString(DB_URL));
        connectOptions.setUser(config.getString(DB_USER));
        connectOptions.setPassword(config.getString(DB_PASSWORD));
        return PgPool.pool(vertx, connectOptions, new PoolOptions().setMaxSize(20));
    }

    static void runDbMigrations(final DataSource dataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migrations")
            .load()
            .migrate();
    }

    static Injector setupDependencies(final Vertx vertx,
                                              final JsonObject config,
                                              final Configuration dbConfig) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toInstance(vertx);
                bind(SqlClient.class).toInstance(getPgPool(vertx, config));
                bind(Configuration.class).toInstance(dbConfig);
                bind(CloudonixService.class);
                bind(Repository.class);
                bind(PgPool.class).toInstance(getPgPool(vertx, config));
            }
        });
    }

    static ConfigRetriever getConfigRetriever(final Vertx vertx) {
        var config = new ConfigStoreOptions()
            .setType("file")
            .setFormat("properties")
            .setConfig(new JsonObject().put("path", "application.properties"));
        return ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(config));
    }

    static Configuration setupDatabase(final JsonObject config) {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:" + config.getString(DB_URL));
        dataSource.setUser(config.getString(DB_USER));
        dataSource.setPassword(config.getString(DB_PASSWORD));
        final DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
        defaultConfiguration.set(new DataSourceConnectionProvider(dataSource));
        defaultConfiguration.set(SQLDialect.POSTGRES);
        Settings settings = new Settings();
        settings.setRenderNameCase(RenderNameCase.AS_IS);
        defaultConfiguration.set(settings);
        runDbMigrations(dataSource);
        return defaultConfiguration;
    }
}
