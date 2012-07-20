package jqian.slicer.core;

import java.io.File;
import java.util.*;

import jqian.slicer.plugin.*;
import jqian.slicer.view.global.ISliceDistribution;
import jqian.sootex.dependency.pdg.DependenceNode;
import jqian.sootex.dependency.slicing.ProgramSlice;
import jqian.util.eclipse.JDTUtils;
import soot.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;


class SliceDistribution implements ISliceDistribution{	
	private ProgramSlice _slice;
	private Map<File,Collection<Integer>> _file2lines = new HashMap<File,Collection<Integer>>();
	
	public SliceDistribution(IProject prj,Collection<?>  criteria,Collection<DependenceNode> depNodes){
		_slice = new ProgramSlice(criteria,depNodes);
		_prj = prj;
	}	
	
	public Collection<Integer> getLines(File file){
		Collection<Integer> lines = _file2lines.get(file);
		if(lines==null){
			lines = collectLines(file);
			_file2lines.put(file,lines);
		}
		return lines;
	}
	
	public Collection<Integer> getLines(IFile ifile){
		File file = WorkbenchHelper.getFile(ifile);
		return getLines(file);
	}

	private static void collectAllTypes(IParent parent, Collection<IType> out) throws JavaModelException {
		IJavaElement[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			IJavaElement childElem = children[i];
			if(childElem instanceof IType){
				out.add((IType)childElem);
			}
			 
			if (childElem instanceof  IParent) {
				collectAllTypes((IParent)childElem, out);
			}
		}
	}

	private Collection<IType> collectAllTypes(ICompilationUnit cu) throws JavaModelException{
		IType[] types = cu.getAllTypes();
		Collection<IType> result = new HashSet<IType>();
		for(IType t: types){
			result.add(t);
			collectAllTypes(t, result);
		}
		
		return result;
	}
	
	private Collection<Integer> collectLines(File file){
		IFile ifile = WorkbenchHelper.getIFile(_prj, file);	
		Collection<Integer> lines = new TreeSet<Integer>();		
	    ICompilationUnit cu = (ICompilationUnit)JavaCore.create(ifile);
	    
	    try{
	    	//IType[] types = cu.getAllTypes();
	    	Collection<IType> types = collectAllTypes(cu);
	    	for(IType t: types){
	    		String sig = JDTUtils.getTypeSootSignature(t);
	    		try{
	    			SootClass cls = Scene.v().getSootClass(sig);
	    			lines.addAll(_slice.getRelevantLines(cls));
	    		}
	    		catch(Exception e){}
	    	}
	    }
	    catch(Exception e){
	    }
	    
	    return lines;
	}
	
	
	
	public Collection<Integer> getLines(String method){
		Collection<Integer> eclipseLines = new LinkedList<Integer>();
		SootMethod m = Scene.v().getMethod(method);
		Collection<Integer> lines = _slice.getRelevantLines(m);
		for(Iterator<Integer> it=lines.iterator();it.hasNext();){
			int l = it.next();
			//in eclipse editor lines starts form 0, while in javac compiler result
			//lines start from 1
			l--;
			eclipseLines.add(l);
		}
		
		return eclipseLines;
	}
	
	private IProject _prj;
}

