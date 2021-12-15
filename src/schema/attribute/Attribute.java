package schema.attribute;

public class Attribute {
    private final String name;
    private final boolean isItPrimaryKey;
    private final boolean isItForeignKey;
    private final String type;
    private final boolean isRequired;

    public Attribute(String name, boolean isItPrimaryKey, boolean isItForeignKey, boolean isRequired,
            String type) {
        this.name = name;
        this.isItPrimaryKey = isItPrimaryKey;
        this.isItForeignKey = isItForeignKey;
        this.type = type;
        this.isRequired = isRequired;
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

    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public String toString() {
        return "Attribute [isItForeignKey=" + isItForeignKey + ", isItPrimaryKey=" + isItPrimaryKey + ", name=" + name
                + ", type=" + type + "]";
    }

}
