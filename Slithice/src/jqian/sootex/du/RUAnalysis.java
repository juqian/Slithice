package jqian.sootex.du;

import java.util.*;

import jqian.sootex.location.AccessPath;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.location.Location;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.PtsToHelper;
import jqian.sootex.sideeffect.ISideEffectAnalysis;
import jqian.sootex.util.CFGEntry;
import jqian.sootex.util.CFGExit;
import jqian.sootex.util.callgraph.Callees;
import soot.*;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.*;

/**
 *  A reaching use analysis implemented with bit vectors.
 */
public class RUAnalysis extends DUAnalysis{
	protected IPtsToQuery _pt2Query;
	protected HeapAbstraction _heapAbstraction;
	
	public RUAnalysis(MethodOrMethodContext mc,DirectedGraph<Unit> graph,IPtsToQuery pt2Query,
	          HeapAbstraction heapAbstraction, ISideEffectAnalysis sideEffect,boolean verbose){
		super(mc, graph, sideEffect, verbose);
		
		this._pt2Query = pt2Query;
		this._heapAbstraction = heapAbstraction;
	} 
	
	public void build(){
		super.build();
		_pt2Query = null;
	}
	
	/** ID name of the analysis  */
	protected String getAnalysisName(){
		return "RU";
	}
	
	/** USE set at the method entry. */
	protected Collection<ReachingDU> getEntryDU(){
		Collection<ReachingDU> entrySet = new ArrayList<ReachingDU>();
			
		// TODO: What should be the reaching uses at the method entry?
		/*Unit entry = CFGEntry.v();
		 * Collection<Location> params = collectParams(); for(Location loc:
		 * params){ AccessPath ap = AccessPath.getByRoot(loc); entrySet.add(new
		 * ReachingDU(entry,ap,loc)); }
		 * 
		 * Collection<Location> use = _sideEffect.getUseHeapLocs(_method);
		 * if(use.size()>0){ entrySet.add(new ReachingDU(entry,null,use)); }
		 */ 
		 
		return entrySet;
	} 
 
	protected Collection<ReachingDU> collectInvokeUses(Unit invokeStmt){
		Collection<ReachingDU> ruSet = new ArrayList<ReachingDU>(); 
	    CallGraph cg = Scene.v().getCallGraph();
	    
	    //for all use to global location
	    Set<Location> useGlobals = new HashSet<Location>();  
	    Callees callees = new Callees(cg, invokeStmt);
	    for(SootMethod tgt: callees.all()){	    	 
			if (!tgt.isConcrete())
				continue;
			
			Collection<Location> use = _sideEffect.getUseGlobals(tgt);
			useGlobals.addAll(use);
	    }
		
		for(Location gb: useGlobals){				
			AccessPath ap = AccessPath.getByRoot(gb);
			ReachingDU use = new ReachingDU(invokeStmt,ap,gb);			          
			ruSet.add(use);
		}
		
		// for heap use
		if(callees.all().size()==1){
			SootMethod tgt = callees.all().iterator().next();
			Collection<Location> useHeaps;
			if (tgt.isConcrete()){
				useHeaps = _sideEffect.getUseHeapLocs(tgt);				
			}
			else{
				useHeaps = getNativeCallUse(invokeStmt, tgt);
	    	}
			
			if(!useHeaps.isEmpty()){
				ReachingDU use = new ReachingDU(invokeStmt,null,useHeaps);			          
				ruSet.add(use);	 
			}
		}
		else{
			Set<Location> useLocs = new HashSet<Location>();
			for(SootMethod tgt: callees.all()) {	
				Collection<Location> calleeUses;
				if (tgt.isConcrete()){
					calleeUses = _sideEffect.getUseHeapLocs(tgt);
				}
				else{
					calleeUses = getNativeCallUse(invokeStmt, tgt);
				}
				useLocs.addAll(calleeUses);
			}
		 
			if (!useLocs.isEmpty()) {
				ReachingDU use = new ReachingDU(invokeStmt, null, useLocs);
				ruSet.add(use);
			}
		}
		
		return ruSet; 
	} 
	
	@SuppressWarnings("unchecked")
	private Collection<Location> getNativeCallUse(Unit u, SootMethod tgt){
		InvokeExpr invoke = ((Stmt)u).getInvokeExpr();
		Value receiver = null;
		if(!invoke.getMethod().isStatic()){
			InstanceInvokeExpr iie = (InstanceInvokeExpr)invoke;
			receiver = iie.getBase();
		}
		Collection<AccessPath> use = NativeMethodDUHelper.v().getUse(tgt, receiver, invoke.getArgs());
		if(use.size()==0){
			return Collections.emptyList();
		}
		else if(use.size()==1){
			AccessPath d = use.iterator().next();
			Collection<Location> useLocs = PtsToHelper.getAccessedLocations(_pt2Query, _heapAbstraction, u, d);
			return useLocs;
		}
		else{
			Collection<Location> locs = new HashSet<Location>();
			for(AccessPath d: use){
				Collection<Location> useLocs = PtsToHelper.getAccessedLocations(_pt2Query, _heapAbstraction, u, d);
				locs.addAll(useLocs);
			}
			return locs;
		}
	}
 

	/** Collect RU of each statement */
	protected Collection<ReachingDU> collectStmtDU(Unit stmt){
		if(stmt==CFGEntry.v()){
			return getEntryDU();
		}
		if(stmt==CFGExit.v() || stmt instanceof IdentityStmt){
			return Collections.emptyList();
		}  	
		
		Collection<ReachingDU> ruSet = new ArrayList<ReachingDU>();	
		
		Set<Value> uses = new HashSet<Value>();
		for(ValueBox box: stmt.getUseBoxes()){
			Value v = box.getValue();	
			uses.add(v);
		}
		
		for(Value v: uses){
			if(v instanceof Local){
				Location root = Location.valueToLocation((Local)v);
				AccessPath ap = AccessPath.getByRoot(root);
				ReachingDU ru = new ReachingDU(stmt,ap,root);		        
		        ruSet.add(ru);
			}
			else if((v instanceof FieldRef) || (v instanceof ArrayRef)){
				AccessPath ap = AccessPath.valueToAccessPath(_method, stmt, v);
				Collection<Location> duLocs = PtsToHelper.getAccessedLocations(_pt2Query, _heapAbstraction, stmt, ap);				
				ReachingDU ru = new ReachingDU(stmt,ap,duLocs);		         
		        ruSet.add(ru);
			}
			else if(v instanceof InvokeExpr){
				Collection<ReachingDU> rds = collectInvokeUses(stmt);
		        ruSet.addAll(rds);			    
			}
		} 
		
	    return ruSet;	
	}
}
