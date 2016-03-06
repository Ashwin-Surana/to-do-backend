package io.vertx.example.todo.service.exception;

public class IllegalFieldException extends Exception {

    public IllegalFieldException(String field) {
        super("Field named \"" + field + "\" does not exist!");
    }
}
