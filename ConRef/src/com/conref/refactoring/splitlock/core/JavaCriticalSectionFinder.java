/** 
 *  
 * 
 * 	@author Ju Qian {jqian@live.com}
 * 	@date 2011-1-24
 * 	@version
 */
package com.conref.refactoring.splitlock.core;

import java.io.*;
import java.util.*;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.conref.sootUtil.SootUtils;
import com.conref.util.ClassFileFinder;
import com.conref.util.JavaSrcFinder;


import soot.Scene;
import soot.SootClass;
import soot.toolkits.scalar.Pair;



/**
 *
 */
public class JavaCriticalSectionFinder {
	private String _srcpaths;
	private String _classpaths;
	private static JavaCriticalSectionFinder _instance;
private	Collection<JavaCriticalSection> criticalSections = new LinkedList<JavaCriticalSection>();
public Collection<JavaCriticalSection> getAllSyncs(){
	return criticalSections;
}
	private JavaCriticalSectionFinder(String srcpaths, String classpaths){
		this._srcpaths = srcpaths;
		this._classpaths =classpaths;
		find();
	}
	public static JavaCriticalSectionFinder getInstance(String srcpaths, String classpaths){
		if(_instance==null){
			_instance=new JavaCriticalSectionFinder(srcpaths, classpaths);
		}
		return _instance;
	}
	public Collection<JavaCriticalSection> find(){
		// list the .java and .class files
		JavaSrcFinder srcFinder = new JavaSrcFinder(_srcpaths);
		ClassFileFinder classFinder = new ClassFileFinder(_classpaths);		
		Set<String> classNameOfSourceFiles = srcFinder.listClasses();
		Set<String> classNameOfClassFiles = classFinder.listClasses();
		
		
		// find critical sections by search .java files
		for(String classname: classNameOfSourceFiles){
			// Some classes not compiled to .class file in JDK
			if(!classNameOfClassFiles.contains(classname)){
				continue;
			}
			
			InputStream is = srcFinder.findClass(classname);			
			char[] source = null;			
			try{
				byte[] data = new byte[is.available()];
				BufferedInputStream input = new BufferedInputStream(is);
				int read = input.read(data);
				
				String text = new String(data, 0, read);
				source = text.toCharArray();
			}
			catch(IOException e){}
			
			ASTParser parser = ASTParser.newParser(AST.JLS3);
	        parser.setSource(source);
	        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);      
	        SyncVisitor syncFinder = new SyncVisitor(classname,classFinder,astRoot,criticalSections);
	        astRoot.accept(syncFinder); 
		}
		
