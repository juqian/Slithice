/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate 
 * copyright notice.  All rights reserved.
 */
package jqian.util.eclipse;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;



public class JDTUtils {
    private static final IJavaElement[] EMPTY_RESULT = new IJavaElement[0];

    /**     
     * Finds a type by its qualified type name (dot separated).
     * @param jproject The java project to search in
     * @param fullyQualifiedName The fully qualified name (type name with enclosing type names and package (all separated by dots))
     * @return The type found, or null if not existing
    */
    public static IType findType(IJavaProject jproject, String fullyQualifiedName) throws JavaModelException {
        fullyQualifiedName = fullyQualifiedName.replace('$', '.');
        IType type = jproject.findType(fullyQualifiedName, (IProgressMonitor)null);
        if (type != null) return type;
        
        IPackageFragmentRoot[] roots = jproject.getPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            IPackageFragmentRoot root = roots[i];
            type = findType(root, fullyQualifiedName);
            if (type != null && type.exists()) 
            	return type;
        }

        return null;
    }    

    private static IType findType(IPackageFragmentRoot root, String fullyQualifiedName) 
         throws JavaModelException {
        IJavaElement[] children = root.getChildren();
        for (int i = 0; i < children.length; i++) {
            IJavaElement element = children[i];
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                IPackageFragment pack = (IPackageFragment)element;
                if (!fullyQualifiedName.startsWith(pack.getElementName())) continue;
                IType type = findType(pack, fullyQualifiedName);
                if (type != null && type.exists()) return type;
            }
        }

        return null;
    }    

    private static IType findType(IPackageFragment pack, String fullyQualifiedName) 
            throws JavaModelException {
        ICompilationUnit[] cus = pack.getCompilationUnits();
        for (int i = 0; i < cus.length; i++) {
            ICompilationUnit unit = cus[i];
            IType type = findType(unit, fullyQualifiedName);
            if (type != null && type.exists()) return type;
        }

        return null;
    }

    private static IType findType(ICompilationUnit cu, String fullyQualifiedName) 
            throws JavaModelException {
        IType[] types = cu.getAllTypes();
        for (int i = 0; i < types.length; i++) {
            IType type = types[i];
            if (getFullyQualifiedName(type).equals(fullyQualifiedName)) return type;
        }

        return null;
    }    

    /**
     * Returns the fully qualified name of the given type using '.' as separators.
     * This is a replace for IType.getFullyQualifiedTypeName
     * which uses '$' as separators. As '$' is also a valid character in an id
     * this is ambiguous. JavaCore PR: 1GCFUNT
    */

    public static String getFullyQualifiedName(IType type) {
        try {
            if (type.isBinary() && !type.isAnonymous()) {
                IType declaringType = type.getDeclaringType();

                if (declaringType != null) {
                    return getFullyQualifiedName(declaringType) + '.' + type.getElementName();
                }
            }
        } catch (JavaModelException e) {
        }

        return type.getFullyQualifiedName('.');
    }

    

    /**
     * Force a reconcile of a compilation unit.
     * @param unit
    */
    public static void reconcile(ICompilationUnit unit) throws JavaModelException {
        unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
    }
    

    /**
     * Returns the package fragment root of &lt;code&gt;IJavaElement&lt;/code&gt;. If the given
     * element is already a package fragment root, the element itself is returned.
    */
    public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
        return (IPackageFragmentRoot)element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    }
    

    /**
     * Resolves a type name in the context of the declaring type.
     * @param refTypeSig the type name in signature notation (for example 'QVector') this can also be an array type,
     *        but dimensions will be ignored.
     * @param declaringType the context for resolving (type where the reference was made in)
     * @return returns the fully qualified type name or build-in-type name. if a unresolved type couldn't be 
     *         resolved null is returned
    */
    public static String getResolvedTypeName(String refTypeSig, IType declaringType) throws JavaModelException {
        int arrayCount = Signature.getArrayCount(refTypeSig);
        char type = refTypeSig.charAt(arrayCount);
        
        String fullName = null; 
        if (type == Signature.C_UNRESOLVED) {
            String name = "";
            int bracket = refTypeSig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
            if (bracket > 0){ 
            	name = refTypeSig.substring(arrayCount + 1, bracket); 
            }
            else{
                int semi = refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
                if (semi == -1) {
                    throw new IllegalArgumentException();
                }

                name = refTypeSig.substring(arrayCount + 1, semi);
            }

            String[][] resolvedNames = declaringType.resolveType(name);
            if (resolvedNames != null && resolvedNames.length > 0) {
            	String pkgname = resolvedNames[0][0];
            	String clsname = resolvedNames[0][1];
            	
            	StringBuffer buf = new StringBuffer();
            	for(int i=0;i<clsname.length();i++){
            		char ch = clsname.charAt(i);
            		if(ch=='.')
            			buf.append('$');
            		else
            			buf.append(ch);
            	}
            	
            	clsname = buf.toString();
            	
                fullName = concatenateName(pkgname, clsname);
            }
        } else  {
            fullName = Signature.toString(refTypeSig.substring(arrayCount));
        }
        
        //append array tag
        for(int i=0;fullName!=null && i<arrayCount;i++){
        	fullName += "[]";
        }
        
        return fullName;
    }


    /*public static String getResolvedTypeFileName(String refTypeSig, IType declaringType) 
             throws JavaModelException {
        int arrayCount = Signature.getArrayCount(refTypeSig);
        char type = refTypeSig.charAt(arrayCount);
        if (type == Signature.C_UNRESOLVED) {
            String name = "";
            int bracket = refTypeSig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
            if (bracket > 0) 
            	name = refTypeSig.substring(arrayCount + 1, bracket); 
            else {
                int semi = refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
                if (semi == -1) {
                    throw new IllegalArgumentException();
                }
                
                name = refTypeSig.substring(arrayCount + 1, semi);
            }

            String[][] resolvedNames = declaringType.resolveType(name);
            if (resolvedNames != null && resolvedNames.length > 0) {
                return concatenateName(resolvedNames[0][0], resolvedNames[0][1].replace('.', '$'));
            }
            return "*";
        } else  {
            return Signature.toString(refTypeSig.substring(arrayCount));
        }
    }*/
    

    /**
     * Determine if refTypeSig is unresolved.
    */
    /*public static boolean isTypeNameUnresolved(String refTypeSig) {
        return refTypeSig.charAt(Signature.getArrayCount(refTypeSig)) == Signature.C_UNRESOLVED;

    }
     
    public static String resolveTypeName(String refTypeSig, IType declaringType) 
           throws JavaModelException {
        int arrayCount = Signature.getArrayCount(refTypeSig);
        char type = refTypeSig.charAt(arrayCount);
        if (type == Signature.C_UNRESOLVED) {
            String name = "";
            int bracket = refTypeSig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
            if (bracket > 0) name = refTypeSig.substring(arrayCount + 1, bracket); else  {
                int semi = refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
                if (semi == -1) {
                    throw new IllegalArgumentException();
                }

                name = refTypeSig.substring(arrayCount + 1, semi);
            }

            String[][] resolvedNames = declaringType.resolveType(name);
            if (resolvedNames != null && resolvedNames.length > 0) {
                return JDTUtils.concatenateName(resolvedNames[0][0], resolvedNames[0][1]);
            } else  {
                throw new JavaModelException(new Exception(name + " not resolvable"), 
                		                     IJavaModelStatusConstants.EVALUATION_ERROR);
            }
        }

        return refTypeSig;
    }
    
    
    public static void resolveTypeName(String refTypeSig, IType declaringType, StringBuffer sb) 
         throws JavaModelException {
        if (isTypeNameUnresolved(refTypeSig)) {
            sb.append(refTypeSig.substring(0, refTypeSig.indexOf(Signature.C_UNRESOLVED)));
            sb.append('L');
            sb.append(resolveTypeName(refTypeSig, declaringType));
            sb.append(';');
        } else  {
            sb.append(refTypeSig);
        }
    }*/
    

    /**
     * Concatenates two names. Uses a dot for separation.
     * Both strings can be empty or &lt;code&gt;null&lt;/code&gt;.
    */
    public static String concatenateName(String name1, String name2) {
        StringBuffer buf = new StringBuffer();
        if (name1 != null && name1.length() > 0) {
            buf.append(name1);
        }

        if (name2 != null && name2.length() > 0) {
            if (buf.length() > 0) {
                buf.append('.');
            }

            buf.append(name2);
        }

        return buf.toString();
    }
    

    public static IJavaElement[] codeResolve(ITextEditor editor) throws JavaModelException {
        return codeResolve(editor, true);
    }
    

    /**
     * @param primaryOnly if &lt;code&gt;true&lt;/code&gt; only primary working copies will be returned
     * @since 3.2
    */
    public static IJavaElement[] codeResolve(ITextEditor editor, boolean primaryOnly) 
           throws JavaModelException {
        return codeResolve(getInput(editor, primaryOnly), 
        		          (ITextSelection)editor.getSelectionProvider().getSelection());
    }
    

    public static IJavaElement getInput(IEditorPart editor) {
        return getInput(editor, true);
    }

    

    /**
     * @param primaryOnly if &lt;code&gt;true&lt;/code&gt; only primary working copies will be returned
     * @since 3.2
    */
    private static IJavaElement getInput(IEditorPart editor, boolean primaryOnly) {
        if (editor == null) 
        	return null;

        return getEditorInputJavaElement(editor, primaryOnly);
    }    

    public static ICompilationUnit getInputAsCompilationUnit(ITextEditor editor) {
        Object editorInput = getInput(editor);
        if (editorInput instanceof ICompilationUnit) 
        	return (ICompilationUnit)editorInput;

        return null;
    }    

    /**
     * Returns the given editor's input as Java element.
     * @param editor the editor
     * @param primaryOnly if &lt;code&gt;true&lt;/code&gt; only primary working copies will be returned
     * @return the given editor's input as Java element or &lt;code&gt;null&lt;/code&gt; if none
     * @since 3.2
    */
    public static IJavaElement getEditorInputJavaElement(IEditorPart editor, boolean primaryOnly) {
        assert (editor!=null);
        IEditorInput editorInput = editor.getEditorInput();
        if (editorInput == null) 
        	return null;

        IJavaElement je = getEditorInputJavaElement(editorInput);

        if (je != null || primaryOnly) 
        	return je;

        return JavaUI.getWorkingCopyManager().getWorkingCopy(editorInput);
    }    

    /**
     * Returns the Java element wrapped by the given editor input.     
     * @param editorInput the editor input
     * @return the Java element wrapped by &lt;code&gt;editorInput&lt;/code&gt; or &lt;code&gt;null&lt;/code&gt; if none
     * @since 3.2
    */
    public static IJavaElement getEditorInputJavaElement(IEditorInput editorInput) {
        IJavaElement je = JavaUI.getWorkingCopyManager().getWorkingCopy(editorInput);
        if (je != null) 
        	return je;
        return (IJavaElement)editorInput.getAdapter(IJavaElement.class);
    }
    

    public static IJavaElement[] codeResolve(IJavaElement input, ITextSelection selection) 
           throws JavaModelException {
        return codeResolve(input,selection.getOffset(), selection.getLength());
    }    

    public static IJavaElement[] codeResolve(IJavaElement input, int offset,int length) 
    	throws JavaModelException {
    	if (input instanceof ICodeAssist) {
    		if (input instanceof ICompilationUnit) {
    			reconcile((ICompilationUnit)input);
    		}

    		IJavaElement[] elements = ((ICodeAssist)input).codeSelect(offset, length);
    		if (elements != null && elements.length > 0)
    			return elements;
    	}

    	return EMPTY_RESULT;
    }    
    
    public static IJavaElement[] codeResolve(IEditorPart editor, int offset,int length)
    	throws JavaModelException {
    	IJavaElement input = getInput(editor);
    	return codeResolve( input, offset, length);
    }
    
    
    /**
     * @param primaryOnly if &lt;code&gt;true&lt;/code&gt; only primary working copies will be returned
     * @since 3.2
     */
    public static IJavaElement getElementAtOffset(ITextEditor editor, boolean primaryOnly) 
    	throws JavaModelException {
    	ITextSelection selection = (ITextSelection)editor.getSelectionProvider().getSelection();
        return getElementAtOffset(getInput(editor, primaryOnly), selection.getOffset());
    }
  
    public static IJavaElement getElementAtOffset(IJavaElement input, int offset) throws JavaModelException {
		if (input instanceof ICompilationUnit) {
			ICompilationUnit cunit = (ICompilationUnit) input;
			reconcile(cunit);
			IJavaElement ref = cunit.getElementAt(offset);
			if (ref == null)
				return input;
			else
				return ref;

		} else if (input instanceof IClassFile) {
			IJavaElement ref = ((IClassFile) input).getElementAt(offset);
			if (ref == null)
				return input;
			else
				return ref;
		}
		return null;
	}
    
    public static IMethod getMethod(ILocalVariable local){
    	IJavaElement parent = local.getParent();
    	IMethod method = null;
    	while(parent!=null){
    		if(parent instanceof IMethod){
    			method = (IMethod)parent;
    			break;
    		}
    		
    		parent = parent.getParent();
    	}
    	
    	return method;
    }
    
    /** Get the soot signature (recognizable by soot) of a given method. */
    public static String getMethodSootSignature(IMethod method){
    	IType declaringType = method.getDeclaringType();

		try {
			String m = "<" + method.getDeclaringType().getFullyQualifiedName();
			String returnType = method.getReturnType();
			returnType = JDTUtils.getResolvedTypeName(returnType, declaringType);
			// returnType = Signature.toString(returnType);
			m += ": " + returnType;
			m += " " + method.getElementName() + "(";

			String[] params = method.getParameterTypes();
			for (int i = 0; i < params.length; i++) {
				String p = params[i];
				p = JDTUtils.getResolvedTypeName(p, declaringType);				
				m += p;
				if (i < params.length - 1) {
					m +=  ",";
				}
			} 

			m += ")>";

			//m+="";
			return m;
		} catch (Exception e) {
		}
    	
    	return null;
    }
    
    public static String getTypeSootSignature(IType type){
    	String name = type.getTypeQualifiedName();
		String pkg = type.getPackageFragment().getElementName();
		String signature = pkg+"."+name;
		return signature;
    }
    
    
    public static String getFieldSignature(IField field){    	
    	try{
    		IType declaringType = field.getDeclaringType();
    		String classname = declaringType.getFullyQualifiedName();						 
    		//classname = JDTUtils.getResolvedTypeName(classname, declaringType);		
    		String type = field.getTypeSignature();
    		type = JDTUtils.getResolvedTypeName(type, declaringType);
    		
    		//if the declaration is a generic type, then unfortunately, the type will
    		//be null, but actually it is of java.lang.Object type
    		if(type==null){
    			type = "java.lang.Object";
    		}
    		
    		String sig = "<"+classname + ": " + type + " " +field.getElementName()+">";
    		return sig;
    	}catch(Exception e){
    		
    	}
    	
		return "";
    }
    
    public static String getLineText(ITextEditor editor,int line,boolean includeLineLabel,boolean keepHeadBlanks){
    	try{
    		IDocumentProvider provider = editor.getDocumentProvider();
        	IDocument doc = provider.getDocument(editor.getEditorInput());
        	IRegion region = doc.getLineInformation(line);
     		int offset = region.getOffset();
     		int length = region.getLength();
     		
     		String text = doc.get(offset,length); 
     		
     		if(keepHeadBlanks)
     			text = text.replaceAll("\t", "  ");
     		else
     			text = text.trim();
     		
     		if(includeLineLabel){
     			String linestr = "" + (line+1);
     			while(linestr.length()<5){
     				linestr += " ";
     			}
     			
     			
     			return linestr+": "+text;
     		}
     		else{
     			return text;
     		}
    	}
    	catch(Exception e){
    		
    	}
		
    	return "";
    }
    
    
    public static String getSelectedText(ITextEditor editor, ITextSelection selection) 
    	throws Exception {
    	
    	IDocumentProvider provider = editor.getDocumentProvider();
    	IDocument doc = provider.getDocument(editor.getEditorInput());

    	return doc.get(selection.getOffset(),selection.getLength());
    }   
    
    
    public static IMethod getEnclosingMethod(ITextEditor editor,int line)
    							throws JavaModelException{
    	ICompilationUnit cunit = getInputAsCompilationUnit(editor);
    	reconcile(cunit);
    	
    	int startOffset = 0;
		int endOffset = 0;
		
		try{
			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument doc = provider.getDocument(editor.getEditorInput());
			IRegion region = doc.getLineInformation(line);
			
			startOffset = region.getOffset();
			endOffset = startOffset + region.getLength();
		}
		catch(Exception e){}
    	
		
		for(int i=startOffset;i<endOffset;i++){
			IJavaElement ref = cunit.getElementAt(i);
			if(ref!=null){
				IMethod m = findMethod(ref);
				if(m!=null)
					return m;
			}
		}
		
    	return null; 
    }
    
    public static int getMethodStartLine(ITextEditor editor,IMethod method){
    	try{
    		IDocumentProvider provider = editor.getDocumentProvider();
    		IDocument doc = provider.getDocument(editor.getEditorInput());
    		//ISourceRange range = method.getSourceRange();
    		ISourceRange range = method.getNameRange();
    		if(range!=null){
    			int offset = range.getOffset();
    			return doc.getLineOfOffset(offset);
    		}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return -1;
    }
    
    
    private static IMethod findMethod(IJavaElement elmt){
    	while(elmt!=null){
    		if(elmt instanceof IMethod){
    			return (IMethod)elmt;    			
    		}
    		
    		elmt = elmt.getParent();
    	}
    	
    	return null;
    }   
    
    
    public static ICompilationUnit getCompliationUnit(ITextEditor editor){
    	IEditorInput input = editor.getEditorInput();
		ICompilationUnit icompilationUnit = null;
		if(input instanceof ICompilationUnit){
			icompilationUnit = (ICompilationUnit)input;
		}
		else{
			FileEditorInput fileEditorInput = (FileEditorInput)input;
			IFile ifile = fileEditorInput.getFile();
			icompilationUnit = JavaCore.createCompilationUnitFrom(ifile);
		}
		
		return icompilationUnit;
    }
}

