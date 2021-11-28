package schema.attribute;

public class Attribute {
    private final String name;
    private final boolean isItPrimaryKey;
    private final boolean isItForeignKey;
    private final String type;

    public Attribute(String name, boolean isItPrimaryKey, boolean isItForeignKey, String type) {
        this.name = name;
        this.isItPrimaryKey = isItPrimaryKey;
        this.isItForeignKey = isItForeignKey;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isItPrimaryKey() {
        return isItPrimaryKey;
    }

    public boolean isItForeignKey() {
        return isItForeignKey;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "\nAttribute [isItForeignKey=" + isItForeignKey + ", isItPrimaryKey=" + isItPrimaryKey + ", name=" + name
                + ", type=" + type + "]\n";
    }

}
