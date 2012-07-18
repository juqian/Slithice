package com.conref.refactoring.splitlock.views;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class NewTreeViewer extends TreeViewer {
	static NewTreeViewer _instance;
	private Shell _shell;
	private Tree _tree;
	private Map<String,Map<String,Integer>> root;
	private NewTreeViewer(Composite parent) {
		super(parent, SWT.FULL_SELECTION | SWT.LINE_DASH | SWT.BORDER);

		this._shell = parent.getShell();
		_tree = getTree();
		_tree.setHeaderVisible(true);
		_tree.setLinesVisible(true);

		addColumns();
		setContentProvider(new MyTreeContenetProvider());
		setLabelProvider(new MyTableLableProvider());
	}

	public static NewTreeViewer build(Composite parent) {
		if (_instance == null)
			return new NewTreeViewer(parent);
		return _instance;
	}

	private void addColumns() {
		TreeColumn column = new TreeColumn(_tree, SWT.CENTER);
		column.setText("candidate synchronizations");
		column.setWidth(550);
		column = new TreeColumn(_tree, SWT.CENTER);
		column.setText("line");
		column.setWidth(80);
	}

	class MyTreeContenetProvider implements ITreeContentProvider {
		

		public Object[] getChildren(Object parentElement) {
			if (root.keySet().contains(parentElement)) {
				return ((Map) root.get(parentElement)).keySet().toArray();
			}
			// return new Object[0];
			return null;
		}

		public Object getParent(Object element) {
			for (Object cls : root.keySet()) {
				if (((Map) root.get(cls)).keySet().contains(element)) {
					return cls;
				}
			}
			return null;

		}

		public boolean hasChildren(Object element) {
			return getChildren(element) != null;
		}

		@SuppressWarnings("rawtypes")
		public Object[] getElements(Object inputElement) {

			if (inputElement instanceof Map) {
				root = (Map) inputElement;
				return root.keySet().toArray();
			}
			return new HashSet(0).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class MyTableLableProvider extends LabelProvider implements
			ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return element.toString();
			}
			if (columnIndex == 1) {
				int line=0;
				for(Object method:root.values()){
					if(((Map)method).containsKey(element)){
						return ((Map<String,Integer>)method).get(element).toString();
					}
			}
			}
			return "";
		}
		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}

}