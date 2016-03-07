package io.vertx.example.todo.verticles;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.example.todo.domain.ToDoItem;
import io.vertx.example.todo.service.ToDoService;
import io.vertx.example.todo.service.exception.IllegalFieldException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ToDoVerticle extends AbstractVerticle {
    /**
     * The following are the routes for the REST endpoints.
     * For each route, you can define a HTTP Method (GET,POST,DELETE, ....) and a
     * handler to process the incoming requests
     */
    private final String TODO_URL = "/todo";

    /**
     * In the below route :id is a path parameter.
     */
    private final String TODO_ID_URL = "/todo/:id";

    ToDoService toDoService;
    private Router router;

    @Override
    public void start() throws Exception {
        init();

        /**
         *  HttpMethod is defined for  route and a handler is assigned
         */

        router.get(TODO_URL).handler(this::getToDos);
        router.delete(TODO_URL).handler(this::clearToDo);
        router.post(TODO_URL).handler(this::createToDo);

        router.get(TODO_ID_URL).handler(this::getToDoWithId);
        router.delete(TODO_ID_URL).handler(this::deleteToDoWithId);
        router.patch(TODO_ID_URL).handler(this::updateToDoWithId);

        startServer();
    }

    private void init() {
        router = Router.router(vertx);
        toDoService = new ToDoService();
        setupCORS();
    }

    private void startServer() {
        /**
         *  The following coe creates an HttpServer which listens to a port
         *  as set in the system property
         */
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(Integer.getInteger("http.port"), System.getProperty("http.address", "0.0.0.0"));
    }

    /**
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

    /**
     * createToDo's implementation is invoked when the application receives a Http post
     * on the relative route "/todo"
     */
    private void createToDo(RoutingContext context) {
        HttpServerRequest req = context.request();
        HttpServerResponse response = context.response();

        /**
         * Body handler to read the request body.
         */
        req.bodyHandler(buffer -> {
            /**
             *  Json.decodeValue converts a Json String to an Object of the class passed
             *  to it as parameter.
             */
            ToDoItem item = Json.decodeValue(buffer.getString(0, buffer.length()), ToDoItem.class);
            if (item != null) {
                item.setUrl(context.request().absoluteURI());
                String responseJson = Json.encode(toDoService.add(item));
                /**
                 * After processing the request, send the response with appropriate HTTP Status.
                 * Since the creation was successful, we set the status code to CREATED and send
                 * the json string for the item created.
                 */
                response.setStatusCode(HttpResponseStatus.CREATED.code())
                        .end(responseJson);
                return;
            }
            response.setStatusCode(HttpResponseStatus.UNPROCESSABLE_ENTITY.code())
                    .end();
        });
    }

    private void getToDos(RoutingContext context) {
        context.response().setStatusCode(HttpResponseStatus.OK.code())
                .end(Json.encode(toDoService.getAll()));
    }

    private void getToDoWithId(RoutingContext context) {
        HttpServerResponse response = context.response();
        String toDoUrl = context.request().absoluteURI();
        Predicate<ToDoItem> condition = item -> item.getUrl().equals(toDoUrl);
        ToDoItem toDoItem = toDoService.getToDoItem(condition);
        if (toDoItem != null) {
            response.setStatusCode(HttpResponseStatus.OK.code())
                    .end(Json.encode(toDoItem));
        } else {
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .end();
        }
    }

    private void clearToDo(RoutingContext context) {
        toDoService.clearToDos();
        context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code())
                .end(Json.encode(new ArrayList<>()));
    }

    private void deleteToDoWithId(RoutingContext context) {
        String toDoUrl = context.request().absoluteURI();
        HttpServerResponse response = context.response();
        Predicate<ToDoItem> condition = toDoItem -> toDoItem.getUrl().equals(toDoUrl);
        if (toDoService.remove(condition)) {
            response.setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        } else {
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }
        context.response().end(Json.encode(new ArrayList<>()));
    }

    private void updateToDoWithId(RoutingContext context) {
        HttpServerRequest req = context.request();
        req.bodyHandler(buffer -> {
            HttpServerResponse response = context.response();
            String toDoItemUrl = req.absoluteURI();
            JsonObject jsonObject = new JsonObject(buffer.getString(0, buffer.length()));
            try {
                ToDoItem toDoItem = toDoService.update(toDoItemUrl, jsonObject.getMap());
                if (toDoItem != null) {
                    String toDoItemSerialized = Json.encode(toDoItem);
                    response.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(toDoItemSerialized.length()))
                            .write(toDoItemSerialized)
                            .setStatusCode(HttpResponseStatus.OK.code());
                } else {
                    response.setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                }
            } catch (IllegalFieldException e) {
                response.setStatusCode(HttpResponseStatus.UNPROCESSABLE_ENTITY.code());

            } finally {
                response.end();
            }

        });
    }

}
