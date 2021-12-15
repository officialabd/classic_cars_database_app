package parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class QueryParser {
    private ArrayList<Query> queries;
    private FileInputStream file = null;
    private BufferedReader in;
    private String file_path;
    private final String BOQ = ">BOQ";
    private final String EOQ = "EOQ<";

    public QueryParser(String file_path) {
        queries = new ArrayList<Query>();
        this.file_path = file_path;
        parse();
    }

    private void parse() {
        try {
            file = new FileInputStream(file_path);
            in = new BufferedReader(new InputStreamReader(file));

            StringBuilder str_query = new StringBuilder();
            String temp = "", id = "", title = "";
            String[] arr;
            Query query;
            while ((temp = in.readLine()) != null) {
                if (temp.contains(BOQ)) {
                    arr = temp.split("~s~");
                    id = arr[1];
                    title = arr[2];
                } else if (temp.contains(EOQ)) {
                    query = new Query(id, title, str_query);
                    queries.add(query);
                    str_query = new StringBuilder();
                } else {
                    str_query.append(temp + "\n");
                }
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Parsing Special Queries failed!");
            alert.setContentText("Couldn't parse queries! Due to " + e.getCause());
            alert.showAndWait();
            System.exit(0);
        }
    }

    public ArrayList<Query> getQueries() {
        return queries;
    }

}
