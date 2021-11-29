package ui;

import java.sql.ResultSet;
import java.util.ArrayList;

import databasemanager.Manager;
import databasemanager.SearchItem;
import javafx.beans.property.SimpleStringProperty;
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
    private TextArea console;

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
            TableView<ObservableList<String>> vtable = build_table(rs);

            HBox search_bar = new HBox();
            search_bar.setSpacing(10.0);

            HBox search_items = new HBox();
            search_items.setSpacing(10.0);
            search_items.setAlignment(Pos.CENTER);
            search_items.getChildren().add(build_search_item(table.getAttributes()));

            Button add_search_item = new Button("Add item");
            add_search_item.setPrefWidth(70);
            add_search_item.setOnMouseClicked(e -> {
                search_items.getChildren().add(build_search_item(table.getAttributes()));
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
                show_this(parse_search(table, search_items.getChildren(), select_all.isSelected(),
                        attributes_checkboxs.getItems()));
            });

            search_bar.getChildren().addAll(search_items, search_bar_btns);

            mid_side.getChildren().clear();
            mid_side.getChildren().addAll(search_bar, attributes_selection, vtable);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void show_this(ResultSet rs) {
        TableView<ObservableList<String>> vtable = build_table(rs);

        mid_side.getChildren().clear();
        mid_side.getChildren().addAll(vtable);
    }

    private TableView<ObservableList<String>> build_table(ResultSet rs) {
        try {
            TableView<ObservableList<String>> vtable = new TableView<ObservableList<String>>();
            vtable.prefHeightProperty().bind(scene.heightProperty().subtract(100).divide(1.01));

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

            for (int att_i = 1; att_i <= rs.getMetaData().getColumnCount(); att_i++) {

                TableColumn<ObservableList<String>, String> att_column = new TableColumn<ObservableList<String>, String>(
                        rs.getMetaData().getColumnName(att_i));
                final int j = att_i;
                att_column.setCellValueFactory(
                        new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
                            public ObservableValue<String> call(
                                    CellDataFeatures<ObservableList<String>, String> param) {
                                return new SimpleStringProperty(param.getValue().get(j - 1));
                            }
                        });
                vtable.getColumns().add(att_column);
            }

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
                vtable.getItems().add(row);

            }

            vtable.setItems(data);
            print_to_console("Table \"" + rs.getMetaData().getTableName(1) + "\" Opened!");

            return vtable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private VBox build_search_item(ArrayList<Attribute> attributes) {
        VBox item = new VBox();
        item.setPrefWidth(100);
        ComboBox<String> menu = new ComboBox<String>();
        // menu.setPromptText("Column?");
        for (Attribute attribute : attributes) {
            menu.getItems().add(attribute.getName());
        }
        TextField tf = new TextField();
        item.getChildren().addAll(menu, tf);
        return item;
    }

    private ResultSet parse_search(Schema table, ObservableList<Node> items, boolean select_all,
            ObservableList<CheckBox> selected_attrs) {
        ArrayList<SearchItem> query_ps = new ArrayList<SearchItem>();
        for (Node item : items) {
            VBox vitem = (VBox) item;
            table.getAttributes().forEach(attr -> {
                String temp = ((ComboBox<String>) vitem.getChildren().get(0)).getSelectionModel().getSelectedItem();
                if (attr.getName().equals(temp)) {
                    query_ps.add(
                            new SearchItem(temp, ((TextField) vitem.getChildren().get(1)).getText(), attr.getType()));
                    return;
                }
            });

        }
        System.out.println("ALL? ----> " + select_all);
        for (CheckBox cb : selected_attrs) {
            System.out.println(cb.isSelected());
        }
        for (SearchItem searchItem : query_ps) {
            System.out.println(searchItem.toString());
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

    private void print_to_console(String msg) {
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
