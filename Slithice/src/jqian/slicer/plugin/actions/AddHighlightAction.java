package jqian.slicer.plugin.actions;

import jqian.slicer.view.global.ISliceDistribution;
import jqian.slicer.view.local.HighLighter;
import jqian.slicer.core.*;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;


public class AddHighlightAction implements IEditorActionDelegate {
	
	
	public AddHighlightAction() {		
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		_editor = (ITextEditor) targetEditor;		
	}	

	public void run(IAction action) {	
		if(_editor!=null){
			ISliceDistribution distribution = SlithiceSlicer.v().getCurrentSliceDistribution();
			if(distribution!=null){
				IEditorInput input = _editor.getEditorInput();
				
				IFile ifile = null;
				if(input instanceof IFile){
					ifile = (IFile)input;
				}
				else if(input instanceof IFileEditorInput){
					ifile = ((IFileEditorInput)input).getFile();
				}
				
				HighLighter.resetHighlights(_editor,distribution.getLines(ifile));	
			}		
		}
	}

	public void selectionChanged(IAction iaction, ISelection selection) {
		
	}
	
	private ITextEditor _editor;
}