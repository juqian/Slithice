/** 
 * @author Ju Qian{jqian@live.com}
 * @date 2011-02-14
 * @version 0.02
 */
package com.conref.sootUtil.callgraph;

import soot.*;
import soot.jimple.toolkits.callgraph.*;
import java.io.PrintStream;
import java.util.*;


/**
 */
public class CallGraphTracer{ 
	private CallGraph _cg;
	
	public CallGraphTracer(CallGraph cg){
		this._cg = cg;
	}
	
	static class Pair {
		public Pair(SootMethod m, LinkedList<SootMethod> tr) {
			this.method = m;
			this.trace = tr;
		}

		SootMethod method;
		LinkedList<SootMethod> trace;
	}
	
	//FIXME Seems there is a dead loop.
	@SuppressWarnings("unchecked")
	public Collection<List<SootMethod>> findTraces(SootMethod start) {
		Collection<List<SootMethod>> traces = new LinkedList<List<SootMethod>>();
		
		Stack<Pair> stack = new Stack<Pair>();
		Pair pair = new Pair(start, new LinkedList<SootMethod>());
		stack.push(pair);

		while (!stack.isEmpty()) {
			Pair p = (Pair) stack.pop();
			p.trace.add(p.method); //add an element to the trace

			Iterator<Edge> inEdges = _cg.edgesInto(p.method);
			if (inEdges.hasNext()) {
				do {
					Edge edge = (Edge) inEdges.next();
					if (p.trace.contains(edge.src()))//avoid infinite loops
						continue;

					LinkedList<SootMethod> newTrace = (LinkedList<SootMethod>)p.trace.clone();
					Pair newPair = new Pair(edge.src(), newTrace);
					stack.push(newPair);
					break;
				} while (inEdges.hasNext());
			} 
			else {
				traces.add(p.trace);
			}
		}
		
		return traces;
	}

	@SuppressWarnings("rawtypes")
	public static void printTraces(Collection<List> traces, PrintStream out) {
		for (List tr: traces) {			 
			out.println("================================ Trace ===============================");
			for (Iterator mIt = tr.iterator(); mIt.hasNext();) {
				SootMethod m = (SootMethod) mIt.next();
				out.println("    " + m);
			}
		}
	}
 }
