package parser;

import java.util.ArrayList;

public class Query {
    private final String ID;
    private final String TITLE;
    private final StringBuilder query;
    private final String QUERY_OPEN_VARIABLE = "-$";
    private final String QUERY_CLOSE_VARIABLE = "$-";
    public static final boolean NEXT = true;
    public static final boolean SAME = false;
    private ArrayList<QueryInput> inputs;
    private int inputs_number = 0;
    private int input_index = -1;

    public Query(String id, String title, StringBuilder query) {
        this.ID = id;
        TITLE = title;
        this.query = query;
        this.inputs = new ArrayList<QueryInput>();
        prase_query_inputs();
    }

    private void prase_query_inputs() {
        String tempo = query.toString();
        String[] strs = tempo.split("\\-\\$");
        QueryInput qi;
        for (String str : strs) {
            if (str.contains(QUERY_CLOSE_VARIABLE)) {
                String variable = str.split("\\$\\-")[0];
                if (is_int(variable)) {
                    qi = new QueryInput(Integer.parseInt(variable), false);
                    inputs.add(qi);
                    inputs_number++;
                } else if (is_empty(variable)) {
                    qi = new QueryInput(0, false);
                    inputs.add(qi);
                    inputs_number++;
                } else if (is_multi(variable)) {
                    int id_temp = Integer.parseInt(variable.replace("...", ""));
                    qi = new QueryInput(id_temp, true);
                    inputs.add(qi);
                    inputs_number++;
                }
            }
        }
    }

    public void add_value(String value, boolean next) {
        if (next) {
            input_index++;
        }
        if (input_index >= inputs_number)
            return;
        if (input_index == -1)
            input_index = 0;
        QueryInput qi = inputs.get(input_index);
        for (QueryInput queryInput : inputs) {
            if (queryInput.getIndex() == qi.getIndex()) {
                if ((queryInput.getValues().size() == 0) || queryInput.is_multi()) {
                    queryInput.add_value(value);
                }
            }
        }
    }

    public void clear_values() {
        input_index = -1;
        for (QueryInput qi : inputs) {
            qi.clear_values();
        }
    }

    public String build_query() {
        StringBuilder strb = new StringBuilder(query);
        for (QueryInput qi : inputs) {
            int open_index = strb.indexOf(QUERY_OPEN_VARIABLE) + QUERY_OPEN_VARIABLE.length();
            int close_index = strb.indexOf(QUERY_CLOSE_VARIABLE);
            if (open_index > -1 && close_index > -1) {
                strb.replace(close_index, close_index + QUERY_CLOSE_VARIABLE.length(), "");
                strb.replace(open_index, close_index, qi.build_values());
                strb.replace(open_index - QUERY_OPEN_VARIABLE.length(), open_index, "");
            }
        }
        return strb.toString();
    }

    public static boolean is_int(String str) {
        return str.matches("\\d+");
    }

    public static boolean is_empty(String str) {
        return str.isEmpty();
    }

    public static boolean is_multi(String str) {
        return str.matches("\\d+\\.+");
    }

    public String getID() {
        return ID;
    }

    public String getTitle() {
        return TITLE;
    }

    public String getQuery() {
        return query.toString();
    }

    public int getInputs_number() {
        return inputs_number;
    }

    public ArrayList<QueryInput> getInputs() {
        return inputs;
    }

    @Override
    public String toString() {
        return "Query [ID=" + ID + ", \nTITLE=" + TITLE + ", \nquery=\n" + query + "]";
    }

}
