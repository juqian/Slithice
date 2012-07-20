package jqian.slicer.view.local;

import java.util.Collection;
import java.util.Arrays;
import java.util.Iterator;

import jqian.util.eclipse.JDTUtils;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.*;
//import org.eclipse.swt.custom.*;
//import org.eclipse.swt.events.*;
//import org.eclipse.jface.dialogs.MessageDialog;

public class DependenceNavigator extends PopupDialog{
	private int[] _lines; 
	private ITextEditor _editor;
	
	public DependenceNavigator(Shell shell,ITextEditor editor,Collection<Integer> lines){
		super(shell, SWT.CENTER, 
				    true, //got focus when open()
				    false, false, false, 
				    null,"dependence assist");
		
		this._editor = editor;
		
		int size = lines.size();
		_lines = new int[size];
		
		int i=0;
		for(Iterator<Integer> it=lines.iterator();it.hasNext();i++){
			_lines[i] = it.next();			
		}
		
		Arrays.sort(_lines);
	}
	
	
	
	protected  Control createDialogArea(Composite parent) {	
		Composite composite = (Composite)super.createDialogArea(parent);
		composite.setLayout(new FillLayout());
	    List list = new List(composite, SWT.BORDER| SWT.V_SCROLL);//SWT.NO_REDRAW_RESIZE | SWT.MULTI
	    fillList(list);
		list.addListener(SWT.MouseDoubleClick,new DbclickListener(list));
	    new ListViewer(list);
	    
		/*composite.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				close();
			}
		});*/
		
	    return composite;
	}
  
	private class DbclickListener implements Listener{
		List _list;
		
		public DbclickListener(List list){
			this._list = list;
		}
		
		public void handleEvent(Event event){
			int i = _list.getSelectionIndex();
			int line = _lines[i]-1;
			
			try{
				IDocumentProvider provider = _editor.getDocumentProvider();
				IDocument doc = provider.getDocument(_editor.getEditorInput());
				IRegion region = doc.getLineInformation(line);

				TextSelection selection = new TextSelection(region.getOffset(),region.getLength());
				_editor.getSelectionProvider().setSelection(selection);
			}catch(BadLocationException e){				
			}
			
			DependenceNavigator.this.close();
		}
	}
	
	protected Point getInitialLocation(Point initialSize) {
		Point p = Display.getCurrent().getCursorLocation();		
		return  p;
	}
	
	/*protected void configureShell(Shell newShell) {
	      super.configureShell(newShell);
	      newShell.setText(MessageUtil.getString("Readme Sections"));
	      ...
	   }*/
	
	private void fillList(List list){
		int size = _lines.length;
		for(int i=0;i<size;i++){
			int line = _lines[i];
			String text = JDTUtils.getLineText(_editor, line-1,true,true);
			list.add(text);
		}		
		//list.add("apple");		
	}
}