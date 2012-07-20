package jqian.slicer.view.local;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IDocument; 
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;

public class AnnotationManager {
	public AnnotationManager(ITextEditor editor) {
		this._editor = editor;
		this._file = ((IFileEditorInput)editor.getEditorInput()).getFile();
	}	
	
	/** Add annotation by line. */
	public void addAnnotationToLine(int line){
	    IDocument doc = _editor.getDocumentProvider().getDocument(_editor.getEditorInput());
	    try{
	        IRegion region = doc.getLineInformation(line - 1);
			String text = doc.get(region.getOffset(), region.getLength());
			String trimmedString = text.trim(); 
			int index = text.indexOf(trimmedString);
			if (!trimmedString.equals("}") && !trimmedString.equals("{")) { 
				if (region.getLength() > 0) {
					Position pos = new Position(region.getOffset()+index,region.getLength()-index);
					Annotation annotation = new Annotation(HIGHLIGHT_STR, false,
							                     (new StringBuilder("Line :")).append(line).toString());
					IAnnotationModel model = _editor.getDocumentProvider().getAnnotationModel(_editor.getEditorInput());
					model.addAnnotation(annotation, pos);
				}
			}
		} catch (Exception e) {

		}
	}	
	
	public void addAnnotationToClasses(){
		ICompilationUnit unit = JavaCore.createCompilationUnitFrom(_file);
		IAnnotationModel model = _editor.getDocumentProvider().getAnnotationModel(_editor.getEditorInput());
		if(unit != null){
			try {
				IType types[] = unit.getAllTypes();
				for (int i = 0; types != null && i < types.length; i++){					
					ISourceRange crange = types[i].getNameRange();
					Position p = new Position(crange.getOffset(),crange.getLength());
					Annotation annot = new Annotation(HIGHLIGHT_STR, false, null);
					model.addAnnotation(annot, p);
				}
			}catch(Exception e){
				
			}
		}
	}

	/** Add annotation by program struture. */
	public void addAnnotationToMethods() {
		ICompilationUnit unit = JavaCore.createCompilationUnitFrom(_file);
		IAnnotationModel model = _editor.getDocumentProvider().getAnnotationModel(_editor.getEditorInput());
		if (unit != null){
			try {
				IType types[] = unit.getAllTypes();
				for (int i = 0; types != null && i < types.length; i++) {
					org.eclipse.jdt.core.IMethod methods[] = types[i].getMethods();
					for (int j = 0; j < methods.length; j++) {
					    org.eclipse.jdt.core.IMethod method = methods[j];
						ISourceRange range = method.getNameRange();
						Position pos = new Position(range.getOffset(),range.getLength());
						Annotation annot = new Annotation(HIGHLIGHT_STR,false, null);
						model.addAnnotation(annot, pos);							
					}					
				}
			} catch(Exception e){}
	    }
	}

	public void removeAnnotations() {
		if(_editor == null) return;
		IAnnotationModel model = _editor.getDocumentProvider().getAnnotationModel(_editor.getEditorInput());
		for (Iterator<?> it = model.getAnnotationIterator(); it.hasNext();) {
			Annotation annotation = (Annotation)it.next();
			if (annotation.getType().equals(HIGHLIGHT_STR)){
				model.removeAnnotation(annotation);
			}
		}
	}
	
	ITextEditor _editor;
	IFile _file;
	private final String HIGHLIGHT_STR = "highlighter.highlightAnnotation";	
}