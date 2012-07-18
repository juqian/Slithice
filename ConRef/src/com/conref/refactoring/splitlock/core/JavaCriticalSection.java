package com.conref.refactoring.splitlock.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.Type;
import com.conref.sootUtil.SootUtils;


import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.EnterMonitorStmt;
import soot.tagkit.LineNumberTag;

public class JavaCriticalSection implements ICriticalSection{
	private String _classnameOfSrcFile;
	private String _classname;
	private ASTNode _classDeclNode;  //class declaration node
	private MethodDeclaration _method;	
	private ASTNode _sync;
	private LockKind _lockKind;
	private String _lockname;
	private JavaStmtChunk[] _body;
	private SootMethod _sootMethod;
	private int _startLine;
	private int _endline;
	public int getStartLine() {
		return _startLine;
	}
	public int getEndline() {
		return _endline;
	}



	
	public JavaCriticalSection(String classnameOfSrcFile, String classname, MethodDeclaration method, ASTNode sync){
		this._classnameOfSrcFile = classnameOfSrcFile;
		this._classname = classname;
		this._method = method;
		this._sync = sync;		
		this._classDeclNode = JavaASTUtils.findClassDeclNode(method);
		
		if(_sync instanceof MethodDeclaration){
			boolean isStatic = JavaASTUtils.isMethodStatic(_method);
			if(isStatic){
				this._lockKind = LockKind.LOCK_WITH_CLASS;
				this._lockname = _classname;
			}
			else{
				this._lockKind = LockKind.LOCK_WITH_THIS;
				this._lockname = "this";
			}
		}
		else{
			this._lockKind = LockKind.LOCK_WITH_LOCAL;
			
			SynchronizedStatement s = (SynchronizedStatement)_sync;
			Expression e = s.getExpression();
			
			this._lockname = e.toString();
		}
		
		resolveBody();
	}
	
	public Local getLockVariable(){
		//1. get lock variable
		Body body = getSootMethod().getActiveBody();
		Local lock = null;
		if(getSynType() == SyncType.SyncMethod){
		}
		else{
			int startLine = getStartLine();
			int endLine = getEndline();		 
			for (Unit u : body.getUnits()) {
				if (!(u instanceof EnterMonitorStmt))
					continue;
				
				LineNumberTag linetag = (LineNumberTag)u.getTag("LineNumberTag");
				if(linetag==null) return null;
				int line = linetag.getLineNumber();
				if (line < startLine || line > endLine) {
					continue;
				}
				
				lock = (Local)((EnterMonitorStmt) u).getOp();
				break;
			}
		}
		
		return lock;
	}
	
	public MethodDeclaration getMethod(){
		return _method;  //c
	}
	
	public String getClassName(){
		return _classname;
	}
	
	public String getShortClassName(){
		int index = _classname.lastIndexOf('.');
		if(index>=0){
			String name = _classname.substring(index+1);
			return name;
		}
		else{
			return _classname;
		}
	}
	
	/** The name of the class corresponding to the enclosing source file. */
	public String getClassNameOfSourceFile(){
		return _classnameOfSrcFile;
	}
	
	public ASTNode getClassDeclNode(){
		return _classDeclNode;
	}
	
	public String getMethodName(){
		String name = _method.getName().getIdentifier();
		return name;
	}
	
	public String getMethodReturnType(){
		Type type = _method.getReturnType2();
		if(type!=null){
			return getShortTypeName(type);
		}
		else{
			return "";
		}
	}
	
	
	public String getLockName(){
		return _lockname;
	}
	
	public boolean isInAnonymousClass(){
		return (_classDeclNode instanceof AnonymousClassDeclaration);
	}
	
	@SuppressWarnings("serial")
	public
	static class NoMatchingSootMethodException extends Exception{
		public NoMatchingSootMethodException(String msg){
			super(msg);
		}
	}
	
	public SootMethod getSootMethod(){
		return _sootMethod;
	}
	
	/** Binding the source code level AST to Soot entities, i.e., jimple. */
	public void bindToSoot() throws NoMatchingSootMethodException{
		bindSootMethod();
		
		boolean bFindMap = false;
		
		Map<Integer,Collection<Unit>> map = buildLineToUnitMap(_sootMethod);
		if(map==null){return ;}
		for(JavaStmtChunk stmt: _body){			
			for(int i=stmt.startLine; i<=stmt.endLine;i++){
				Collection<Unit> units = map.get(i);
				if(units!=null){
					stmt.instructions.addAll(units);
					if(!units.isEmpty()){
						bFindMap = true;
					}
				}
			}
		}
		
//		if(!bFindMap){
//			throw new NoMatchingSootMethodException("No matching body for " + _sootMethod + ". Maybe no line inforamtion kept.");
//		}
	}
	
