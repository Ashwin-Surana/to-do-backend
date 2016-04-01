package io.vertx.example.todo.verticles;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.example.todo.domain.ToDoItem;
import io.vertx.example.todo.service.ToDoService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ToDoVerticle extends AbstractVerticle {
    /*
     * The following are the routes for the REST endpoints.
     * For each route, you can define a HTTP Method (GET,POST,DELETE, ....) and a
     * handler to process the incoming requests
     */
    private final String TODO_URL = "/todo";

    /*
     * In the below route :id is a path parameter.
     */
    private final String TODO_ID_URL = "/todo/:id";

    ToDoService toDoService;
    private Router router;

    @Override
    public void start() throws Exception {
        init();
        setRoutes();
        startServer();
    }

    private void init() {
        router = Router.router(vertx);
        toDoService = new ToDoService();
        setupCORS();
    }

    private void setRoutes() {
        /*
         *  HttpMethod is defined for route and a handler is assigned
         */

        router.get(TODO_URL).handler(this::getAllToDo);
        router.delete(TODO_URL).handler(this::deleteAllToDo);
        router.post(TODO_URL).handler(this::createToDo);

        router.get(TODO_ID_URL).handler(this::getToDo);
        router.delete(TODO_ID_URL).handler(this::deleteToDo);
        router.patch(TODO_ID_URL).handler(this::updateToDo);
    }

    private void startServer() {
        /*
         *  The following code creates an HttpServer which listens to a port
         *  as set in the system property
         */
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(Integer.getInteger("http.port"), System.getProperty("http.address", "0.0.0.0"));
    }

    /*
     * For understanding on CORS, you may find the below resources helpful
     * 1. http://enable-cors.org/
     * 2. http://www.html5rocks.com/en/tutorials/cors/
     * 3. https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
     */

    private void setupCORS() {
        Set<HttpMethod> toDoUrlMethodSet = new HashSet<>(Arrays.asList(HttpMethod.GET,
                HttpMethod.DELETE, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.OPTIONS));

        Set<HttpMethod> toDoIdUrlMethodSet = new HashSet<>(Arrays.asList(HttpMethod.GET,
                HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.OPTIONS));

        router.route(TODO_URL).handler(CorsHandler.create("*")
                .allowedMethods(toDoUrlMethodSet)
                .allowedHeader("Content-Type"));

        router.route(TODO_ID_URL).handler(CorsHandler.create("*")
                .allowedMethods(toDoIdUrlMethodSet)
                .allowedHeader("Content-Type"));
    }

    /*
     * createToDo's implementation is invoked when the application receives a Http post
     * on the relative route "/todo"
     */
    private void createToDo(RoutingContext context) {


        /*
         * Body handler to read the request body.
         */
        context.request().bodyHandler(buffer -> {
            /*
             *  Json.decodeValue converts a Json String to an Object of the class passed
             *  to it as parameter.
             */
            ToDoItem item = Json.decodeValue(buffer.getString(0, buffer.length()), ToDoItem.class);
            item.setUrl(context.request().absoluteURI());
             /*
              * After processing the request, send the response with appropriate HTTP Status.
              * Since the creation was successful, we set the status code to CREATED and send
              * the json string for the item created.
              */
            context.response().setStatusCode(HttpResponseStatus.CREATED.code())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encode(toDoService.add(item)));

        });
    }

    /*
     * Echos JsonArray of todolist items
     */
    private void getAllToDo(RoutingContext context) {
        context.response().setStatusCode(HttpResponseStatus.OK.code())
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encode(toDoService.getAll()));
    }

    /*
     * Echos Json of the todo item
     */
    private void getToDo(RoutingContext context) {
        HttpServerResponse response = context.response();
        String toDoUrl = context.request().absoluteURI();
        Predicate<ToDoItem> condition = item -> item.getUrl().equals(toDoUrl);
        ToDoItem toDoItem = toDoService.getToDoItem(condition);
        if (toDoItem != null) {
            response.setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encode(toDoItem));
        } else {
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .end();
        }
    }

    /*
     * Clears the todo list
     */
    private void deleteAllToDo(RoutingContext context) {
        toDoService.clearToDos();
        context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code())
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encode(new ArrayList<>()));
    }

    /*
     * Deletes todo for the requested url
     */
    private void deleteToDo(RoutingContext context) {
        String toDoUrl = context.request().absoluteURI();
        Predicate<ToDoItem> condition = toDoItem -> toDoItem.getUrl().equals(toDoUrl);
        if (toDoService.remove(condition)) {
            context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encode(new ArrayList<>()));
        } else {
            context.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .end();
        }

    }

    /*
     * Updates to do for the requested url
     */
    private void updateToDo(RoutingContext context) {
        HttpServerRequest req = context.request();
        req.bodyHandler(buffer -> {
            String toDoItemUrl = req.absoluteURI();
            JsonObject jsonObject = new JsonObject(buffer.getString(0, buffer.length()));
            ToDoItem toDoItem = toDoService.update(toDoItemUrl, jsonObject.getMap());
            if (toDoItem != null) {
                context.response()
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encode(toDoItem));
            } else {
                context.response()
                        .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                        .end();
            }
        });
    }
}
