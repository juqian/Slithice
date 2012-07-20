package jqian.util.eclipse;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.jdt.core.*;

/**
 * A dialog to select concrete methods in the source code
 */
public class MethodSelectionDialog extends Dialog implements JavaElementFilter{
	private IJavaProject _project;
	private Text _resultText;
	private String _title;
	private String _selection;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public MethodSelectionDialog(Shell parentShell,String title,IJavaProject project) {
		super(parentShell);
		
		this._project = project;
		this._title = title;
	}	
	
	public void setTitle(String s){
		
	}
	
	protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        // Dialog Title
        newShell.setText(_title);
    }

	
	public String getSelection(){
		return _selection;
	}
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(null);

		final Label methodLabel = new Label(container, SWT.NONE);
		methodLabel.setText("method");
		methodLabel.setBounds(10, 10, 40, 15);

		final Text resultText = new Text(container, SWT.BORDER);
		resultText.setBounds(56, 7, 426, 23);
		this._resultText = resultText;

		//final Tree tree = new Tree(container, SWT.BORDER);
		//tree.setBounds(10, 38, 472, 255);		
		ProjectSourceViewer treeViewer = new ProjectSourceViewer(container,resultText,_project,null);
		final Tree tree = treeViewer.getTree();	
		tree.setBounds(10, 38, 472, 255);
		//
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}
	
	public boolean isLeafDisplayed(IJavaElement element){
		return true;
	}
	
	public boolean showLibraryCode(){
		return false;
	}
	
	protected void buttonPressed(int buttonId) {		
		if (buttonId == IDialogConstants.OK_ID) {
			//return only if the inputted slicing criterion is validate
			if(_resultText.getText().equals("")){
				ToolTip tip = new ToolTip(getShell(),SWT.CENTER);
				tip.setText("Hind");
				tip.setMessage("Please select one method");
				tip.setVisible(true);
				
				return;			
			}
			
			_selection = _resultText.getText();
		}
		
		super.buttonPressed(buttonId);
	}
	
	public static void main(String[] args){
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Configurator");		
		
		shell.open();		
	
		MethodSelectionDialog dlg = new MethodSelectionDialog(shell,"Hello",null);
		dlg.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		//viewer.run();
		 
		display.dispose();
	}
}