	private Map<Integer,Collection<Unit>> buildLineToUnitMap(SootMethod m){
		if(m==null){return null;}
		Map<Integer,Collection<Unit>> map = new HashMap<Integer,Collection<Unit>>();
		Body body = m.retrieveActiveBody();
		for(Unit u: body.getUnits()){
			int line = SootUtils.getLine(u);
			
			Collection<Unit> units = map.get(line);
			if(units==null){
				units = new ArrayList<Unit>(5);
				map.put(line, units);
			}
			
			units.add(u);
		}
		
		return map;
	}
	
	private SootMethod bindSootMethod() throws NoMatchingSootMethodException{
		String className = getClassName();
		SootClass cls = null;
		
		try {
			cls = Scene.v().getSootClass(className);
		} catch (Exception e) {
			throw new NoMatchingSootMethodException("Can not find the soot class: " + className);
		}

		String methodName = getMethodName();

		// check if is a constructor
		String shortClassName = getShortClassName();
		int sp = shortClassName.lastIndexOf('$');
		if (sp >= 0) {
			shortClassName = shortClassName.substring(sp + 1);
		}
		if (methodName.equals(shortClassName)) {
			methodName = "<init>";
		}

		String identifier = className + "." + methodName;
		
		// find candidate by name
		List<SootMethod> candidates = filterMethodByName(cls.getMethods(), methodName);
		if (candidates.size() == 1) {
			_sootMethod = candidates.iterator().next();
			return _sootMethod;
		} else if (candidates.isEmpty()) {
		//	throw new NoMatchingSootMethodException("No soot method match for name: " + identifier);
		}

		// find by parameter count
		List<String> params = getMethodParameters();
		candidates = filterMethodByParamCount(candidates, params.size());
		if (candidates.size() == 1) {
			_sootMethod = candidates.iterator().next();		
			return _sootMethod;
		} else if (candidates.isEmpty()) {
		//	throw new NoMatchingSootMethodException("No soot method match the parameter number for: " + identifier);
		}

		// find by parameter match
		candidates = filterMethodByParamType(candidates, params);
		if (candidates.size() == 1) {
			_sootMethod = candidates.iterator().next();	
			return _sootMethod;
		} else if (candidates.isEmpty()) {
		//	throw new NoMatchingSootMethodException("No soot method match the parameter types for: " + identifier);
		}

		// find by return type match
		candidates = filterMethodByReturnType(candidates, getMethodReturnType());
		if (candidates.size() == 1) {
			_sootMethod = candidates.iterator().next();	
			return _sootMethod;
		} else if (candidates.size() > 1) {
			return null;
		//	throw new NoMatchingSootMethodException("Found two soot method for: " + identifier);
		} else {
			return null;
		//	throw new NoMatchingSootMethodException("No soot method found for: " + identifier);
		}
	}
	
	private List<SootMethod> filterMethodByName(List<SootMethod> methods, String methodName){
		List<SootMethod> candidates = new ArrayList<SootMethod>();
		for(SootMethod m: methods){
			if(m.getName().equals(methodName)){
				candidates.add(m);
			}
		}
		
		return candidates;
	}
	
	private List<SootMethod> filterMethodByParamCount(List<SootMethod> methods, int params){
		List<SootMethod> candidates = new ArrayList<SootMethod>();
		for(SootMethod m: methods){
			if(m.getParameterCount()==params){
				candidates.add(m);
			}
		}
		
		return candidates;
	}
	
	private boolean isParameterTypeMatch(SootMethod m, List<String> params){
		Iterator<String> it=params.iterator();
		for(int i=0; i<params.size(); i++){
			String type1 = m.getParameterType(i).toString();
			String type2 = it.next().toString();
			if(!isTypeMatch(type1, type2)){
				return false;
			}
		}
		
		return true;
	}
	
	private List<SootMethod> filterMethodByParamType(List<SootMethod> methods, List<String> params){
		List<SootMethod> candidates = new ArrayList<SootMethod>();
		for(SootMethod m: methods){
			if(isParameterTypeMatch(m, params)){
				candidates.add(m);
			}
		}
		
		return candidates;
	}

	private List<SootMethod> filterMethodByReturnType(List<SootMethod> methods, String returnType){
		List<SootMethod> candidates = new ArrayList<SootMethod>();
		for(SootMethod m: methods){
			String type1 = m.getReturnType().toString();
			String type2 = getMethodReturnType();
			if(isTypeMatch(type1, type2)){
				candidates.add(m);
			}
		}
		
		return candidates;
	}
	
