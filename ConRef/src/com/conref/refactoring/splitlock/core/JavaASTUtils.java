/** 
 *  
 * 
 * 	@author Ju Qian {jqian@live.com}
 * 	@date 2011-2-22
 * 	@version
 */
package com.conref.refactoring.splitlock.core;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 *
 */
public class JavaASTUtils {
	public static ASTNode findClassDeclNode(ASTNode node){
		ASTNode classDeclNode = node;
		while(node!=null){
			if(node instanceof TypeDeclaration){		 
				classDeclNode = node;
				break;
			}
			
			if(node instanceof AnonymousClassDeclaration){
				classDeclNode = node;
				break;
			}
			 
			node = node.getParent();
		}
		
		return classDeclNode;
	}
	
	
	public static CompilationUnit findComplilationUnit(ASTNode node){
		CompilationUnit cu = null;
		while(node!=null){
			if(node instanceof CompilationUnit){		 
				cu = (CompilationUnit)node;
				break;
			} 
		 
			node = node.getParent();
		}
		
		return cu;
	}
	
	public static boolean isLocalClass(ASTNode classDecl){
		ASTNode node = classDecl;
		while (node != null) {
			if (node instanceof MethodDeclaration) {
				return true;
			} 

			node = node.getParent();
		}

		return false;
	}
	
	public static boolean isMethodSynchronized(MethodDeclaration node) {
		Collection<?> modifiers = node.modifiers();
		for(Object o: modifiers){
			if(o instanceof Modifier){
				Modifier m = (Modifier)o;
				if(m.isSynchronized()){
					return true;						
				}
			}				
		}
		
		return false;
	}
	
	public static boolean isMethodNative(MethodDeclaration node){
		Collection<?> modifiers = node.modifiers();		 
		for(Object o: modifiers){
			if(o instanceof Modifier){
				Modifier m = (Modifier)o;
				if(m.isNative()){
					return true;
				}
			}				
		}
		
		return false;
	}
	
	public static boolean isMethodStatic(MethodDeclaration node){
		Collection<?> modifiers = node.modifiers();
		for(Object o: modifiers){
			if(o instanceof Modifier){
				Modifier m = (Modifier)o;
				if(m.isStatic()){
					return true;					 
				}
			}				
		}
		
		return false;
	}

	public static TypeDeclaration getOutMostClass(MethodDeclaration method){
		ASTNode node = method;
		TypeDeclaration lastTypeDecl = null;
		while(node!=null){
			if(node instanceof TypeDeclaration){
				TypeDeclaration type = (TypeDeclaration)node;
				lastTypeDecl = type;
			} 
			 
			node = node.getParent();
		}
		
		return lastTypeDecl;
	}
}
