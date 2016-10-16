package org.dbflute.erflute.editor.view.dialog.relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.erflute.Activator;
import org.dbflute.erflute.core.DisplayMessages;
import org.dbflute.erflute.core.dialog.AbstractDialog;
import org.dbflute.erflute.core.util.Format;
import org.dbflute.erflute.core.widgets.CompositeFactory;
import org.dbflute.erflute.editor.model.diagram_contents.element.connection.Relationship;
import org.dbflute.erflute.editor.model.diagram_contents.element.node.table.ERTable;
import org.dbflute.erflute.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.dbflute.erflute.editor.model.diagram_contents.element.node.table.unique_key.ComplexUniqueKey;
import org.dbflute.erflute.editor.view.dialog.relationship.RelationshipDialog.RelationshipColumnState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author modified by jflute (originated in ermaster)
 */
public class RelationshipByExistingColumnsDialog extends AbstractDialog {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final int COLUMN_WIDTH = 200;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final ERTable source; // parent table e.g. MEMBER_STATUS
    private List<NormalColumn> referencedColumnList;
    private final List<NormalColumn> foreignKeyColumnList;
    private final List<NormalColumn> candidateForeignKeyColumns;
    private final Map<NormalColumn, List<NormalColumn>> referencedMap;
    private final Map<Relationship, Set<NormalColumn>> foreignKeySetMap;
    private final List<TableEditor> tableEditorList;
    private final Map<TableEditor, List<NormalColumn>> editorReferencedMap;

    private Combo columnCombo;
    private Table comparisonTable;
    private RelationshipColumnState relationshipColumnState;

    private boolean referenceForPK;
    private ComplexUniqueKey referencedComplexUniqueKey;
    private NormalColumn referencedColumn;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public RelationshipByExistingColumnsDialog(Shell parentShell, ERTable source, List<NormalColumn> candidateForeignKeyColumns,
            Map<NormalColumn, List<NormalColumn>> referencedMap, Map<Relationship, Set<NormalColumn>> foreignKeySetMap) {
        super(parentShell, 2);
        this.source = source;
        this.referencedColumnList = new ArrayList<NormalColumn>();
        this.foreignKeyColumnList = new ArrayList<NormalColumn>();
        this.candidateForeignKeyColumns = candidateForeignKeyColumns;
        this.referencedMap = referencedMap;
        this.foreignKeySetMap = foreignKeySetMap;
        this.tableEditorList = new ArrayList<TableEditor>();
        this.editorReferencedMap = new HashMap<TableEditor, List<NormalColumn>>();
    }

    // ===================================================================================
    //                                                                         Dialog Area
    //                                                                         ===========
    @Override
    protected String getTitle() {
        return "dialog.title.relation";
    }

    // -----------------------------------------------------
    //                                                Layout
    //                                                ------
    @Override
    protected void initLayout(GridLayout layout) {
        super.initLayout(layout);
        layout.verticalSpacing = 20;
    }

    // -----------------------------------------------------
    //                                             Component
    //                                             ---------
    @Override
    protected void initComponent(Composite composite) {
        final GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        final Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(gridData);
        label.setText(DisplayMessages.getMessage("dialog.message.create.relation.by.existing.columns"));
        createColumnCombo(composite);
        createComparisonTable(composite);
    }

    // -----------------------------------------------------
    //                                          Column Combo
    //                                          ------------
    private void createColumnCombo(Composite composite) {
        final Label label = new Label(composite, SWT.NONE);
        label.setText(DisplayMessages.getMessage("label.reference.column"));
        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        columnCombo = new Combo(composite, SWT.READ_ONLY);
        columnCombo.setLayoutData(gridData);
        columnCombo.setVisibleItemCount(20);
    }

