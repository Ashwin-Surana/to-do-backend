package io.vertx.example.todo.service;

import io.vertx.example.todo.domain.ToDoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This service maintains a list of todos and provides CRUD (Create, Read, Update, Delete) operations.
 * This example holds the to-do list in memory for the simplicity of this example.
 */
public class ToDoService {

    public final static String ORDER = "order";
    public final static String TITLE = "title";
    public final static String COMPLETED = "completed";

    List<ToDoItem> toDoItemList;
    long counter;


    public ToDoService() {
        counter = 0;
        toDoItemList = new ArrayList<>();
    }

    public ToDoItem add(ToDoItem item) {
        item.setUrl(item.getUrl() + "/" + counter++);
        toDoItemList.add(item);
        return item;
    }

    public List<ToDoItem> getAll() {
        return toDoItemList;
    }

    public boolean remove(Predicate<ToDoItem> condition) {
        return toDoItemList.removeIf(condition);
    }

    public ToDoItem update(final String itemUrl, final Map<String, Object> updates) {
        ToDoItem item = getToDoItem(testItem -> testItem.getUrl().equals(itemUrl));
        if (item == null)
            return null;
        for (Map.Entry<String, Object> update : updates.entrySet()) {
            switch (update.getKey()) {
                case ORDER:
                    item.setOrder((Integer) update.getValue());
                    break;
                case TITLE:
                    item.setTitle(update.getValue().toString());
                    break;
                case COMPLETED:
                    item.setCompleted((Boolean) update.getValue());
                    break;
            }
        }
        return item;
    }

    public ToDoItem getToDoItem(Predicate<ToDoItem> condition) {
        for (ToDoItem item : toDoItemList) {
            if (condition.test(item))
                return item;
        }
        return null;
    }

    public void clearToDos() {
        toDoItemList.clear();
    }

}
