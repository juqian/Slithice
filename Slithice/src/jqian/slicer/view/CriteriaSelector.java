package jqian.slicer.view;

import java.util.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
//import org.eclipse.swt.widgets.FileDialog;
//import org.eclipse.core.resources.*; 
//import org.eclipse.ui.part.*;
//import org.eclipse.ui.*; 
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.texteditor.*;
import org.eclipse.jdt.ui.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import jqian.slicer.util.*;
import jqian.util.eclipse.JDTUtils;
import jqian.util.eclipse.MethodSelectionDialog;

public class CriteriaSelector extends Dialog {
	protected IJavaProject _project;
	protected ITextEditor _editor;
	protected ITextSelection _selection;	
	protected String _entry;
	protected String _scenarioPath;
	protected String _slicedMethod;
	protected Collection<String> _vars = new LinkedList<String>();
	protected int    _line;
	protected boolean _sliceInGlobal;
	protected boolean _slicingOnAssignedOne;
	
	//--------------------- UI elements--------------------------//
	private Text _prjEntryText; 
	private Text _criterionMethodText;
	private Text _lineText;
	private Text _lineContextText;
	private Text _variablesText;
	private String _defaultPrjEntry;
	private Button assignedButton;
	private boolean _prjEntryEditable;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public CriteriaSelector(Shell parentShell,IJavaProject project,ITextEditor editor,
			                ITextSelection selection, String defaultPrjEntry,boolean prjEntryEditable) {
		super(parentShell);
		this._project = project;
		this._editor = editor;
		this._selection = selection;
		this._defaultPrjEntry = defaultPrjEntry;
		this._prjEntryEditable = prjEntryEditable;
	}

	public int getLine(){
		return _line;
	}
	
	public String getSlicingStartMethod(){
		return _slicedMethod;
	}
	
	public String getScenarioPath(){
		return _scenarioPath;
	}	
	
	public boolean isSlicingInGlobal(){
		return _sliceInGlobal;
	}
	
	public Collection<String> getSlicedVars(){
		return _vars;
	}
	
	public boolean slicingOnAssignedVar(){
		return _slicingOnAssignedOne;
	}
	//public String get
	
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(null);

		final Label projectEntryLabel = new Label(container, SWT.NONE);
		projectEntryLabel.setText("Project entry");
		projectEntryLabel.setBounds(15, 17, 73, 17);

		_prjEntryText = new Text(container, SWT.BORDER);
		_prjEntryText.setBounds(94, 14, 303, 23);	
		_prjEntryText.setText(_defaultPrjEntry);

