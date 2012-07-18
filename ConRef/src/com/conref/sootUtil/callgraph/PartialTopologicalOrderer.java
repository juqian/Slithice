package com.conref.sootUtil.callgraph;

import java.util.*;

import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.*;
import soot.util.NumberedSet;

public class PartialTopologicalOrderer extends TopologicalOrderer{
	private Collection<SootMethod> _entries;
	CallGraph _cg;
	List<SootMethod> _order = new ArrayList<SootMethod>();
    NumberedSet _visited = new NumberedSet( Scene.v().getMethodNumberer() );

	public PartialTopologicalOrderer(CallGraph cg, Collection<SootMethod> entries) {
		super(cg);

		this._entries = new ArrayList<SootMethod>(entries);
		this._cg = cg;
	}

	public void go() {
		Iterator<SootMethod> methods = _entries.iterator();
		while (methods.hasNext()) {
			SootMethod m = (SootMethod) methods.next();
			dfsVisit(m);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void dfsVisit( SootMethod m ) {
        if( _visited.contains( m ) ) return;
        _visited.add( m );
        Iterator targets = new Targets(_cg.edgesOutOf(m) );
        while( targets.hasNext() ) {
            SootMethod target = (SootMethod) targets.next();
            dfsVisit( target );
        }
        _order.add( m );
    }
	
	public List<SootMethod> order() { return _order; }
}
