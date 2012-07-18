package com.conref.refactoring.splitlock.actions;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.refactoring.splitlock.core.ClassFeildsAnalyzer;
import com.conref.refactoring.splitlock.core.JavaCriticalSection;
import com.conref.refactoring.splitlock.core.JavaCriticalSectionFinder;
import com.conref.refactoring.splitlock.core.JavaCriticalSection.NoMatchingSootMethodException;
import com.conref.refactoring.splitlock.views.MethodsView;
import com.conref.util.PathUtils;
import com.conref.util.WorkbenchHelper;

import soot.Scene;


public class ShowTableViewAction implements IEditorActionDelegate,
		IWorkbenchWindowActionDelegate {
	private ICompilationUnit select;
	private static IFile _file;
	private ITextEditor _editor;
	private static String id = "test.views.SampleView";
	private static ClassFeildsAnalyzer analyzer;

	@Override
	public void run(IAction action) {
		// 
		try {
			IWorkbenchPage page = WorkbenchHelper.openViewPage(id);
//			MethodsView viewpart = (MethodsView) page.findView(id);
//			Map<String, Map<String, Integer>> classMap = getClasses(_file);
//			viewpart.getViewer().setInput(classMap);
			
			page.showView(id);
			 Job job = new Job("Analysis") {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("analysis in running,please wait!!!", 8);
					
						try {
							runAnalysis();
							//
							monitor.subTask("getting All synchrnizations");
							analyzer.getAllSyncs();
							monitor.worked(1);
							monitor.subTask("loading class");
							analyzer.SootExOpreation();
							monitor.worked(1);
							monitor.subTask("building callgraph");
							Collection<JavaCriticalSection> validSyncs = new HashSet<JavaCriticalSection>();
							analyzer.buildCallGraph(validSyncs);
							monitor.worked(1);
							monitor.subTask("assuring All Syncs In CallGraph");
							analyzer.assureAllSyncInCallGraph(validSyncs);
							monitor.worked(1);
							monitor.subTask("getting all involved mthods");
							analyzer.getAllInvolvedMethods();
							monitor.worked(1);
							monitor.subTask("building VarConn Graph");
							analyzer.buildVarConn(validSyncs);
							monitor.worked(1);
							monitor.subTask("classifying VarConn Graph");
							analyzer.classifyVarConn();
							//
						} catch (PartInitException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (NoMatchingSootMethodException e) {
							e.printStackTrace();
						}
					monitor.done();
					Display.getDefault().asyncExec(new Runnable(){

						@Override
						public void run() {
							try {
								updateView();
							} catch (PartInitException e) {
								e.printStackTrace();
							}
						}
						
					});
				return Status.OK_STATUS;
				}
			};
		job.setUser(true);
		job.schedule();
		} catch (PartInitException e) {

			e.printStackTrace();

		}
			
		
//		//
//		try {
//			IWorkbenchPage page = WorkbenchHelper.openViewPage(id);
//			MethodsView viewpart = (MethodsView) page.findView(id);
//			Map<String, Map<String, Integer>> classMap = getClasses(_file);
//			viewpart.getViewer().setInput(classMap);
//			page.showView(id);
//			runAnalysis();
//		} catch (PartInitException e) {
//
//			e.printStackTrace();
//
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
	}

	private void runAnalysis() throws PartInitException, InterruptedException {
		String srcpath = PathUtils.getSrcPath(_file);
		String classpath = PathUtils.getClassPath(_file);
		try {
			analyzer = ClassFeildsAnalyzer.v(null, srcpath, classpath);
		} catch (NoMatchingSootMethodException e) {
			e.printStackTrace();
		}
//		Thread t = new Thread(analyzer);
//		t.start();
	}

	public static void updateView() throws PartInitException {
		IWorkbenchPage page = WorkbenchHelper.openViewPage(id);
		MethodsView viewpart = (MethodsView) page.findView(id);
		viewpart.getViewer().setInput(analyzer.getCandidateCls());
		page.showView(id);
	};

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		if (selection.isEmpty())
			select = null;
		else if (selection instanceof IStructuredSelection) {
			IStructuredSelection strut = ((IStructuredSelection) selection);
			if (strut.size() != 1)
				select = null;
			if (strut.getFirstElement() instanceof IJavaElement)
				select = (ICompilationUnit) strut.getFirstElement();
		} else if (selection instanceof ITextSelection) {
			select = (ICompilationUnit) JavaCore
					.createCompilationUnitFrom(_file);
		} else
			// _file =
			// (IFile)SelectionResolver.getSelectedResource(selection,IResource.FILE);
			select = null;
		action.setEnabled(true);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

		_editor = (ITextEditor) targetEditor;

		_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();
	}

	public static Map<String, Map<String, Integer>> getClasses(IFile file) {

		String srcpath = PathUtils.getSrcPath(file);
		String classpath = PathUtils.getClassPath(file);
		Collection<JavaCriticalSection> result = JavaCriticalSectionFinder
				.getInstance(srcpath, classpath).getAllSyncs();

		Set<String> classes = new HashSet<String>();
		Map<String, Map<String, Integer>> mapResult = new HashMap<String, Map<String, Integer>>();
		for (JavaCriticalSection cs : result) {
			if (!cs.getLockName().equals("this"))
				continue;
			String classname = cs.getClassNameOfSourceFile();
			if (!classes.contains(classname)) {
				classes.add(classname);
				mapResult.put(classname, new HashMap<String, Integer>());
			}
			String method = cs.getMethodName();
			Integer line = new Integer(cs.getStartLine());
			mapResult.get(classname).put(method, line);
		}
		return mapResult;
	}

}
