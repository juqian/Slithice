package jqian.sootex.dependency.slicing;

import soot.MethodOrMethodContext;

import java.util.*;

import jqian.sootex.dependency.pdg.DependenceEdge;
import jqian.sootex.dependency.pdg.DependenceNode;
import jqian.sootex.dependency.pdg.PDG;

/**
 * Perform intraprocedural slicing. 
 */
public class LocalSlicer extends Slicer{
    public LocalSlicer(PDG pdg){
    	this._pdg = pdg;
    }
    
    protected PDG getDependenceGraph(MethodOrMethodContext mc){
    	return _pdg;
    }
    
	public Collection<DependenceEdge> getInEdges(DependenceNode n){
		return _pdg.edgesInto(n);
	}
    
    protected class MyTraverseHelper implements TraverseHelper{
		public boolean isExcluded(DependenceEdge e){
			return false;
		}
	}
    
    public Set<DependenceNode> slice(Collection<JimpleSlicingCriterion> stmtCriteria){
    	Set<DependenceNode> result = new HashSet<DependenceNode>();  
    	Set<DependenceNode> criteriaNodes = new HashSet<DependenceNode>();
    	getStartingNodes(stmtCriteria,criteriaNodes,result);
    	
    	traverse(result,new MyTraverseHelper());
    	
    	// XXX: include the criterion site itself
    	result.addAll(criteriaNodes);
    	return result;
    }
        
    private PDG _pdg;
}
