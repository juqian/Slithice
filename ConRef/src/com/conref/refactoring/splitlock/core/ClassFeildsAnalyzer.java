package com.conref.refactoring.splitlock.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.Stack;

import com.conref.refactoring.splitlock.core.JavaCriticalSection.NoMatchingSootMethodException;
import com.conref.refactoring.splitlock.core.JavaCriticalSection.SyncType;
import com.conref.sootUtil.SootUtils;
import com.conref.sootUtil.Test;
import com.conref.sootUtil.callgraph.CallGraphHelper;
import com.conref.sootUtil.callgraph.Callees;
import com.conref.sootUtil.graph.UndirectedGraph;

import soot.ArrayType;
import soot.Body;
import soot.FastHierarchy;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.callgraph.VirtualCalls;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.tagkit.LineNumberTag;
import soot.util.NumberedString;

public class ClassFeildsAnalyzer implements Runnable {
	@SuppressWarnings("unused")
	private List<SootMethod> _patchedEntries;
	private static Collection<JavaCriticalSection> results;
	private Set<SootMethod> allInvolvedMethod = new HashSet<SootMethod>();
	// classMap用来存放每个类对应的无向图
	private static String entryClass;
	private static Collection<SootMethod> syncsEnclosingMethods = new LinkedList<SootMethod>();
	private static ClassFeildsAnalyzer _instance;
	FieldScaner scaner;
	private static String _srcpath;
	private static String _classpath;
	private boolean setMainClass = true;
	private UndirectedGraph varConn = new UndirectedGraph();
	private Map<String, Set<SootField>> protectedFieldsInAllClass = new HashMap<String, Set<SootField>>();
	private boolean needMod = true;
	// 存放所有同步块（不包括同步方法）中读写的属性域
	private Map<String, Set<SootField>> everyBlockFields = new HashMap<String, Set<SootField>>();
	// 存放所有方法(包括非同步方法)中读写的属性域
	private Map<String, Set<SootField>> everyMethodFields = new HashMap<String, Set<SootField>>();

	public Map getEveryBlockField() {
		return everyBlockFields;
	}

	public static Collection<JavaCriticalSection> getResults() {
		return results;
	}

	private ClassFeildsAnalyzer(String entryclass, String srcpaths,
			String classpaths) throws NoMatchingSootMethodException {
		entryClass = entryclass;
		this._srcpath = srcpaths;
		this._classpath = classpaths;
	}

