package jqian.slicer.view.local;

import jqian.slicer.core.SlithiceSlicer;
import jqian.slicer.plugin.ID;
import jqian.slicer.plugin.actions.HotkeyAction;
import java.util.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author bruteforce
 *
 */
public class HighLighter {
    public static void addHighlights(ITextEditor editor,Collection<Integer> lines){
    	if (editor == null)	return;
    	
    	//add high light annotations
		IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
		if (file != null && hasJavaNature(file)){
			AnnotationManager mng =  new AnnotationManager(editor);
			
			for(Iterator<Integer> it=lines.iterator();it.hasNext();){
				mng.addAnnotationToLine(it.next());
			}		
		}
		
		//add hotkey
		Boolean state = _editor2bool.get(editor);
		if(state==null ||state.equals(Boolean.FALSE)){
			if(SlithiceSlicer.v().useDependenceNavigator()){
				registerHotkey(editor);
			}
		}
    }
    
    public static void resetHighlights(ITextEditor editor,Collection<Integer> lines){
    	removeHighlights(editor);
    	addHighlights(editor,lines);
    }
    
    public static void removeHighlights(ITextEditor editor){
    	AnnotationManager mng = new AnnotationManager(editor);
		mng.removeAnnotations();
    }
    
	public static void removeAnnotations(String viewId) {
		IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; windows != null && i < windows.length; i++) {
			IWorkbenchPage pages[] = windows[i].getPages();
			for (int j = 0; pages != null && j < pages.length; j++) {
				IEditorReference references[] = pages[j].getEditorReferences();
				for (int k = 0; references != null && k < references.length; k++) {
					IEditorReference ref = references[k];
					String id = ref.getId();
					if (id.equals(viewId)) {
						ITextEditor edPart = (ITextEditor) ref.getEditor(false);
						if (edPart != null) {
							AnnotationManager mng = new AnnotationManager(edPart);
							mng.removeAnnotations();
						}
					}
				}
			}
		}
	}
	
	private static boolean hasJavaNature(IFile file) {
		boolean hasNature = false;
		try {
			hasNature = file.getProject().hasNature("org.eclipse.jdt.core.javanature");
		} catch (CoreException ex) {
			hasNature = false;
		}
		return hasNature;
	}
	
	private static void registerHotkey(ITextEditor editor){
		if(_editor2action.get(editor)==null){
			HotkeyAction action = new HotkeyAction();
			IHandlerService handlerService = (IHandlerService)editor.getSite().getService(IHandlerService.class);
			ActionHandler handler = new ActionHandler(action);
			handlerService.activateHandler(ID.HOTKEY_CMD_ID,handler); 
			_editor2action.put(editor,handler);
		}
	}
	
	
	static void unregisterHotkey(ITextEditor editor){
		ActionHandler handler = _editor2action.get(editor);
		if(handler!=null){		 
			//IHandlerService handlerService = (IHandlerService)editor.getSite().getService(IHandlerService.class);		
			//handlerService.deactivateHandler(activation);
			//deactivateHandler(handler);
			//_editor2bool.put(editor,true);
		}
	}
	
	private static Map<ITextEditor,Boolean> _editor2bool = new HashMap<ITextEditor,Boolean>();
	private static Map<ITextEditor,ActionHandler> _editor2action = new HashMap<ITextEditor,ActionHandler>();
}
