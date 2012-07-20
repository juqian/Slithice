package jqian.slicer.view.global;

import java.util.*;
import java.io.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Composite;

import com.swtdesigner.SWTResourceManager;

/*
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.dialogs.MessageDialog; 
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
*/


public class SliceViewer extends TreeViewer {
	protected static final String[] columnNames = { 
		        "entity", "total lines",	"slice lines", "percentage" };
	protected static int _coloredIndex = 3;
	protected static int _coloredColumnWidth = 400;
	protected static Map<String, Integer> _prop2index = new HashMap<String, Integer>();	
	static {
		for (int i = 0; i < columnNames.length; i++)
			_prop2index.put(columnNames[i], new Integer(i));
	}

	private static class Wrapper{
		public Wrapper(File file){
			this._content = file;
		}
		public File _content;
	}
	
	protected Shell _shell;	
	protected Wrapper _root;
	protected Tree _tree;
	protected SliceDistributionBar _distributionBar;
	
	
	public SliceViewer(Composite parent) {
		super(parent, SWT.FULL_SELECTION | SWT.LINE_DASH | SWT.BORDER);

		this._shell = parent.getShell();
		_tree = getTree();
		_tree.setHeaderVisible(true);
		_tree.setLinesVisible(true);
		
		_root = new Wrapper(null);
			
		addColumns();
		setContentProvider(new MyTreeContenetProvider());
		setLabelProvider(new MyTableLableProvider());
		setColumnProperties(columnNames);	
		
		//show slice distribution bar		
		_distributionBar = new SliceDistributionBar(_tree,_coloredIndex);
		_distributionBar.enable();
		
		setInput(_root);
		
		addDbClickListener();
	}
	
	public void setProject(File projectRoot,ISliceDistribution distribution){
		_root._content = projectRoot;
		_distributionBar.setProject(projectRoot,distribution);
		
		//should be changed to selective expand, item containing no slicing results not expanded
		//expandAll();
		selectiveExpand();
		
		refresh();
		getTree().redraw();
	}

	public void run() {		
		Display display = _shell.getDisplay();
		while (!_shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	private void addColumns() {
		TreeColumn column = new TreeColumn(_tree, SWT.CENTER);
		column.setText(columnNames[0]);		
		column.setWidth(350);
		column = new TreeColumn(_tree, SWT.CENTER);
		column.setText(columnNames[1]);
		column.setWidth(80);
		column = new TreeColumn(_tree, SWT.CENTER);
		column.setText(columnNames[2]);
		column.setWidth(80);
		column = new TreeColumn(_tree, SWT.CENTER);
		column.setText(columnNames[3]);
		column.setWidth(_coloredColumnWidth);
	}	
	
	

	/** Listen to Double-Clicks */
	private void addDbClickListener() {	
		_tree.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event e) {
				/*TreeItem[] selection = _tree.getSelection();
				if (selection.length == 0)
					return;

				TreeItem item = selection[0];
				MessageDialog.openInformation(_shell, "Dbclick on item", item.toString());	*/			
			}
		});
	}

	private void selectiveExpand(){
		expandToLevel(_root, 1);
		
		Object[] childs = getRawChildren(_root);
		for(int i=0;i<childs.length;i++){
			 selectiveExpand(childs[i],1);
		}
	}
	
	private void selectiveExpand(Object root,int level){
		if (root instanceof File) {
			File file = (File)root;
			if(!file.isDirectory()){
				return;
			}	
			
			if(hasSlice(file)){
				expandToLevel(file, level);
			}
			
			//level++;
			Object[] childs = getRawChildren(file);
			for(int i=0;i<childs.length;i++){
				 selectiveExpand(childs[i],level);
			}
		}
	}
	
	private boolean hasSlice(File file){
		boolean flag = false;
		File[] contents = file.listFiles();
		int size = contents.length;		 
		for(int i=0;!flag && i<size;i++){
			File item = contents[i];
			if(item.isDirectory()){
				flag = flag || hasSlice(item);
			}
			else if(isJavaFile(item.getName())){									
				flag = flag || (_distributionBar.getSliceLineCount(item)>0);			
			}
		}
		
		return flag;
	}
	
	private boolean isJavaFile(String name){
		int length = name.length();			
		String ext = "";
		
		name = name.toLowerCase();			
		if(length>5){
			ext = name.substring(length-5);	
		}
		
		if(ext.equals(".java"))
			return true;
		else
			return false;
	}
	

	class MyTreeContenetProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {			
			if (parentElement instanceof File) {
				File file = (File)parentElement;
				
				if(!file.isDirectory()){
					return new Object[0];
				}				
				
				//a directory
				File[] contents = file.listFiles();
				int size = contents.length;
				List<File> javaContents = new LinkedList<File>();
				for(int i=0;i<size;i++){
					File item = contents[i];
					if(item.isDirectory() && containsJavaFile(item)){
						javaContents.add(item);
					}
					else if(isJavaFile(item.getName())){									
						javaContents.add(item);						
					}
				}
				
				return javaContents.toArray();					
			} else if (parentElement instanceof Wrapper && _root._content!=null) {
				Object[] childs = { _root._content };
				return childs;				
			} else
				return new Object[0];
		}
		
		
		private boolean containsJavaFile(File file){
			if(!file.isDirectory()){
				return isJavaFile(file.getName());
			}
			
			//a directory
			File[] contents = file.listFiles();			
			int size = contents.length;
			boolean containJava = false;
			for(int i=0;i<size;i++){
				File item = contents[i];
				if(item.isDirectory()){
					containJava |= containsJavaFile(item);
				}
				else{
					containJava |= isJavaFile(item.getName());
				}
				
				if(containJava)
					break;			
			}
			
			return containJava;
		}

		public Object getParent(Object element) {
			if (element instanceof File) {
				File file = (File)element;
				return file.getParentFile();
			} else {
				return null;
			}
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length>0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}		

	class MyTableLableProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if(columnIndex>0){
				return null;
			}
			
			if(element instanceof Wrapper){
				return SWTResourceManager.getImage(SliceViewer.class, "/img/javaproject.PNG");				
			}
			else if (element instanceof File){
				File file = (File)element;
				if(file.isDirectory()){
					return SWTResourceManager.getImage(SliceViewer.class, "/img/directory.PNG");	
				}
				else{
					return SWTResourceManager.getImage(SliceViewer.class, "/img/javafile.PNG");	
				}			
			}/*else if(element instanceof ){
				
			}*/
			else{
				return null;
			}
		}

		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof File){
				switch(columnIndex){
				case 0:{
					File file = (File)element;
					return file.getName();					
				}
				case 1:{
					int count = _distributionBar.getFileLineCount(element);
					if(count==0)
						return "";
					else
					    return ""+count;
				}
				case 2:{
					int count = _distributionBar.getSliceLineCount(element);
					if(count==0)
						return "";
					else
					    return ""+count;
				}
				default: return "";
				}
			}			
			else
				return element.toString();
		}

		public void addListener(ILabelProviderListener listener) {}

		public void dispose() {}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {}
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Configurator");
		shell.setLayout(new FillLayout());
		
		SliceViewer viewer = new SliceViewer(shell);
		viewer.setProject(new File("."),null);

		shell.open();
		viewer.run();
		 
		display.dispose();
	}
}