/*
 private IJavaElement[] codeResolve(ICompilationUnit input, ITextSelection selection)
	    throws JavaModelException {
		if (input instanceof ICodeAssist) {
			IJavaElement[] elements = ((ICodeAssist) input).codeSelect(
					selection.getOffset(), selection.getLength());
			if (elements != null && elements.length > 0)
				return elements;
		}
		return EMPTY_RESULT;
	} */


/*
package de.loskutov.bco.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.ITextSelection;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import de.loskutov.bco.BytecodeOutlinePlugin;
import de.loskutov.bco.asm.DecompiledClass;


public class JdtUtils {
   // package separator in bytecode notation 
    private static final char PACKAGE_SEPARATOR = '/';
    // type name separator (for inner types) in bytecode notation 
    private static final char TYPE_SEPARATOR = '$';
  

    public static IJavaElement getMethod(IParent parent, String signature){
        try {
            IJavaElement[] children = parent.getChildren();
            for (int i = 0; i < children.length; i++) {
                IJavaElement javaElement = children[i];
                switch (javaElement.getElementType()) {
                    case IJavaElement.INITIALIZER :
                        // fall through
                    case IJavaElement.METHOD :
                        if(signature.equals(getMethodSignature(javaElement))){
                            return javaElement;
                        }
                        break;
                    default :
                        break;
                }
                if(javaElement instanceof IParent){
                    javaElement = getMethod((IParent) javaElement, signature);
                    if(javaElement != null){
                        return javaElement;
                    }
                }
            }
        } catch (JavaModelException e) {
            // just ignore it. Mostly caused by class files not on the class path
            // which is not a problem for us, but a big problem for JDT
        }
        return null;
    }

    
     * @param childEl
     * @return method signature, if given java element is either initializer or method,
     * otherwise returns null.
     
    public static String getMethodSignature(IJavaElement childEl) {
        String methodName = null;
        if (childEl.getElementType() == IJavaElement.INITIALIZER) {
            IInitializer ini = (IInitializer) childEl;
            try {
                if (Flags.isStatic(ini.getFlags())) {
                    methodName = "<clinit>()V";
                } else {
                    methodName = "<init>()";
                }
            } catch (JavaModelException e) {
                // this is compilation problem - don't show the message
                BytecodeOutlinePlugin.log(e, IStatus.WARNING);
            }
        } else if (childEl.getElementType() == IJavaElement.METHOD) {
            IMethod iMethod = (IMethod) childEl;
            try {
                methodName = createMethodSignature(iMethod);
            } catch (JavaModelException e) {
                // this is compilation problem - don't show the message
                BytecodeOutlinePlugin.log(e, IStatus.WARNING);
            }
        }
        return methodName;
    }

    public static String createMethodSignature(IMethod iMethod)
        throws JavaModelException {
        StringBuffer sb = new StringBuffer();

        // Eclipse put class name as constructor name - we change it!
        if (iMethod.isConstructor()) {
            sb.append("<init>"); //$NON-NLS-1$
        } else {
            sb.append(iMethod.getElementName());
        }

        if (iMethod.isBinary()) { // iMethod instanceof BinaryMember
            // binary info should be full qualified
            return sb.append(iMethod.getSignature()).toString();
        }

        // start method parameter descriptions list
        sb.append('(');
        IType declaringType = iMethod.getDeclaringType();
        String[] parameterTypes = iMethod.getParameterTypes();

       
         * For non - static inner classes bytecode constructor should contain as first
         * parameter the enclosing type instance, but in Eclipse AST there are no
         * appropriated parameter. So we need to create enclosing type signature and
         * add it as first parameter.
         
        if (iMethod.isConstructor() && isNonStaticInner(declaringType)) {
            // this is a very special case
            String typeSignature = getTypeSignature(getFirstAncestor(declaringType));
            if(typeSignature != null) {
                String [] newParams = new String [parameterTypes.length + 1];
                newParams[0] = typeSignature;
                System.arraycopy(parameterTypes, 0, newParams, 1, parameterTypes.length);
                parameterTypes = newParams;
            }
        }

        // doSomething(Lgenerics/DummyForAsmGenerics;)Lgenerics/DummyForAsmGenerics;
        for (int i = 0; i < parameterTypes.length; i++) {
            String resolvedType = getResolvedType(parameterTypes[i], declaringType);
            if(resolvedType != null && resolvedType.length() > 0){
                sb.append(resolvedType);
            } else {
                // this is a generic type
                appendGenericType(sb, iMethod, parameterTypes[i]);
            }
        }
        sb.append(')');

        // continue here with adding resolved return type
        String returnType = iMethod.getReturnType();
        String resolvedType = getResolvedType(returnType, declaringType);
        if(resolvedType != null && resolvedType.length() > 0){
            sb.append(resolvedType);
        } else {
            // this is a generic type
            appendGenericType(sb, iMethod, returnType);
        }

        return sb.toString();
    }

    
     * @param type
     * @return full qualified, resolved type name in bytecode notation
    
    private static String getTypeSignature(IType type) {
        if(type == null){
            return null;
        }
        
         * getFullyQualifiedName() returns name, where package separator is '.',
         * but we need '/' for bytecode. The hack with ',' is to use a character
         * which is not allowed as Java char to be sure not to replace too much
        
        String name = type.getFullyQualifiedName(',');
        // replace package separators
        name = name.replace(Signature.C_DOT, PACKAGE_SEPARATOR);
        // replace class separators
        name = name.replace(',', TYPE_SEPARATOR);
        return Signature.C_RESOLVED + name + Signature.C_SEMICOLON;
    }

    private static void appendGenericType(StringBuffer sb, IMethod iMethod,
        String unresolvedType) throws JavaModelException{
        IType declaringType = iMethod.getDeclaringType();

        // unresolvedType is here like "QA;" => we remove "Q" and ";"
        if(unresolvedType.length() < 3){
            // ???? something wrong here ....
            sb.append(unresolvedType);
            return;
        }
        unresolvedType = unresolvedType.substring(1, unresolvedType.length() - 1);

        ITypeParameter typeParameter = iMethod.getTypeParameter(unresolvedType);
        if(typeParameter == null || !typeParameter.exists()){
            typeParameter = declaringType.getTypeParameter(unresolvedType);
        }

        String[] bounds = typeParameter.getBounds();
        if(bounds.length == 0){
            sb.append("Ljava/lang/Object;");
        } else {
            for (int i = 0; i < bounds.length; i++) {
                String simplyName = bounds[i];
                simplyName =  Signature.C_UNRESOLVED + simplyName + Signature.C_NAME_END;
                String resolvedType = getResolvedType(simplyName, declaringType);
                sb.append(resolvedType);
            }
        }
    }

   
     * @param typeToResolve
     * @param declaringType
     * @return full qualified "bytecode formatted" type
     * @throws JavaModelException
   
    private static String getResolvedType(String typeToResolve,
        IType declaringType) throws JavaModelException {
        StringBuffer sb = new StringBuffer();
        int arrayCount = Signature.getArrayCount(typeToResolve);
        // test which letter is following - Q or L are for reference types
        boolean isPrimitive = isPrimitiveType(typeToResolve.charAt(arrayCount));
        if (isPrimitive) {
            // simply add whole string (probably with array chars like [[I etc.)
            sb.append(typeToResolve);
        } else {
            boolean isUnresolvedType = isUnresolvedType(typeToResolve, arrayCount);
            if(!isUnresolvedType) {
                sb.append(typeToResolve);
            } else {
                // we need resolved types
                String resolved = getResolvedTypeName(typeToResolve, declaringType);
                if(resolved != null) {
                    while (arrayCount > 0) {
                        sb.append(Signature.C_ARRAY);
                        arrayCount--;
                    }
                    sb.append(Signature.C_RESOLVED);
                    sb.append(resolved);
                    sb.append(Signature.C_SEMICOLON);
                }
            }
        }
        return sb.toString();
    }

   
     * Copied and modified from JavaModelUtil. Resolves a type name in the context of the
     * declaring type.
     * @param refTypeSig the type name in signature notation (for example 'QVector') this
     * can also be an array type, but dimensions will be ignored.
     * @param declaringType the context for resolving (type where the reference was made
     * in)
     * @return returns the fully qualified <b>bytecode </b> type name or build-in-type
     * name. if a unresoved type couldn't be resolved null is returned

    private static String getResolvedTypeName(String refTypeSig,
        IType declaringType) throws JavaModelException {

       the whole method is copied from JavaModelUtil.getResolvedTypeName(...).
         * The problem is, that JavaModelUtil uses '.' to separate package
         * names, but we need '/' -> see JavaModelUtil.concatenateName() vs
         * JdtUtils.concatenateName()
         
        int arrayCount = Signature.getArrayCount(refTypeSig);
        if (isUnresolvedType(refTypeSig, arrayCount)) {
            String name= ""; //$NON-NLS-1$
            int bracket= refTypeSig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
            if (bracket > 0) {
                name= refTypeSig.substring(arrayCount + 1, bracket);
            } else {
                int semi= refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
                if (semi == -1) {
                    throw new IllegalArgumentException();
                }
                name= refTypeSig.substring(arrayCount + 1, semi);
            }
            String[][] resolvedNames= declaringType.resolveType(name);
            if (resolvedNames != null && resolvedNames.length > 0) {
                return concatenateName(resolvedNames[0][0], resolvedNames[0][1]);
            }
            return null;
        }
        return refTypeSig.substring(arrayCount);// Signature.toString(substring);
    }


    


    
   
     * Test which letter is following - Q or L are for reference types
     * @param first
     * @return true, if character is not a simbol for reference types
     
    private static boolean isPrimitiveType(char first) {
        return (first != Signature.C_RESOLVED && first != Signature.C_UNRESOLVED);
    }

   
     * @param childEl may be null
     * @return first ancestor with IJavaElement.TYPE element type, or null
   
    public static IType getEnclosingType(IJavaElement childEl) {
        if (childEl == null) {
            return null;
        }
        return (IType) childEl.getAncestor(IJavaElement.TYPE);
    }

  
     * @param cf
     * @param dc
     * @return inner type which has the same name as the given string, or null
   
    public static IClassFile getInnerType(IClassFile cf, DecompiledClass dc,
        String typeSignature) {
        if(typeSignature.endsWith(";")){
            typeSignature = typeSignature.substring(0, typeSignature.length()-1);
            if(typeSignature.startsWith("L")){
                typeSignature = typeSignature.substring(1, typeSignature.length());
            }
        }
       
         * For inner and anonymous classes from the blocks or methods
         * getFullyQualifiedName() does not work if class was compiled with 1.5
         * and will never match the fullTypeName...
         * I'm not sure if it is intended or if it is a bug
         * in Eclipse: instead of A$1B we get A$B for B class from a method in A
         *
         * NB: for binary types without source attachment the method elements doesn't
         * contain source and therefore could not resolve child elements.
         * So the search for local types will never work...
         *
         * Therefore we do not use Eclipse API and use ClassNode->InnerClassNode elements
        
        ClassNode cn = dc.getClassNode();
        List*<InnerClassNode>* innerClasses = cn.innerClasses;

        for (int i = 0; i < innerClasses.size(); i++) {
            InnerClassNode in = (InnerClassNode) innerClasses.get(i);
            if(typeSignature.equals(in.name)){
                int idx = typeSignature.lastIndexOf(PACKAGE_SEPARATOR);
                String className = typeSignature;
                if (idx > 0) {
                    className = typeSignature.substring(idx + 1, typeSignature.length());
                }
                className += ".class";
                return cf.getType().getPackageFragment().getClassFile(className);
            }
        }
        return null;
    }

   
     * Modified copy from org.eclipse.jdt.internal.ui.actions.SelectionConverter
     * @param input
     * @param selection
     * @return null, if selection is null or could not be resolved to java element
     * @throws JavaModelException
     
    public static IJavaElement getElementAtOffset(IJavaElement input,
        ITextSelection selection) throws JavaModelException {
        if(selection == null){
            return null;
        }
        ICompilationUnit workingCopy = null;
        if (input instanceof ICompilationUnit) {
            workingCopy = (ICompilationUnit) input;
            // be in-sync with model
            // instead of using internal JavaModelUtil.reconcile(workingCopy);
            synchronized(workingCopy)  {
                workingCopy.reconcile(
                    ICompilationUnit.NO_AST,
                    false don't force problem detection ,
                    null use primary owner 
                    null no progress monitor );
            }
            IJavaElement ref = workingCopy.getElementAt(selection.getOffset());
            if (ref != null) {
                return ref;
            }
        } else if (input instanceof IClassFile) {
            IClassFile iClass = (IClassFile) input;
            IJavaElement ref = iClass.getElementAt(selection.getOffset());
            if (ref != null) {
                // If we are in the inner class, try to refine search result now
                if(ref instanceof IType){
                    IType type = (IType) ref;
                    IClassFile classFile = type.getClassFile();
                    if(classFile != iClass){
                       
                         * WORKAROUND it seems that source range for constructors from
                         * bytecode with source attached from zip files is not computed
                         * in Eclipse (SourceMapper returns nothing useful).
                         * Example: HashMap$Entry class with constructor
                         * <init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Entry;)V
                         * We will get here at least the inner class...
                         * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=137847
                         
                        ref = classFile.getElementAt(selection.getOffset());
                    }
                }
                return ref;
            }
        }
        return null;
    }

  
     * Modified copy from JavaModelUtil.
     * @param javaElt
     * @return true, if corresponding java project has compiler setting to generate
     * bytecode for jdk 1.5 and above
    
    public static boolean is50OrHigher(IJavaElement javaElt) {
        IJavaProject project = javaElt.getJavaProject();
        String option = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
        boolean result = JavaCore.VERSION_1_5.equals(option);
        if(result){
            return result;
        }
        // probably > 1.5?
        result = JavaCore.VERSION_1_4.equals(option);
        if(result){
            return false;
        }
        result = JavaCore.VERSION_1_3.equals(option);
        if(result){
            return false;
        }
        result = JavaCore.VERSION_1_2.equals(option);
        if(result){
            return false;
        }
        result = JavaCore.VERSION_1_1.equals(option);
        if(result){
            return false;
        }
        // unknown = > 1.5
        return true;
    }

  
     * Cite: jdk1.1.8/docs/guide/innerclasses/spec/innerclasses.doc10.html: For the sake
     * of tools, there are some additional requirements on the naming of an inaccessible
     * class N. Its bytecode name must consist of the bytecode name of an enclosing class
     * (the immediately enclosing class, if it is a member), followed either by `$' and a
     * positive decimal numeral chosen by the compiler, or by `$' and the simple name of
     * N, or else by both (in that order). Moreover, the bytecode name of a block-local N
     * must consist of its enclosing package member T, the characters `$1$', and N, if the
     * resulting name would be unique.
     * <br>
     * Note, that this rule was changed for static blocks after 1.5 jdk.
     * @param javaElement
     * @return simply element name
   
    public static String getElementName(IJavaElement javaElement) {
        if (isAnonymousType(javaElement)) {
            IType anonType = (IType) javaElement;
            List allAnonymous = new ArrayList();
           
             * in order to resolve anon. class name we need to know about all other
             * anonymous classes in declaring class, therefore we need to collect all here
            
            collectAllAnonymous(allAnonymous, anonType);
            int idx = getAnonimousIndex(anonType, (IType[]) allAnonymous
                .toArray(new IType[allAnonymous.size()]));
            return Integer.toString(idx);
        }
        String name = javaElement.getElementName();
        if (isLocal(javaElement)) {
          
             * Compiler have different naming conventions for inner non-anon. classes in
             * static blocks or any methods, this difference was introduced with 1.5 JDK.
             * The problem is, that we could have projects with classes, generated
             * with both 1.5 and earlier settings. One could not see on particular
             * java element, for which jdk version the existing bytecode was generated.
             * If we could have a *.class file, but we are just searching for one...
             * So there could be still a chance, that this code fails, if java element
             * is not compiled with comiler settings from project, but with different
            
            if(is50OrHigher(javaElement)){
                name = "1" + name; // compiler output changed for > 1.5 code
            } else {
                name = "1$" + name; // see method comment, this was the case for older code
            }
        }

        if (name.endsWith(".java")) { //$NON-NLS-1$
            name = name.substring(0, name.lastIndexOf(".java")); //$NON-NLS-1$
        } else if (name.endsWith(".class")) { //$NON-NLS-1$
            name = name.substring(0, name.lastIndexOf(".class")); //$NON-NLS-1$
        }
        return name;
    }

   
     * @param javaElement
     * @return null, if javaElement is top level class
    
    static IType getFirstAncestor(IJavaElement javaElement) {
        IJavaElement parent = javaElement;
        if (javaElement.getElementType() == IJavaElement.TYPE) {
            parent = javaElement.getParent();
        }
        if (parent != null) {
            return (IType) parent.getAncestor(IJavaElement.TYPE);
        }
        return null;
    }

    static IJavaElement getLastAncestor(IJavaElement javaElement,
        int elementType) {
        IJavaElement lastFound = null;
        if (elementType == javaElement.getElementType()) {
            lastFound = javaElement;
        }
        IJavaElement parent = javaElement.getParent();
        if (parent == null) {
            return lastFound;
        }
        IJavaElement ancestor = parent.getAncestor(elementType);
        if (ancestor != null) {
            return getLastAncestor(ancestor, elementType);
        }
        return lastFound;
    }

   
     * @param javaElement
     * @return distance to given ancestor, 0 if it is the same, -1 if ancestor with type
     * IJavaElement.TYPE does not exist
    
    static int getTopAncestorDistance(IJavaElement javaElement,
        IJavaElement topAncestor) {
        if (topAncestor == javaElement) {
            return 0;
        }
        IJavaElement ancestor = getFirstAncestor(javaElement);
        if (ancestor != null) {
            return 1 + getTopAncestorDistance(ancestor, topAncestor);
        }
        // this is not possible, if ancestor exist - which return value we should use?
        return -1;
    }

   
     * @param javaElement
     * @return first non-anonymous ancestor
    
    static IJavaElement getFirstNonAnonymous(IJavaElement javaElement,
        IJavaElement topAncestor) {
        if (javaElement.getElementType() == IJavaElement.TYPE
            && !isAnonymousType(javaElement)) {
            return javaElement;
        }
        IJavaElement parent = javaElement.getParent();
        if (parent == null) {
            return topAncestor;
        }
        IJavaElement ancestor = parent.getAncestor(IJavaElement.TYPE);
        if (ancestor != null) {
            return getFirstNonAnonymous(ancestor, topAncestor);
        }
        return topAncestor;
    }

   
     * @param javaElement
     * @return true, if given element is anonymous inner class
   
    private static boolean isAnonymousType(IJavaElement javaElement) {
        try {
            return javaElement instanceof IType && ((IType)javaElement).isAnonymous();
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
        }
        return false;
    }

    
     * @param innerType should be inner type.
     * @return true, if given element is inner class from initializer block or method body
   
    private static boolean isLocal(IJavaElement innerType) {
        try {
            return innerType instanceof IType && ((IType)innerType).isLocal();
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
        }
        return false;
    }

   
     * @param type
     * @return true, if given element is non static inner class
     * @throws JavaModelException
    
    private static boolean isNonStaticInner(IType type) throws JavaModelException {
        if(type.isMember()){
            return !Flags.isStatic(type.getFlags());
        }
        return false;
    }

   
     * @param innerType should be inner type.
     * @return true, if given element is inner class from initializer block
     
    private static boolean isInnerFromBlock(IType type) {
        try {
            IJavaElement ancestor = type.getAncestor(IJavaElement.INITIALIZER);
            return type.isLocal() && ancestor != null;
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
        }
        return false;
    }

    
     * @param javaElement
     * @return absolute path of generated bytecode package for given element
     * @throws JavaModelException
   
    private static String getPackageOutputPath(IJavaElement javaElement)
        throws JavaModelException {
        String dir = ""; //$NON-NLS-1$
        if (javaElement == null) {
            return dir;
        }

        IJavaProject project = javaElement.getJavaProject();

        if (project == null) {
            return dir;
        }
        // default bytecode location
        IPath path = project.getOutputLocation();

        IResource resource = javaElement.getUnderlyingResource();
        if (resource == null) {
            return dir;
        }
        // resolve multiple output locations here
        if (project.exists() && project.getProject().isOpen()) {
            IClasspathEntry entries[] = project.getRawClasspath();
            for (int i = 0; i < entries.length; i++) {
                IClasspathEntry classpathEntry = entries[i];
                if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    IPath outputPath = classpathEntry.getOutputLocation();
                    if (outputPath != null
                        && classpathEntry.getPath().isPrefixOf(
                            resource.getFullPath())) {
                        path = outputPath;
                        break;
                    }
                }
            }
        }

        if (path == null) {
            // check the default location if not already included
            IPath def = project.getOutputLocation();
            if (def != null && def.isPrefixOf(resource.getFullPath())){
                path = def;
            }
        }

        if(path == null){
            return dir;
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        if (!project.getPath().equals(path)) {
            IFolder outputFolder = workspace.getRoot().getFolder(path);
            if (outputFolder != null) {
                // linked resources will be resolved here!
                IPath rawPath = outputFolder.getRawLocation();
                if (rawPath != null) {
                    path = rawPath;
                }
            }
        } else {
            path = project.getProject().getLocation();
        }

        // here we should resolve path variables,
        // probably existing at first place of path
        IPathVariableManager pathManager = workspace.getPathVariableManager();
        path = pathManager.resolvePath(path);

        if (path == null) {
            return dir;
        }

        if (isPackageRoot(project, resource)) {
            dir = path.toOSString();
        } else {
            String packPath = EclipseUtils.getJavaPackageName(javaElement)
                .replace(Signature.C_DOT, PACKAGE_SEPARATOR);
            dir = path.append(packPath).toOSString();
        }
        return dir;
    }

    
     * @param project
     * @param pack
     * @return true if 'pack' argument is package root
     * @throws JavaModelException
    
    private static boolean isPackageRoot(IJavaProject project, IResource pack)
        throws JavaModelException {
        boolean isRoot = false;
        if (project == null || pack == null) {
            return isRoot;
        }
        IPackageFragmentRoot root = project.getPackageFragmentRoot(pack);
        IClasspathEntry clPathEntry = null;
        if (root != null) {
            clPathEntry = root.getRawClasspathEntry();
        }
        isRoot = clPathEntry != null;
        return isRoot;
    }

   
     * Works only for eclipse - managed/generated bytecode, ergo not with imported
     * classes/jars
     * @param javaElement
     * @return full os-specific file path to .class resource, containing given element
    
    public static String getByteCodePath(IJavaElement javaElement) {
        if (javaElement == null) {
            return "";//$NON-NLS-1$
        }
        String packagePath = ""; //$NON-NLS-1$
        try {
            packagePath = getPackageOutputPath(javaElement);
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
            return "";
        }
        IJavaElement ancestor = getLastAncestor(javaElement, IJavaElement.TYPE);
        StringBuffer sb = new StringBuffer(packagePath);
        sb.append(File.separator);
        sb.append(getClassName(javaElement, ancestor));
        sb.append(".class"); //$NON-NLS-1$
        return sb.toString();
    }

   
     * @param javaElement
     * @return new generated input stream for given element bytecode class file, or null
     * if class file cannot be found or this element is not from java source path
    
    public static InputStream createInputStream(IJavaElement javaElement) {
        IClassFile classFile = (IClassFile) javaElement
            .getAncestor(IJavaElement.CLASS_FILE);
        InputStream is = null;

        // existing read-only class files
        if (classFile != null) {
            IJavaElement jarParent = classFile.getParent();
            // TODO dirty hack to be sure, that package is from jar -
            // because JarPackageFragment is not public class, we cannot
            // use instanceof here
            boolean isJar = jarParent != null
                && jarParent.getClass().getName()
                    .endsWith("JarPackageFragment"); //$NON-NLS-1$
            if (isJar) {
                is = createStreamFromJar(classFile);
            } else {
                is = createStreamFromClass(classFile);
            }
        } else {
            // usual eclipse - generated bytecode

            boolean inJavaPath = isOnClasspath(javaElement);
            if (!inJavaPath) {
                return null;
            }
            String classPath = getByteCodePath(javaElement);

            try {
                is = new FileInputStream(classPath);
            } catch (FileNotFoundException e) {
                // if autobuild is disabled, we get tons of this errors.
                // but I think we cannot ignore them, therefore WARNING and not
                // ERROR status
                BytecodeOutlinePlugin.log(e, IStatus.WARNING);
            }
        }
        return is;
    }

    
     * Creates stream from external class file from Eclipse classpath (means, that this
     * class file is read-only)
     * @param classFile
     * @return new generated input stream from external class file, or null, if class file
     * for this element cannot be found
    
    private static InputStream createStreamFromClass(IClassFile classFile) {
        IResource underlyingResource = null;
        try {
            // to tell the truth, I don't know why that different methods
            // are not working in a particular case. But it seems to be better
            // to use getResource() with non-java elements (not in model)
            // and getUnderlyingResource() with java elements.
            if (classFile.exists()) {
                underlyingResource = classFile.getUnderlyingResource();
            } else {
                // this is a class file that is not in java model
                underlyingResource = classFile.getResource();
            }
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
            return null;
        }
        IPath rawLocation = underlyingResource.getRawLocation();
        // here we should resolve path variables,
        // probably existing at first place of "rawLocation" path
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IPathVariableManager pathManager = workspace.getPathVariableManager();
        rawLocation = pathManager.resolvePath(rawLocation);
        try {
            return new FileInputStream(rawLocation.toOSString());
        } catch (FileNotFoundException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
        }
        return null;
    }

   
     * Creates stream from external class file that is stored in jar file
     * @param classFile
     * @param javaElement
     * @return new generated input stream from external class file that is stored in jar
     * file, or null, if class file for this element cannot be found
    
    private static InputStream createStreamFromJar(IClassFile classFile) {
        IPath path = null;
        IResource resource = classFile.getResource();
        // resource == null => this is a external archive
        if (resource != null) {
            path = resource.getRawLocation();
        } else {
            path = classFile.getPath();
        }
        if (path == null) {
            return null;
        }
        // here we should resolve path variables,
        // probably existing at first place of path
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IPathVariableManager pathManager = workspace.getPathVariableManager();
        path = pathManager.resolvePath(path);

        JarFile jar = null;
        try {
            jar = new JarFile(path.toOSString());
        } catch (IOException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
            return null;
        }
        String fullClassName = getFullBytecodeName(classFile);
        if (fullClassName == null) {
            return null;
        }
        JarEntry jarEntry = jar.getJarEntry(fullClassName);
        if (jarEntry != null) {
            try {
                return jar.getInputStream(jarEntry);
            } catch (IOException e) {
                BytecodeOutlinePlugin.log(e, IStatus.ERROR);
            }
        }
        return null;
    }

    private static boolean isOnClasspath(IJavaElement javaElement) {
        IJavaProject project = javaElement.getJavaProject();
        if (project != null) {
            boolean result = project.isOnClasspath(javaElement);
            return result;
        }
        return false;
    }

   
     * @param classFile
     * @return full qualified bytecode name of given class
   
    public static String getFullBytecodeName(IClassFile classFile) {
        IPackageFragment packageFr = (IPackageFragment) classFile
            .getAncestor(IJavaElement.PACKAGE_FRAGMENT);
        if (packageFr == null) {
            return null;
        }
        String packageName = packageFr.getElementName();
        // switch to java bytecode naming conventions
        packageName = packageName.replace(Signature.C_DOT, PACKAGE_SEPARATOR);

        String className = classFile.getElementName();
        if (packageName != null && packageName.length() > 0) {
            return packageName + PACKAGE_SEPARATOR + className;
        }
        return className;
    }

   
     * @param javaElement
     * @param topAncestor
     * @param sb
    
    private static String getClassName(IJavaElement javaElement,
        IJavaElement topAncestor) {
        StringBuffer sb = new StringBuffer();
        if (!javaElement.equals(topAncestor)) {
            int elementType = javaElement.getElementType();
            if(elementType == IJavaElement.FIELD
                || elementType == IJavaElement.METHOD
                 || elementType == IJavaElement.INITIALIZER) {
                // it's field or method
                javaElement = getFirstAncestor(javaElement);
            } else {
                boolean is50OrHigher = is50OrHigher(javaElement);
                if (!is50OrHigher &&
                    (isAnonymousType(javaElement) || isLocal(javaElement))) {
                    // it's inner type
                    sb.append(getElementName(topAncestor));
                    sb.append(TYPE_SEPARATOR);
                } else {
                   
                     * TODO there is an issue with < 1.5 compiler setting and with inner
                     * classes with the same name but defined in different methods in the same
                     * source file. Then compiler needs to generate *different* content for
                     *  A$1$B and A$1$B, which is not possible so therefore compiler generates
                     *  A$1$B and A$2$B. The naming order is the source range order of inner
                     *  classes, so the first inner B class will get A$1$B and the second
                     *  inner B class A$2$B etc.
                    

                    // override top ancestor with immediate ancestor
                    topAncestor = getFirstAncestor(javaElement);
                    while (topAncestor != null) {
                        sb.insert(0, getElementName(topAncestor) + TYPE_SEPARATOR);
                        topAncestor = getFirstAncestor(topAncestor);
                    }
                }
            }
        }
        sb.append(getElementName(javaElement));
        return sb.toString();
    }

   
     * Collect all anonymous classes which are on the same "name shema level"
     * as the given element for the compiler. The list could contain different set of
     * elements for the same source code, depends on the compiler and jdk version
     * @param list for the found anon. classes, elements instanceof IType.
     * @param anonType the anon. type
    
    private static void collectAllAnonymous(List list, IType anonType) {
        
         * For JDK >= 1.5 in Eclipse 3.1+ the naming shema for nested anonymous
         * classes was changed from A$1, A$2, A$3, A$4, ..., A$n
         * to A$1, A$1$1, A$1$2, A$1$2$1, ..., A$2, A$2$1, A$2$2, ..., A$x$y
        
        boolean allowNested = ! is50OrHigher(anonType);

        IParent declaringType;
        if(allowNested) {
            declaringType = (IType) getLastAncestor(anonType, IJavaElement.TYPE);
        } else {
            declaringType = anonType.getDeclaringType();
        }

        try {
            collectAllAnonymous(list, declaringType, allowNested);
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
        }
    }

   
     * Traverses down the children tree of this parent and collect all child anon. classes
     * @param list
     * @param parent
     * @param allowNested true to search in IType child elements too
     * @throws JavaModelException
     
    private static void collectAllAnonymous(List list, IParent parent,
        boolean allowNested) throws JavaModelException {
        IJavaElement[] children = parent.getChildren();
        for (int i = 0; i < children.length; i++) {
            IJavaElement childElem = children[i];
            if (isAnonymousType(childElem)) {
                list.add(childElem);
            }
            if (childElem instanceof IParent) {
                if(allowNested || !(childElem instanceof IType)) {
                    collectAllAnonymous(list, (IParent) childElem, allowNested);
                }
            }
        }
    }

   
     * @param anonType
     * @param anonymous
     * @return the index of given java element in the anon. classes list, which was used
     *  by compiler to generate bytecode name for given element. If the given type is not
     *  in the list, then return value is '-1'
     
    private static int getAnonimousIndex(IType anonType, IType[] anonymous) {
        sortAnonymous(anonymous, anonType);
        for (int i = 0; i < anonymous.length; i++) {
            if (anonymous[i] == anonType) {
                // +1 because compiler starts generated classes always with 1
                return i + 1;
            }
        }
        return -1;
    }

  
     * Sort given anonymous classes in order like java compiler would generate output
     * classes, in context of given anonymous type
     * @param anonymous
     
    private static void sortAnonymous(IType[] anonymous, IType anonType) {
        SourceOffsetComparator sourceComparator = new SourceOffsetComparator();
        Arrays.sort(anonymous, new AnonymClassComparator(
            anonType, sourceComparator));
    }

   
     * 1) from instance init 2) from deepest inner from instance init (deepest first) 3) from
     * static init 4) from deepest inner from static init (deepest first) 5) from deepest inner
     * (deepest first) 6) regular anon classes from main class
     *
     * <br>
     * Note, that nested inner anon. classes which do not have different non-anon. inner class
     * ancestors, are compiled in they nesting order, opposite to rule 2)
     *
     * @param javaElement
     * @return priority - lesser mean wil be compiled later, a value > 0
     * @throws JavaModelException
    
    static int getAnonCompilePriority(IJavaElement javaElement,
        IJavaElement firstAncestor, IJavaElement topAncestor, boolean is50OrHigher) {

        // search for initializer block
        IJavaElement lastAncestor = getLastAncestor(
            javaElement, IJavaElement.INITIALIZER);
        // test is for anon. classes from initializer blocks
        if (lastAncestor != null) {
            IInitializer init = (IInitializer) lastAncestor;
            int initFlags = 0;
            try {
                initFlags = init.getFlags();
            } catch (JavaModelException e) {
                BytecodeOutlinePlugin.log(e, IStatus.ERROR);
            }

            if(is50OrHigher){
                if (!Flags.isStatic(initFlags)) {
                    if (firstAncestor == topAncestor) {
                        return 10; // instance init
                    }
                    return 9; // from inner from instance init
                }

                if (firstAncestor == topAncestor) {
                    return 8; // class init
                }
                return 7; // from inner from class init
            }

            
             * crazy 1.4 logic
             
            if (Flags.isStatic(initFlags)) {
                return 10;
            }
            IJavaElement firstNonAnon = getFirstNonAnonymous(javaElement, topAncestor);
            if (isStatic((IMember)firstNonAnon)) {
                return 9;
            }

            return 6; // class init
        }
        // from inner from main type
        if(!is50OrHigher){
           
             * crazy 1.4 logic
           
            int topFlags = 0;
            IJavaElement firstNonAnon = getFirstNonAnonymous(javaElement, topAncestor);
            try {
                topFlags = ((IType)firstNonAnon).getFlags();
            } catch (JavaModelException e) {
                BytecodeOutlinePlugin.log(e, IStatus.ERROR);
            }
            if (Flags.isStatic(topFlags)) {
                return 8;
            }

            if (firstAncestor == topAncestor) {
                return 6; // regular anonyme classes
            }
            return 6; // the same prio as regular anonyme classes
        }

        // test for anon. classes from "regular" code
        if (firstAncestor == topAncestor) {
            return 5; // regular anonyme classes
        }
        return 6;
    }

    private static boolean isStatic(IMember firstNonAnon) {
        int topFlags = 0;
        try {
            topFlags = firstNonAnon.getFlags();
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.log(e, IStatus.ERROR);
        }
        return Flags.isStatic(topFlags);
    }

   
     * @param type
     * @return
     
    public static ClassLoader getClassLoader(IJavaElement type) {
        ClassLoader cl;

        IJavaProject javaProject = type.getJavaProject();
        List urls = new ArrayList();

        getClassURLs(javaProject, urls);

        if (urls.isEmpty()) {
            cl = JdtUtils.class.getClassLoader();
        } else {
            cl = new URLClassLoader((URL[]) urls.toArray(new URL[urls.size()]));
        }
        return cl;
    }

    private static void getClassURLs(IJavaProject javaProject, List urls) {
        IProject project = javaProject.getProject();
        IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();

        IClasspathEntry[] paths = null;
        IPath defaultOutputLocation = null;
        try {
            paths = javaProject.getResolvedClasspath(true);
            defaultOutputLocation = javaProject.getOutputLocation();
        } catch (JavaModelException e) {
            // don't show message to user neither log it
            // BytecodeOutlinePlugin.log(e, IStatus.ERROR);
        }
        if (paths != null) {
            IPath projectPath = javaProject.getProject().getLocation();
            for (int i = 0; i < paths.length; ++i) {
                IClasspathEntry cpEntry = paths[i];
                IPath p = null;
                if (cpEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    // filter out source container - there are unused for class
                    // search - add bytecode output location instead
                    p = cpEntry.getOutputLocation();
                    if (p == null) {
                        // default output used:
                        p = defaultOutputLocation;
                    }
                } else if (cpEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    String projName = cpEntry.getPath().toPortableString()
                        .substring(1);
                    IProject proj = workspaceRoot.getProject(projName);
                    IJavaProject projj = JavaCore.create(proj);
                    getClassURLs(projj, urls);
                    continue;
                } else {
                    p = cpEntry.getPath();
                }

                if (p == null) {
                    continue;
                }
                if (!p.toFile().exists()) {
                    // removeFirstSegments: remove project from relative path
                    p = projectPath.append(p.removeFirstSegments(1));
                    if (!p.toFile().exists()) {
                        continue;
                    }
                }
                try {
                    urls.add(p.toFile().toURL());
                } catch (MalformedURLException e) {
                    // don't show message to user
                    BytecodeOutlinePlugin.log(e, IStatus.ERROR);
                }
            }
        }
    }

   
     * Check if java element is an interface or abstract method or a method from
     * interface.
    
    public static boolean isAbstractOrInterface(IJavaElement javaEl) {
        if (javaEl == null) {
            return true;
        }
        boolean abstractOrInterface = false;
        try {
            switch (javaEl.getElementType()) {
                case IJavaElement.CLASS_FILE :
                    IClassFile classFile = (IClassFile) javaEl;
                    if(isOnClasspath(javaEl)) {
                        abstractOrInterface = classFile.isInterface();
                    } else {
                       this is the case for eclipse-generated class files.
                       if we do not perform the check in if, then we will have java model
                       exception on classFile.isInterface() call.
                    }
                    break;
                case IJavaElement.COMPILATION_UNIT :
                    ICompilationUnit cUnit = (ICompilationUnit) javaEl;
                    IType type = cUnit.findPrimaryType();
                    abstractOrInterface = type != null && type.isInterface();
                    break;
                case IJavaElement.TYPE :
                    abstractOrInterface = ((IType) javaEl).isInterface();
                    break;
                case IJavaElement.METHOD :
                    // test for "abstract" flag on method in a class
                    abstractOrInterface = Flags.isAbstract(((IMethod) javaEl)
                        .getFlags());
                    // "abstract" flags could be not exist on interface methods
                    if (!abstractOrInterface) {
                        IType ancestor = (IType) javaEl
                            .getAncestor(IJavaElement.TYPE);
                        abstractOrInterface = ancestor != null
                            && ancestor.isInterface();
                    }
                    break;
                default :
                    IType ancestor1 = (IType) javaEl
                        .getAncestor(IJavaElement.TYPE);
                    abstractOrInterface = ancestor1 != null
                        && ancestor1.isInterface();
                    break;
            }
        } catch (JavaModelException e) {
            // No point to log it here
            // BytecodeOutlinePlugin.log(e, IStatus.ERROR);
        }
        return abstractOrInterface;
    }

    static class SourceOffsetComparator implements Comparator {

       
         * First source occurence win.
         * @param o1 should be IType
         * @param o2 should be IType
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         
        public int compare(Object o1, Object o2) {
            IType m1 = (IType) o1;
            IType m2 = (IType) o2;
            int idx1, idx2;
            try {
                ISourceRange sr1 = m1.getSourceRange();
                ISourceRange sr2 = m2.getSourceRange();
                if (sr1 == null || sr2 == null) {
                    return 0;
                }
                idx1 = sr1.getOffset();
                idx2 = sr2.getOffset();
            } catch (JavaModelException e) {
                BytecodeOutlinePlugin.log(e, IStatus.ERROR);
                return 0;
            }
            if (idx1 < idx2) {
                return -1;
            } else if (idx1 > idx2) {
                return 1;
            }
            return 0;
        }
    }

    static class AnonymClassComparator implements Comparator {

        private IType topAncestorType;
        private SourceOffsetComparator sourceComparator;
        private boolean is50OrHigher;

       
         * @param javaElement
         * @param sourceComparator
         
        public AnonymClassComparator(IType javaElement,
            SourceOffsetComparator sourceComparator) {
            this.sourceComparator = sourceComparator;
            is50OrHigher = is50OrHigher(javaElement);
            topAncestorType = (IType) getLastAncestor(
                javaElement, IJavaElement.TYPE);
        }

        
         * If "deep" is the same, then source order win. 1) from instance init 2) from
         * deepest inner from instance init (deepest first) 3) from static init 4) from
         * deepest inner from static init (deepest first) 5) from deepest inner (deepest
         * first) 7) regular anon classes from main class
         *
         * <br>
         * Note, that nested inner anon. classes which do not have different
         * non-anon. inner class ancestors, are compiled in they nesting order, opposite
         * to rule 2)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
        
        public int compare(Object o1, Object o2) {
            if(o1 == o2){
                return 0;
            }
            IType m1 = (IType) o1;
            IType m2 = (IType) o2;
            // both have the same ancestor as immediate ancestor
            if (!is50OrHigher && isInnerFromBlock(m1) && isInnerFromBlock(m2)) {
                return sourceComparator.compare(o1, o2);
            }
            IJavaElement firstAncestor1 = getFirstAncestor(m1);
            IJavaElement firstAncestor2 = getFirstAncestor(m2);
            int compilePrio1 = getAnonCompilePriority(
                m1, firstAncestor1, topAncestorType, is50OrHigher);
            int compilePrio2 = getAnonCompilePriority(
                m2, firstAncestor2, topAncestorType, is50OrHigher);

//            System.out.println("" + compilePrio1 + " -> " + o1.toString().substring(0, 50));
//            System.out.println("" + compilePrio2 + " -> " + o2.toString().substring(0, 50));

            if (compilePrio1 > compilePrio2) {
                return -1;
            } else if (compilePrio1 < compilePrio2) {
                return 1;
            } else {
                firstAncestor1 = getFirstNonAnonymous(m1, topAncestorType);
                firstAncestor2 = getFirstNonAnonymous(m2, topAncestorType);

                if(firstAncestor1 == firstAncestor2){
                  
                     * for anonymous classes from same chain and same first common ancestor,
                     * the order is the definition order
                    
                    int topAncestorDistance1 = getTopAncestorDistance(
                        m1, topAncestorType);
                    int topAncestorDistance2 = getTopAncestorDistance(
                        m2, topAncestorType);
                    if (topAncestorDistance1 < topAncestorDistance2) {
                        return -1;
                    } else if (topAncestorDistance1 > topAncestorDistance2) {
                        return 1;
                    } else {
                        return sourceComparator.compare(o1, o2);
                    }
                }

//                if(!is50OrHigher && !isStatic(m1) && !isStatic(m2)){
//                    return sourceComparator.compare(o1, o2);
//                }

               
                 * for anonymous classes which have first non-common non-anonymous ancestor,
                 * the order is the reversed definition order
                 
                int topAncestorDistance1 = getTopAncestorDistance(
                    firstAncestor1, topAncestorType);
                int topAncestorDistance2 = getTopAncestorDistance(
                    firstAncestor2, topAncestorType);
                if (topAncestorDistance1 > topAncestorDistance2) {
                    return -1;
                } else if (topAncestorDistance1 < topAncestorDistance2) {
                    return 1;
                } else {
                    return sourceComparator.compare(o1, o2);
                }
            }
        }
    }
}*/

/*
public static String getJavaPackageName(IJavaElement resource) {
    String name = resource == null
        ? null : resource.getElementName(); //$NON-NLS-1$
    if (name == null) {
        return ""; //$NON-NLS-1$
    }
    int type = resource.getElementType();
    if (type == IJavaElement.PACKAGE_FRAGMENT
        || type == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
        return name;
    }
    IJavaElement ancestor = resource
        .getAncestor(IJavaElement.PACKAGE_FRAGMENT);
    if (ancestor != null) {
        return ancestor.getElementName();
    }
    return ""; //$NON-NLS-1$
}*/


