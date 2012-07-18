package com.conref.refactoring.splitlock.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.refactoring.splitlock.core.JavaCriticalSection.NoMatchingSootMethodException;
import com.conref.util.PathUtils;
import com.conref.util.WorkbenchHelper;

import soot.SootField;

public class JDTRewriter {
	private final class syncStmtVisitor extends ASTVisitor {
		@SuppressWarnings("unchecked")
		public boolean visit(SynchronizedStatement synstmt) {

			if (synstmt.getExpression() instanceof ThisExpression) {
				ASTNode node = synstmt;
				while (!(node instanceof MethodDeclaration)) {
					node = node.getParent();
				}
				String methodname = ((MethodDeclaration) node).getName()
						.toString();
				Set<SootField> blockField = (Set<SootField>) analyzer
						.getEveryBlockField().get(methodname);
				Object[] fieldArr = modules.toArray();
				int i = 0;
				int size = fieldArr.length;
				for (; i < size;) {
					Collection<SootField> ss = (Collection<SootField>) fieldArr[i];

					if (ss.containsAll(blockField)) {
						break;
					}
					i++;
				}
				Expression exp = ast.newSimpleName("splitedLock" + i);
				rewriter.replace(synstmt.getExpression(), exp, null);
			}
			return true;

		}
	}

	private final class syncMethodVisitor extends ASTVisitor {
		private final String methodname;

		private syncMethodVisitor(String methodname) {
			this.methodname = methodname;
		}

		@SuppressWarnings({ "unchecked" })
		public boolean visit(MethodDeclaration method) {
			if (method.getName().toString().equals(methodname)) {
				cls = (TypeDeclaration) method.getParent();
				List<Modifier> modifiers = method.modifiers();
				List<String> md = new ArrayList<String>();
				for (Modifier md1 : modifiers) {
					md.add(md1.toString());
				}
				if (md.contains("synchronized")) {
					Iterator<Modifier> it = modifiers.iterator();
					while (it.hasNext()) {
						Modifier str = it.next();
						if (str.toString().equals("synchronized")) {
							rewriter.remove(str, null);
						}
					}

					Block newBody = ast.newBlock();
					Set<SootField> methodfield = analyzer
							.getMethodFields(methodname);

					SynchronizedStatement statement = ast
							.newSynchronizedStatement();
					ASTNode s = ASTNode.copySubtree(ast, method.getBody());

					statement.setBody((Block) s);
					Object[] fieldArr = modules.toArray();
					int i = 0;
					int size = fieldArr.length;
					for (; i < size;) {
						Collection<SootField> ss = (Collection<SootField>) fieldArr[i];
						if (ss.containsAll(methodfield)) {
							break;
						}
						i++;
					}
					statement.setExpression(ast
							.newSimpleName("splitedLock" + i));
					newBody.statements().add(statement);
					rewriter.replace(method.getBody(), newBody, null);
				} else {
					method.accept(new syncStmtVisitor());
				}
			}
			return true;
		}
	}

	static IFile _file;
	private ClassFeildsAnalyzer analyzer;
	private Collection<Collection> modules;
	private AST ast;
	private TypeDeclaration cls;
	private ASTRewrite rewriter;
	private boolean flag = false;

	public JDTRewriter(IFile file) {
		this._file = file;
	}

	public Change run(String methodname, String clsName)
			throws JavaModelException, MalformedTreeException,
			BadLocationException {
		moduleAnalysis(clsName);
		Change change = collectASTChange(methodname);
		WorkbenchHelper.showEditor(_file);

		return change;
	}

	public Change collectASTChange(String methodname)
			throws JavaModelException, BadLocationException {
		return rewriteAST(methodname);
	}

