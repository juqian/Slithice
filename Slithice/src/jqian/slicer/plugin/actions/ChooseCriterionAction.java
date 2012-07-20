package jqian.slicer.plugin.actions;

import java.util.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.*; 
import java.lang.reflect.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jdt.core.*;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import jqian.slicer.core.*;
import jqian.slicer.plugin.*;
import jqian.slicer.view.*;

import jqian.util.eclipse.JDTUtils;


public class ChooseCriterionAction implements IEditorActionDelegate{

	private ITextEditor _editor;
	
	public ChooseCriterionAction() {}

	public void setActiveEditor(IAction iaction, IEditorPart ieditorpart) {	
		_editor = (ITextEditor) ieditorpart;	
	}

	public void run(IAction action) {
		Shell shell = WorkbenchHelper.getActiveJDTEditorShell();
		IProject prj = WorkbenchHelper.getCurrentProject();
		IJavaProject javaPrj = JavaCore.create(prj);
		SlithiceSlicer slicer = SlithiceSlicer.v();
		
		//check whether in the project with entry specified
		FileEditorInput input = (FileEditorInput)_editor.getEditorInput();
		IFile ifile = input.getFile();
		while(!slicer.inCurrentProject(ifile)){
			ProjectEntrySelector selector = new ProjectEntrySelector(shell,javaPrj);
			if(selector.open()==IDialogConstants.OK_ID){
				slicer.setProject(javaPrj,selector.getEntry());
			}	
		}
		
		//choose slicing criteria
		String entry = slicer.getEntryClass();
		if(entry==null || SlithiceSlicer.v().getSDG()==null){
			MessageDialog.openInformation(shell,"Choosing slicing criterion", 
					                      "Project entry not specified or SDG not constructed yet.\n\n"
					                      +"If you have specifed the project entry, please wait for dependence graph construction. \n\n"
					                      +"If you haven't specified an entry for the project, please right click on the project item"
					                      +"and use menu \"Program Slicing\" -> \"Set Project Entry\" to set an entry.");	
		}
		
		ITextSelection tsel = null;
		ISelection sel = _editor.getSelectionProvider().getSelection();
		if(sel==null || !(sel instanceof ITextSelection)){
			tsel = forceSelection();
		}
		else{
			tsel = (ITextSelection)sel;
			if(tsel.getLength()==0){
				tsel = forceSelection();
			}
		}
		
		if(tsel==null)
			return;

		
		CriteriaSelector dlg = new CriteriaSelector(shell,javaPrj,_editor,tsel,entry,false);		
		if(dlg.open()==IDialogConstants.OK_ID){
			String method = dlg.getSlicingStartMethod();
			int line = dlg.getLine();
			Collection<String> vars = dlg.getSlicedVars();
			boolean postExecution = dlg.slicingOnAssignedVar();
			boolean sliceInGlobal = dlg.isSlicingInGlobal();
			slicer.doSlicing(shell,method, line, vars,postExecution,sliceInGlobal);
		}
	}

	public void selectionChanged(IAction iaction, ISelection selection) {
	}
	
	public ITextSelection forceSelection(){
		Control control = (Control)_editor.getAdapter(Control.class);
		if (!(control instanceof StyledText)){
			return null;
		}
		
		final StyledText text = (StyledText) control;
		int caretOffset = text.getCaretOffset();

		try {
			//get source view, and translate caret offset to document offset
			Class<?> edClass = _editor.getClass();
			ISourceViewer viewer = null;
			while (edClass != null && viewer == null) {
				try {
					Method m = edClass.getDeclaredMethod("getSourceViewer");
					m.setAccessible(true);
					viewer = (ISourceViewer) m.invoke(_editor);
				} catch (Exception e) {
				}

				edClass = edClass.getSuperclass();
			}

			ITextViewerExtension5 ext = (ITextViewerExtension5) viewer;
			int docOffset = ext.widgetOffset2ModelOffset(caretOffset);

			IDocument doc = _editor.getDocumentProvider().getDocument(_editor.getEditorInput());
			
			//prefer the left identifier
			char cur = doc.getChar(docOffset);
			if(!Character.isJavaIdentifierPart(cur) && !Character.isJavaIdentifierStart(cur)){
				docOffset--;
			}
			
			while(docOffset>0){
				char ch = doc.getChar(docOffset);
				if(Character.isJavaIdentifierPart(ch) || Character.isJavaIdentifierStart(ch)){
					docOffset--;
				}
				else{
					docOffset++;
					break;
				}
			}
			
			int length = doc.getLength();
			int end = docOffset;
			 
			while(end<length){
				char ch = doc.getChar(end);
				if(Character.isJavaIdentifierPart(ch) || Character.isJavaIdentifierStart(ch)){
					end++;
				}
				else{					 
					break;
				}
			}
			
			length = end - docOffset;
			
			//for instance fields also select the reference variable
			IJavaElement[] elemts = JDTUtils.codeResolve(_editor, docOffset, length);
			if(elemts.length==1 && elemts[0] instanceof IField){
				IField field = (IField)elemts[0];				
				if(!Flags.isStatic(field.getFlags())){
					char ch = doc.getChar(docOffset-1);
					if(ch=='.'){
						docOffset -= 2;
						
						while(docOffset>0){
							ch = doc.getChar(docOffset); 
							if(Character.isJavaIdentifierPart(ch) || Character.isJavaIdentifierStart(ch)){
								docOffset--;
							}
							else{
								docOffset++;
								break;
							}
						}
						
						length = end - docOffset;
					}
				}
			}
			
			return new TextSelection(doc, docOffset, length);
			//ICompilationUnit icompilationUnit = JDTUtils.getCompliationUnit(_editor);			 
			//IJavaElement[] elements = JDTUtils.codeResolve(icompilationUnit,docOffset,length);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}