package ui;

import java.sql.ResultSet;
import java.util.ArrayList;

import databasemanager.Manager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import schema.Schema;

public class HomeScreen {

    private Stage stg;
    private Pane root;
    private VBox mid_side;
    private Scene scene;
    private final double WIDTH, HEIGHT;
    private final String TITLE;
    private final boolean MAXIMIZED_SCREEN;
    private ArrayList<Schema> tables;
    private Manager mgr;

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
        stg.setMaximized(MAXIMIZED_SCREEN);
        stg.setTitle(TITLE);
        stg.setScene(scene);
    }

    private void buildPage() {
        buildTablesMenu();
        buildMiddle();
    }

    private void buildMiddle() {
        mid_side = new VBox();
        mid_side.setLayoutX(300);
        mid_side.setLayoutY(100);
        mid_side.setPrefWidth(1000);
        mid_side.setPrefHeight(1000);
        mid_side.setMinHeight(1000);

        mid_side.prefWidthProperty().bind(scene.widthProperty().subtract(300).divide(1.01));
        mid_side.prefHeightProperty().bind(scene.heightProperty().subtract(100).divide(1.01));

        // HBox search_bar = new HBox();
        // TextField
    }

    private void buildMainTable(Schema table) {
        try {
            TableView<ObservableList<String>> vtable = new TableView<ObservableList<String>>();
            vtable.prefHeightProperty().bind(scene.heightProperty().subtract(100).divide(1.01));

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int att_i = 0; att_i < table.getAttributesNumber(); att_i++) {
                TableColumn<ObservableList<String>, String> att_column = new TableColumn<ObservableList<String>, String>(
                        table.getAttributes().get(att_i).getName());
                final int j = att_i;
                att_column.setCellValueFactory(
                        new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
                            public ObservableValue<String> call(
                                    CellDataFeatures<ObservableList<String>, String> param) {
                                return new SimpleStringProperty(param.getValue().get(j));
                            }
                        });

                ResultSet rs = mgr.execute_this("SELECT * FROM " + table.getTableName());

                while (rs.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        row.add(rs.getString(i));
                    }
                    data.add(row);
                    vtable.getItems().add(row);

                }

                vtable.getColumns().add(att_column);

            }
            vtable.setItems(data);
            mid_side.getChildren().clear();
            mid_side.getChildren().add(vtable);
            root.getChildren().remove(mid_side);
            root.getChildren().addAll(mid_side);
        } catch (

        Exception e) {
            e.printStackTrace();
        }

    }

    private void buildTablesMenu() {
        ListView<String> vtables = new ListView<String>();
        for (int i = 0; i < tables.size(); i++) {
            vtables.getItems().add(tables.get(i).getTableName());
        }

        Label tableTitle = new Label("Tables");

        VBox tablesGroup = new VBox(10, tableTitle, vtables);
        tablesGroup.prefWidthProperty().add(Screen.getPrimary().getBounds().getWidth() / 10);
        tablesGroup.setPadding(new Insets(10.0));
        tablesGroup.setAlignment(Pos.CENTER);

        vtables.getSelectionModel().selectedItemProperty().addListener(e -> {
            String table_name = vtables.getSelectionModel().getSelectedItem();
            buildMainTable(find_table(table_name));

        });

        root.getChildren().addAll(tablesGroup);
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
