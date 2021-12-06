package schema.attribute;

import schema.Schema;

public class Attribute {
    private final String name;
    private final boolean isItPrimaryKey;
    private final boolean isItForeignKey;
    private final String type;
    private final Schema reference_fk_table;
    private final boolean isRequired;

    public Attribute(String name, boolean isItPrimaryKey, boolean isItForeignKey, boolean isRequired,
            Schema reference_fk_table,
            String type) {
        this.name = name;
        this.isItPrimaryKey = isItPrimaryKey;
        this.isItForeignKey = isItForeignKey;
        this.type = type;
        this.reference_fk_table = reference_fk_table;
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

    public Schema getReference_fk_table() {
        return reference_fk_table;
    }

    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public String toString() {
        return "Attribute [isItForeignKey=" + isItForeignKey + ", isItPrimaryKey=" + isItPrimaryKey + ", name=" + name
                + ", reference_fk_table=" + reference_fk_table + ", type=" + type + "]";
    }

}
