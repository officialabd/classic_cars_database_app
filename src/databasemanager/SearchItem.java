package databasemanager;

public class SearchItem {
    private final String ATTRIBUTE_NAME;
    private final String VALUE;
    private final String TYPE;

    public SearchItem(String attribute_name, String value, String type) {
        this.ATTRIBUTE_NAME = attribute_name;
        this.VALUE = value;
        this.TYPE = type;
    }

    public String getATTRIBUTE_NAME() {
        return ATTRIBUTE_NAME;
    }

    public String getVALUE() {
        return VALUE;
    }

    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "SearchItem [ATTRIBUTE_NAME=" + ATTRIBUTE_NAME + ", TYPE=" + TYPE + ", VALUE=" + VALUE + "]";
    }

}
