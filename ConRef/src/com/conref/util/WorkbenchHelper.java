/* 
 * @author Ju Qian{jqian@live.com}
 * @date 2006-11-17
 * @version 0.01
 */
package com.conref.util;

import java.io.File;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.core.resources.*; 
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.ide.IDE;
public class WorkbenchHelper {
	private static final String JDT_VIEW_ID = "org.eclipse.jdt.ui.CompilationUnitEditor";

	/**
	 * Find a workbench page by the given id
	 * @return null if nothing found
	 */
	public static IWorkbenchPage findViewPage(String viewId){
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();        
             
        for(int i=0; i<windows.length; i++){            
            IWorkbenchPage[] pages = windows[i].getPages();  
            for(int k=0;k<pages.length;k++){
                IViewPart view = pages[k].findView(viewId);
                if(view!=null){
                    return pages[i];
                }
            }
        }
        return null;
    }
	
	/** Open a workbench page, if not exist, just create it. */
    public static IWorkbenchPage openViewPage(String id){
        IWorkbenchPage page = findViewPage(id);
        //If not aready docked in a WorkbenchPage       
        if(page==null){
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            page = window.getActivePage();
            try{
                page.showView(id);
            }catch(PartInitException e){
                //e.printStackTrace();
            }
        }
        
        return page;
    }
    
    public static IViewPart openView(String id){
    	IWorkbenchPage page = openViewPage(id);
    	IViewPart view = page.findView(id);        
        page.activate(view);  
        return view;    	
    }
    
    public static Shell getActiveShell(){
    	 IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
         return window.getShell();         
    }
    
    public static Shell getActiveJDTEditorShell(){
    	IWorkbenchPage page = WorkbenchHelper.openViewPage(JDT_VIEW_ID);
		ITextEditor editor = (ITextEditor)page.getActiveEditor();
		return editor.getSite().getShell();		
    }
    
    public static IProject getCurrentProject(){
    	IFile file = getCurrentFile();
    	return file.getProject();
    }    
	public static void selectInEditor(ITextEditor editor, int offset, int length) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart active = page.getActiveEditor();
		if (active != editor) {
			editor.getSite().getPage().activate(editor);
		}
		editor.selectAndReveal(offset, length);
	}
    public static IFile getCurrentFile(){
    	try{
    		IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			IEditorPart editor = page.getActiveEditor();
			FileEditorInput edInput = (FileEditorInput)(editor.getEditorInput());
			return edInput.getFile();
    	}
    	catch(Exception e){
    		return null;
    	}		
    }  
    private static IProject getActiveProject() {
        IEditorPart editor = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor.getEditorInput() instanceof FileEditorInput) {
            FileEditorInput fei = (FileEditorInput) editor
                    .getEditorInput();
            IFile f = fei.getFile();
            return f.getProject();
        }
        return null;
    }
	public static String getJDKPath() {
		String result =System.getProperty("java.home");
		  result=result.substring(0, result.length()-4);
		  File jdkDir=new File(result);
		  File[] files=jdkDir.listFiles();
		  for(int i=0,size=files.length;i<size;i++){
			  String name=files[i].getName();
			   if(name.startsWith("jdk")){
				   result=result.concat(name);
			   }
		  }
		return result;
	}
	public static IFile getselectedFile(String fileName){
    String path=getCurrentFile().getLocation().toString();
    IProject prj = getActiveProject();
  //  fileName=fileName.replace(".", "/");
    String Prjpath=prj.getLocation().toString();
	//int index = path.indexOf("src", 0);
	int prjLocation=path.indexOf(Prjpath);
	int srcLocation=path.indexOf("/", prjLocation+Prjpath.length()+1);
	String srcpath = path.substring(0, srcLocation);
           
           String filePath=srcpath+"/"+fileName;
            filePath= filePath.replace(".", "/");
            filePath=filePath+".java";
            File file=new File(filePath);
            IFile ifile=getIFile(prj,file);
          
           return ifile;

        }

	public static void showEditor(IFile ifile) {
		//FileEditorInput fei=new FileEditorInput(ifile);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart dirtyEditor = null;
		try {
			dirtyEditor = IDE.openEditor(page, ifile);
		} catch (PartInitException e) {
			// 
			e.printStackTrace();
		}
		page.bringToTop(dirtyEditor);
	}
    
    public static IFile getIFile(IProject project,File file){     
    	try {
			String prjPath = project.getLocation().toString();
			//String path = file.getCanonicalPath();
			String path = file.getAbsolutePath();
			String relevantPath = path.substring(prjPath.length());

			IFile ifile = project.getFile(relevantPath);
			ifile.refreshLocal(1, null);
			return ifile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
    }
    
    public static File getFile(IFile ifile){
    	IPath path = ifile.getLocation();
    	//IPath path = ifile.getFullPath();
    	return path.toFile();    	
    }
    
}
