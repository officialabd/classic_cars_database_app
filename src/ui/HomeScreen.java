package ui;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import databasemanager.Item;
import databasemanager.Manager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import schema.Schema;
import schema.attribute.Attribute;

public class HomeScreen {

    private final double WIDTH, HEIGHT;
    private final String TITLE;
    private final boolean MAXIMIZED_SCREEN;
    private Manager mgr;
    private ArrayList<Schema> tables;
    private Stage stg;
    private Scene scene;
    private Pane root;
    private VBox left_side;
    private VBox mid_side;
    private static TextArea console;
    private VBox previuos_state;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final boolean FULL_SCREEN = true;
    public static final boolean MINI_SCREEN = false;

    public HomeScreen(Stage stg, String TITLE, Manager mgr) {
        this(stg, TITLE, Screen.getPrimary().getBounds().getWidth() / 2,
                Screen.getPrimary().getBounds().getHeight() / 2, mgr);
    }

    public HomeScreen(Stage stg, String TITLE, boolean MAXIMIZED_SCREEN, Manager mgr) {
        this(stg, TITLE, Screen.getPrimary().getBounds().getWidth() / 2,
                Screen.getPrimary().getBounds().getHeight() / 2, MAXIMIZED_SCREEN, mgr);
    }

    public HomeScreen(Stage stg, String TITLE, double WIDTH, double HEIGHT, Manager mgr) {
        this(stg, TITLE, WIDTH, HEIGHT, false, mgr);
    }

    public HomeScreen(Stage stg, String TITLE, double WIDTH, double HEIGHT, boolean MAXIMIZED_SCREEN, Manager mgr) {
        this.MAXIMIZED_SCREEN = MAXIMIZED_SCREEN;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.TITLE = TITLE;
        this.stg = stg;
        this.mgr = mgr;
        this.tables = this.mgr.get_tables();
        init();
        buildPage();
    }

    private void init() {
        root = new Pane();
        scene = new Scene(root, WIDTH, HEIGHT);
        // scene.getStylesheets().add("style/style.css");
        stg.setMaximized(MAXIMIZED_SCREEN);
        stg.setTitle(TITLE);
        stg.setScene(scene);
    }

    private void buildPage() {
        buildLeft();
        buildMiddle();
    }

    private void buildLeft() {
        left_side = new VBox(10);
        left_side.setPrefWidth(290);
        left_side.setPadding(new Insets(10.0));
        left_side.prefHeightProperty().bind(scene.heightProperty().divide(1.01));

        buildTablesMenu();
        buildConsole();

        root.getChildren().add(left_side);
    }

    private void buildConsole() {
        Label console_title = new Label("Console");
        console_title.setStyle("-fx-font: bold 12pt 'Arial'");

        console = new TextArea();
        console.setEditable(false);
        console.prefHeightProperty().bind(left_side.heightProperty().divide(2.0));
        console.setWrapText(true);

        VBox tablesGroup = new VBox(10, console_title, console);
        tablesGroup.prefHeightProperty().bind(left_side.heightProperty().divide(2.0));
        tablesGroup.setAlignment(Pos.CENTER);

        left_side.getChildren().addAll(tablesGroup);
    }

    private void buildMiddle() {
        mid_side = new VBox(10);
        mid_side.setLayoutX(300);
        mid_side.setLayoutY(100);
        mid_side.setPadding(new Insets(10.0));
        mid_side.prefWidthProperty().bind(scene.widthProperty().subtract(300).divide(1.01));
        mid_side.prefHeightProperty().bind(scene.heightProperty().subtract(100).divide(1.01));
        root.getChildren().addAll(mid_side);
    }