		final Button entrySelectButton = new Button(container, SWT.NONE);
		entrySelectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onSelectProjectEntry(e);
			}
		});
		entrySelectButton.setText("Select");
		entrySelectButton.setBounds(403, 12, 46, 27);
		
		if(!_prjEntryEditable){			
			_prjEntryText.setEditable(false);
			entrySelectButton.setEnabled(false);
		}

		final Group slicingCriteriaGroup = new Group(container, SWT.NONE);
		slicingCriteriaGroup.setText("Slicing criterion");
		slicingCriteriaGroup.setBounds(10, 52, 439, 252);

		final Label methodLabel = new Label(slicingCriteriaGroup, SWT.NONE);
		methodLabel.setText("method");
		methodLabel.setBounds(10, 35, 53, 15);		

		_criterionMethodText = new Text(slicingCriteriaGroup, SWT.BORDER);
		_criterionMethodText.setBounds(82, 32, 294, 23);	
		
		//method not editable
		//_criterionMethodText.setEditable(false);

		final Label lineLabel = new Label(slicingCriteriaGroup, SWT.NONE);
		lineLabel.setText("line");
		lineLabel.setBounds(22, 66, 20, 15);

		_lineText = new Text(slicingCriteriaGroup, SWT.BORDER);
		_lineText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				onChangeLine(e);
			}
		});
		_lineText.setBounds(82, 61, 347, 23);
		int line = getSlicingCriteriionLine()+1;
		_lineText.setText(""+line);
		
		//line not editable
		//_lineText.setEditable(false);

		_lineContextText = new Text(slicingCriteriaGroup, SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
		_lineContextText.setBounds(82, 90, 347, 61);
		updateLineText(getSlicingCriteriionLine());

		final Label variableLabel = new Label(slicingCriteriaGroup, SWT.NONE);
		variableLabel.setText("variables");
		variableLabel.setBounds(10, 160, 53, 15);

		_variablesText = new Text(slicingCriteriaGroup, SWT.BORDER);
		_variablesText.setBounds(82, 157, 347, 23);
		
		//variable not editable
		//_variablesText.setEditable(false);
		
		final Button selectButton = new Button(slicingCriteriaGroup, SWT.NONE);
		selectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onSelectCriterionMethod(e);
			}
		});
		selectButton.setText("Select");
		selectButton.setBounds(382, 30, 47, 27);

		assignedButton = new Button(slicingCriteriaGroup, SWT.CHECK);
		assignedButton.setText("Focus on variable on the left of assignment");
		assignedButton.setBounds(70, 186, 335, 20);
		
				final Button isGlobalButton = new Button(slicingCriteriaGroup, SWT.CHECK);
				isGlobalButton.setBounds(229, 212, 200, 30);
				isGlobalButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						onSelectGlobalMode(e);
					}
				});
				isGlobalButton.setText("Slicing in the global?");
		if(isSelectionOnTheLeftOfAssignment()){
			assignedButton.setSelection(true);
		}
		
		showSelectedVariables();
		
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
		return new Point(467, 389);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select criterion for slicing");
	}
	
	//----------------------- Event handles ----------------------------//
	private void onSelectProjectEntry(SelectionEvent e){
		String entry = selectProjectEntry();
		if(entry!=null){
			_prjEntryText.setText(entry);
		}
	}
	
	private void onSelectGlobalMode(SelectionEvent e){
		_sliceInGlobal = !_sliceInGlobal;
	}
	
	private void onSelectCriterionMethod(SelectionEvent e){
		String method = selectMethod("Select the method where a slicing criterion lies in");
		_criterionMethodText.setText(method);
	}
	
	
	private void showSelectedVariables(){
		//find selected java element
		ICompilationUnit icompilationUnit = JDTUtils.getCompliationUnit(_editor);
		IJavaElement[] elements = null;
		try{
			elements = JDTUtils.codeResolve(icompilationUnit,_selection); 
		}catch(Exception e){}
		
		//
		String all = "";
		Object[] selected = getSelectedVariables(elements);
		for(int i=0;i<selected.length;i++){
			String s = selected[i].toString();			
			all += s;
			if(i<selected.length-1)
				all +=", ";
		}
		
		_variablesText.setText(all);
		
		
		//The method label will be update with the line
		
		//get the selected method
		//if(selected.length>0){
			//ILocalVariable local = (ILocalVariable)selected[0];
			//IMethod method = JDTUtils.getMethod(local);
			//int line = getSlicingCriteriionLine();
			//updateMethodSignature(line);
		//}
	}
	
	private void updateMethodSignature(int line){
		try{
			IMethod method = JDTUtils.getEnclosingMethod(_editor,line);
			String sig = JDTUtils.getMethodSootSignature(method);
			_criterionMethodText.setText(sig);
		}
		catch(Exception e){}
	}
	
	private Object[] getSelectedVariables(IJavaElement[] elements){
		if(elements==null){
			return new Object[0];
		}
		
		try{						
			LinkedList<String> vars = new LinkedList<String>();
			
			for(int i=0;i<elements.length;i++){
				IJavaElement elmt = elements[i];
				if(elmt instanceof ILocalVariable){					 
					String valName = elmt.getElementName();
					vars.add(valName);
				}
				else if(elmt instanceof IField){
					IField field = (IField)elmt;
					int flags = field.getFlags();
					
					//static field
					if(Flags.isStatic(flags) || Flags.isEnum(flags)){
						String fieldSig = JDTUtils.getFieldSignature(field);
						vars.add(fieldSig);
					}
					else{
						String selText = JDTUtils.getSelectedText(_editor, _selection);
						int index = selText.lastIndexOf('.');
						String prefix = selText.substring(0,index+1);	
						
						if(prefix.length()==0){
							prefix = "this.";
						}

						String fieldSig = JDTUtils.getFieldSignature(field);
						String name = prefix + fieldSig;
						vars.add(name);
					}
				}
			}
			
			return vars.toArray();			
		}catch(Exception e){
			ErrorPrinter.printError(e);
		}
		
		return null;
	}
	
	private boolean isSelectionOnTheLeftOfAssignment(){
		try{
			int offset = _selection.getOffset() + _selection.getLength();
			IDocument doc = _editor.getDocumentProvider().getDocument(_editor.getEditorInput());
			
			int length = doc.getLength();
			char ch = ' ';
			while(offset<length){
				ch = doc.getChar(offset);
				if(Character.isJavaIdentifierPart(ch) || Character.isJavaIdentifierStart(ch)){
					break;
				}
				else if(Character.isWhitespace(ch)){
					offset++;
				}
				else{
					break;
				}
			}
			
			if(ch=='='){
				return true;
			}
		}
		catch(Exception e){			
		}
		
		return false;
	}

	
	protected String selectMethod(String title){
		try {
			Shell shell = this.getParentShell();			
			/*IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{_project}, false);
			SelectionDialog dialog = JavaUI.createTypeDialog(shell,
					new ProgressMonitorDialog(shell), scope,
					IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false);*/
			MethodSelectionDialog dlg = new MethodSelectionDialog(shell,title,_project);			
			if (dlg.open() != IDialogConstants.OK_ID)
				return null;

			return dlg.getSelection();
		} catch (Exception e) {
			return null;
		}		
	}

	protected String selectProjectEntry(){
		Shell shell = this.getParentShell();
		int style = 0;//IJavaElementSearchConstants.CONSIDER_BINARIES;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{_project}, false);		
		SelectionDialog dlg = JavaUI.createMainTypeDialog(shell, new ProgressMonitorDialog(shell),				                             
				                           scope,style,false);		
			
		dlg.setTitle("Select a project entry");
		dlg.setMessage("Ench analysis require a entry method, please select a method for slicing");
		
		if (dlg.open() != IDialogConstants.OK_ID)
            return null;
		
		Object[] results = dlg.getResult();
		if (results != null && results.length > 0) {
			IType type= (IType)results[0];
			String name = type.getTypeQualifiedName();
			String pkg = type.getPackageFragment().getElementName();
			String signature = "<"+pkg+"."+name+": void main(java.lang.String[])>";
			
			//OpenTypeHierarchyUtil.open(new IType[] { type }, fWindow);
			return signature;
		}
		
		return null;
	}
	
	private void onChangeLine(ModifyEvent e){
		//update line
		String lineStr = _lineText.getText();
		int line = Integer.parseInt(lineStr);
		
		//update method signature
		updateMethodSignature(line-1);
		
		//update line context
		updateLineText(line-1);
	}
	
	private int getSlicingCriteriionLine(){
		return _selection.getEndLine();
	}
	
	private void updateLineText(int line){
		try{
			IDocumentProvider provider = _editor.getDocumentProvider();
			IDocument doc = provider.getDocument(_editor.getEditorInput());
			int startLine = line-2;
			startLine = startLine>=0? startLine:0;
			int endLine = line+2;
			while(endLine>doc.getNumberOfLines()){
				endLine--;
			}
						
			String text = "";
			
			for(int i=startLine;i<=endLine;i++){
				text += getLineText(i,doc);
				if(line<endLine)
					text += "\n";
			}			
			
			if(_lineContextText!=null)
			    _lineContextText.setText(text);			
		}catch(Exception e){
			if(_lineContextText!=null)
			    _lineContextText.setText("");	
		}		
	}
	
	private String getLineText(int line,IDocument doc) throws Exception{
		IRegion region = doc.getLineInformation(line);
		int offset = region.getOffset();
		int length = region.getLength();
		return doc.get(offset,length);
		
	}
	
	
	/** Check the validence of a selected slicing criteria. */
	protected boolean checkValidence(){
		return true;
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			//return only if the inputted slicing criterion is validate
			if(!checkValidence())
			    return;	
			
			//get sliced variables
			String varText = _variablesText.getText();
			String[] strs = varText.split(",");
			 
			_vars = Arrays.asList(strs);
			
			_line = Integer.parseInt(_lineText.getText());
			_slicedMethod = _criterionMethodText.getText();
			_slicingOnAssignedOne = assignedButton.getSelection();
		}
		
		super.buttonPressed(buttonId);
	}

}