    // -----------------------------------------------------
    //                                      Comparison Table
    //                                      ----------------
    private void createComparisonTable(Composite composite) {
        final GridData tableGridData = new GridData();
        tableGridData.horizontalSpan = 2;
        tableGridData.heightHint = 100;
        tableGridData.horizontalAlignment = GridData.FILL;
        tableGridData.grabExcessHorizontalSpace = true;
        comparisonTable = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        comparisonTable.setLayoutData(tableGridData);
        comparisonTable.setHeaderVisible(true);
        comparisonTable.setLinesVisible(true);
        final TableColumn referencedColumn = new TableColumn(this.comparisonTable, SWT.NONE);
        referencedColumn.setWidth(COLUMN_WIDTH);
        referencedColumn.setText(DisplayMessages.getMessage("label.reference.column"));
        final TableColumn foreignKeyColumn = new TableColumn(this.comparisonTable, SWT.NONE);
        foreignKeyColumn.setWidth(COLUMN_WIDTH);
        foreignKeyColumn.setText(DisplayMessages.getMessage("label.foreign.key"));
    }

    // -----------------------------------------------------
    //                                           Set up Data
    //                                           -----------
    @Override
    protected void setupData() {
        relationshipColumnState = RelationshipDialog.setReferencedColumnComboData(columnCombo, source);
        // TODO jflute making automatically creating column (2016/10/16)
        // columnCombo.add("(new column)")
        // relationshipColumnState.add("(new column)")
        columnCombo.select(0);
        createComparisonTableRows();
    }

