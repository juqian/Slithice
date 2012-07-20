package jqian.slicer.plugin.actions;

import java.util.*;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;
import jqian.slicer.plugin.*;
import jqian.slicer.view.local.*;
import jqian.slicer.core.*;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jdt.core.*;

import jqian.util.eclipse.JDTUtils;


public class HotkeyAction extends Action {
	public HotkeyAction() {
         setText("");
         // The id is used to refer to the action in a menu or toolbar
         setId("jqian.slicer.xxx");
         // Associate the action with a pre-defined command, to allow key bindings.
         setActionDefinitionId(ID.HOTKEY_CMD_ID);
    }
	
	 public void run(){		 
		 IWorkbenchPage page = WorkbenchHelper.openViewPage(ID.JDT_VIEW_ID);
		 ITextEditor editor = (ITextEditor)page.getActiveEditor();
		 Shell shell = editor.getSite().getShell();		
		 
		//get current editor line
		 ITextSelection selection = (ITextSelection)editor.getSelectionProvider().getSelection();
		 int line = selection.getStartLine()+1; 
		 
		 //get current edited method
		 String method = null;	
		 int methodStartLine = -1;
		 try{
			 IMethod m = JDTUtils.getEnclosingMethod(editor,line-1);		
			 method = JDTUtils.getMethodSootSignature(m);			 
			 methodStartLine = JDTUtils.getMethodStartLine(editor,m);
		 }
		 catch(Exception e){			 
		 }		
		 
		 if(method!=null){
			 Collection<Integer> depended = SlithiceSlicer.v().getDependedLines(method, methodStartLine,line);		 
			 
			 
			 //show dependence navigator
			 PopupDialog dlg = new DependenceNavigator(shell,editor,depended);
			 dlg.open();	
		 }		 	 
	 }
	 
	 public void runWithEvent(Event event) {		 
		 run();
	 }
	 
}
