package parser;

import java.util.ArrayList;

public class QueryInput {
    private final int index;
    private final boolean is_multi;
    private ArrayList<String> values;

    public QueryInput(int index, boolean is_multi) {
        this.index = index;
        this.is_multi = is_multi;
        values = new ArrayList<String>();
    }

    void add_value(String value) {
        values.add(value);
    }

    void clear_values() {
        values.clear();
    }

    public int getIndex() {
        return index;
    }

    public boolean is_multi() {
        return is_multi;
    }

    ArrayList<String> getValues() {
        return values;
    }

    String build_values() {
        StringBuilder strb = new StringBuilder();
        for (String value : values) {
            if (strb.length() > 0)
                strb.append(" | ");
            strb.append(value);
        }
        return strb.toString();
    }

    @Override
    public String toString() {
        return "QueryInput [index=" + index + ", is_multi=" + is_multi + "]";
    }
}
