package org.dbflute.erflute.core.widgets;

import org.dbflute.erflute.Activator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * #analyzed workspace内部領域としてのディレクトリ入力テキスト
 * @author ermaster
 * @author jflute
 */
public class InnerDirectoryText {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final Text text;
    private final Button openBrowseButton;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public InnerDirectoryText(Composite parent, int style) {
        this.text = new Text(parent, style);

        this.openBrowseButton = new Button(parent, SWT.NONE);
        openBrowseButton.setText(JFaceResources.getString("openBrowse"));

        openBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final String saveFilePath = Activator.showDirectoryDialogInternal(text.getText());
                text.setText(saveFilePath);
            }
        });
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setLayoutData(Object layoutData) {
        text.setLayoutData(layoutData);
    }

    public void setText(String text) {
        this.text.setText(text);
        this.text.setSelection(text.length());
    }

    public boolean isBlank() {
        if (text.getText().trim().length() == 0) {
            return true;
        }
        return false;
    }

    public String getFilePath() {
        return text.getText().trim();
    }

    public void addModifyListener(ModifyListener listener) {
        text.addModifyListener(listener);
    }
}
