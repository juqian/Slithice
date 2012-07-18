package com.conref.refactoring.splitlock.views;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.conref.refactoring.Activator;
import com.conref.refactoring.splitlock.actions.ShowTableViewAction;
import com.conref.refactoring.splitlock.core.JDTRewriter;
import com.conref.refactoring.splitlock.refactor.splitRefactoring;
import com.conref.refactoring.splitlock.refactoringWizard.splitRefactoringWizard;
import com.conref.util.WorkbenchHelper;


/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class MethodsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "test.views.SampleView";

	private NewTreeViewer viewer;
	public NewTreeViewer getViewer() {
		return viewer;
	}

	private Action SplitLock;
	private Action showCandidateSync;
	private Action doubleClickAction;

	private JDTRewriter jdtRewriter;

	private String id= "test.views.SampleView";
	

//	private String methodname;


	

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */


	/**
	 * The constructor.
	 */
	public MethodsView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer =NewTreeViewer.build(parent);
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MethodsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(SplitLock);
		manager.add(new Separator());
		manager.add(showCandidateSync);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(SplitLock);
		manager.add(showCandidateSync);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
//		manager.add(SplitLock);
//		manager.add(action2);
	}

	private void makeActions() {
		SplitLock = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
			TreeItem[] parent=viewer.getTree().getSelection();
			TreeItem cls=parent[0].getParentItem();
				   String methodname=obj.toString();
			     String clsName=cls.getText();
				 IFile file = WorkbenchHelper.getselectedFile(clsName);
				 jdtRewriter=new JDTRewriter(file);
				try {
					Change change=jdtRewriter.run(methodname,clsName);
					IWorkbench workbench = PlatformUI.getWorkbench();
			        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			        Shell shell = window.getShell();
					splitRefactoring refactor = new splitRefactoring(change);
					splitRefactoringWizard wizard = new splitRefactoringWizard(refactor);
					RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
							wizard);
					try {
						op.run(shell, "Split Refactoring");
//						IWorkbenchPage page = WorkbenchHelper.openViewPage(id);
//						MethodsView viewpart = (MethodsView) page.findView(id);
//						Map classMap = ShowTableViewAction.getClasses(file);
//						viewpart.getViewer().setInput(classMap);
						ShowTableViewAction.updateView();
						//	page.showView(id);
						jdtRewriter.addAnnotation(methodname);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} catch (JavaModelException e) {
					// 
					e.printStackTrace();
				} catch (MalformedTreeException e) {
					// 
					e.printStackTrace();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				//showMessage("split lock is done");
			}
		};

		SplitLock.setText("SplitLock");
		SplitLock.setToolTipText("split lock");
//		SplitLock.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
//				.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
		SplitLock.setImageDescriptor(Activator.getImageDescriptor("./icons/lock.png"));

		showCandidateSync = new Action() {
			public void run() {
				try {
					ShowTableViewAction.updateView();
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		};
		showCandidateSync.setText("list all candidate sync");
		showCandidateSync.setToolTipText("candidate sync");
		showCandidateSync.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				TreeItem[] parent=viewer.getTree().getSelection();
				TreeItem cls=parent[0].getParentItem();
				if(cls!=null){
					   String methodname=obj.toString();
				     String clsName=cls.getText();
				     IFile file = WorkbenchHelper.getselectedFile(clsName);
					 jdtRewriter=new JDTRewriter(file);
					 jdtRewriter.addAnnotation(methodname);
					} 
			}
		};
	}


	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Sample View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}