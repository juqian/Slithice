/**
 * @author Ju Qian
 * @date 2011-02-19
 * @version 0.02
 */

package com.conref.refactoring.splitlock.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.conref.Global;
import com.conref.sootUtil.SootUtils;
import com.conref.sootUtil.callgraph.Callees;

import soot.ArrayType;
import soot.Body;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.PseudoTopologicalOrderer;

/**
 * Find what fields a method read and write. Use a SCC-based approach to improve
 * time and space efficiency. XXX: The algorithm do not collect information
 * through thread boundaries
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FieldScaner {
	public FieldScaner(Collection entries) {
		this.entries = entries;
		build();
	}

	private Set<SootField>[] _defInstFields;
	private Set<SootField>[] _useInstFields;
	private Set<SootField>[] _defStaticFields;
	private Set<SootField>[] _useStaticFields;
	private Set<ArrayType>[] _defArrayTypes;
	private Set<ArrayType>[] _useArrayTypes;
	private Collection entries;

	// /////////////////////////////////////////////////

	protected static void reset() {
	}

	private void build() {
		Date start = new Date();
		Global.v().out.println("[FieldScaner] starting...");

		int mthdCount = SootUtils.getMethodCount();
		_defInstFields = new Set[mthdCount];
		_useInstFields = new Set[mthdCount];
		_defStaticFields = new Set[mthdCount];
		_useStaticFields = new Set[mthdCount];
		_defArrayTypes = new Set[mthdCount];
		_useArrayTypes = new Set[mthdCount];

		// 1. get the collapse call graph, each strong connected commponent into
		// a single graph node
		CallGraph cg = Scene.v().getCallGraph();
		DirectedGraph graph = SootUtils.getSCCGraph(cg, entries);

		// 2. topological sort
		PseudoTopologicalOrderer pto = new PseudoTopologicalOrderer();
		List order = pto.newList(graph, true);

		// 3. bottom-up phase
		for (Iterator it = order.iterator(); it.hasNext();) {
			Collection node = (Collection) it.next();

			// a strong connected component, can be single method with recursion
			Set<SootMethod> component = new HashSet(node); // trun to set for
															// fast access
			findComponentModUse(cg, component);
		}

		Date end = new Date();
		Global.v().out.println("[FieldScaner] finish in "
				+ getTimeConsumed(start, end));
	}

	private String getTimeConsumed(Date start, Date end) {

		long time = end.getTime() - start.getTime();
		return "" + time / 1000 + "." + (time / 100) % 10 + "s";
	}

	private void findComponentModUse(CallGraph cg, Set<SootMethod> component) {
		// in this case, all the methods in the recursion share the same
		// side-effect set
		Set defInstFields = new HashSet();
		Set useInstFields = new HashSet();
		Set defStaticFields = new HashSet();
		Set useStaticFields = new HashSet();
		Set defArrayTypes = new HashSet();
		Set useArrayTypes = new HashSet();

		// a. collect from this strong connected component
		for (SootMethod m : component) {
			scanNontransitively(m, defInstFields, useInstFields,
					defStaticFields, useStaticFields, defArrayTypes,
					useArrayTypes);
		}

		// b. collect from callees
		Set<SootMethod> computedCallees = new HashSet();
		collectComponentCallees(component, cg, computedCallees);
		mergeCallee(computedCallees, defInstFields, useInstFields,
				defStaticFields, useStaticFields, defArrayTypes, useArrayTypes);

		for (SootMethod m : component) {
			if (m == null)
				continue;
			int id = m.getNumber();
			_defInstFields[id] = defInstFields;
			_useInstFields[id] = useInstFields;
			_defStaticFields[id] = defStaticFields;
			_useStaticFields[id] = useStaticFields;
			_defArrayTypes[id] = defArrayTypes;
			_useArrayTypes[id] = useArrayTypes;
		}
	}


	public static void collectComponentCallees(
			Collection<SootMethod> component, CallGraph cg,
			Collection<SootMethod> out) {
		for (SootMethod m : component) {
			if (m == null)
				continue;
			Callees callees = new Callees(cg, m);
			Set<SootMethod> threads = callees.threads();
			for (SootMethod tgt : callees.all()) {
				if (!component.contains(tgt) && !threads.contains(tgt)) {
					out.add(tgt);
				}
			}
		}
	}

	private void mergeCallee(Set<SootMethod> callees, Set defInstFields,
			Set useInstFields, Set defStaticFields, Set useStaticFields,
			Set defArrayTypes, Set useArrayTypes) {
		for (SootMethod tgt : callees) {
			int tgtId = tgt.getNumber();
			defInstFields.addAll(_defInstFields[tgtId]);
			defStaticFields.addAll(_defStaticFields[tgtId]);
			defArrayTypes.addAll(_defArrayTypes[tgtId]);
			useInstFields.addAll(_useInstFields[tgtId]);
			useStaticFields.addAll(_useStaticFields[tgtId]);
			useArrayTypes.addAll(_useArrayTypes[tgtId]);
		}
	}

	/** Collect field use intraprocedurally. */
	private static void scanNontransitively(SootMethod m, Set defInstFields,
			Set useInstFields, Set defStaticFields, Set useStaticFields,
			Set defArrayTypes, Set useArrayTypes) {

		if (m == null || !m.hasActiveBody())
			return;
		if (!m.isConcrete())
			return;
		Body body = m.getActiveBody();
		for (Iterator it = body.getUnits().iterator(); it.hasNext();) {
			Object obj = it.next();

			if (obj instanceof DefinitionStmt) {
				DefinitionStmt stmt = (DefinitionStmt) obj;

				Value left = stmt.getLeftOp();
				Value right = stmt.getRightOp();

				if (left instanceof FieldRef) {
					FieldRef ref = (FieldRef) left;
					addFieldRef(ref, defInstFields, defStaticFields);
				} else if (left instanceof ArrayRef) {
					ArrayRef ref = (ArrayRef) left;
					defArrayTypes.add(ref.getBase().getType());
				} else if (right instanceof FieldRef) {
					FieldRef ref = (FieldRef) right;
					addFieldRef(ref, useInstFields, useStaticFields);
				} else if (right instanceof ArrayRef) {
					ArrayRef ref = (ArrayRef) right;
					useArrayTypes.add(ref.getBase().getType());
				}
			} else if (obj instanceof Stmt) {
				Stmt s = (Stmt) obj;
				if (s.containsFieldRef()) {
					addFieldRef(s.getFieldRef(), useInstFields, useStaticFields);
				}

				if (s.containsArrayRef()) {
					useArrayTypes.add(s.getArrayRef().getBase().getType());
				}
			}
		}
	}

	private static void addFieldRef(FieldRef ref, Set instFields,
			Set staticFields) {
		SootField field = ref.getField();

		if (field.isStatic()) {
			staticFields.add(field);
		} else {
			instFields.add(field);
		}
	}

	// ------------------------ Accessors -----------------------------------
	public Set<SootField> getModInstanceFields(SootMethod m) {
		return _defInstFields[m.getNumber()];
	}

	public Set<SootField> getUseInstanceFields(SootMethod m) {
		return _useInstFields[m.getNumber()];
	}

	public Set<SootField> getModGlobals(SootMethod m) {
		return _defStaticFields[m.getNumber()];
	}

	public Set<SootField> getUseGlobals(SootMethod m) {
		return _useStaticFields[m.getNumber()];
	}

	public Set<ArrayType> getModArrayTypes(SootMethod m) {
		return _defArrayTypes[m.getNumber()];
	}

	public Set<ArrayType> getUseArrayTypes(SootMethod m) {
		return _useArrayTypes[m.getNumber()];
	}

	public void getAccessedInstanceFields(SootMethod m, Collection out) {
		int mId = m.getNumber();
		out.addAll(_defInstFields[mId]);
		out.addAll(_useInstFields[mId]);
	}

	public void getAccessedGlobals(SootMethod m, Collection out) {
		int mId = m.getNumber();
		out.addAll(_defStaticFields[mId]);
		out.addAll(_useStaticFields[mId]);
	}

	public void getAccessedArrayTypes(SootMethod m, Collection out) {
		int mId = m.getNumber();
		out.addAll(_defArrayTypes[mId]);
		out.addAll(_useArrayTypes[mId]);
	}

	void release(SootMethod m) {
		int mId = m.getNumber();
		_defInstFields[mId] = null;
		_useInstFields[mId] = null;
		_defStaticFields[mId] = null;
		_useStaticFields[mId] = null;
		_defArrayTypes[mId] = null;
		_useArrayTypes[mId] = null;
	}
}
