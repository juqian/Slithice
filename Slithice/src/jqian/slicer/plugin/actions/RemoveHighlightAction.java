package jqian.slicer.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;
import jqian.slicer.view.local.HighLighter;

public class RemoveHighlightAction implements IEditorActionDelegate{
	public RemoveHighlightAction() {}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		_editor = (ITextEditor) targetEditor;		
	}	

	public void run(IAction action) {
		HighLighter.removeHighlights(_editor);
	}

	public void selectionChanged(IAction iaction, ISelection iselection) {}
	
	private ITextEditor _editor;
}