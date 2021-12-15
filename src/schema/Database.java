package schema;

import java.util.ArrayList;

public class Database {
    private String NAME;
    private String PASSWORD;
    private ArrayList<Schema> tables;
    private ArrayList<Relation> relations;

    public Database(String name, String password) {
        tables = new ArrayList<Schema>();
        relations = new ArrayList<Relation>();

        NAME = name;
        PASSWORD = password;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String name) {
        NAME = name;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public void setPASSWORD(String password) {
        PASSWORD = password;
    }

    public ArrayList<Schema> getTables() {
        return tables;
    }

    public Schema find_table(String name) {
        for (Schema schema : tables) {
            if (schema.getTableName().equalsIgnoreCase(name))
                return schema;
        }
        return null;
    }

    public void add_table(Schema table) {
        if (table != null)
            this.tables.add(table);
    }

    public ArrayList<Relation> getRelations() {
        return relations;
    }

    public Relation find_relations(String referenced_by) {
        for (Relation relation : relations) {
            if (relation.getReferenced_by_att().getName().equalsIgnoreCase(referenced_by))
                return relation;
        }
        return null;
    }

    public void add_relation(Relation relation) {
        if (relation != null)
            this.relations.add(relation);
    }

    @Override
    public String toString() {
        return "Database [NAME=" + NAME + ", PASSWORD=" + PASSWORD + ", relations=" + relations + ", tables=" + tables
                + "]";
    }

}
