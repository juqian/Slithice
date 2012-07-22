package jqian.sootex.dependency;

import java.util.*;

import jqian.sootex.util.CFGExit;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.InverseGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.StronglyConnectedComponents;

public class DependencyHelper {
	static class PatchedGraph<N> implements DirectedGraph<N> {    	
    	final DirectedGraph<N> g;
    	final HashMap<N, HashSet<N>> node2preds = new HashMap<N, HashSet<N>>();
    	final HashMap<N, HashSet<N>> node2succs = new HashMap<N, HashSet<N>>();

    	public PatchedGraph(DirectedGraph<N> g) {
    		this.g = g;
    	}
    	
    	
    	private void addPredInternal(N n, N pred){
    		HashSet<N> preds = node2preds.get(n);
    		if(preds==null){
    			preds = new HashSet<N>();
    			node2preds.put(n, preds);
    		}
    		preds.add(pred);
    	}
    	
    	public void addSuccInternal(N n, N succ){
    		HashSet<N> succs = node2succs.get(n);
    		if(succs==null){
    			succs = new HashSet<N>();
    			node2succs.put(n, succs);
    		}
    		succs.add(succ);
    	}    	
    	
    	public void addPred(N n, N pred){
    		addPredInternal(n, pred);    		
    		addSuccInternal(pred, n);
    	}
    	
    	public void addSucc(N n, N succ){
    		addSuccInternal(n, succ);    		
    		addPredInternal(succ, n);
    	}    	

    	public List<N> getPredsOf(N s) {
    		HashSet<N> preds = node2preds.get(s);
    		if(preds!=null){
    			List<N> out = new ArrayList<N>(g.getPredsOf(s));
    			out.addAll(preds);
    			return out;
    		}
    		else{
    			return g.getPredsOf(s);
    		}    		
    	}

    	public List<N> getSuccsOf(N s) {
    		HashSet<N> succs = node2succs.get(s);
    		if(succs!=null){
    			List<N> out = new ArrayList<N>(g.getSuccsOf(s));
    			out.addAll(succs);
    			return out;
    		}
    		else{
    			return g.getSuccsOf(s);
    		}
    	}

    	public List<N> getHeads() {
    		return g.getHeads();
    	}
    	
    	public List<N> getTails() {
    		return g.getTails();
    	}

    	public Iterator<N> iterator() {
    		return g.iterator();
    	}

    	public int size() {
    		return g.size();
    	}
    }
	
	@SuppressWarnings({"rawtypes", "unchecked" })
	static DirectedGraph<Unit> patchCFG(DirectedGraph<Unit> graph){
		// XXX if there are infinite loops, patch edges to the graph so that the algorithm can work
		// StronglyConnectedComponentsFast<N> scc = new StronglyConnectedComponentsFast<N>(graph);
		StronglyConnectedComponents scc = new StronglyConnectedComponents(graph);
		List<List> components = scc.getComponents();
		
		Collection<Unit> tails = graph.getTails();
		PatchedGraph<Unit> pgraph = new PatchedGraph<Unit>(graph);		
		for (List<?> c : components) {
			HashSet s = new HashSet(c);
			
			boolean withSuccs = false;
			boolean containTails = false;

			for (Object n : c) {
				if (withSuccs) {
					break;
				}

				if (tails.contains(n)) {
					containTails = true;
					break;
				}

				List<Unit> succs = graph.getSuccsOf((Unit) n);
				for (Unit p : succs) {
					if (!s.contains(p)) {
						withSuccs = true;
						break;
					}
				}
			}

			// infinite loop if the condition is satisfied
			if (!withSuccs && !containTails) {
				Unit nodeWithPred = null;
				for (Object n : c) {
					if (nodeWithPred != null) {
						break;
					}

					List<Unit> preds = graph.getPredsOf((Unit) n);
					for (Unit p : preds) {
						if (!s.contains(p)) {
							nodeWithPred = (Unit) n;
							break;
						}
					}
				}

				pgraph.addSucc(nodeWithPred, tails.iterator().next());
			}
		}
		
		return pgraph;
	}

	/**
	 * Compute the control flow dependences for each statement. 
	 * Using the algorithm from:
	 *   Ferrantet et al. The Program Dependence Graph and Its Use in Optimization. TOPLAS, 1987.
	 * @note previous algorithm using INFL set seems incorrect.
	 * @note Currently only handle hammock graph
	 * 
	 * TODO: 这里用一个带权重的Worklist算法来进行后向必经节点迭代可能更快一些.
	 *       目前的版本未考虑迭代顺序, 迭代效率可能不高
	 */
	public static Map<Unit,Collection<Unit>> calcCtrlDependences(DirectedGraph<Unit> cfg){
		int size = cfg.size();
   	 	Map<Unit,Collection<Unit>> node2depend = new HashMap<Unit,Collection<Unit>>(size*2+1,0.7f);  
   	 
		// find post dominators and build post dominator tree
		//MHGPostDominatorsFinder postdomFinder = new MHGPostDominatorsFinder(cfg);
   	 	CFGDominatorsFinder<Unit> postdomFinder = new CFGDominatorsFinder<Unit>(new InverseGraph<Unit>(cfg));
   	 	while(postdomFinder.containInfiniteLoops()){
   	 		cfg = patchCFG(cfg);
   	 		postdomFinder = new CFGDominatorsFinder<Unit>(new InverseGraph<Unit>(cfg));
   	 	}   	 	
   	 	
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
