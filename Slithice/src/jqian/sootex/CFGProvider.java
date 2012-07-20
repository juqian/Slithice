package jqian.sootex;

import soot.SootMethod;
import soot.toolkits.graph.UnitGraph;

/**
 *
 */
public interface CFGProvider {
	public UnitGraph getCFG(SootMethod m);
	
	public void release(SootMethod m);
}
