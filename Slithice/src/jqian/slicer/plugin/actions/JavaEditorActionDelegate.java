package jqian.slicer.plugin.actions;

import jqian.slicer.plugin.WorkbenchHelper;
import jqian.slicer.core.*;
import jqian.util.eclipse.JDTUtils;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.ui.*;


public class JavaEditorActionDelegate implements IEditorActionDelegate {
	
	
	public JavaEditorActionDelegate() {		
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		_editor = (ITextEditor) targetEditor;		
	}	

	public void run(IAction action) {	
		if(_editor==null){
			return;
		}
		
		String id = action.getId();
		if(id.equals("jqian.slicer.actions.ShowJavaPDG")){
			showJavaPDG();
		}
		else if(id.equals("jqian.slicer.actions.ShowJimplePDG")){
			showJimplePDG();
		}
		else if(id.equals("jqian.slicer.actions.ShowSDG")){
			SlithiceSlicer.v().showSDG();
		}
		else if(id.equals("jqian.slicer.actions.DotSDG")){
			SlithiceSlicer.v().dotSDG();	
		}
		else if(id.equals("jqian.slicer.actions.ShowCG")){
			showCallGraph();
		} 	
		else if(id.equals("jqian.slicer.actions.showCFG")){
			showJimpleCFG();
		}
	}
	
	private void showCallGraph(){
		IMethod m = getCurrentMethod();
		if(m==null)
			return;
		
		Shell shell = WorkbenchHelper.getActiveJDTEditorShell();
		
		IInputValidator validator = new IInputValidator(){
			public String isValid(String newText){
				int i = -1;
				try{
					i = Integer.parseInt(newText);
				}catch(Exception e){}
				
				if(i>0 && i<100){
					return null;
				}else if(i>0){
					return "To large display depth";
				}
				else{
					return "Invalide input";
				}
			}
		};
		
		
		InputDialog dlg = new InputDialog(shell,"Select Call Graph Depth", 
				"A full call graph is always large, please select a maximun depth to show",
                "4", validator);
		
		if(dlg.open()!=Dialog.OK){
			return;
		}
		
		String depthStr = dlg.getValue();
		int depth = -1;
		try{
			depth = Integer.parseInt(depthStr);
		}catch(Exception e){}		
		
		String method = JDTUtils.getMethodSootSignature(m);
		String imgfile = signatureToFileName(method);
		imgfile += "_cg.dot";
		imgfile = SlithiceSlicer.v().getCurPrjTemporalPath() + "/" +imgfile;
		SlithiceSlicer.v().dotCallGraph(method, depth, imgfile);
	}
	
	private void showJavaPDG(){
		IMethod m = getCurrentMethod();
		if(m==null)
			return;
		
		String method = JDTUtils.getMethodSootSignature(m);			 
		String imgfile = signatureToFileName(method);
		imgfile += ".dot";
		SlithiceSlicer.v().dotJavaPDG(_editor,method, imgfile);		
	}
	
	private void showJimplePDG(){
		IMethod m = getCurrentMethod();
		if(m==null)
			return;
		
		String method = JDTUtils.getMethodSootSignature(m);			 
		String imgfile = signatureToFileName(method);
		imgfile += ".dot";
		SlithiceSlicer.v().dotJimplePDG(_editor,method, imgfile);		
	}
 
	private void showJimpleCFG(){
		IMethod m = getCurrentMethod();
		if(m==null)
			return;
		
		String method = JDTUtils.getMethodSootSignature(m);
		SlithiceSlicer.v().showJimpleCFG(method);		
	}
	
	// get current edited method	
	private IMethod getCurrentMethod(){
		try {
			ITextSelection selection = (ITextSelection) _editor.getSelectionProvider().getSelection();
			IMethod m = JDTUtils.getEnclosingMethod(_editor, selection.getStartLine());
			return m;	 
		} catch (Exception e) {}
		
		return null;
	}
	
	private static String signatureToFileName(String signature){
		StringBuffer buf = new StringBuffer();
		for(int i=0;i<signature.length();i++){
			char ch = signature.charAt(i);
			
			if(ch=='<' || ch=='>' || ch==':'|| ch==']'){
				ch = 0;
			}
			else if(ch=='['){
				ch = '-';
			}
			else if(ch=='(' || ch==')' || ch==',' || ch==' ' || ch=='$'){
				ch = '_'; 					
			}
			
			if(ch!=0) buf.append(ch);
		}
		
		return buf.toString();
	}

	public void selectionChanged(IAction iaction, ISelection selection) {
		
	}
	
	private ITextEditor _editor;
}