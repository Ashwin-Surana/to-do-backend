package io.vertx.example.todo.application;


import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.example.todo.verticles.ToDoVerticle;

public class Application {
    public static void main(String args[]) {
        Vertx vertx = Vertx.vertx(new VertxOptions().setClustered(false));
        vertx.deployVerticle(new ToDoVerticle());
    }
}
