package databasemanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import parser.Query;
import parser.QueryParser;
import schema.Database;
import schema.Relation;
import schema.Schema;
import schema.attribute.Attribute;
import ui.HomeScreen;

public class Manager {

    private final String DB_URL;
    private final String DB_NAME;
    private final String USERNAME;
    private final String PASSWORD;
    private Connection connection;
    private Database db;
    private QueryParser qp;
    private String last_query = "";

    private final String[] STRINGS_TYPES = { "CHAR", "VARCHAR", "TEXT" };
    private final String[] INTEGER_TYPES = { "INT", "INTEGER", "FLOAT", "DOUBLE", "DECIMAL" };
    private final String[] DATE_TYPES = { "DATE", "DATETIME" };
    private final String[] OPERATORS = { "<", ">", ">=", "<=", "!=" };

    public Manager(String db_url, String db_name, String username, String password) {
        this.DB_URL = db_url;
        this.USERNAME = username;
        this.PASSWORD = password;
        this.DB_NAME = db_name;
        db = new Database(db_name, password);
        open_connection();
        init();
        close_connection();
        qp = new QueryParser("special_queries");
    }

    private void init() {

        try {
            Statement stm = connection.createStatement();
            Statement stm2 = connection.createStatement();
            Statement stm3 = connection.createStatement();

            ResultSet tables_names = stm.executeQuery("show tables");
            ResultSet tables_attributes;

            String table_name, att_name, att_type, att_key;
            boolean primary = false, foreign = false, att_required = false;

            while (tables_names.next()) {
                table_name = tables_names.getString(1);
                Schema table = new Schema(table_name);
                db.add_table(table);
            }

            for (Schema table : db.getTables()) {
                table_name = table.getTableName();
                tables_attributes = stm2.executeQuery("desc " + table_name);
                while (tables_attributes.next()) {
                    primary = false;
                    foreign = false;
                    att_required = false;

                    att_name = tables_attributes.getString(1);
                    att_type = tables_attributes.getString(2);

                    if (tables_attributes.getString(3).equals("NO"))
                        att_required = true;

                    att_key = tables_attributes.getString(4);

                    if (att_key.equals("PRI")) {
                        primary = true;
                    }
                    if (att_key.equals("MUL")) {
                        foreign = true;
                    }

                    Attribute att = new Attribute(att_name, primary, foreign, att_required, att_type);

                    table.addAttribute(att);
                }

            }

            ResultSet relations_rs = stm3.executeQuery("SELECT " +
                    "TABLE_NAME, " +
                    "COLUMN_NAME, " +
                    "CONSTRAINT_NAME, " +
                    "REFERENCED_TABLE_NAME, " +
                    "REFERENCED_COLUMN_NAME " +
                    "FROM " +
                    "INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    " WHERE " +
                    " REFERENCED_TABLE_SCHEMA = '" + db.getNAME() + "'"); // abd_al-muttalib_201904158

            while (relations_rs.next()) {
                Schema reference_table = db.find_table(relations_rs.getString(1));
                Attribute reference_att = reference_table.find_attribute(relations_rs.getString(2));
                String id = relations_rs.getString(3);
                Schema referenced_table = db.find_table(relations_rs.getString(4));
                Attribute referenced_att = referenced_table.find_attribute(relations_rs.getString(5));

                Relation relation = new Relation(reference_att, reference_table, referenced_att, referenced_table, id);
                db.add_relation(relation);
            }

        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Initialization failed!");
            alert.setContentText("Couldn't initialize the app! Due to " + e.getCause());
            alert.showAndWait();
            System.exit(0);
        }
    }

    public ResultSet search_this(String table_name, ArrayList<Item> query_ps, boolean select_all) {
        ResultSet rs = null;
        String query = "SELECT *";
        String conditions = parse_conditions(table_name, query_ps);
        query += " FROM " + table_name + " WHERE " + conditions + ";";
        last_query = query;
        rs = execute_this(query);
        return rs;
    }

    public ResultSet search_this(String table_name, ArrayList<Item> query_ps,
            ObservableList<CheckBox> selected_attrs) {
        ResultSet rs = null;
        String query = "SELECT ";
        boolean first_attr = true;
        if (selected_attrs.size() == 0) {
            return null;
        }
        for (CheckBox cb : selected_attrs) {
            if (cb.isSelected()) {
                if (!first_attr) {
                    query += ", ";
                }
                query += cb.getText();
                first_attr = false;
            }
        }
        String conditions = parse_conditions(table_name, query_ps);
        query += " FROM " + table_name + " WHERE " + conditions + ";";
        last_query = query;
        rs = execute_this(query);
        return rs;
    }

    private String parse_conditions(String table_name, ArrayList<Item> query_ps) {
        String conditions = "";

        for (Item item : query_ps) {
            if (conditions.length() != 0)
                conditions += " AND ";
            conditions += parse_search_item(table_name, item);
        }
        return conditions;
    }

