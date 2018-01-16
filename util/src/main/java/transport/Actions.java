package transport;

/**
 * Class for sending object for change and an action
 */

import exceptions.FaRWrongActionException;
import exceptions.FaRWrongClassException;


public class Actions<T> {

    // actions: "delete", "add", "edit", "update"

    private T objectForChange;//
    private String action;

    public Actions(T objectForChange, String action) {

        this.objectForChange = objectForChange;
        this.action = action;
    }

    public T getObjectForChange() {

        return objectForChange;
    }

    public void setObjectForChange(T objectForChange) {

        if (objectForChange.getClass().getName().equals("Route") || objectForChange.getClass().getName().equals("Flight")) {

            this.objectForChange = objectForChange;
        }
        else throw new FaRWrongClassException("Wrong class of object (neither Route nor Flight)!");
    }

    public void setAction(String action) {

        if (action.equals("add") || action.equals("delete") || action.equals("edit") || action.equals("update")) {

            this.action = action;
        }
        else throw new FaRWrongActionException("Wrong action for object sent to server!");

    }

    public String getAction() {

        return action;
    }
}

