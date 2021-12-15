package schema;

import schema.attribute.Attribute;

public class Relation {
    private final Attribute referenced_by_att;
    private final Schema referenced_by_schema;
    private final Attribute reference_att;
    private final Schema reference_schema;
    private final String id;

    public Relation(Attribute referenced_by_att, Schema referenced_by_schema, Attribute reference_att,
            Schema reference_schema, String id) {
        this.referenced_by_att = referenced_by_att;
        this.referenced_by_schema = referenced_by_schema;
        this.reference_att = reference_att;
        this.reference_schema = reference_schema;
        this.id = id;
    }

    public Attribute getReferenced_by_att() {
        return referenced_by_att;
    }

    public Schema getReferenced_by_schema() {
        return referenced_by_schema;
    }

    public Attribute getReference_att() {
        return reference_att;
    }

    public Schema getReference_schema() {
        return reference_schema;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Relation [id=" + id + ", reference_att=" + reference_att.getName() + ", reference_schema="
                + reference_schema.getTableName()
                + ", referenced_by_att=" + referenced_by_att.getName() + ", referenced_by_schema="
                + referenced_by_schema.getTableName() + "]\n";
    }

}
