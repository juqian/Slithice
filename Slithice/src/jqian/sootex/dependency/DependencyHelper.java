package jqian.sootex.dependency;

import java.util.*;

import jqian.sootex.util.CFGExit;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.MHGPostDominatorsFinder;

public class DependencyHelper {
	/**
	 * Compute the control flow dependences for each statement. 
	 * Using the algorithm from:
	 *   Ferrantet et al. The Program Dependence Graph and Its Use in Optimization. TOPLAS, 1987.
	 * @note previous algorithm using INFL set seems incorrect.
	 * @note Currently only handle hammock graph
	 */
	@SuppressWarnings("unchecked")
	public static Map<Unit,Collection<Unit>> calcCtrlDependences(DirectedGraph<Unit> cfg){
		int size = cfg.size();
   	 	Map<Unit,Collection<Unit>> node2depend = new HashMap<Unit,Collection<Unit>>(size*2+1,0.7f);  
   	 
		// find post dominators and build post dominator tree
		MHGPostDominatorsFinder postdomFinder = new MHGPostDominatorsFinder(cfg);
		DominatorTree postdomTree = new DominatorTree(postdomFinder);
		
		// process each conditional edge to find the control dependence relationships
        // a. a virtual edge from ENTRY -> START
		Unit exit = CFGExit.v();
		for(Unit head: cfg.getHeads()){
			DominatorNode startNode = postdomTree.getDode(head);
			DominatorNode endNode = postdomTree.getDode(exit);
			Collection<DominatorNode> ancestors = findAncestors(postdomTree, startNode, endNode, true);
			Collection<Unit> dependby = dominatorsToUnits(ancestors);			 
			addCtrlDep(node2depend, head, dependby);
		}
		 
		// b. other conditional edges
		for(Iterator<Unit> it=cfg.iterator(); it.hasNext(); ){
			Unit u = it.next();			
			List<Unit> succs = cfg.getSuccsOf(u);
			// conditional edges
			if(succs.size()>1){
				DominatorNode srcNode = postdomTree.getDode(u);	
				for(Unit tgt: succs){
					// skip edge to dominators 
					if(postdomFinder.isDominatedBy(u, tgt)){
						continue;
					}
					 
					DominatorNode tgtNode = postdomTree.getDode(tgt);	
					DominatorNode commonAncestor = findCommonAncestors(postdomTree, srcNode, tgtNode);
					
					Collection<DominatorNode> ancestors;
					if(commonAncestor==srcNode){
						//Case: L = A. All nodes in the post-dominator tree on the path from A to B,
						//including A and B, should be made control dependent on A.
						//XXX: exclude A to avoid loop dependence
						ancestors = findAncestors(postdomTree, tgtNode, srcNode, false);
						ancestors.add(tgtNode);
					}
					else{
						//Case. L = parent of A. All nodes in the post-dominator tree on the path
						//from L to B, including B but not L, should be made control dependent on A.
						ancestors = findAncestors(postdomTree, tgtNode, commonAncestor, false);
						ancestors.add(tgtNode);
					}
					
					Collection<Unit> dependby = dominatorsToUnits(ancestors);
					addCtrlDep(node2depend, u, dependby);
				}
			}			
		}

		return node2depend;
	}	
	
	private static Set<DominatorNode> findAncestors(DominatorTree postdomTree, 
										DominatorNode start, DominatorNode end, boolean includeEnd){
		Set<DominatorNode> ancestors = new HashSet<DominatorNode>();		
		DominatorNode parent = start.getParent();
		while(parent!=null && parent!=end){		 
			ancestors.add(parent);
			parent = parent.getParent();
		} 
		
		if(parent!=null && includeEnd){
			ancestors.add(parent);
		}
		
		return ancestors;
	}
	
	private static DominatorNode findCommonAncestors(DominatorTree postdomTree, DominatorNode n1, DominatorNode n2){
		Collection<DominatorNode> ancestors2 = findAncestors(postdomTree, n2, null, true);
		ancestors2.add(n2);
		DominatorNode parent = n1;
		while(parent!=null){		 
			if(ancestors2.contains(parent)){
				return parent;				 
			}
			
			parent = parent.getParent();
		}
		
		return null;
	}
	
	private static Collection<Unit> dominatorsToUnits(Collection<DominatorNode> dominators) {
		Collection<Unit> dependby = new ArrayList<Unit>(dominators.size());
		for (DominatorNode d : dominators) {
			Unit u = (Unit) d.getGode();
			dependby.add(u);
		}
		return dependby;
	}
	
	private static void addCtrlDep(Map<Unit, Collection<Unit>> node2depend, Unit n, Collection<Unit> dependby) {
		for (Unit dependbyUnit : dependby) {
			Collection<Unit> depend = node2depend.get(dependbyUnit);
			if (depend == null) {
				depend = new ArrayList<Unit>();
				node2depend.put(dependbyUnit, depend);
			}

			depend.add(n);
		}
	}
}