    public String insert_into_db(Schema table, ArrayList<Item> items) {
        String query = "INSERT INTO " + table.getTableName();
        String columns_names = "";
        String values = "";
        boolean temp = false;
        for (Attribute attribute : table.getAttributes()) {
            temp = false;
            for (Item item : items) {
                if (attribute.getName().equals(item.getATTRIBUTE_NAME())) {
                    if (item.getType() != "EMPTY") {
                        if (attribute.isItPrimaryKey()) {

                        }
                        temp = true;
                        if (columns_names.length() > 0)
                            columns_names += ", ";
                        columns_names += item.getATTRIBUTE_NAME();
                        if (values.length() > 0)
                            values += ", ";
                        values += "'" + item.getVALUE() + "'";
                    }
                    break;
                }
            }
            if (attribute.isRequired() && !temp) {
                if (attribute.isItPrimaryKey() && check_if_this_is_integer_type(attribute.getType())) {
                    try {
                        ResultSet trs = execute_p_this(
                                "SELECT MAX(" + table.getTableName() + "." + attribute.getName() + ") AS MAX_ID FROM "
                                        + table.getTableName() + ";");
                        if (trs.next()) {
                            int max_id = trs.getInt("MAX_ID");
                            while (true) {
                                max_id++;
                                trs = execute_p_this("SELECT COUNT(" + table.getTableName() + "."
                                        + attribute.getName()
                                        + ") AS counter FROM "
                                        + table.getTableName() + " WHERE " + attribute.getName() + "=" + max_id + ";");
                                if (trs.next())
                                    if (trs.getInt("counter") == 0) {
                                        break;
                                    }
                            }
                            if (columns_names.length() > 0)
                                columns_names += ", ";
                            columns_names += attribute.getName();
                            if (values.length() > 0)
                                values += ", ";
                            values += "'" + max_id + "'";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Unexpected exception while inserting into database";
                    }
                } else {
                    return "'" + attribute.getName() + "'" + " is required!";
                }
            }
        }
        query += " (" + columns_names + ") VALUES (" + values + ");";

        if (update_this(query) == -1) {
            return "Insertion failed!";
        }
        return "Insertion completed into table \"" + table.getTableName() + "\"!";
    }

    private String parse_search_item(String table_name, Item item) {
        String re = "NULL";
        if (check_if_this_is_string_type(item.getType())) {
            re = table_name + "." + item.getATTRIBUTE_NAME() + " LIKE '%" + item.getVALUE() + "%'";
        } else if (check_if_this_is_integer_type(item.getType())) {
            if (does_it_contains_ops(item.getVALUE())) {
                re = table_name + "." + item.getATTRIBUTE_NAME() + " " + item.getVALUE();
            } else {
                re = table_name + "." + item.getATTRIBUTE_NAME() + " = " + item.getVALUE();
            }
        } else if (check_if_this_is_date_type(item.getType())) {
            if (does_it_contains_ops(item.getVALUE())) {
                String temp = "";
                boolean opened = false;
                for (int i = 0; i < item.getVALUE().length(); i++) {
                    if (item.getVALUE().charAt(i) >= '0' && item.getVALUE().charAt(i) <= '9' && !opened) {
                        opened = true;
                        temp += "'";
                    }
                    temp += item.getVALUE().charAt(i);
                }
                temp += "'";
                re = table_name + "." + item.getATTRIBUTE_NAME() + " " + temp;
            } else {
                re = table_name + "." + item.getATTRIBUTE_NAME() + " = '" + item.getVALUE() + "'";
            }
        }
        return re;
    }

    public boolean check_if_this_is_string_type(String type) {
        for (int i = 0; i < STRINGS_TYPES.length; i++) {
            if (type.toLowerCase().contains(STRINGS_TYPES[i].toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean check_if_this_is_integer_type(String type) {
        for (int i = 0; i < INTEGER_TYPES.length; i++) {
            if (type.toLowerCase().contains(INTEGER_TYPES[i].toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean check_if_this_is_date_type(String type) {
        for (int i = 0; i < DATE_TYPES.length; i++) {
            if (type.toLowerCase().contains(DATE_TYPES[i].toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean does_it_contains_ops(String value) {
        for (int i = 0; i < OPERATORS.length; i++) {
            if (value.contains(OPERATORS[i]))
                return true;
        }
        return false;
    }

    public ResultSet execute_this(String query) {
        open_connection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rs = stm.executeQuery(query);
            return rs;
        } catch (Exception e) {
            HomeScreen.print_to_console("Incorrect input! due to \n" + e.getMessage());
        }
        close_connection();
        return null;
    }

    public ResultSet execute_p_this(String query) {
        open_connection();
        try {
            PreparedStatement pstm = connection.prepareStatement(query);
            ResultSet rs = pstm.executeQuery();
            return rs;
        } catch (Exception e) {
            HomeScreen.print_to_console("Incorrect input! due to \n" + e.getMessage());
        }
        close_connection();
        return null;
    }

    public int update_this(String query) {
        open_connection();
        try {
            Statement stm = connection.createStatement();
            return stm.executeUpdate(query);
        } catch (Exception e) {
            HomeScreen.print_to_console("Update incomplete! due to \n" + e.getMessage());
        }
        close_connection();
        return -1;
    }

    private void open_connection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL + DB_NAME, USERNAME, PASSWORD);
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Open connection failed!");
            alert.setContentText("Couldn't open connection with the database! Due to " + e.getCause());
            alert.showAndWait();
            System.exit(0);
        }
    }

    private void close_connection() {
        try {
            connection.close();
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Close connection failed!");
            alert.setContentText("Couldn't close connection with the database! Due to " + e.getCause());
            alert.showAndWait();
            System.exit(0);
        }
    }

    public String get_last_query() {
        return last_query;
    }

    public ArrayList<Query> get_special_queries() {
        return qp.getQueries();
    }

    public void set_last_query(String last_query) {
        this.last_query = last_query;
    }

    public Database getDb() {
        return db;
    }

}