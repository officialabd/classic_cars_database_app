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
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
import javafx.util.Pair;
import parser.Query;
import parser.QueryInput;
import schema.Database;
import schema.Relation;
import schema.Schema;
import schema.attribute.Attribute;

public class HomeScreen {

    private final double WIDTH, HEIGHT;
    private final String TITLE;
    private final boolean MAXIMIZED_SCREEN;
    private Manager mgr;
    private ArrayList<Schema> tables;
    private Database db;
    private Stage stg;
    private Scene scene;
    private Pane root;
    private static TextArea console;
    private VBox mid_side;
    private VBox left_side;
    private HBox content;
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
        this.db = mgr.getDb();
        this.tables = this.mgr.getDb().getTables();
        init();
        build_page();
    }

    private void init() {
        root = new Pane();
        scene = new Scene(root, WIDTH, HEIGHT);
        stg.setMaximized(MAXIMIZED_SCREEN);
        stg.setTitle(TITLE);
        stg.setScene(scene);
    }

    private void build_page() {
        root.getChildren().addAll(new VBox(build_navigation_bar(), build_content()));
    }

    private Node build_content() {
        content = new HBox(buildLeft(), build_middle());
        return content;
    }

    private Node build_navigation_bar() {
        Menu edit = new Menu("Edit");

        Menu special_queries = new Menu("Special Queries");
        special_queries.getItems().addAll(build_special_queries_menu());

        MenuBar mb = new MenuBar(edit, special_queries);
        mb.prefWidthProperty().bind(scene.widthProperty());
        return mb;
    }

    private ObservableList<MenuItem> build_special_queries_menu() {
        ObservableList<MenuItem> list = FXCollections.observableArrayList();
        for (Query q : mgr.get_special_queries()) {
            MenuItem mi = new MenuItem(q.getTitle());
            mi.setOnAction(e -> {
                build_special_queries_form(q);
            });
            mi.setId(q.getID());
            list.add(mi);
        }
        return list;
    }

    private void build_special_queries_form(Query q) {
        if (q.getInputs_number() == 0) {
            parse_special_query(q, null);
            return;
        }

        VBox vb = new VBox();
        vb.setSpacing(10.0);
        HBox hb = new HBox();
        hb.setSpacing(5.0);

        Label l = new Label(q.getTitle());
        vb.getChildren().addAll(l, hb);

        boolean bool_added = false;
        for (QueryInput qi : q.getInputs()) {
            if (!bool_added) {
                TextField tf = new TextField();
                tf.setId("" + qi.getIndex());
                tf.setPrefWidth(100);
                hb.getChildren().add(tf);
                if (qi.is_multi()) {
                    Button btn = new Button("+");
                    btn.setOnAction(e -> {
                        TextField tf2 = new TextField();
                        tf2.setId("" + qi.getIndex());
                        tf2.setPrefWidth(100);

                        hb.getChildren().add(hb.getChildren().indexOf(btn), tf2);
                    });
                    hb.getChildren().add(btn);
                }

                if (qi.getIndex() == 0) {
                    bool_added = true;
                }
            }
        }
        Dialog<Void> tid = new Dialog<Void>();
        tid.getDialogPane().setContent(vb);
        tid.setTitle("Input Required!");
        tid.setHeaderText("Please fill the required data!");
        tid.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        tid.setResultConverter(dbtn -> {
            if (dbtn == ButtonType.OK) {
                for (Node node : ((VBox) tid.getDialogPane().getContent()).getChildren()) {
                    if (node instanceof HBox) {
                        ArrayList<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
                        for (Node n : ((HBox) node).getChildren()) {
                            if (n instanceof TextField) {
                                list.add(new Pair<String, String>(((TextField) n).getId(), ((TextField) n).getText()));
                            }
                        }
                        parse_special_query(q, list);
                    }
                }
            }
            return null;
        });
        tid.show();
    }

    private Node buildLeft() {
        left_side = new VBox(10);
        left_side.setPadding(new Insets(10.0));
        left_side.prefHeightProperty().bind(scene.heightProperty().divide(1.01));
        left_side.prefWidthProperty().bind(scene.widthProperty().divide(5));
        left_side.getChildren().addAll(build_all_tables_menu(), buildConsole());
        return left_side;
    }

    private Node buildConsole() {
        Label console_title = new Label("Console");
        console_title.setStyle("-fx-font: bold 12pt 'Arial'");

        console = new TextArea();
        console.setEditable(false);
        console.setWrapText(true);

        VBox vb = new VBox(10, console_title, console);
        vb.prefHeightProperty().bind(left_side.heightProperty().divide(2));
        vb.setAlignment(Pos.TOP_CENTER);
        console.prefHeightProperty().bind(vb.heightProperty().divide(1.16));

        return vb;
    }

    private Node build_middle() {
        mid_side = new VBox(10);
        mid_side.setPadding(new Insets(10.0));
        mid_side.prefWidthProperty()
                .bind(scene.widthProperty().subtract(scene.widthProperty().divide(5.0)).divide(1.01));
        mid_side.prefHeightProperty().bind(scene.heightProperty().divide(1.01));
        return mid_side;
    }

    private Node build_main_table(Schema table) {
        try {
            ResultSet rs = mgr.execute_this("SELECT * FROM " + table.getTableName());
            TableView<ObservableList<Object>> vtable = build_table(rs, table, true);

            VBox search_bar = new VBox();
            search_bar.setSpacing(5.0);

            HBox search_items = new HBox();
            search_items.setSpacing(5.0);
            search_items.getChildren().add(build_search_item(search_items, table.getAttributes()));

            Button add_search_item = new Button("Add item");
            add_search_item.setPrefWidth(70);
            add_search_item.setOnMouseClicked(e -> {
                search_items.getChildren().add(build_search_item(search_items, table.getAttributes()));
            });

            Button search_btn = new Button("Search");
            search_btn.setPrefWidth(70);

            ListView<CheckBox> attributes_checkboxs = new ListView<CheckBox>();
            attributes_checkboxs.setPrefHeight(200);
            for (int i = 0; i < table.getAttributesNumber(); i++) {
                CheckBox attr_cmi = new CheckBox(table.getAttributes().get(i).getName());
                attributes_checkboxs.getItems().add(attr_cmi);
                attr_cmi.setIndeterminate(true);
            }

            CheckBox select_all = new CheckBox("Select All");
            select_all.setIndeterminate(true);

            Button show_attributes = new Button("Select");
            Label lor = new Label("Or");

            HBox search_bar_btns = new HBox(show_attributes, lor, select_all, add_search_item, search_btn);
            search_bar_btns.setAlignment(Pos.CENTER_LEFT);
            search_bar_btns.setSpacing(10.0);

            show_attributes.setOnMouseClicked(e -> {
                if (search_bar_btns.getChildren().contains(attributes_checkboxs)) {
                    search_bar_btns.getChildren().remove(attributes_checkboxs);
                    show_attributes.setText("Select");
                } else {
                    search_bar_btns.getChildren().add(1, attributes_checkboxs);
                    show_attributes.setText("Hide");
                }
            });

            search_btn.setOnMouseClicked(e -> {
                show_this_table(parse_search(table, search_items.getChildren(),
                        select_all.isSelected(),
                        attributes_checkboxs.getItems()), null);
            });
            VBox bs = new VBox(search_bar_btns, search_items);
            bs.setSpacing(5.0);

            search_bar.getChildren().addAll(search_bar_btns, bs);

            Button insert = new Button("+");
            insert.setMinWidth(25);
            insert.setPadding(new Insets(23, 0, 0, 0));
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
                        print_to_console("Something weird happened!");
                    }
                }
                String rmsg = mgr.insert_into_db(table, items);
                print_to_console(rmsg);
                if (rmsg.toLowerCase().contains("completed")) {
                    ResultSet trs = mgr.execute_this("SELECT * FROM " + table.getTableName());
                    vtable.getItems().clear();
                    vtable.setItems(get_data_of_table(trs, table, true));
                }
            });

            vtable.prefWidthProperty().bind(scene.widthProperty().divide(1.01));

            print_to_console("Table \"" + rs.getMetaData().getTableName(1) + "\" Opened!");
            VBox vb_all = new VBox(search_bar, htable);

            vb_all.prefHeightProperty().bind(scene.heightProperty().divide(1.07));
            vb_all.setSpacing(5.0);
            vtable.prefHeightProperty().bind(vb_all.heightProperty().divide(1.01));

            return vb_all;
        } catch (Exception e) {
            print_to_console("Building main table failed due to " + e.getMessage());
            return null;
        }
    }

    private void show_this_table(ResultSet rs, String query) {
        if (rs == null) {
            print_to_console("Error! Make sure you entered the variables correctly!");
            return;
        }
        TableView<ObservableList<Object>> vtable = build_table(rs, null, false);
        vtable.prefHeightProperty().bind(scene.heightProperty().divide(1.2));

        previuos_state = new VBox();
        previuos_state.getChildren().addAll(mid_side.getChildren());
        Button go_back = new Button("Go Back");
        go_back.setMinWidth(80);

        go_back.setOnMouseClicked(e -> {
            content.getChildren().remove(mid_side);
            mid_side.getChildren().clear();
            mid_side.getChildren().addAll(previuos_state.getChildren());
            content.getChildren().add(mid_side);
        });
        Label show_query = new Label();
        show_query.setAlignment(Pos.CENTER_LEFT);
        show_query.setWrapText(true);
        if (query == null) {
            show_query.setText(mgr.get_last_query());
        } else {
            show_query.setText(query);
        }
        HBox hb = new HBox(go_back, show_query);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_LEFT);
        mid_side.getChildren().clear();
        mid_side.getChildren().addAll(hb, vtable);
        try {
            print_to_console("Search completed on table \"" + rs.getMetaData().getTableName(1) + "\"! ");
        } catch (

        Exception e) {
            print_to_console("Unknow: Retrieving metadata incomplete!");
        }
    }

    private TableView<ObservableList<Object>> build_table(ResultSet rs, Schema table, boolean allow_insertion) {
        try {
            TableView<ObservableList<Object>> vtable = new TableView<ObservableList<Object>>();

            for (int att_i = 1; att_i <= rs.getMetaData().getColumnCount(); att_i++) {

                TableColumn<ObservableList<Object>, Object> att_column = new TableColumn<ObservableList<Object>, Object>(
                        rs.getMetaData().getColumnName(att_i));
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
            ObservableList<ObservableList<Object>> data = get_data_of_table(rs, table, allow_insertion);
            vtable.setItems(data);
            return vtable;
        } catch (Exception e) {
            print_to_console("building table failed due to " + e.getMessage());
        }
        return null;
    }

    private ObservableList<ObservableList<Object>> get_data_of_table(ResultSet rs, Schema table,
            boolean allow_insertion) {
        try {

            ObservableList<Object> insertion_inputs = FXCollections.observableArrayList();
            ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                if (allow_insertion) {
                    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                        if (table.getAttributes().get(i).isItForeignKey()) {
                            ComboBox<Object> cb = new ComboBox<Object>();
                            cb.setId(table.getAttributes().get(i).getName());
                            cb.setEditable(true);

                            Relation ref_table = db.find_relations(table.getAttributes().get(i).getName());

                            ResultSet trs = mgr
                                    .execute_this("SELECT * FROM "
                                            + ref_table.getReference_schema().getTableName());

                            while (trs.next()) {
                                cb.getItems().add(trs.getString(ref_table.getReference_att().getName()));
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
                }
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }
            return data;
        } catch (Exception e) {
            print_to_console("Retrieving data faild! due to " + e.getMessage());
            return null;
        }
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

    private void parse_special_query(Query q, ArrayList<Pair<String, String>> list) {
        String prev_key = "";
        q.clear_values();
        if (list != null)
            for (Pair<String, String> pstr : list) {
                if (pstr.getKey().equalsIgnoreCase(prev_key)) {
                    q.add_value(pstr.getValue(), Query.SAME);
                } else {
                    q.add_value(pstr.getValue(), Query.NEXT);
                }
                prev_key = pstr.getKey();
            }
        show_this_table(mgr.execute_this(q.build_query()), q.build_query());
    }

    private Node build_all_tables_menu() {
        ListView<String> vtables = new ListView<String>();
        for (int i = 0; i < tables.size(); i++) {
            vtables.getItems().add(tables.get(i).getTableName());
        }

        Label tableTitle = new Label("Tables");
        tableTitle.setStyle("-fx-font: bold 12pt 'Arial'");

        VBox tablesGroup = new VBox(10, tableTitle, vtables);
        tablesGroup.prefHeightProperty().bind(left_side.heightProperty().divide(2));
        tablesGroup.setAlignment(Pos.CENTER);

        vtables.getSelectionModel().selectedItemProperty().addListener(e -> {
            String table_name = vtables.getSelectionModel().getSelectedItem();
            mid_side.getChildren().clear();
            mid_side.getChildren().add(build_main_table(db.find_table(table_name)));
        });

        return tablesGroup;
    }

    public static void print_to_console(String msg) {
        console.appendText(msg + "\n");
        console.appendText("--------------------------------\n");
    }

    public Stage getStage() {
        return stg;
    }
}