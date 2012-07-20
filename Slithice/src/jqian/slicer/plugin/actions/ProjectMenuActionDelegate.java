package jqian.slicer.plugin.actions;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;

import jqian.slicer.plugin.*;
import jqian.slicer.plugin.view.PluginSliceViewer;
import jqian.slicer.view.ConfigDlg;
import jqian.slicer.view.ProjectEntrySelector;
import jqian.slicer.view.global.ISliceDistribution;
import jqian.slicer.core.*;
import jqian.util.eclipse.ConsoleUtil;


public class ProjectMenuActionDelegate implements IObjectActionDelegate {
    private IProject _project;
    
	public ProjectMenuActionDelegate() {		
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
    
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		String id = action.getId();
		
		if(id.equals("jqian.slicer.setProjectEntryAction")){
			setProjectEntry();
		}
		else if(id.equals("jqian.slicer.configSlicer")){
			configSlicer();
		}
		else if(id.equals("jqian.slicer.showGbViewAction")){
			showGlobalSliceView();
		}
		else if(id.equals("jqian.slicer.resetAction")){
			resetAll();
		}
	}
	
	
	private void setProjectEntry(){
		if(!SlithiceSlicer.v().isConfigurated()){
			Shell shell = WorkbenchHelper.getActiveJDTEditorShell();
			MessageDialog.openInformation(shell, "The slicer has not been configurated", 
                    "Please rigth click on the project item, and configurate from menu "
					+"\"program slicing\" -> \"Configurate Slicer\" first.");
            return;        
		}
		
		IJavaProject javaPrj = JavaCore.create(_project);
		Shell shell = WorkbenchHelper.getActiveShell();
		ProjectEntrySelector selector = new ProjectEntrySelector(shell,javaPrj);
		if(selector.open()==IDialogConstants.OK_ID){
			SlithiceSlicer.v().setProject(javaPrj,selector.getEntry());
			ConsoleUtil.showConsole(ID.CONSOLE);
		}		
	}
	
	
	private void configSlicer(){
		Shell shell = WorkbenchHelper.getActiveShell();
		SlicerOptions options = SlithiceSlicer.v().getConfiguration();
		ConfigDlg configer = new ConfigDlg(shell,options);
		if(configer.open()==IDialogConstants.OK_ID){
			options = configer.getConfiguration();
			SlithiceSlicer.v().reconfig(options);
		}		
	}
	
	private void showGlobalSliceView() {		
		PluginSliceViewer view = (PluginSliceViewer)WorkbenchHelper.openView(ID.GLOBAL_VIEW_ID);
		ISliceDistribution distribution = SlithiceSlicer.v().getCurrentSliceDistribution();
		
		if(distribution==null){
			Shell shell = WorkbenchHelper.getActiveJDTEditorShell();
			MessageDialog.openInformation(shell,"Show Slicing Result Distribution", 
                    "No slicing result found.\n"
                    +"Choose a new slicing criterion and perform slicing first. \n");	
		}
		else{
			view.setProject(_project,distribution);      
		}		
	}
	
	
	private void resetAll() {
		Shell shell = WorkbenchHelper.getActiveJDTEditorShell();
		boolean ok = MessageDialog.openConfirm(shell,"Reset Slicer", 
                "This operation will clear the dependence graph and the current slicing data. "
                +"Recomputing them can be time-consumming. \n"
                +"Are you sure to continue?");
               
		if(ok){
			SlithiceSlicer.v().reset();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		_project = (IProject)SelectionResolver.getSelectedResource(selection,IResource.PROJECT);        
	}
}
