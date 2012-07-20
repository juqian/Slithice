package jqian.slicer.plugin.view;

import java.io.File;
import jqian.slicer.plugin.*;
import jqian.slicer.view.global.*;
import jqian.slicer.view.local.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.core.resources.*;


/**
 *
 */
public class PluginSliceViewer extends ViewPart {
	private SliceViewer viewer;	
	private ISliceDistribution _distribution;
	private IProject _project;
	private Action action1;
	private Action doubleClickAction;

	/**
	 * The constructor.
	 */
	public PluginSliceViewer() {
		
	}
	
	public void setProject(IProject project,ISliceDistribution distribution){		
		if(!project.equals(this._project) || !_distribution.equals(distribution)){
			this._project = project;
			this._distribution = distribution;
			String path = project.getLocation().toString();	
			File file = new File(path);			
			viewer.setProject(file,distribution);
		}
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {		
		viewer = new SliceViewer(parent);
		
		makeActions();
		hookContextMenu();		
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				PluginSliceViewer.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		//getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(new Separator());
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof File){
					File file = (File)obj;
					if(!file.isDirectory()){
						onOpenFileView(file);						
					}					
				}
			}
		};
		action1.setText("Show Slice In Editor");
		action1.setToolTipText("Show slice in a Java editor by highlighting covered lines");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof File){
					File file = (File)obj;
					if(!file.isDirectory()){
						onOpenFileView(file);						
					}					
				}
			}
		};
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	
	private void onOpenFileView(File file) {
		try {
			IFile ifile = WorkbenchHelper.getIFile(_project,file);
			IWorkbenchPage page = WorkbenchHelper.openViewPage(ID.JDT_VIEW_ID);
			IEditorInput input = new FileEditorInput(ifile);
			IEditorPart view = page.openEditor(input, ID.JDT_VIEW_ID, true);
			ITextEditor editor = (ITextEditor)view;				
			
			HighLighter.resetHighlights(editor,_distribution.getLines(file));			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void showSliceInCurrentEditor(ISliceDistribution distribution){
		try {
			IFile ifile = WorkbenchHelper.getCurrentFile();
			IWorkbenchPage page = WorkbenchHelper.openViewPage(ID.JDT_VIEW_ID);
			IEditorInput input = new FileEditorInput(ifile);
			IEditorPart view = page.openEditor(input, ID.JDT_VIEW_ID, true);
			ITextEditor editor = (ITextEditor)view;	
			 
			HighLighter.resetHighlights(editor,distribution.getLines(ifile));	
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
