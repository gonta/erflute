package org.dbflute.erflute.editor.view.action.outline.index;

import java.util.List;

import org.dbflute.erflute.core.DisplayMessages;
import org.dbflute.erflute.editor.controller.command.diagram_contents.not_element.index.CreateIndexCommand;
import org.dbflute.erflute.editor.model.ERDiagram;
import org.dbflute.erflute.editor.model.diagram_contents.element.node.table.ERTable;
import org.dbflute.erflute.editor.view.action.outline.AbstractOutlineBaseAction;
import org.dbflute.erflute.editor.view.dialog.table.sub.IndexDialog;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

public class CreateIndexAction extends AbstractOutlineBaseAction {

    public static final String ID = CreateIndexAction.class.getName();

    public CreateIndexAction(TreeViewer treeViewer) {
        super(ID, DisplayMessages.getMessage("action.title.create.index"), treeViewer);
    }

    @Override
    public void execute(Event event) {
        final ERDiagram diagram = getDiagram();
        final List<?> selectedEditParts = getTreeViewer().getSelectedEditParts();
        final EditPart editPart = (EditPart) selectedEditParts.get(0);
        final ERTable table = (ERTable) editPart.getModel();
        final IndexDialog dialog = new IndexDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), null, table);
        if (dialog.open() == IDialogConstants.OK_ID) {
            final CreateIndexCommand command = new CreateIndexCommand(diagram, dialog.getResultIndex());
            execute(command);
        }
    }
}
