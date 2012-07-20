package jqian.sootex.sideeffect;

import java.util.*;

import jqian.Global;
import jqian.sootex.AtomicTypes;
import jqian.sootex.Cache;
import jqian.sootex.location.AccessPath;
import jqian.sootex.location.GlobalLocation;
import jqian.sootex.location.HeapLocation;
import jqian.sootex.location.InstanceObject;
import jqian.sootex.location.Location;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.PtsToHelper;
import jqian.sootex.util.SootUtils;
import jqian.util.Utils;
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
import soot.toolkits.graph.PseudoTopologicalOrderer;


/**
 * Context-insensitive side effect information collector.
 * When using context-insensitive pointer analysis, the side-effect sets of a method can be huge.
 * 
 * TODO: Use some kinds of escape analysis to improve the analysis precision.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SideEffectAnalysis implements ISideEffectAnalysis{
    private Collection _entries;
    private IPtsToQuery _ptsto;    
    HeapAbstraction _heapAbstraction;
    
    private Set<Location>[] _method2ModHeaps; 
    private Set<Location>[] _method2UseHeaps;    
    
    private Set<Location>[] _method2ModGb;
    private Set<Location>[] _method2UseGb;   
    
    public SideEffectAnalysis(IPtsToQuery ptsto,Collection entries, HeapAbstraction heapMemAbstraction){
    	this._entries = entries;
    	this._ptsto = ptsto;
    	this._heapAbstraction = heapMemAbstraction;
    }    
    
    /**Get outer abstract locations that can be modified by a method.*/ 
    public Collection<Location> getModGlobals(SootMethod m){
        return _method2ModGb[m.getNumber()];        
    }
    
    public Collection<Location> getUseGlobals(SootMethod m){
        return _method2UseGb[m.getNumber()];  
    }
    
    public Collection<Location> getModHeapLocs(SootMethod m){
        return _method2ModHeaps[m.getNumber()];
    }   
    
    public Collection<Location> getUseHeapLocs(SootMethod m){
        return _method2UseHeaps[m.getNumber()];
    } 
    
    private Set<InstanceObject> collectObjects(Collection<Location> locations){
    	Set<InstanceObject> objects = new HashSet<InstanceObject>();
    	for(Location loc: locations){
    		if(loc instanceof HeapLocation){
    			HeapLocation hLoc = (HeapLocation)loc;
    			objects.add(hLoc.getWrapperObject());
    		}
    	}
    	return objects;
    }
    
    public Collection<InstanceObject> getModObjects(SootMethod m){
    	Collection<Location> locations = getModHeapLocs(m);
    	return  collectObjects(locations);
    }
    
    public Collection<InstanceObject> getUseObjects(SootMethod m){
    	Collection<Location> locations = getUseHeapLocs(m);
    	return  collectObjects(locations);
    }
    
	public void build(){    	
        Date startBuild = new Date();  
        
        int methodNum = SootUtils.getMethodCount(); 
        _method2ModHeaps = new Set[methodNum];
        _method2UseHeaps = new Set[methodNum];
        _method2ModGb = new Set[methodNum];        
        _method2UseGb = new Set[methodNum]; 
                
        //1. get the collapse call graph, each strong connected component into a single graph node
   		CallGraph cg = Scene.v().getCallGraph();
   		DirectedGraph graph = SideEffectHelper.getSCCGraph(cg,_entries);		
   			
   		//2. topological sort
   		PseudoTopologicalOrderer pto = new PseudoTopologicalOrderer();
        List order = pto.newList(graph,true);
        
   		//3. bottom-up phase to find read/write on globals
        methodNum = 0;
        for(Iterator it=order.iterator();it.hasNext();){          
        	Collection node = (Collection) it.next();
        	methodNum += node.size();
        	findRWGlobalsForComponent(node, cg);
        }	
        	
        //3. bottom-up phase to find read/write on instance fields
        for(Iterator it=order.iterator();it.hasNext();){          
        	Collection node = (Collection) it.next();
        	findRWInstFieldsForComponent(node, cg);
        }  
		
		//free memories
		_entries = null;
		_ptsto = null;
      
        Date endBuild = new Date();        
        Global.v().out.println("[SideEffect]"+methodNum+" methods complete in "+
     		    Utils.getTimeConsumed(startBuild,endBuild)+"."); 
    }      
    
    
  //return the number of all side effects for statistics
    private void findRWGlobalsForComponent(Collection methods, CallGraph cg){        
    	Set mod = new HashSet();
        Set use = new HashSet();
        
        //intra-procedural analysis            
        for(Iterator it = methods.iterator();it.hasNext();){
            SootMethod m = (SootMethod)it.next();          
            collectRWStaticFields(m,mod,use);   
        }
        
    	// collect from callees 		
 		Set<SootMethod> callees = new HashSet();
		SideEffectHelper.collectComponentCallees(methods,cg,callees);
    	for(SootMethod tgt: callees){
			int tgtId = tgt.getNumber();
			mod.addAll(_method2ModGb[tgtId]);
			use.addAll(_method2UseGb[tgtId]);		  
		}
        
        mod = SideEffectHelper.compact(mod);
        use = SideEffectHelper.compact(use);        
    
        for(Iterator it = methods.iterator();it.hasNext();){
            SootMethod m=(SootMethod)it.next();
            int id = m.getNumber();                     
            
            _method2ModGb[id] = mod;
            _method2UseGb[id] = use;
        }
    }
    
    private void findRWInstFieldsForComponent(Collection methods, CallGraph cg){        
    	Set mod = new HashSet();
        Set use = new HashSet();
        
        //intra-procedural analysis            
        for(Iterator it = methods.iterator();it.hasNext();){
            SootMethod m = (SootMethod)it.next();             
            collectRWInstanceFields(m, _ptsto, mod, use); 
        }
		
    	// collect from callees 		
 		Set<SootMethod> callees = new HashSet();
		SideEffectHelper.collectComponentCallees(methods,cg,callees);
    	for(SootMethod tgt: callees){
			int tgtId = tgt.getNumber();
			mod.addAll(_method2ModHeaps[tgtId]);
			use.addAll(_method2UseHeaps[tgtId]);		  
		}
    	
        mod = SideEffectHelper.compact(mod);
        use = SideEffectHelper.compact(use);        
    
        for(Iterator it = methods.iterator();it.hasNext();){
            SootMethod m=(SootMethod)it.next();
            int id = m.getNumber();                     
            
            _method2ModHeaps[id] = mod;
            _method2UseHeaps[id] = use;
        }
    }    
    
	private void addSideEffect(IPtsToQuery ptrQuery, AccessPath ap, Set<Location> out) {
		Set<Location> locs = PtsToHelper.getAccessedLocations(ptrQuery, _heapAbstraction, null, ap);
		out.addAll(locs);
	}
	
	void collectRWInstanceFields(SootMethod m, IPtsToQuery ptsto, 
									   Set<Location> mod, Set<Location> use) {
		Set<AccessPath> modAps = new HashSet<AccessPath>();
		Set<AccessPath> useAps = new HashSet<AccessPath>();
		
		collectRWAccessPaths(m, modAps, useAps);
		
		for(AccessPath ap: modAps){ addSideEffect(ptsto, ap, mod); }
		for(AccessPath ap: useAps){ addSideEffect(ptsto, ap, use); }
	}
	
	static void collectRWAccessPaths(SootMethod m, Set<AccessPath> mod, Set<AccessPath> use){
		if (!m.isConcrete())
			return;

		for (Unit stmt : m.getActiveBody().getUnits()) {
			for (ValueBox box : stmt.getDefBoxes()) {
				Value v = box.getValue();
				if(v instanceof InstanceFieldRef || v instanceof ArrayRef){
					AccessPath ap = AccessPath.valueToAccessPath(m, stmt, v);
					mod.add(ap);
        		}
			}

			List<ValueBox> useBoxes = stmt.getUseBoxes();
			if (useBoxes == null)
				continue;

			for (ValueBox useBox : useBoxes) {
				Value u = useBox.getValue();
				if(u instanceof InstanceFieldRef || u instanceof ArrayRef){
					AccessPath ap = AccessPath.valueToAccessPath(m, stmt, u);
    				use.add(ap);
        		}
				else if (u instanceof AnyNewExpr) {
					// XXX: new instructions have initialization effects
					Value lhs = stmt.getDefBoxes().get(0).getValue();
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
						GlobalLocation loc = Location.getGlobalLocation(ref.getField());
						use.add(loc);
					}
				}
			}
		}
	}
}
