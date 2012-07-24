package jqian.sootex.dependency.pdg.builder;

import java.util.*;

import jqian.sootex.dependency.pdg.DependenceNode;
import jqian.sootex.dependency.pdg.FormalIn;
import jqian.sootex.dependency.pdg.FormalNode;
import jqian.sootex.dependency.pdg.FormalOut;
import jqian.sootex.dependency.pdg.DepGraphOptions;
import jqian.sootex.dependency.pdg.JimpleStmtNode;
import jqian.sootex.du.IReachingDUQuery;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.location.Location;
import jqian.sootex.location.StackLocation;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.util.CFGEntry;
import soot.*;
import soot.toolkits.graph.*;

/**
 * An unsafe PDGBuilder that ignore the side effects of method calls.
 */
public class UnsafePDGBuilder extends AbstractPDGBuilder {
    public UnsafePDGBuilder(SootMethod m,UnitGraph cfg, DepGraphOptions pdgOptions,
    		IPtsToQuery ptsTo, HeapAbstraction heapAbstraction, IReachingDUQuery rd){
    	super(m,cfg,pdgOptions,ptsTo,heapAbstraction, rd);
    }  
    

    protected Collection<DependenceNode> getDefinitionNodes(Unit stmt,Location loc){
    	Collection<DependenceNode> nodes = new LinkedList<DependenceNode>();
    	if(stmt==CFGEntry.v() && loc instanceof StackLocation){
    		DependenceNode n = _pdg.getBindingFormal(loc, true);
    		nodes.add(n);
    	}
    	else{
    		DependenceNode src = _pdg.getStmtBindingNode(stmt);
    		nodes.add(src);
    	}
    	
    	return nodes;
    }
    
	protected void buildNodesForCall(Unit callsite) {
		JimpleStmtNode node = new JimpleStmtNode(_method, callsite);
		_pdg.addNode(node);
	}
    
    /**
     * This method currently defined to nothing, any PDG builder wants formal nodes
     * should overwrite this method
     */
    protected void buildFormals(){
    	//formal outs: only the return value are treated as formal outs
    	if(!(_method.getReturnType() instanceof VoidType)){
    		Location mod = Location.methodToRet(_method);
        	FormalNode node = new FormalOut(_method,mod);
        	_pdg.addNode(node);  
    	}    	  	
    	
    	//formal ins: only the parameters are treated as formal ins
    	int size = _method.getParameterCount();
    	for(int i=0;i<size;i++){   
    		Local l = _method.getActiveBody().getParameterLocal(i);
    		Location param = Location.valueToLocation(l);
    		DependenceNode node = new FormalIn(_method,param,i);
    		_pdg.addNode(node);
    	}
    	
    	//receiver
    	if(!_method.isStatic()){
    		Location loc = Location.getThisPointer(_method);
    		FormalNode node = new FormalIn(_method,loc,FormalIn.THIS_INDEX);
    		_pdg.addNode(node);    		
    	}
    } 

	/** [Unsafe] Build dependence for immediate arguments. Side effects are ignored. */
	protected void buildDepForInvoke(Unit curStmt, DependenceNode curNode) {
		for(ValueBox box: curStmt.getUseBoxes()){
			Value v = box.getValue();
			if(v instanceof Local){
				Location loc = Location.valueToLocation(v);
				buildDepForLocation(curStmt,loc,curNode);
			}
		}
	}


	@Override
	protected void buildFormalInDependences() {
	}


	@Override
	protected void buildFormalOutDependences() {
	}
}
