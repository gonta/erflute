package org.dbflute.erflute.db.impl.db2;

import org.dbflute.erflute.core.util.Check;
import org.dbflute.erflute.core.util.Format;
import org.dbflute.erflute.db.impl.db2.tablespace.DB2TablespaceProperties;
import org.dbflute.erflute.editor.model.ERDiagram;
import org.dbflute.erflute.editor.model.dbexport.ddl.DDLCreator;
import org.dbflute.erflute.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.dbflute.erflute.editor.model.diagram_contents.not_element.sequence.Sequence;
import org.dbflute.erflute.editor.model.diagram_contents.not_element.tablespace.Tablespace;

public class DB2DDLCreator extends DDLCreator {

    public DB2DDLCreator(ERDiagram diagram, boolean semicolon) {
        super(diagram, semicolon);
    }

    @Override
    protected String buildColumnPart(NormalColumn normalColumn) {
        final StringBuilder ddl = new StringBuilder();
        ddl.append(super.buildColumnPart(normalColumn));

        if (normalColumn.isAutoIncrement()) {
            ddl.append(" GENERATED ALWAYS AS IDENTITY ");

            final Sequence sequence = normalColumn.getAutoIncrementSetting();
            if (sequence.getIncrement() != null || sequence.getStart() != null) {
                ddl.append("(START WITH ");
                if (sequence.getStart() != null) {
                    ddl.append(sequence.getStart());

                } else {
                    ddl.append("1");
                }

                if (sequence.getIncrement() != null) {
                    ddl.append(", INCREMENT BY ");
                    ddl.append(sequence.getIncrement());
                }

                ddl.append(")");
            }
        }

        return ddl.toString();
    }

    @Override
    protected String doBuildCreateTablespace(Tablespace tablespace) {
        final DB2TablespaceProperties tablespaceProperties =
                (DB2TablespaceProperties) tablespace.getProperties(environment, getDiagram());

        final StringBuilder ddl = new StringBuilder();

        ddl.append("CREATE ");
        if (!Check.isEmpty(tablespaceProperties.getType())) {
            ddl.append(tablespaceProperties.getType());
            ddl.append(" ");
        }

        ddl.append("TABLESPACE ");
        ddl.append(filter(tablespace.getName()));
        ddl.append("\r\n");

        if (!Check.isEmpty(tablespaceProperties.getPageSize())) {
            ddl.append(" PAGESIZE ");
            ddl.append(tablespaceProperties.getPageSize());
            ddl.append("\r\n");
        }

        ddl.append(" MANAGED BY ");
        ddl.append(tablespaceProperties.getManagedBy());
        ddl.append(" USING(");
        ddl.append(tablespaceProperties.getContainer());
        ddl.append(")\r\n");

        if (!Check.isEmpty(tablespaceProperties.getExtentSize())) {
            ddl.append(" EXTENTSIZE ");
            ddl.append(tablespaceProperties.getExtentSize());
            ddl.append("\r\n");
        }

        if (!Check.isEmpty(tablespaceProperties.getPrefetchSize())) {
            ddl.append(" PREFETCHSIZE ");
            ddl.append(tablespaceProperties.getPrefetchSize());
            ddl.append("\r\n");
        }

        if (!Check.isEmpty(tablespaceProperties.getBufferPoolName())) {
            ddl.append(" BUFFERPOOL ");
            ddl.append(tablespaceProperties.getBufferPoolName());
            ddl.append("\r\n");
        }

        if (semicolon) {
            ddl.append(";");
        }

        return ddl.toString();
    }

    @Override
    public String doBuildCreateSequence(Sequence sequence) {
        final StringBuilder ddl = new StringBuilder();

        final String description = sequence.getDescription();
        if (semicolon && !Check.isEmpty(description) && ddlTarget.inlineTableComment) {
            ddl.append("-- ");
            ddl.append(description.replaceAll("\n", "\n-- "));
            ddl.append("\r\n");
        }

        ddl.append("CREATE ");
        ddl.append("SEQUENCE ");
        ddl.append(filter(getNameWithSchema(sequence.getSchema(), sequence.getName())));
        if (!Check.isEmpty(sequence.getDataType())) {
            ddl.append(" AS ");
            String dataType = sequence.getDataType();
            dataType = dataType.replaceAll("\\(p\\)", "(" + Format.toString(sequence.getDecimalSize() + ")"));
            ddl.append(dataType);
        }
        if (sequence.getIncrement() != null) {
            ddl.append(" INCREMENT BY ");
            ddl.append(sequence.getIncrement());
        }
        if (sequence.getMinValue() != null) {
            ddl.append(" MINVALUE ");
            ddl.append(sequence.getMinValue());
        }
        if (sequence.getMaxValue() != null) {
            ddl.append(" MAXVALUE ");
            ddl.append(sequence.getMaxValue());
        }
        if (sequence.getStart() != null) {
            ddl.append(" START WITH ");
            ddl.append(sequence.getStart());
        }
        if (sequence.getCache() != null) {
            ddl.append(" CACHE ");
            ddl.append(sequence.getCache());
        }
        if (sequence.isCycle()) {
            ddl.append(" CYCLE");
        }
        if (sequence.isOrder()) {
            ddl.append(" ORDER");
        }
        if (semicolon) {
            ddl.append(";");
        }

        return ddl.toString();

    }
}
