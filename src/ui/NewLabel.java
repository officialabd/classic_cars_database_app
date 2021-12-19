package ui;

import javafx.scene.control.Label;

public class NewLabel extends Label {

    private final String id;

    public NewLabel(String string, String id) {
        super(string);
        this.id = id;
    }

    public String get_id() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
