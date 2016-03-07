package io.vertx.example.todo.application;


import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.example.todo.verticles.ToDoVerticle;

/**
 * Main class to easily run the application in IDE
 */
public class Application {

    public static void main(String args[]) {

        /**
         * Create an instance of the vertx to deploy the verticle
         *
         * Vertx can be instantiated with various options. Look in to
         * <link>
         *     http://vertx.io/docs/vertx-core/java/#_specifying_options_when_creating_a_vertx_object
         * </link>
         * for more infomration.
         */
        Vertx vertx = Vertx.vertx(new VertxOptions().setClustered(false));
        /**
         *  Once the vertx environment is setup. We can deploy the verticle
         */
        vertx.deployVerticle(new ToDoVerticle());
    }
}