    private void buildMainTable(Schema table) {
        try {
            ResultSet rs = mgr.execute_this("SELECT * FROM " + table.getTableName());
            TableView<ObservableList<Object>> vtable = build_table(rs, table, true);

            HBox search_bar = new HBox();
            search_bar.setSpacing(10.0);

            HBox search_items = new HBox();
            search_items.setSpacing(10.0);
            search_items.setAlignment(Pos.CENTER);
            search_items.getChildren().add(build_search_item(search_items, table.getAttributes()));

            Button add_search_item = new Button("Add item");
            add_search_item.setPrefWidth(70);
            add_search_item.setOnMouseClicked(e -> {
                search_items.getChildren().add(build_search_item(search_items, table.getAttributes()));
            });

            Button search_btn = new Button("Search");
            search_btn.setPrefWidth(70);

            VBox search_bar_btns = new VBox(add_search_item, search_btn);

            ListView<CheckBox> attributes_checkboxs = new ListView<CheckBox>();
            attributes_checkboxs.setVisible(false);
            attributes_checkboxs.setPrefHeight(200);
            for (int i = 0; i < table.getAttributesNumber(); i++) {
                CheckBox attr_cmi = new CheckBox(table.getAttributes().get(i).getName());
                attributes_checkboxs.getItems().add(attr_cmi);
                attr_cmi.setIndeterminate(true);
            }

            Button show_hide_attr_cb = new Button();
            show_hide_attr_cb.setText("Select Attributes");

            show_hide_attr_cb.setOnMouseClicked(e -> {
                if (attributes_checkboxs.isVisible()) {
                    show_hide_attr_cb.setText("Select Attributes");
                    attributes_checkboxs.setVisible(false);
                } else {
                    show_hide_attr_cb.setText("Hide Attributes");
                    attributes_checkboxs.setVisible(true);
                }
            });

            CheckBox select_all = new CheckBox("Select All");
            select_all.setIndeterminate(true);

            VBox left_side_selection_attr = new VBox(show_hide_attr_cb, select_all);
            left_side_selection_attr.setSpacing(10);

            HBox attributes_selection = new HBox(left_side_selection_attr, attributes_checkboxs);
            attributes_selection.setSpacing(10);

            search_btn.setOnMouseClicked(e -> {
                show_this_table(parse_search(table, search_items.getChildren(), select_all.isSelected(),
                        attributes_checkboxs.getItems()));
            });

            // Line l = new Line();
            // l.endXProperty().bind(search_bar.widthProperty().subtract(10).divide(1.01));
            search_bar.getChildren().addAll(search_items, search_bar_btns);

            Button insert = new Button("+");
            insert.setMinWidth(25);
            insert.setPadding(new Insets(23, 0, 0, 0));
            // insert.setTranslateY(insert.getTranslateY() + 23);
            // insert.setLayoutY(23);
            insert.setStyle(
                    "-fx-border-color: transparent;" +
                            "-fx-border-width: 0;" +
                            "-fx-background-radius: 0;" +
                            "-fx-background-color: transparent;" +
                            "-fx-font-size: 2em;" +
                            "-fx-text-fill: green;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(three-pass-box, black, 1, 0.1, 0, 0);");

            HBox htable = new HBox(insert, vtable);
            htable.setSpacing(5);

            insert.setOnMouseClicked(e -> {
                ArrayList<Item> items = new ArrayList<Item>();

                Item item;
                Object ob;
                for (int i = 0; i < vtable.getItems().get(0).size(); i++) {
                    ob = vtable.getItems().get(0).get(i);
                    if ((Node) ob != null) {
                        if (ob instanceof Label) {
                            item = new Item(((Label) ob).getId(), "", "EMPTY");
                        } else if (ob instanceof TextField) {
                            if (((TextField) ob).getText() == null || ((TextField) ob).getText() == "") {
                                continue;
                            }
                            item = new Item(((TextField) ob).getId(), ((TextField) ob).getText(), "STRING");
                        } else if (ob instanceof DatePicker) {
                            DatePicker dp = (DatePicker) ob;
                            if (dp.getValue() == null) {
                                continue;
                            }
                            LocalDate date = dp.getValue();

                            item = new Item(dp.getId(), formatter.format(date), "DATE");
                        } else {
                            if (((ComboBox) ob).getValue() == null || ((ComboBox) ob).getValue() == "") {
                                continue;
                            }
                            item = new Item(((ComboBox) ob).getId(), (String) ((ComboBox) ob).getValue(), "STRING");
                        }
                        items.add(item);

                    } else {
                        System.out.println("null");
                    }
                }
                print_to_console(mgr.insert_into_db(table, items));
                // update_table_data();
                // ResultSet trs = mgr.execute_this("SELECT * FROM " + table.getTableName());
                // htable.getChildren().remove(vtable);
                // htable.getChildren().add(build_table(trs, table, true));
            });

            vtable.prefWidthProperty().bind(scene.widthProperty().divide(1.01));

            mid_side.getChildren().clear();
            mid_side.getChildren().addAll(search_bar, attributes_selection, htable);
            print_to_console("Table \"" + rs.getMetaData().getTableName(1) + "\" Opened!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // private void update_table(TableView<ObservableList<Object>> old_table,
    // TableView<ObservableList<Object>> new_table) {
    // old_table = new_table;
    // }

    private void show_this_table(ResultSet rs) {
        if (rs == null) {
            print_to_console("Error! Make sure you entered the variables correctly!");
            return;
        }
        TableView<ObservableList<Object>> vtable = build_table(rs, null, false);
        previuos_state = new VBox();
        previuos_state.getChildren().addAll(mid_side.getChildren());
        Button go_back = new Button("Go Back");
        go_back.setOnMouseClicked(e -> {
            root.getChildren().remove(mid_side);
            mid_side.getChildren().clear();
            mid_side.getChildren().addAll(previuos_state.getChildren());
            root.getChildren().add(mid_side);
        });
        Label show_query = new Label(mgr.getLast_query());
        show_query.setAlignment(Pos.CENTER_LEFT);
        HBox hb = new HBox(go_back, show_query);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_LEFT);
        mid_side.getChildren().clear();
        mid_side.getChildren().addAll(hb, vtable);
        try {
            print_to_console("Search completed on table \"" + rs.getMetaData().getTableName(1) + "\"! ");
        } catch (Exception e) {
            print_to_console("Unknow: Retrieving metadata incomplete!");
        }
    }

    private TableView<ObservableList<Object>> build_table(ResultSet rs, Schema table, boolean allow_insertion) {
        try {
            TableView<ObservableList<Object>> vtable = new TableView<ObservableList<Object>>();
            vtable.prefHeightProperty().bind(scene.heightProperty().subtract(100).divide(1.01));
            ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

            for (int att_i = 1; att_i <= rs.getMetaData().getColumnCount(); att_i++) {

                TableColumn<ObservableList<Object>, Object> att_column = new TableColumn<ObservableList<Object>, Object>(
                        rs.getMetaData().getColumnName(att_i));
                // att_column.setMaxWidth(300);
                // if (table.getAttributes().get(att_i - 1).isItPrimaryKey()) {
                // att_column.setStyle("-fx-underline: true;");
                // }

                final int j = att_i;
                att_column.setCellValueFactory(
                        new Callback<CellDataFeatures<ObservableList<Object>, Object>, ObservableValue<Object>>() {
                            public ObservableValue<Object> call(
                                    CellDataFeatures<ObservableList<Object>, Object> param) {
                                return new SimpleObjectProperty<Object>(param.getValue().get(j - 1));
                            }
                        });
                vtable.getColumns().add(att_column);
            }
            ObservableList<Object> insertion_inputs = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                if (allow_insertion) {
                    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                        if (table.getAttributes().get(i).getReference_fk_table() != null) {
                            ComboBox<Object> cb = new ComboBox<Object>();
                            cb.setId(table.getAttributes().get(i).getName());
                            cb.setEditable(true);
                            String atts = "";
                            String tn = table.getAttributes().get(i).getReference_fk_table().getTableName();
                            for (Attribute att : table.getAttributes().get(i).getReference_fk_table().getPrimaryKey()) {
                                if (atts.length() > 0)
                                    atts += ", ";
                                atts += tn + "." + att.getName();
                            }
                            ResultSet trs = mgr.execute_this("SELECT " + atts + " FROM " + tn);
                            while (trs.next()) {
                                cb.getItems().add(trs.getString(1));
                            }
                            insertion_inputs.add(cb);
                        } else if (table.getAttributes().get(i).isItPrimaryKey()
                                && (table.getPrimaryKey().size() == 1)
                                && mgr.check_if_this_is_integer_type(table.getAttributes().get(i).getType())) {
                            Label tf = new Label();
                            tf.setId(table.getAttributes().get(i).getName());
                            tf.setText("Auto generated key!");
                            tf.setStyle("-fx-font-weight: bold;");
                            insertion_inputs.add(tf);
                        } else if (mgr.check_if_this_is_date_type(table.getAttributes().get(i).getType())) {
                            DatePicker dp = new DatePicker();
                            dp.setPromptText("M/D/Y: 05/16/2000");
                            dp.setId(table.getAttributes().get(i).getName());
                            insertion_inputs.add(dp);
                        } else {
                            TextField tf = new TextField();
                            tf.setId(table.getAttributes().get(i).getName());
                            insertion_inputs.add(tf);
                        }
                    }
                    allow_insertion = false;
                    data.add(insertion_inputs);
                    vtable.getItems().add(insertion_inputs);
                }
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
                vtable.getItems().add(row);
            }
            vtable.setItems(data);
            return vtable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private VBox build_search_item(HBox search_items, ArrayList<Attribute> attributes) {
        VBox item = new VBox();
        item.setSpacing(3);
        item.setAlignment(Pos.CENTER);
        item.setPrefWidth(100);

        ComboBox<String> menu = new ComboBox<String>();
        for (Attribute attribute : attributes) {
            menu.getItems().add(attribute.getName());
        }
        TextField tf = new TextField();
        Button remove_item = new Button("X");
        remove_item.setFont(new Font(12));
        remove_item.setTextFill(Color.RED);
        remove_item.setPrefSize(20, 20);
        remove_item.setStyle(
                "-fx-border-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-background-radius: 0;" +
                        "-fx-background-color: transparent;" +
                        "-fx-font-size: 1em;" +
                        "-fx-text-fill: red;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, black, 1, 0.1, 0, 0);");
        remove_item.setOnMouseClicked(e -> {
            search_items.getChildren().remove(item);
        });
        item.getChildren().addAll(menu, tf, remove_item);
        return item;
    }

    private ResultSet parse_search(Schema table, ObservableList<Node> items, boolean select_all,
            ObservableList<CheckBox> selected_attrs) {
        ArrayList<Item> query_ps = new ArrayList<Item>();
        for (Node item : items) {
            VBox vitem = (VBox) item;
            table.getAttributes().forEach(attr -> {
                String temp = ((ComboBox<String>) vitem.getChildren().get(0)).getSelectionModel().getSelectedItem();
                if (attr.getName().equals(temp)) {
                    query_ps.add(
                            new Item(temp, ((TextField) vitem.getChildren().get(1)).getText(), attr.getType()));
                    return;
                }
            });

        }
        ResultSet rs;
        if (select_all) {
            rs = mgr.search_this(table.getTableName(), query_ps, select_all);
        } else {
            rs = mgr.search_this(table.getTableName(), query_ps, selected_attrs);
        }
        return rs;
    }

    private void buildTablesMenu() {
        ListView<String> vtables = new ListView<String>();
        for (int i = 0; i < tables.size(); i++) {
            vtables.getItems().add(tables.get(i).getTableName());
        }

        Label tableTitle = new Label("Tables");
        tableTitle.setStyle("-fx-font: bold 12pt 'Arial'");

        VBox tablesGroup = new VBox(10, tableTitle, vtables);
        tablesGroup.prefHeightProperty().bind(left_side.heightProperty().divide(2.0));
        tablesGroup.setAlignment(Pos.CENTER);

        vtables.getSelectionModel().selectedItemProperty().addListener(e -> {
            String table_name = vtables.getSelectionModel().getSelectedItem();
            buildMainTable(find_table(table_name));

        });

        left_side.getChildren().addAll(tablesGroup);
    }

    public static void print_to_console(String msg) {// , Color color
        // console.setStyle(
        // "-fx-text-fill: rgb(" + (int) (color.getRed() * 255) + ", " + (int)
        // (color.getGreen() * 255) + ", "
        // + (int) (color.getBlue() * 255) + ");");

        console.appendText(msg + "\n");
    }

    private Schema find_table(String table_name) {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getTableName().equals(table_name)) {
                return tables.get(i);
            }
        }
        return null;
    }

    public Stage getStage() {
        return stg;
    }
}