	private boolean isTypeMatch(String type1, String type2){
		int dot = type1.lastIndexOf('.');
		if(dot>=0){
			type1 = type1.substring(dot+1);
		}
		
		dot = type1.lastIndexOf('$');
		if(dot>=0){
			type1 = type1.substring(dot+1);
		}
		
		if(!type1.equals(type2)){
			return false;
		}
		
		/*if(!type2.contains(type1)){
			return false;
		}
		
		// array type consistent
		if(type2.lastIndexOf('[')>=0 && type2.lastIndexOf('[')<0){
			return false;
		}*/
		
		return true;
	}
	
	
	private String getShortTypeName(Type type){	 
		String shortTypeName = type.toString();
		
		// process generic types
		if(type.isParameterizedType()){
			if(shortTypeName.indexOf('<')<0){
				shortTypeName = "Object";
			}
			else{
				int start = shortTypeName.indexOf('<');
				int end = shortTypeName.lastIndexOf('>');
				
				StringBuffer buf = new StringBuffer(shortTypeName);
				buf.delete(start, end+1);
				shortTypeName = buf.toString();					 
			}
		}
		
		//if is a sub type
		int dot = shortTypeName.lastIndexOf('.');
		if(dot>=0){
			/*StringBuffer buf = new StringBuffer(shortTypeName);
			for(int i=0; i<shortTypeName.length(); i++){
				if(buf.charAt(i)=='.'){
					buf.setCharAt(i, '$');
				}
			}
			
			shortTypeName = buf.toString();*/
			shortTypeName = shortTypeName.substring(dot+1);
		} 
		
		return shortTypeName;
	}
	
	public List<String> getMethodParameters(){
		List<String> result = new ArrayList<String>();
		List<?> params = _method.parameters();
		for(Object o: params){
			SingleVariableDeclaration v = (SingleVariableDeclaration)o;		
			Type type = v.getType();
			String shortTypeName = getShortTypeName(type);
			if(v.toString().lastIndexOf('[')>=0 
				&& shortTypeName.lastIndexOf('[')<0){
				shortTypeName += "[]";
			} 
		
			result.add(shortTypeName); 
		}

		return result;
	}
	
	public LockKind getLockKind(){
		return _lockKind;
	}
	
	private void resolveBody(){
		CompilationUnit cu = JavaASTUtils.findComplilationUnit(_method);
		Block block = null;
		if (_sync instanceof SynchronizedStatement)
			block = ((SynchronizedStatement) _sync).getBody();
		else if (_sync instanceof MethodDeclaration)
			block = ((MethodDeclaration) _sync).getBody();

		if (block == null) {
			throw new RuntimeException("Cannot find the synchronized code block.");
		}
		
		_startLine = cu.getLineNumber(_sync.getStartPosition());	 
         _endline= cu.getLineNumber(_sync.getStartPosition()+_sync.getLength());
		List<JavaStmtChunk> body = new LinkedList<JavaStmtChunk>();
		List<?> stmts = block.statements();
		for (Object o : stmts) {
			Statement s = (Statement) o;

			int start = s.getStartPosition();
			int end = start + s.getLength();

			int startLine = cu.getLineNumber(start);
			int endLine = cu.getLineNumber(end);

			JavaStmtChunk range = new JavaStmtChunk(s, startLine, endLine);
			body.add(range);
		}

		this._body = body.toArray(new JavaStmtChunk[0]);
	}
	
	public JavaStmtChunk[] getBody(){
		return _body;
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer();
		if(_sootMethod!=null){
			buf.append(_sootMethod.toString() + "\n");
		}
		else{
			buf.append("<"+_classname+": "+getMethodReturnType()+" "+getMethodName()+"("+getMethodParameters()+")>\n");
		}
		
		buf.append(_sync.toString());
		return buf.toString();
	}

	public ASTNode getSync(){
		return _sync;
	}
	
	public static enum SyncType{ SyncBlock, SyncMethod};
	
	public SyncType getSynType(){
		if (_sync instanceof SynchronizedStatement){
			//SynchronizedStatement s = (SynchronizedStatement)_sync;			 
			return SyncType.SyncBlock;
			 
		}
		else if (_sync instanceof MethodDeclaration){
			//MethodDeclaration m = (MethodDeclaration)_sync;
			return SyncType.SyncMethod;
		}
		return null;
	}
	
	public String getHeadString(){
		String ret = ""+_startLine +": ";
		if (_sync instanceof SynchronizedStatement){
			SynchronizedStatement s = (SynchronizedStatement)_sync;			 
			ret += "synchronized(" + s.getExpression() + "){";
			 
		}
		else if (_sync instanceof MethodDeclaration){
			MethodDeclaration m = (MethodDeclaration)_sync;
			ret += " synchronized ";
			if(m.getReturnType2()!=null){
				ret += m.getReturnType2() + " ";
			}
			ret += m.getName() + "(";
			List<?> params = m.parameters();
			for(Iterator<?> it=params.iterator(); it.hasNext();){
				ret += it.next();
				if(it.hasNext())
					ret += ", ";
			}
			ret += "){";
		}
		
		return ret;
	}
}