	public void addAnnotation(String methodname) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart dirtyEditor = null;
		try {
			dirtyEditor = IDE.openEditor(page, _file);
		} catch (PartInitException e) {
			//
			e.printStackTrace();
		}
		String srcpath = PathUtils.getSrcPath(_file);
		String classpath = PathUtils.getClassPath(_file);
		JavaCriticalSectionFinder.reset();
		Collection<JavaCriticalSection> result = JavaCriticalSectionFinder
				.getInstance(srcpath, classpath).getAllSyncs();
		int offset = 0;
		int length = 0;
		for (JavaCriticalSection cs : result) {
			if (cs.getMethodName().equals(methodname)) {
				offset = cs.getSync().getStartPosition();
				length = cs.getSync().getLength();
				break;
			}
		}
		WorkbenchHelper.selectInEditor((ITextEditor) dirtyEditor, offset,
				length);
	}

	private Change rewriteAST(final String methodname)
			throws JavaModelException, MalformedTreeException,
			BadLocationException {
		ICompilationUnit cu = (ICompilationUnit) JavaCore
				.createCompilationUnitFrom(_file);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);

		// unit.recordModifications();
		rewriter = ASTRewrite.create(unit.getAST());
		ast = unit.getAST();
		unit.accept(new syncMethodVisitor(methodname));
		unit.accept(new ASTVisitor() {
			public boolean visit(FieldDeclaration fd) {
				List<VariableDeclarationFragment> list = fd.fragments();
				for (VariableDeclarationFragment f : list) {
					boolean isLock = f.getName().toString()
							.startsWith("splitedLock");
					Expression ini = f.getInitializer();
					if (ini != null && ini instanceof ClassInstanceCreation) {
						Type type = ((ClassInstanceCreation) ini).getType();
						if (type instanceof SimpleType) {
							String typename = ((SimpleType) type).getName()
									.toString();
							boolean isObject = typename.equals("Object");
							flag = isLock && isObject;
						}
					}

				}
				return true;
			}
		});
		if (flag == false) {
			for (int i = 0, size = modules.size(); i < size; i++) {
				SimpleName fieldname = ast.newSimpleName("splitedLock" + i);

				ClassInstanceCreation classInstanceCreation = ast
						.newClassInstanceCreation();
				classInstanceCreation.setType(ast.newSimpleType(ast
						.newName("Object")));
				VariableDeclarationFragment fragment = ast
						.newVariableDeclarationFragment();
				fragment.setName(fieldname);
				fragment.setInitializer(classInstanceCreation);
				FieldDeclaration fd = ast.newFieldDeclaration(fragment);
				fd.setType(ast.newSimpleType(ast.newSimpleName("Object")));
				fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
				ListRewrite lrw = rewriter.getListRewrite(cls,
						TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				lrw.insertFirst(fd, null);
			}
		}

		String source = cu.getSource();
		Document document = new Document(source);

		TextEdit edits = rewriter.rewriteAST(document, cu.getJavaProject()
				.getOptions(true));
		TextFileChange change = new TextFileChange("", (IFile) cu.getResource());
		change.setEdit(edits);
		// edits.apply(document);
		// String newSource = document.get();
		//
		// // update of the compilation unit
		// cu.getBuffer().setContents(newSource);
		return change;
	}

	private String getPath() {
		String path = _file.getLocation().toString();
		return path;
	}

	@SuppressWarnings("unchecked")
	public void moduleAnalysis(String className) {
		String srcpath = PathUtils.getSrcPath(_file);
		String classpath = PathUtils.getClassPath(_file);
		try {
			analyzer = ClassFeildsAnalyzer.v(null, srcpath, classpath);
		} catch (NoMatchingSootMethodException e) {

			e.printStackTrace();
		}
		modules = analyzer.getClassModules(className);
		System.out.println("this class can be split into " + modules.size()
				+ " modules" + modules);

	}

	public static String getFileName(IFile _file) {
		String name = _file.getLocation().toString();
		int location = name.indexOf("src") + 4;
		name = name.substring(location);
		name = name.replaceAll("/", ".");
		location = name.lastIndexOf(".");
		name = name.substring(0, location);
		return name;
	}

}
