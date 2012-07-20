package jqian.util.eclipse;

import java.util.*;


import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.ui.*;


public class ProjectSourceViewer extends TreeViewer {		
	private Text _selectionDisplay;
	
	public ProjectSourceViewer(Composite parent,Text text,IJavaProject project,JavaElementFilter filter) {
		super(parent, SWT.FULL_SELECTION | SWT.LINE_DASH | SWT.BORDER);
		
		this._selectionDisplay = text;
		
		//ITreeContentProvider contentProvider = new StandardJavaElementContentProvider(true);
		ITreeContentProvider contentProvider = new MyTreeContenetProvider();
	    setContentProvider(contentProvider);		
		
		ILabelProvider labelProvider= new JavaElementLabelProvider(
		        JavaElementLabelProvider.SHOW_DEFAULT |
		        JavaElementLabelProvider.SHOW_BASICS );
		setLabelProvider(labelProvider);		
		
		setInput(project);		
		addDbClickListener();
		
		//expandAll();
	}	

	/** Listen to Double-Clicks */
	private void addDbClickListener() {	
		getTree().addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event e) {
				TreeItem[] selection = getTree().getSelection();
				if (selection.length == 0)
					return;

				TreeItem item = selection[0];
				Object data = item.getData();
				if(data instanceof IMethod){
					try{
						IMethod method = (IMethod)data;
						String sig = JDTUtils.getMethodSootSignature(method);
					   _selectionDisplay.setText(sig);
					}catch(Exception except){
						_selectionDisplay.setText("");
					}
				}else{
					_selectionDisplay.setText("");
				}
			}
		});
	}


	class MyTreeContenetProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {	
			try{
			if (parentElement instanceof IJavaProject) {
					IJavaProject prj = (IJavaProject) parentElement;
					IPackageFragmentRoot[] roots = prj.getPackageFragmentRoots();
					LinkedList<IPackageFragmentRoot> displayedRoots = new LinkedList<IPackageFragmentRoot>();
					for (int i = 0; i < roots.length; i++) {
						if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
							displayedRoots.add(roots[i]);
						}
					}

					return displayedRoots.toArray();
				} else if (parentElement instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot pkgRoot = (IPackageFragmentRoot) parentElement;
					return pkgRoot.getChildren();
				} else if (parentElement instanceof IPackageFragment) {
					IPackageFragment pkg = (IPackageFragment) parentElement;
					ICompilationUnit[] cunits = pkg.getCompilationUnits();
					LinkedList<IType> types = new LinkedList<IType>();
					int length = cunits.length;
					for (int i = 0; i < length; i++) {
						IType[] unitTypes = cunits[i].getAllTypes();
						int cnt = unitTypes.length;
						for (int j = 0; j < cnt; j++) {
							IMethod[] methods = getConcreteMethods(unitTypes[j]);
							if(methods.length>0){
								types.add(unitTypes[j]);
							}							
						}
					}

					return types.toArray();
				} else if(parentElement instanceof IType){
					return getConcreteMethods((IType)parentElement);
				}
				
			    return new Object[0];
			}
			catch(Exception e){
				return null;
			}
		}
		
		private IMethod[] getConcreteMethods(IType type)throws Exception{
			if(type.isInterface() || type.isAnnotation())
				return new IMethod[0];
			
			IMethod[] methods = type.getMethods();		
			return methods;
		}

		public Object getParent(Object element) {
			if (element instanceof IJavaElement) {
				IJavaElement javaElmt = (IJavaElement)element;
				return javaElmt.getParent();
			} else {
				return null;
			}
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length>0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}	

	/*class MyTableLableProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if(element instanceof Wrapper){
				return SWTResourceManager.getImage(SliceViewer.class, "/img/javaproject.PNG");				
			}
			
			
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		        if(TVContentProvider.isLeaf(obj))       
		           imageKey = ISharedImages.IMG_DEF_VIEW ;
		        return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		        
			
			else if (element instanceof File){
				File file = (File)element;
				if(file.isDirectory()){
					return SWTResourceManager.getImage(SliceViewer.class, "/img/directory.PNG");	
				}
				else{
					return SWTResourceManager.getImage(SliceViewer.class, "/img/javafile.PNG");	
				}			
			}
			else{
				return null;
			}
		}

		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof File){
				switch(columnIndex){
				case 0:{
					File file = (File)element;
					return file.getName();					
				}
				case 1:{
					int count = _distributionBar.getFileLineCount(element);
					if(count==0)
						return "";
					else
					    return ""+count;
				}
				case 2:{
					int count = _distributionBar.getSliceLineCount(element);
					if(count==0)
						return "";
					else
					    return ""+count;
				}
				default: return "";
				}
			}			
			else
				return element.toString();
		}

		public void addListener(ILabelProviderListener listener) {}

		public void dispose() {}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {}
	}*/
}

