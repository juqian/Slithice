package jqian.sootex.sideeffect;

import java.util.*;

import jqian.sootex.AtomicTypes;
import jqian.sootex.Cache;
import jqian.sootex.location.AccessPath;
import jqian.sootex.location.GlobalLocation;
import jqian.sootex.location.Location;
import jqian.sootex.util.NumberableComparator;
import jqian.sootex.util.callgraph.CallGraphEdgeFilter;
import jqian.sootex.util.callgraph.CallGraphHelper;
import jqian.sootex.util.callgraph.CallGraphNodeFilter;
import jqian.sootex.util.callgraph.Callees;
import jqian.sootex.util.callgraph.DirectedCallGraph;
import jqian.util.SortedArraySet;
import soot.*;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.toolkits.callgraph.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.StronglyConnectedComponents;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SideEffectHelper {
	static Set compact(Collection s) {
		if (s == null){
			return null;
		}
		if (s.isEmpty()) {
			return Collections.EMPTY_SET;
		} else {
			return new SortedArraySet(s, NumberableComparator.v());
		}
	}

	static void mergeCallees(SootMethod m, Set<SootMethod> mergeSources,
			                 Set<Location>[] method2Writes, Set<Location>[] method2Reads) {
		int id = m.getNumber();

		Set<Location> defs = method2Writes[id];
		Set<Location> uses = method2Reads[id];

		for (SootMethod tgt : mergeSources) {
			int tgtId = tgt.getNumber();
			Set tgtDef = method2Writes[tgtId];
			Set tgtUse = method2Reads[tgtId];

			assert (tgtDef != null && tgtUse != null);

			defs.addAll(tgtDef);
			uses.addAll(tgtUse);
		}
	}

	static void collectComponentCallees(
			Collection<SootMethod> component, CallGraph cg,
			Collection<SootMethod> out) {
		for (SootMethod m : component) {
			Callees callees = new Callees(cg, m);
			Set<SootMethod> threads = callees.threads();
			for (SootMethod tgt : callees.all()) {
				if (!component.contains(tgt) && !threads.contains(tgt)) {
					out.add(tgt);
				}
			}
		}
	}
	
	static boolean isRefTgtLocal(ILocalityQuery localityQuery, SootMethod m, Value ap){
		if (localityQuery != null) {
			Local root = null;
			if (ap instanceof InstanceFieldRef) {
				root = (Local) ((InstanceFieldRef) ap).getBase();
			} else {
				root = (Local) ((ArrayRef) ap).getBase();
			}

			boolean isLocal = localityQuery.isRefTgtLocal(m, root);
			if (isLocal) {
				return true;
			}
		}
		
		return false;
	}


	
	static void collectRWAccessPaths(SootMethod m, ILocalityQuery locality, Set<AccessPath> mod, Set<AccessPath> use){
		if (!m.isConcrete())
			return;

		for (Unit stmt : m.getActiveBody().getUnits()) {
			for (ValueBox box : stmt.getDefBoxes()) {
				Value v = box.getValue();
				if(v instanceof InstanceFieldRef || v instanceof ArrayRef){
					if(!isRefTgtLocal(locality, m, v)){
						AccessPath ap = AccessPath.valueToAccessPath(m, stmt, v);
						mod.add(ap);
					}
        		}
			}

			List<ValueBox> useBoxes = stmt.getUseBoxes();
			if (useBoxes == null)
				continue;

			for (ValueBox useBox : useBoxes) {
				Value u = useBox.getValue();
				if(u instanceof InstanceFieldRef || u instanceof ArrayRef){
					if(!isRefTgtLocal(locality, m, u)){
						AccessPath ap = AccessPath.valueToAccessPath(m, stmt, u);
	    				use.add(ap);
					}
        		}
				else if (u instanceof AnyNewExpr) {
					// XXX: new instructions have initialization effects
					Value lhs = stmt.getDefBoxes().get(0).getValue();
					if (locality!=null && locality.isRefTgtLocal(m, (Local)lhs)) {
						continue;
					}
					
					AccessPath left = AccessPath.valueToAccessPath(m, stmt, lhs);

					if (u instanceof NewExpr) {
						RefType type = (RefType) u.getType();
						SootClass cls = type.getSootClass();
						if (!AtomicTypes.isAtomicType(cls)) {
							Collection<SootField> fields = Cache.v().getAllInstanceFields(cls);
							for (SootField f : fields) {
								AccessPath ap = left.appendFieldRef(f);
								mod.add(ap);
							}
						}
					} else if (u instanceof NewArrayExpr || u instanceof NewMultiArrayExpr) {
						AccessPath ap = left.appendArrayRef();
						mod.add(ap);
					}
				} 
			}
		}
	}
	
	static void collectRWStaticFields(SootMethod m, Set mod, Set use) {
		if (!m.isConcrete())
			return;

		for (Unit stmt : m.getActiveBody().getUnits()) {
			List<ValueBox> defBoxes = stmt.getDefBoxes();
			for (ValueBox box : defBoxes) {
				Value v = box.getValue();
				if (v instanceof StaticFieldRef) {
					StaticFieldRef ref = (StaticFieldRef) v;
					GlobalLocation loc = Location.getGlobalLocation(ref.getField());
					mod.add(loc);
				}
			}

			List<ValueBox> useBoxes = stmt.getUseBoxes();
			if (useBoxes != null) {
				for (ValueBox useBox : useBoxes) {
					Value v = useBox.getValue();
					if (v instanceof StaticFieldRef) {
						StaticFieldRef ref = (StaticFieldRef) v;
						SootField f = ref.getField();
						//XXX: Treat static final fields as constants
						if(!f.isFinal()){
							GlobalLocation loc = Location.getGlobalLocation(f);
							use.add(loc);
						}						
					}
				}
			}
		}
	}
}
