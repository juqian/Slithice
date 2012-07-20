package jqian.sootex.dependency.slicing;

import java.util.*;

import jqian.sootex.dependency.pdg.*;
import soot.MethodOrMethodContext;

/**
 * Horwitz-Reps-Binkely two phased slicing algorithm
 */
public class GlobalSlicer extends Slicer{
	public GlobalSlicer(SDG sdg){
    	this._sdg = sdg;
    }
	
	public Set<DependenceNode> slice(Collection<JimpleSlicingCriterion> stmtCriteria){
		Set<DependenceNode> reached = new HashSet<DependenceNode>();		
		Set<DependenceNode> criteriaNodes = new HashSet<DependenceNode>();
		
    	getStartingNodes(stmtCriteria,criteriaNodes,reached);
		
		phase1(reached);
		phase2(reached);
		
		reached.addAll(criteriaNodes);
		return reached;
	}
	
	protected PDG getDependenceGraph(MethodOrMethodContext mc){
    	return _sdg.getPDG(mc);
    }
	
	public Collection<DependenceEdge> getInEdges(DependenceNode n){
		return _sdg.edgesInto(n);			
	}
   
	
    /** excluding parameter-out edge. */
	protected void phase1(Set<DependenceNode> reached){
	    class Helper implements TraverseHelper{			
			public boolean isExcluded(DependenceEdge e){
				if((e.getFrom() instanceof FormalOut) &&
				   (e.getTo() instanceof ActualOut)){
					return true;
				}
				
				return false;
			}
		}
	    
		traverse(reached,new Helper());
	}	
	
	/** excluding parameter-in edge and call edge. */
	protected void phase2(Set<DependenceNode> reached){
		class Helper implements TraverseHelper{			
			public boolean isExcluded(DependenceEdge e){
				DependenceNode from = e.getFrom();
				DependenceNode to = e.getTo();
				if((from instanceof ActualIn && to instanceof FormalIn) ||
				   (from instanceof CallNode && to instanceof EntryNode)){
					return true;
				}
				
				return false;
			}
		}
	    
		traverse(reached,new Helper());
	}	
	
	protected SDG _sdg;
}