		return criticalSections;
	}
	
	static class LocalClassIndexFinder extends ASTVisitor {
		int index = 0; 
		final ASTNode _find;
		
		LocalClassIndexFinder(ASTNode find){
			this._find = find;
		}
		
		public boolean visit(AnonymousClassDeclaration node){
			index++;
			return (node!=_find);
		}
		
		public boolean visit(TypeDeclaration node){
			//local class also has index
			if(JavaASTUtils.isLocalClass(node)){
				index++;
			}
			
			return (node!=_find);
		}
		
		public int getIndex(){
			return index;
		}
	}

	
	static class SyncVisitor extends ASTVisitor {
		public Collection<JavaCriticalSection> _result;
		private String _classNameOfSourceFile;
		private CompilationUnit _cu;
		private ClassFileFinder _classFinder;
		
		public SyncVisitor(String classNameOfSourceFile,ClassFileFinder classFinder,
				             CompilationUnit cu,Collection<JavaCriticalSection> css){
			this._cu = cu;
			this._classNameOfSourceFile = classNameOfSourceFile;
			this._result = css;
			this._classFinder = classFinder;
		}
		
		private void addCriticalSection(ASTNode sync){
			ASTNode method;
			if(sync instanceof MethodDeclaration){
				method = sync;
			}
			else{
				method = sync;
				while(method!=null&&!(method instanceof MethodDeclaration)){
					method = method.getParent();
				}
			}
			if(method==null) return ;
			MethodDeclaration methodDecl = (MethodDeclaration)method;
			String outMostClassName = getOutMostClassName(methodDecl);
			String className = findClassName(outMostClassName, methodDecl);
			JavaCriticalSection cs = new JavaCriticalSection(_classNameOfSourceFile,className,methodDecl,sync);
			_result.add(cs);
		}
		
		public boolean visit(SynchronizedStatement node){
			addCriticalSection(node);
			return true;
		}

		/** Here do not consider native methods. */
		public boolean visit(MethodDeclaration node) {			
			boolean isSynchronized = JavaASTUtils.isMethodSynchronized(node);
			boolean isNative = JavaASTUtils.isMethodNative(node);
			
			if(isSynchronized && !isNative){
				addCriticalSection(node);
			}			 
			
			return true;
		}
		
		private final String addPackageName(String className){
			int index = _classNameOfSourceFile.lastIndexOf('.');
			if(index>=0){
				String pkgName = _classNameOfSourceFile.substring(0, index);
				className = pkgName + "." + className;
			}
			
			return className;
		}
		
		private String getOutMostClassName(MethodDeclaration method){			 
			TypeDeclaration outMostClass = JavaASTUtils.getOutMostClass(method);
			String className = outMostClass.getName().getIdentifier();
			className = addPackageName(className);
			
			return className;
		}
		
		// The class can be:
		//    an inner class
		//    a class defined in the .java file of another class
		//    an anonymous class
		private String findClassName(String outMostClassName, MethodDeclaration method){
			//int start = method.getStartPosition();
			//int end = start + method.getLength();

			//int startLine = _cu.getLineNumber(start);
			//int endLine = _cu.getLineNumber(end);
			 
			String className = "";
			ASTNode node = method;
			while(node!=null){
				if(node instanceof TypeDeclaration){
					TypeDeclaration type = (TypeDeclaration)node;
					String typeName = type.getName().getIdentifier();
					
					if(JavaASTUtils.isLocalClass(type)){
						LocalClassIndexFinder finder = new LocalClassIndexFinder(type);
					    _cu.accept(finder); 
					    int index = finder.getIndex();
					    typeName = ""+index+typeName;
					}
					
					if(className.length()==0){
						className = typeName;
					}
					else{
						className = typeName +"$" +className;
					}
				}
				
				if(node instanceof AnonymousClassDeclaration){	
					AnonymousClassDeclaration decl = (AnonymousClassDeclaration)node;     
					LocalClassIndexFinder finder = new LocalClassIndexFinder(decl);
				    _cu.accept(finder); 
				    int index = finder.getIndex();
				    
				    if(className.length()==0){
				    	className = ""+index;
				    }
				    else{
				    	className = ""+index +"$" + className;
				    }
				        
					//className = findAnonymousClassName(outMostClassName, startLine, endLine);
					//break;
				}
				 
				node = node.getParent();
			}
	 
			if(className.length()==0){
				throw new RuntimeException("No class declaration found.");
			} 
			 
			className = addPackageName(className);
			return className;
		}
		
		protected String findAnonymousClassName(String outMostClassName, int startLine, int endLine){
			Collection<String> innerClasses = _classFinder.findInnerClasses(outMostClassName);
			if(innerClasses.size()==1){
				return innerClasses.iterator().next();
			}
			
			Collection<SootClass> sootClasses = new ArrayList<SootClass>();
			for(String className: innerClasses){				 
				//Scene.v().addBasicClass(className,SootClass.BODIES);
				SootClass cls = Scene.v().loadClass(className, SootClass.BODIES);
				sootClasses.add(cls);
			}		
			
			Scene.v().loadNecessaryClasses();
			//Scene.v().loadBasicClasses();
			
			SootClass find = null;
			for(SootClass cls: sootClasses){
				Pair<Integer,Integer> lineRange = SootUtils.getLineRange(cls);
				int clsLineStart = lineRange.getO1();
				int clsLineEnd = lineRange.getO2();
				
				if(clsLineStart==0 && clsLineEnd==0){
					continue;
				}
				
				if(startLine<=clsLineEnd && endLine>=clsLineStart){
					find = cls;
					break;
				}
			}
			
			if(find!=null)
				return find.getName();
			else
				return null;
		}
	}


	public static void reset() {
		_instance=null;
		
	}
}
