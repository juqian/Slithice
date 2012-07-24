package jqian.sootex.sideeffect;

import java.util.*;

import jqian.Global;
import jqian.sootex.location.AccessPath;
import jqian.sootex.location.Location;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.PtsToHelper;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.callgraph.Callers;
import jqian.util.Utils;
import soot.*;
import soot.jimple.toolkits.callgraph.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.PseudoTopologicalOrderer;


/**
 * TODO incorrect implementation, dropped!
 * @deprecated
 * Collect the locations that are accessed outside each method.
 * Use this information to filter side-effect sets
 */
@SuppressWarnings({"rawtypes","unchecked"})
class OutsideAccessAnalysis{
    private Collection _entries;
    private IPtsToQuery _ptsto;    
    HeapAbstraction _heapAbstraction;
    
    private Set<Location>[] _method2Access; 
    private Set<Location>[] _method2OutsideAccess;    
    
    public OutsideAccessAnalysis(IPtsToQuery ptsto,Collection entries, HeapAbstraction heapMemAbstraction){
    	this._entries = entries;
    	this._ptsto = ptsto;
    	this._heapAbstraction = heapMemAbstraction;
    }    
    
    /**Get outer abstract locations that can be modified by a method.*/ 
    public Set<Location> getOutsideAccess(SootMethod m){
        return _method2OutsideAccess[m.getNumber()];        
    }    
    
    Set<Location> getOutsideAccess(int id){
        return _method2OutsideAccess[id];        
    }   
       
    void clearMethod(int id){
    	_method2OutsideAccess[id] = null; 
    }
    
	public void build(){    	
        Date startBuild = new Date();  
        
        int methodNum = SootUtils.getMethodCount(); 
        _method2Access = new Set[methodNum];
        _method2OutsideAccess = new Set[methodNum];
                
        //1. get the collapse call graph, each strong connected component into a single graph node
   		CallGraph cg = Scene.v().getCallGraph();
   		DirectedGraph graph = SootUtils.getSCCGraph(cg,_entries);		
   			
   		//2. topological sort
   		PseudoTopologicalOrderer pto = new PseudoTopologicalOrderer();
        List order = pto.newList(graph,false);
        
   		//3. top-down phase to find read/write on globals
        methodNum = 0;
        for(Iterator it=order.iterator();it.hasNext();){          
        	Collection node = (Collection) it.next();
        	methodNum += node.size();
        	findAccessForComponent(node, cg);
        }	
        	
        //3. bottom-up phase to find read/write on instance fields
        for(Iterator it=order.iterator();it.hasNext();){          
        	Collection node = (Collection) it.next();
        	findOutsideAccessForComponent(node, cg);
        }  
		
		//free memories
		_entries = null;
		_ptsto = null;
		_method2Access = null;
      
        Date endBuild = new Date();        
        Global.v().out.println("[OutsideAccess]"+methodNum+" methods complete in "+
     		    Utils.getTimeConsumed(startBuild,endBuild)+"."); 
    }      
    
	private void findAccessForComponent(Collection methods, CallGraph cg){ 
		Set access = new HashSet(); 
		
        for(Iterator it = methods.iterator();it.hasNext();){
            SootMethod m = (SootMethod)it.next();          
            SideEffectHelper.collectRWStaticFields(m,access,access);
            collectRWInstanceFields(m, access); 
        }
           
        for(Iterator it = methods.iterator();it.hasNext();){
            SootMethod m=(SootMethod)it.next();
            int id = m.getNumber(); 
            _method2Access[id] = access;
        }
	}
    
    private void findOutsideAccessForComponent(Collection methods, CallGraph cg){    	
 		Set<SootMethod> callers = new HashSet<SootMethod>(); 		 
 		for (Iterator it = methods.iterator();it.hasNext();) {
 			SootMethod m = (SootMethod)it.next();
			Callers c = new Callers(cg, m);			
			for (MethodOrMethodContext tgt : c.all()) {
				if (!methods.contains(tgt)) {
					callers.add(tgt.method());
				}
			}
		}
 		
 		Set outsideAccess = new HashSet();
 		for(SootMethod m: callers){
 			int id = m.getNumber();
 			Set<Location> access = _method2Access[id];
 			if(access!=null){
 				outsideAccess.addAll(access);
 				outsideAccess.addAll(_method2OutsideAccess[id]); 
 			}
        }
 		
    	if(methods.size()>1){
    		SootMethod m = (SootMethod)methods.iterator().next();
    		outsideAccess.addAll(_method2Access[m.getNumber()]);
    	}
    	
        for(Iterator it = methods.iterator();it.hasNext();){
            SootMethod m=(SootMethod)it.next();
            int id = m.getNumber();
            _method2OutsideAccess[id] = outsideAccess;
        }
    }
	
	void collectRWInstanceFields(SootMethod m, Set<Location> out) {
		Set<AccessPath> aps = new HashSet<AccessPath>();
		SideEffectHelper.collectRWAccessPaths(m, null, aps, aps);
		
		for(AccessPath ap: aps){ 
			Set<Location> locs = PtsToHelper.getAccessedLocations(_ptsto, _heapAbstraction, null, ap);
			out.addAll(locs); 
		}
	}
}
