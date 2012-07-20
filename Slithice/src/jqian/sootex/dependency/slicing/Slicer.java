package jqian.sootex.dependency.slicing;

import java.util.*;

import jqian.sootex.dependency.pdg.ActualNode;
import jqian.sootex.dependency.pdg.DataDependenceEdge;
import jqian.sootex.dependency.pdg.CtrlDependenceEdge;
import jqian.sootex.dependency.pdg.DependenceEdge;
import jqian.sootex.dependency.pdg.DependenceNode;
import jqian.sootex.dependency.pdg.PDG;
import jqian.sootex.location.Location;
import soot.*;
import soot.jimple.*;


/*
 * FIX 2008-07-19 fix the problem when selecting InvokeStmt as slicing criterion
 */
public abstract class Slicer {
	protected static interface TraverseHelper{		
		public boolean isExcluded(DependenceEdge e);
	}
	
	protected abstract Collection<DependenceEdge> getInEdges(DependenceNode n);
	//protected abstract Collection<DependenceEdge> getOutEdges(DependenceNode n);
	
	protected abstract PDG getDependenceGraph(MethodOrMethodContext mc);
	
	/**
	 * Get the starting points for slicing traverse
	 * @param criteriaNodes    DependenceNode's corresponding to the slicing criteria,
	 *                         This set is kept for constructing the final slices.
	 * @param startingNodes    DependenceNode's where the slicing traverse should start from. 
	 */
	protected void getStartingNodes(Collection<JimpleSlicingCriterion> stmtCriteria,
			                        Collection<DependenceNode> criteriaNodes,Collection<DependenceNode> startingNodes){
		for(JimpleSlicingCriterion criterion: stmtCriteria){    		
			Stmt stmt = (Stmt) criterion.statement();
    		Set<Location> variables = criterion.variables();
    		boolean postExecution = criterion.startFromPostExecution();
    		
    		PDG pdg = getDependenceGraph(criterion.context());
    		DependenceNode node = pdg.getStmtBindingNode(stmt);
    		criteriaNodes.add(node);
    		
    		//if no variable is specified
    		if(variables==null || variables.isEmpty()){
    			startingNodes.add(node); 
    			
    			if(stmt.containsInvokeExpr()){
    				pdg.getActualIns(startingNodes, stmt);
    			}    			
    		}
    		else{
    			Collection<DependenceEdge> inEdges = getInEdges(node);
    			for(DependenceEdge e: inEdges){
    				DependenceNode from = e.getFrom();
    				if(e instanceof CtrlDependenceEdge){
    					startingNodes.add(from);    						
    				}
    				else{
    					DataDependenceEdge dd = (DataDependenceEdge)e;
    					Object reason = dd.getReason();				
    					
    					//if(!(reason instanceof StackLocation))
    					//	continue;
    					
    					//Value v = ((StackLocation)reason).getValue();
    					if(reason==null || variables.contains(reason)){
    						startingNodes.add(from);
    					}
    				}
    			}
    				
    			// NOTE: Since a use defined slicing criterion can only
				// concerns on stack variables,
    			// we just handle definition statements simply
    			boolean isDefinedVarConcerned = false;
    			if(postExecution && stmt instanceof DefinitionStmt){
    				DefinitionStmt def = (DefinitionStmt)stmt;
    				String defedvar = def.getLeftOp().toString();
    				for(Location v: variables){
    					String name = v.toString();
    					if(name.equals(defedvar)){
    						isDefinedVarConcerned = true;
    						break;
    					}
    				}
    			}
    			
    			// add the node where slicing criteria is specified to starting nodes
    			if(isDefinedVarConcerned){
    				startingNodes.add(node);
    			}
    			
    			if(stmt.containsInvokeExpr()){ 
    				//get actual in nodes, and find starting nodes from them
    				List<ActualNode> actualIns = new LinkedList<ActualNode>();
    				pdg.getActualIns(actualIns, stmt);
    				for(ActualNode actual: actualIns){
    					Object loc = actual.getBinding();
    					//if(!(loc instanceof StackLocation)){
    					//	continue;
    					//}
    					
    					//Value binding = ((StackLocation)loc).getValue();
    					if(loc==null || variables.contains(loc)){
    						startingNodes.add(actual);
    					}
    				}    				
    				
    				//check whether the actual outs should be considered as starting nodes
    				if(isDefinedVarConcerned){
    					pdg.getActualOutsOfReturnValue(startingNodes, stmt);
    				}
    			}
    		}
    	}
	}
	
	protected void traverse(Set<DependenceNode> reached,TraverseHelper helper){
		Set<DependenceNode> worklistElmts = new HashSet<DependenceNode>();
    	Stack<DependenceNode> worklist = new Stack<DependenceNode>();
    	
    	//initialize worklist
    	for(DependenceNode node: reached){    	 
    		worklist.add(node);
    		worklistElmts.add(node);
    	}
    	
		//traversing
		while(!worklist.isEmpty()){
    		DependenceNode node = (DependenceNode)worklist.pop();
    		worklistElmts.remove(node);
    		reached.add(node);
    		
    		//get incoming edges    		  		
    		Collection<DependenceEdge> ins = getInEdges(node);    		
    		if(ins!=null){
    		    for (DependenceEdge edge: ins) {
					if(helper.isExcluded(edge))
						continue;
					
					DependenceNode from = edge.getFrom();
					if (!reached.contains(from) && !worklistElmts.contains(from)) {
						worklist.push(from);
					}
				}
    		}
    	}
	}

}
