package databasemanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import schema.Schema;
import schema.attribute.Attribute;

public class Manager {

    private final String DB_URL;
    private final String USERNAME;
    private final String PASSWORD;
    private Connection connection;
    private ArrayList<Schema> tables_schemas;

    private static final String CHAR = "CHAR";
    private static final String VARCHAR = "VARCHAR";
    private static final String INT = "INT";
    private static final String INTEGER = "INTEGER";
    private static final String FLOAT = "FLOAT";
    private static final String DOUBLE = "DOUBLE";
    private static final String DECIMAL = "DECIMAL";
    private static final String[] OPERATORS = { "<", ">", ">=", "<=", "!=" };

    public Manager(String db_url, String username, String password) {
        this.DB_URL = db_url;
        this.USERNAME = username;
        this.PASSWORD = password;
        open_connection();
        init_tables_schemas();
        close_connection();
    }

    private void init_tables_schemas() {
        tables_schemas = new ArrayList<Schema>();
        try {
            Statement stm = connection.createStatement();
            Statement stm2 = connection.createStatement();

            ResultSet tables_names = stm.executeQuery("show tables");
            ResultSet tables_attributes;

            String table_name, att_name, att_type, att_key;
            boolean primary = false, foreign = false;

            while (tables_names.next()) {
                table_name = tables_names.getString(1);
                tables_attributes = stm2.executeQuery("desc " + table_name);

                Schema table = new Schema(table_name);

                while (tables_attributes.next()) {
                    primary = false;
                    foreign = false;

                    att_name = tables_attributes.getString(1);
                    att_type = tables_attributes.getString(2);
                    att_key = tables_attributes.getString(4);

                    if (att_key.equals("PRI")) {
                        primary = true;
                    }
                    if (att_key.equals("MUL")) {
                        foreign = true;
                    }

                    Attribute att = new Attribute(att_name, primary, foreign, att_type);

                    table.addAttribute(att);
                }
                tables_schemas.add(table);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public ResultSet execute_this(String query) {
        open_connection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rs = stm.executeQuery(query);
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        close_connection();
        return null;
    }

    public ResultSet search_this(String table_name, ArrayList<SearchItem> query_ps, boolean select_all) {
        ResultSet rs = null;
        String query = "SELECT *";
        String conditions = parse_conditions(table_name, query_ps);
        query += " FROM " + table_name + " WHERE " + conditions + ";";
        rs = execute_this(query);
        return rs;
    }

    public ResultSet search_this(String table_name, ArrayList<SearchItem> query_ps,
            ObservableList<CheckBox> selected_attrs) {
        ResultSet rs = null;
        String query = "SELECT ";
        boolean first_attr = true;
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
        System.out.println("----------------------> " + query);
        System.out.println("");
        rs = execute_this(query);
        return rs;
    }

    private String parse_conditions(String table_name, ArrayList<SearchItem> query_ps) {
        String conditions = "";

        for (SearchItem item : query_ps) {
            if (conditions.length() != 0)
                conditions += " AND ";
            conditions += parse_search_item(table_name, item);
        }
        System.out.println(conditions);
        return conditions;
    }

    private String parse_search_item(String table_name, SearchItem item) {
        String re = "NULL";
        if (item.getType().toLowerCase().contains(CHAR.toLowerCase())
                || item.getType().toLowerCase().contains(VARCHAR.toLowerCase())) {
            re = table_name + "." + item.getATTRIBUTE_NAME() + " LIKE '%" + item.getVALUE() + "%'";
        } else if (item.getType().toLowerCase().contains(INT.toLowerCase())
                || item.getType().toLowerCase().contains(INTEGER.toLowerCase())
                || item.getType().toLowerCase().contains(FLOAT.toLowerCase()) ||
                item.getType().toLowerCase().contains(DOUBLE.toLowerCase())
                || item.getType().toLowerCase().contains(DECIMAL.toLowerCase())) {
            if (does_it_contains_ops(item.getVALUE())) {
                re = table_name + "." + item.getATTRIBUTE_NAME() + " " + item.getVALUE();
            } else {
                re = table_name + "." + item.getATTRIBUTE_NAME() + " = " + item.getVALUE();
            }
        }
        return re;
    }

    private boolean does_it_contains_ops(String value) {
        for (int i = 0; i < OPERATORS.length; i++) {
            if (value.contains(OPERATORS[i]))
                return true;
        }
        return false;
    }

    private void open_connection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void close_connection() {
        try {
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public ArrayList<Schema> get_tables() {
        return tables_schemas;
    }
}
