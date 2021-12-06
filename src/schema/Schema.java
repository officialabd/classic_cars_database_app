package schema;

import java.util.ArrayList;

import schema.attribute.Attribute;

public class Schema {
    private String tableName;
    private ArrayList<Attribute> attributes;
    private ArrayList<Attribute> primaryKeys;
    private ArrayList<Attribute> foreignKeys;
    private int ATTRIBUTE_NUMBER = 0;

    public Schema(String tableName) {
        this.tableName = tableName;
        attributes = new ArrayList<Attribute>();
        primaryKeys = new ArrayList<Attribute>();
        foreignKeys = new ArrayList<Attribute>();
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void addAttribute(Attribute attribute) {
        ATTRIBUTE_NUMBER++;
        this.attributes.add(attribute);
        if (attribute.isItPrimaryKey()) {
            this.primaryKeys.add(attribute);
        }
        if (attribute.isItForeignKey()) {
            this.foreignKeys.add(attribute);
        }
    }

    public ArrayList<Attribute> getPrimaryKey() {
        return this.primaryKeys;
    }

    public ArrayList<Attribute> getForeignKey() {
        return this.primaryKeys;
    }

    public int getAttributesNumber() {
        return ATTRIBUTE_NUMBER;
    }

    // @Override
    // public String toString() {
    // return "Schema [attributes=" + attributes + ", foreignKeys=" + foreignKeys +
    // ", primaryKeys=" + primaryKeys
    // + ", tableName=" + tableName + "]";
    // }

    @Override
    public String toString() {
        return getTableName();
    }

}
