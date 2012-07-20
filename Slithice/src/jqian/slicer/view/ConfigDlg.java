package jqian.slicer.view;

import jqian.slicer.core.SlicerOptions;
import jqian.slicer.plugin.ID;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.PointsToAnalysisType;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;


public class ConfigDlg extends Dialog {
	private Text text;
	private Combo comboSummaryEdge;
	private Button showSDGBttn;
	private Button verboseBttn;
	private Button simplifyCGBttn;
	private Button ignoreJreClinitBttn;
	private Button showSliceBttn;
	private Button useNavigatorBttn;
	private Combo comboPointsTo;
	private Combo comboSideEffect;
	private Text libDepth;
	
	private final SlicerOptions _options; 
	private Button btnDistinguishMemLocs;
	 
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ConfigDlg(Shell parentShell,SlicerOptions initOptions) {
		super(parentShell);	
	
		this._options = new SlicerOptions(initOptions);
	}

	public SlicerOptions getConfiguration(){
		return _options;
	}
	
	private String onSelectFile(){
		 FileDialog fileDialog = new FileDialog(this.getShell(), SWT.MULTI);
		 String firstFile = fileDialog.open(); 
		 return firstFile;
	}
	
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {		
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(null);

		final Group optionsGroup = new Group(container, SWT.NONE);
		optionsGroup.setText("Options");
		optionsGroup.setBounds(0, 0, 451, 336);

		final Label dotPathLabel = new Label(optionsGroup, SWT.NONE);
		dotPathLabel.setBounds(21, 34,50, 16);
		dotPathLabel.setText("dot path");

		text = new Text(optionsGroup, SWT.BORDER);
		text.setBounds(76, 31,315, 25);	 
		if(_options.dotpath!=null){
			text.setText(_options.dotpath);
		}

		final Button button = new Button(optionsGroup, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				String file = onSelectFile();
				text.setText(file);
			}
		});
		button.setBounds(397, 31,44, 23);
		button.setText("...");
		
		Label lblBasePointerAnalysis = new Label(optionsGroup, SWT.NONE);
		lblBasePointerAnalysis.setText("Base pointer analysis");
		lblBasePointerAnalysis.setBounds(21, 65, 134, 16);
		
		comboPointsTo = new Combo(optionsGroup, SWT.NONE);
		comboPointsTo.setItems(new String[] {"Spark: a context-insensitive inclusion-based pointer analysis", "Type-based pointer analysis", "Naive pointer analysis"});
		comboPointsTo.setBounds(181, 62, 260, 25);
		if(_options.pointToAnalysis==PointsToAnalysisType.SPARK){
			comboPointsTo.select(0);
		}
		else if(_options.pointToAnalysis==PointsToAnalysisType.TYPE_BASED){
			comboPointsTo.select(1);
		}
		else{
			comboPointsTo.select(2);
		}		
		
		Label lblInSummaryedgeConstruction = new Label(optionsGroup, SWT.NONE);
		lblInSummaryedgeConstruction.setText("Heap memory abstraction");
		lblInSummaryedgeConstruction.setBounds(21, 95, 166, 16);
		
		comboSideEffect = new Combo(optionsGroup, SWT.NONE);
		comboSideEffect.setItems(new String[] {"field-sensitive (slow)", "field-based", "type-based", "do not distinguish heap locations"});
		comboSideEffect.setBounds(220, 90, 221, 25);
		if(_options.heapAbstraction==HeapAbstraction.FIELD_SENSITIVE){
			comboSideEffect.select(0);
		}
		else if(_options.heapAbstraction==HeapAbstraction.FIELD_BASED){
			comboSideEffect.select(1);
		}
		else if(_options.heapAbstraction==HeapAbstraction.TYPE_BASED){
			comboSideEffect.select(2);
		}
		else{
			comboSideEffect.select(3);
		}
		
		
		Label lblNewLabel = new Label(optionsGroup, SWT.NONE);
		lblNewLabel.setBounds(21, 123, 218, 16);
		lblNewLabel.setText("SDG formal/actual node construction");
		
		comboSummaryEdge = new Combo(optionsGroup, SWT.NONE);
		comboSummaryEdge.setItems(new String[] {"field-sensitive (slow)", "field-based", "type-based", "do not distinguish heap locations"});
		comboSummaryEdge.setBounds(245, 118, 196, 25);
		if(_options.sdgFormalActualOption==HeapAbstraction.FIELD_SENSITIVE){
			comboSummaryEdge.select(0);
		}
		else if(_options.sdgFormalActualOption==HeapAbstraction.FIELD_BASED){
			comboSummaryEdge.select(1);
		}
		else if(_options.sdgFormalActualOption==HeapAbstraction.TYPE_BASED){
			comboSummaryEdge.select(2);
		}
		else{
			comboSummaryEdge.select(3);
		}
		
		Label lblMaxCallTracing = new Label(optionsGroup, SWT.NONE);
		lblMaxCallTracing.setText("Max call tracing depth when analyzing library methods");
		lblMaxCallTracing.setBounds(21, 150, 323, 16);
		
		libDepth = new Text(optionsGroup, SWT.BORDER);
		libDepth.setBounds(350, 146, 91, 23);
		if(_options.libTracingDepth>0){
			libDepth.setText("" + _options.libTracingDepth);
		}

		showSDGBttn = new Button(optionsGroup, SWT.CHECK);
		showSDGBttn.setBounds(21, 179,386, 16);
		showSDGBttn.setText("Show Jimple System Dependence Graph (for expert use)");
		showSDGBttn.setToolTipText("The dependence graph can be too large to show, only select this for expert use");
		if(_options.showJimpleSDG){
			showSDGBttn.setSelection(true);
		}

		showSliceBttn = new Button(optionsGroup, SWT.CHECK);		
		showSliceBttn.setText("Show Slices in Jimple Dependence Graph (for expert use)");
		showSDGBttn.setToolTipText("The dependence graph can be too large to show, only select this for expert use");
		showSliceBttn.setBounds(21, 201, 386, 16);
		if(_options.showSliceInSDG){
			showSliceBttn.setSelection(true);
		}

		simplifyCGBttn = new Button(optionsGroup, SWT.CHECK);
		simplifyCGBttn.setBounds(21, 223,386, 16);
		simplifyCGBttn.setText("Simplify Call Graph Using Heristics");
		if(_options.simplifyCallGraph){
			simplifyCGBttn.setSelection(true);
		}

		ignoreJreClinitBttn = new Button(optionsGroup, SWT.CHECK);
		ignoreJreClinitBttn.setBounds(21, 245,386, 16);
		ignoreJreClinitBttn.setText("Ignore Class Initialization of Java Library in Analysis");		
		if(_options.ignoreJreClinits){
			ignoreJreClinitBttn.setSelection(true);
		}

		btnDistinguishMemLocs = new Button(optionsGroup, SWT.CHECK);
		btnDistinguishMemLocs.setText("Distinguish def/use locations when build dep edges for statements");
		btnDistinguishMemLocs.setBounds(21, 267, 420, 16);
		if(_options.distinguishDULocInDepEdges){
			btnDistinguishMemLocs.setSelection(true);
		}
		
		verboseBttn = new Button(optionsGroup, SWT.CHECK);
		verboseBttn.setBounds(21, 291,376, 16);
		verboseBttn.setText("Show Running Status in Console");		
		if(_options.verbose){
			verboseBttn.setSelection(true);
		}

		useNavigatorBttn = new Button(optionsGroup, SWT.CHECK);
		useNavigatorBttn.setText("Use Dependence Navigator (Triggered by CTRL+X)");
		useNavigatorBttn.setBounds(21, 313, 386, 16);		
		if(_options.useDepNavigator){
			useNavigatorBttn.setSelection(true);
		}	
		
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
		return new Point(457, 422);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configuration Options for "+ID.DISPLAY_NAME+" Slicer");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			_options.dotpath = text.getText();
			String pointsTo = comboPointsTo.getItem(comboPointsTo.getSelectionIndex());
			pointsTo = pointsTo.toLowerCase();
			if(pointsTo.contains("spark")){
				_options.pointToAnalysis = PointsToAnalysisType.SPARK;
			}
			else if(pointsTo.contains("type-based")){
				_options.pointToAnalysis = PointsToAnalysisType.TYPE_BASED;
			}
			else{
				_options.pointToAnalysis = PointsToAnalysisType.NAIVE;
			}
			
			String sideEffect = comboSideEffect.getItem(comboSideEffect.getSelectionIndex());
			sideEffect = sideEffect.toLowerCase();
			if(sideEffect.contains("field-sensitive")){
				_options.heapAbstraction = HeapAbstraction.FIELD_SENSITIVE;
			}
			else if(sideEffect.contains("field-based")){
				_options.heapAbstraction = HeapAbstraction.FIELD_BASED;
			}
			else if(sideEffect.contains("type-based")){
				_options.heapAbstraction = HeapAbstraction.TYPE_BASED;
			}
			else{
				_options.heapAbstraction = HeapAbstraction.NO_DISTINGUISH;
			}
			
			String summaryEdge = comboSummaryEdge.getItem(comboSummaryEdge.getSelectionIndex());
			summaryEdge = summaryEdge.toLowerCase();
			if(summaryEdge.contains("field-sensitive")){
				_options.sdgFormalActualOption = HeapAbstraction.FIELD_SENSITIVE;
			}
			else if(summaryEdge.contains("field-based")){
				_options.sdgFormalActualOption = HeapAbstraction.FIELD_BASED;
			}
			else if(summaryEdge.contains("type-based")){
				_options.sdgFormalActualOption = HeapAbstraction.TYPE_BASED;
			}
			else{
				_options.sdgFormalActualOption = HeapAbstraction.NO_DISTINGUISH;
			}
			
			String text = libDepth.getText();
			if(text.length()>0){
				_options.libTracingDepth = Integer.parseInt(libDepth.getText());	
			}
			else{
				_options.libTracingDepth = -1;
			}
				 
			_options.showJimpleSDG = showSDGBttn.getSelection();
			_options.verbose = verboseBttn.getSelection();
			_options.simplifyCallGraph = simplifyCGBttn.getSelection();
			_options.ignoreJreClinits = ignoreJreClinitBttn.getSelection();
			_options.showSliceInSDG = showSliceBttn.getSelection();
			_options.useDepNavigator = useNavigatorBttn.getSelection();			 
		}
		
		super.buttonPressed(buttonId);
	}
	
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		ConfigDlg dlg = new ConfigDlg(shell,null);

		shell.pack();
		shell.open();

		dlg.open();

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}

		display.dispose();
	}
}