	public Set<SootField> getMethodFields(String methodName) {
		Set<SootField> set = everyMethodFields.get(methodName);
		return set;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void analysis(String srcpaths, String classpaths)
			throws NoMatchingSootMethodException {
		Date analysisStart = new Date();
		getAllSyncs();
		System.out.println("the total number of synchronization is "
				+ results.size());
		Collection<JavaCriticalSection> validSyncs = new HashSet();
		SootExOpreation();

		CallGraph cg = buildCallGraph(validSyncs);
		assureAllSyncInCallGraph( validSyncs);
		Date end = new Date();
		System.out.println("[assureAllSyncInCallgraph]  finished in "
				+ getTimeConsumed(analysisStart, end));
		getAllInvolvedMethods();
		System.out.println("buliding UndirectedGraph...");
		Date buildStart=new Date();
		buildVarConn(validSyncs);
		Date buildEnd=new Date();
        System.out.println("[UndirectedGraph build] finished in "+ClassFeildsAnalyzer.getTimeConsumed(buildStart, buildEnd));
        classifyVarConn();
		Date analysisEnd = new Date();
		System.out.println("[analysis] finished in "+ ClassFeildsAnalyzer.getTimeConsumed(analysisStart, analysisEnd));
	}

	public void buildVarConn(Collection<JavaCriticalSection> validSyncs) {
		scaner = new FieldScaner(allInvolvedMethod);
		for (JavaCriticalSection cs : validSyncs) {
			SootMethod m = cs.getSootMethod();
			if (m == null)
				continue;
			Set<SootField> fields = null;
			if (cs.getSynType() == SyncType.SyncMethod) {
				fields = scaner.getUseInstanceFields(m);
				if (fields != null && needMod)
					fields.addAll(scaner.getModInstanceFields(m));
				fields = filter(cs, fields);
				everyMethodFields.put(cs.getMethodName(), fields);
			}// synchronization is a block
			else {
				fields = collectBlockField(cs);
				fields = filter(cs, fields);
				everyBlockFields.put(cs.getMethodName(), fields);
			}
			
			// -------------------------------------addEdges-----------------------------------

			String clsName = cs.getClassNameOfSourceFile();
			if (!protectedFieldsInAllClass.containsKey(clsName)) {
				protectedFieldsInAllClass.put(clsName, new HashSet());
			}
			protectedFieldsInAllClass.get(clsName).addAll(fields);
			
			SootField[] fs = fields.toArray(new SootField[0]);
			for (int i = 0; i < fs.length; i++) {
				Set<SootField> neighbors = new HashSet();
				for (int j = 0; j < fs.length; j++) {
					if (i == j)
						continue;
					neighbors.add(fs[j]);
				}
				varConn.add(fs[i], neighbors);
			}
		}
	}

	public void getAllInvolvedMethods() {
		
		ReachableMethods rm = CallGraphHelper.getReachableMethod(Scene.v().getCallGraph(),
				syncsEnclosingMethods);
		for (Iterator<?> it = rm.listener(); it.hasNext();) {
			SootMethod m = (SootMethod) it.next();
			allInvolvedMethod.add(m);
		}
	}

	public CallGraph buildCallGraph(Collection<JavaCriticalSection> validSyncs)
			throws NoMatchingSootMethodException {
		CallGraphBuilder cgb = new CallGraphBuilder(DumbPointerAnalysis.v());
		cgb.build();
		CallGraph cg = Scene.v().getCallGraph();
		for (JavaCriticalSection cs : results) {
			String clsname = cs.getClassName();
			String regex = "\\$\\d";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(clsname);
			if (matcher.find()) {
				continue;
			}
			validSyncs.add(cs);
			cs.bindToSoot();
			syncsEnclosingMethods.add(cs.getSootMethod());
		}
		Scene.v().setCallGraph(cg);
		return cg;
	}

	public void SootExOpreation() {
		setSootOptions(_classpath);
		loadClasses();
	}

	public void getAllSyncs() {
		results = JavaCriticalSectionFinder.getInstance(_srcpath, _classpath)
				.getAllSyncs();
	}

	private Set<SootField> filter(JavaCriticalSection cs, Set<SootField> fields) {
		Local lock = cs.getLockVariable();
		Type lockVarType = null;
		if (lock == null) {
			lockVarType = cs.getSootMethod().getDeclaringClass().getType();
		} else {
			lockVarType = lock.getType();
			if (lockVarType.toString().equals("java.lang.Object"))
				return new HashSet<SootField>(0);
		}
		FastHierarchy fastHier = new FastHierarchy();

		Set<SootField> filteredSet = new HashSet<SootField>();
		for (SootField field : fields) {
			SootClass fieldDC = field.getDeclaringClass();

			Type fieldClsType = fieldDC.getType();
			if (fastHier.canStoreType(fieldClsType, lockVarType)
					|| fastHier.canStoreType(lockVarType, fieldClsType)) {
				filteredSet.add(field);
			}
		}
		return filteredSet;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<SootField> collectBlockField(JavaCriticalSection cs) {
		Set result = new HashSet();
		int startLine = cs.getStartLine();
		int endLine = cs.getEndline();
		Body body = cs.getSootMethod().getActiveBody();
		PatchingChain<Unit> units = body.getUnits();
		for (Unit u : units) {
			Stmt s = (Stmt) u;
			LineNumberTag linetag = (LineNumberTag) s.getTag("LineNumberTag");
			if (linetag == null)
				continue;
			int line = linetag.getLineNumber();
			if (line < startLine || line > endLine) {
				continue;
			}

			List<ValueBox> Fieldslist = u.getUseBoxes();
			Fieldslist.addAll(u.getDefBoxes());
			for (ValueBox box : Fieldslist) {
				Value v = box.getValue();
				if (v instanceof JInstanceFieldRef) {
					result.add(((JInstanceFieldRef) v).getField());
				}
			}
			if (s.containsInvokeExpr()) {
				// 用调用图获得调用目标
				Callees callees = new Callees(getCallGragh(), u);
				for (SootMethod invokeMethod : callees.explicits()) {
					if (invokeMethod == null)
						continue;
					Collection use = scaner.getUseInstanceFields(invokeMethod);
					Collection mod = new HashSet();
					if (needMod) {
						mod.addAll(scaner.getModInstanceFields(invokeMethod));
					}
					if (use != null)
						result.addAll(use);
					result.addAll(mod);
				}
			}
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void classifyVarConn() {
		System.out.println("[classify] starting...");
		Collection<HashSet> groups = varConn.findConnectedComponents();
		Collection<Set> refactorableGroups = new HashSet();
		Set<String> noRefactorableClsNameInRG = new HashSet<String>();
		for (Set c : groups) {
			Set clsNames=getClassNamesInGroup(c);
			if(clsNames.size()==1){
				Set<SootField> clsFields=protectedFieldsInAllClass.get(clsNames.iterator().next());
				if (clsFields!=null&&!c.containsAll(clsFields)) {
				refactorableGroups.add(c);
				} 
			}
		}
		for (Set c : refactorableGroups) {
			SootField sf = (SootField) c.iterator().next();
			String clsName = sf.getDeclaringClass().getName();
			Collection fieldsModules = refactorableClass.get(clsName);
			if (fieldsModules == null) {
				fieldsModules = new HashSet();
				refactorableClass.put(clsName, fieldsModules);
			}
			fieldsModules.add(c);
		}
		
		for(Entry entry:refactorableClass.entrySet()){
			if(((Collection)entry.getValue()).size()==1){
				noRefactorableClsNameInRG.add((String) entry.getKey());
			}
		}
		for (String s : noRefactorableClsNameInRG) {
			Collection c = refactorableClass.get(s);
			if (c != null) {
				refactorableClass.remove(s);
			}
		}
	}


	private Set<String> getClassNamesInGroup(Collection<SootField> c) {
		Set<String> clsNames=new HashSet<String>();
		for(SootField sf:c){
			clsNames.add(sf.getDeclaringClass().getName());
		}
		return clsNames;
	}

	private Map<String, Collection> refactorableClass = new HashMap<String, Collection>();

	public Collection getClassModules(String absolutelyClassName) {
		return refactorableClass.get(absolutelyClassName);
	}

	private void setSootOptions(String classpaths) {
		Test.setDefaultSootOptions(classpaths, true, false);
	}

	private void loadClasses() {
		Date start = new Date();
		if (entryClass != null) {
			SootUtils.loadClassesForEntry(entryClass);
		} else {
			for (JavaCriticalSection cs : results) {
				String clsname = cs.getClassName();
				String regex = "\\$\\d";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(clsname);
				if (matcher.find()) {
					System.out.println("can't find the class created randomly by compiler");
					continue;
				}
				SootClass cls = Scene.v().loadClassAndSupport(clsname);
				if (setMainClass == true&& cls.declaresMethod(Scene.v().getSubSigNumberer()
								.findOrAdd("void main(java.lang.String[])"))) {
					Scene.v().setMainClass(cls);
					setMainClass = false;
				}
			}
			Scene.v().loadNecessaryClasses();
		}
		Date end = new Date();
		System.out.println("load " + Scene.v().getClasses().size()
				+ " classes in " + getTimeConsumed(start, end));
	}

	private CallGraph getCallGragh() {

		return Scene.v().getCallGraph();

	}

	public void assureAllSyncInCallGraph(
			Collection<JavaCriticalSection> criticalSections) {
		System.out.println("[assureAllSyncInCallgraph] starting");
		Date start = new Date();
		ReachableMethods oldReach = Scene.v().getReachableMethods();
		// Collection et = EntryPoints.v().all();
		// ReachableMethods reachableFromMain = new ReachableMethods(cg, et);
		// reachableFromMain.update();

		Set<SootMethod> notInCG = new HashSet<SootMethod>();
		for (JavaCriticalSection cs : criticalSections) {
			SootMethod m = cs.getSootMethod();
			if (!oldReach.contains(m))
				notInCG.add(m);
		}
		Set<SootMethod> alreadyInCG = new HashSet<SootMethod>();
		for (Iterator<MethodOrMethodContext> it = oldReach.listener(); it
				.hasNext();) {
			alreadyInCG.add(it.next().method());
		}
		/*
		 * for(Iterator<?> it=cg.sourceMethods();it.hasNext();){ SootMethod m =
		 * (SootMethod)it.next(); alreadyInCG.add(m); if(notInCG.contains(m)){
		 * notInCG.remove(m); } }
		 */

		// add new methods to the call graph
		Stack<SootMethod> worklist = new Stack<SootMethod>();
		for (SootMethod m : notInCG) {
			worklist.push(m);
		}

		while (!worklist.isEmpty()) {
			SootMethod m = worklist.pop();
			if (alreadyInCG.contains(m) || m == null) {
				continue;
			}
			alreadyInCG.add(m);

			Body body = m.retrieveActiveBody();
			for (Unit u : body.getUnits()) {
				if (!(u instanceof Stmt)) {
					continue;
				}

				Stmt s = (Stmt) u;
				if (!s.containsInvokeExpr()) {
					continue;
				}

				InvokeExpr invoke = s.getInvokeExpr();
				SootMethod declCallee = invoke.getMethod();
				Collection<SootMethod> targets = new ArrayList<SootMethod>();
				if (declCallee.isStatic()) {
					targets.add(declCallee);
				} else {
					// XXX: pay attention to reflexive call, thread call, and
					// etc.
					// Here we do not add edges for implicit targets, as these
					// parts of methods is not reachable
					// from the entry, and they will most likely to be analyzed
					// in a very conservative ways.
					InstanceInvokeExpr iie = (InstanceInvokeExpr) invoke;
					Local receiver = (Local) iie.getBase();
					NumberedString subSig = iie.getMethodRef()
							.getSubSignature();

					if (invoke instanceof SpecialInvokeExpr) {
						SootMethod tgt = VirtualCalls.v().resolveSpecial(
								(SpecialInvokeExpr) invoke, subSig, m);
						targets.add(tgt);
					} else {
						Type t = receiver.getType();
						if (t instanceof ArrayType) {
							// t = RefType.v("java.lang.Object");
							targets.add(declCallee);
						}
						// BottomType bug
						else if (t instanceof soot.jimple.toolkits.typing.fast.BottomType) {
							System.out.println("BottomType:" + s);
						} else if (t instanceof soot.NullType) {
							System.out.println("NullType:" + s);
						} else {
							SootClass c = ((RefType) t).getSootClass();
							// Soot has a type resolving bug, and here may be
							// exceptions
							try {
								Set<SootMethod> callees = Scene.v()
										.getOrMakeFastHierarchy()
										.resolveAbstractDispatch(c, declCallee);
								targets.addAll(callees);
							} catch (Exception e) {
								System.out.println(e.getMessage());
							}
						}
					}
				}

				for (SootMethod t : targets) {
					// add call edge
					Edge e = new Edge(m, s, t);
					Scene.v().getCallGraph().addEdge(e);

					// add to worklist
					if (t.isConcrete() && !alreadyInCG.contains(t)) {
						worklist.push(t);
					}
				}
			}
		}

		// infer entries for the methods not reachable from main entry
		List<SootMethod> entries = Scene.v().getEntryPoints();
		_patchedEntries = new ArrayList<SootMethod>(notInCG);
		for (SootMethod m : notInCG) {
			// XXX there can be recursive calls, so for simple processing, all
			// methods not in old
			// call graph should be added to the entry. Checking the
			// non-existence of incoming edges
			// may miss some recursively called methods
			// if(!cg.edgesInto(m).hasNext()){
			entries.add(m);
			// }
		}

		// update reachable methods
		Scene.v().setReachableMethods(null);

	}

	public static String getTimeConsumed(Date start, Date end) {
		long time = end.getTime() - start.getTime();
		return "" + time / 1000 + "." + (time / 100) % 10 + "s";
	}

	public static ClassFeildsAnalyzer v(String name, String srcpath,
			String classpath) throws NoMatchingSootMethodException {
		if (_instance == null) {
			_instance = new ClassFeildsAnalyzer(name, srcpath, classpath);
		}
		return _instance;

	}

	@Override
	public void run() {
		try {
			analysis(_srcpath, _classpath);
		} catch (NoMatchingSootMethodException e) {
			//
			e.printStackTrace();
		}
	}

	public Map getCandidateCls() {
		Collection<JavaCriticalSection> result = JavaCriticalSectionFinder
				.getInstance(_srcpath, _classpath).getAllSyncs();

		Set<String> classes = new HashSet<String>();
		Map<String, Map<String, Integer>> candidate = new HashMap<String, Map<String, Integer>>();
		for (JavaCriticalSection cs : result) {
			String classname = cs.getClassNameOfSourceFile();
			if ((!cs.getLockName().equals("this"))
					|| !refactorableClass.containsKey(classname))
				continue;
			if (!classes.contains(classname)) {
				classes.add(classname);
				candidate.put(classname, new HashMap<String, Integer>());
			}
			String method = cs.getMethodName();
			Integer line = new Integer(cs.getStartLine());
			candidate.get(classname).put(method, line);
		}
		return candidate;
	}

}
