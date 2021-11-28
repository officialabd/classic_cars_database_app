package databasemanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import schema.Schema;
import schema.attribute.Attribute;

public class Manager {
    private final String DB_URL;
    private final String USERNAME;
    private final String PASSWORD;
    private Connection connection;
    private ArrayList<Schema> tables_schemas;

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