    // ===================================================================================
    //                                                                            Listener
    //                                                                            ========
    @Override
    protected void addListener() {
        super.addListener();
        columnCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO jflute making automatically creating column (2016/10/16)
                //final ERTable sourceTable = (ERTable) source.getModel(); // e.g. MEMBER_STATUS
                //final Relationship temp = sourceTable.createRelation();
                //relationship.setReferenceForPK(temp.isReferenceForPK());
                //relationship.setReferencedComplexUniqueKey(temp.getReferencedComplexUniqueKey());
                //relationship.setReferencedColumn(temp.getReferencedColumn());
                // prepare new columns for comparisonTable here
                comparisonTable.removeAll();
                disposeTableEditor();
                createComparisonTableRows();
                validate();
            }
        });
        comparisonTable.addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.height = columnCombo.getSize().y;
            }
        });
    }

    private void createComparisonTableRows() {
        try {
            final int index = columnCombo.getSelectionIndex();
            if (index < relationshipColumnState.complexUniqueKeyStartIndex) {
                referencedColumnList = source.getPrimaryKeys();
            } else if (index < relationshipColumnState.columnStartIndex) {
                final ComplexUniqueKey complexUniqueKey =
                        source.getComplexUniqueKeyList().get(index - relationshipColumnState.complexUniqueKeyStartIndex);
                referencedColumnList = complexUniqueKey.getColumnList();
            } else {
                final NormalColumn referencedColumn =
                        relationshipColumnState.candidateColumns.get(index - relationshipColumnState.columnStartIndex);
                referencedColumnList = new ArrayList<NormalColumn>();
                referencedColumnList.add(referencedColumn);
            }
            for (final NormalColumn referencedColumn : referencedColumnList) {
                column2TableItem(referencedColumn);
            }
        } catch (final Exception e) {
            Activator.showExceptionDialog(e);
        }
    }

    private void column2TableItem(NormalColumn referencedColumn) {
        final TableItem tableItem = new TableItem(comparisonTable, SWT.NONE);
        tableItem.setText(0, Format.null2blank(referencedColumn.getLogicalName()));
        final List<NormalColumn> foreignKeyList = referencedMap.get(referencedColumn.getRootReferencedColumn());
        final TableEditor tableEditor = new TableEditor(this.comparisonTable);
        tableEditor.grabHorizontal = true;
        tableEditor.setEditor(createForeignKeyCombo(foreignKeyList), tableItem, 1);
        this.tableEditorList.add(tableEditor);
        this.editorReferencedMap.put(tableEditor, foreignKeyList);
    }

    protected Combo createForeignKeyCombo(List<NormalColumn> foreignKeyList) {
        final Combo foreignKeyCombo = CompositeFactory.createReadOnlyCombo(this, this.comparisonTable, null);
        foreignKeyCombo.add("");
        if (foreignKeyList != null) {
            for (final NormalColumn normalColumn : foreignKeyList) {
                foreignKeyCombo.add(Format.toString(normalColumn.getName()));
            }
        }
        for (final NormalColumn normalColumn : this.candidateForeignKeyColumns) {
            foreignKeyCombo.add(Format.toString(normalColumn.getName()));
        }
        if (foreignKeyCombo.getItemCount() > 0) {
            foreignKeyCombo.select(0);
        }
        return foreignKeyCombo;
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    @Override
    protected String doValidate() {
        final Set<NormalColumn> selectedColumns = new HashSet<NormalColumn>();
        for (final TableEditor tableEditor : tableEditorList) {
            final Combo foreignKeyCombo = (Combo) tableEditor.getEditor();
            final int index = foreignKeyCombo.getSelectionIndex();
            if (index == 0) {
                return "error.foreign.key.not.selected";
            }
            final NormalColumn selectedColumn = findSelectedColumn(tableEditor);
            if (selectedColumns.contains(selectedColumn)) {
                return "error.foreign.key.must.be.different";
            }
            selectedColumns.add(selectedColumn);
        }
        if (this.existForeignKeySet(selectedColumns)) {
            return "error.foreign.key.already.exist";
        }
        return null;
    }

    private boolean existForeignKeySet(Set<NormalColumn> columnSet) {
        boolean exist = false;
        for (final Set<NormalColumn> foreignKeySet : this.foreignKeySetMap.values()) {
            if (foreignKeySet.size() == columnSet.size()) {
                exist = true;
                for (final NormalColumn normalColumn : columnSet) {
                    if (!foreignKeySet.contains(normalColumn)) {
                        exist = false;
                        continue;
                    }
                }
                break;
            }
        }
        return exist;
    }

    // ===================================================================================
    //                                                                          Perform OK
    //                                                                          ==========
    @Override
    protected void performOK() {
        final int index = columnCombo.getSelectionIndex();
        if (index < relationshipColumnState.complexUniqueKeyStartIndex) {
            referenceForPK = true;
        } else if (index < relationshipColumnState.columnStartIndex) {
            final ComplexUniqueKey complexUniqueKey =
                    source.getComplexUniqueKeyList().get(index - relationshipColumnState.complexUniqueKeyStartIndex);
            referencedComplexUniqueKey = complexUniqueKey;
        } else {
            referencedColumn = relationshipColumnState.candidateColumns.get(index - relationshipColumnState.columnStartIndex);
        }
        for (final TableEditor tableEditor : tableEditorList) {
            final NormalColumn foreignKeyColumn = findSelectedColumn(tableEditor);
            foreignKeyColumnList.add(foreignKeyColumn);
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private NormalColumn findSelectedColumn(TableEditor tableEditor) {
        final Combo foreignKeyCombo = (Combo) tableEditor.getEditor();
        final int foreignKeyComboIndex = foreignKeyCombo.getSelectionIndex();
        int startIndex = 1;
        NormalColumn foreignKeyColumn = null;
        final List<NormalColumn> foreignKeyList = editorReferencedMap.get(tableEditor);
        if (foreignKeyList != null) {
            if (foreignKeyComboIndex <= foreignKeyList.size()) {
                foreignKeyColumn = foreignKeyList.get(foreignKeyComboIndex - startIndex);
            } else {
                startIndex += foreignKeyList.size();
            }
        }
        if (foreignKeyColumn == null) {
            foreignKeyColumn = this.candidateForeignKeyColumns.get(foreignKeyComboIndex - startIndex);
        }
        return foreignKeyColumn;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean close() {
        disposeTableEditor();
        return super.close();
    }

    private void disposeTableEditor() {
        for (final TableEditor tableEditor : this.tableEditorList) {
            tableEditor.getEditor().dispose();
            tableEditor.dispose();
        }
        this.tableEditorList.clear();
        this.editorReferencedMap.clear();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<NormalColumn> getReferencedColumnList() {
        return referencedColumnList;
    }

    public List<NormalColumn> getForeignKeyColumnList() {
        return foreignKeyColumnList;
    }

    public boolean isReferenceForPK() {
        return referenceForPK;
    }

    public ComplexUniqueKey getReferencedComplexUniqueKey() {
        return referencedComplexUniqueKey;
    }

    public NormalColumn getReferencedColumn() {
        return this.referencedColumn;
    }
}
