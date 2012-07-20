package jqian.sootex.sideeffect;

import java.util.*;

import jqian.sootex.location.Location;
import jqian.sootex.util.NumberableComparator;
import jqian.sootex.util.callgraph.CallGraphEdgeFilter;
import jqian.sootex.util.callgraph.CallGraphHelper;
import jqian.sootex.util.callgraph.CallGraphNodeFilter;
import jqian.sootex.util.callgraph.Callees;
import jqian.sootex.util.callgraph.DirectedCallGraph;
import jqian.util.SortedArraySet;
import soot.*;
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

	/**
	 * get graph of strong connected components. 
	 * XXX: Here we break thread start calls in the call graph when finding SCCs
	 */
	static DirectedGraph<Collection> getSCCGraph(CallGraph cg, Collection entries) {
		ReachableMethods rm = CallGraphHelper.getReachableMethod(cg, entries);
		CallGraphNodeFilter nodeFilter = CallGraphHelper
				.getCallGraphNodeFilter(rm);

		CallGraphEdgeFilter edgeFilter = new CallGraphEdgeFilter() {
			public boolean isIgnored(Edge e) {
				return (e.kind() == Kind.THREAD);
			}
		};

		DirectedCallGraph dcg = new DirectedCallGraph(cg, entries, nodeFilter, edgeFilter);
		StronglyConnectedComponents scc = new StronglyConnectedComponents(dcg);
		return scc.getSuperGraph();
	}
}
