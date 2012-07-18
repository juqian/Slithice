package com.conref.sootUtil.callgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.MethodOrMethodContext;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.DirectedGraph;

/** Turn a call graph into a DirectedGraph interface. */	
@SuppressWarnings("unchecked")
public class DirectedCallGraph implements DirectedGraph<MethodOrMethodContext>{
	private Map<MethodOrMethodContext,List<MethodOrMethodContext>> _node2succs;
	private Map<MethodOrMethodContext,List<MethodOrMethodContext>> _node2preds;
	private List<MethodOrMethodContext> _heads;
	private List<MethodOrMethodContext> _tails;
	
	public DirectedCallGraph(CallGraph cg, Collection<MethodOrMethodContext> entries){
		this(cg, entries, new CallGraphNodeFilter(), new CallGraphEdgeFilter());
	}
	
	public DirectedCallGraph(CallGraph cg, Collection<MethodOrMethodContext> entries, CallGraphNodeFilter nodeFilter, CallGraphEdgeFilter edgeFilter){
		_node2succs = new HashMap<MethodOrMethodContext,List<MethodOrMethodContext>>();
		_node2preds = new HashMap<MethodOrMethodContext,List<MethodOrMethodContext>>(); 
		_heads = new ArrayList<MethodOrMethodContext>(100);
		_tails = new ArrayList<MethodOrMethodContext>(100);
		Set<MethodOrMethodContext> allTgts = new HashSet<MethodOrMethodContext>();
	 
		for(Iterator<MethodOrMethodContext> it=cg.sourceMethods();it.hasNext();){
			MethodOrMethodContext m = it.next();
			if(nodeFilter.isIngored(m)){
				continue;
			} 
 
			Set<MethodOrMethodContext> predSet = new HashSet<MethodOrMethodContext>();
			for(Iterator<Edge> eIt=cg.edgesInto(m);eIt.hasNext();){
				Edge e = eIt.next();
				MethodOrMethodContext src = e.getSrc();	
				
				if(!nodeFilter.isIngored(src) && !edgeFilter.isIgnored(e)){
					predSet.add(src);
				}
			} 
			
			Set<MethodOrMethodContext> succSet = new HashSet<MethodOrMethodContext>();
			for(Iterator<Edge> eIt=cg.edgesOutOf(m);eIt.hasNext();){
				Edge e = eIt.next();
				MethodOrMethodContext tgt = e.getTgt();					
				if(!nodeFilter.isIngored(tgt) && !edgeFilter.isIgnored(e)){
					succSet.add(tgt);
				}
			}  
			
			List<MethodOrMethodContext> preds = new ArrayList<MethodOrMethodContext>(predSet);
			List<MethodOrMethodContext> succs = new ArrayList<MethodOrMethodContext>(succSet);
			_node2preds.put(m, preds);
			_node2succs.put(m, succs);			
			
			if(preds.isEmpty()){
				_heads.add(m);
			}
			
			if(succs.isEmpty()){
				_tails.add(m);
			} 
			 
			allTgts.addAll(succSet);
		}
		
		// some nodes not reachable by sourceMethods() call
		for(MethodOrMethodContext m: allTgts){
			if(_node2succs.get(m)!=null){
				continue;
			} 
			 
			_node2succs.put(m, Collections.EMPTY_LIST);
			_tails.add(m);

			Set<MethodOrMethodContext> predSet = new HashSet<MethodOrMethodContext>();
			for (Iterator<Edge> eIt = cg.edgesInto(m); eIt.hasNext();) {
				Edge e = eIt.next();
				MethodOrMethodContext src = e.getSrc();

				if (!nodeFilter.isIngored(src) && !edgeFilter.isIgnored(e)) {
					predSet.add(src);
				}
			}

			List<MethodOrMethodContext> preds = new ArrayList<MethodOrMethodContext>(predSet);
			_node2preds.put(m, preds);
		}
		
		//augment with entries methods that has not out coming edge
		for(MethodOrMethodContext m: entries){
			if(_node2succs.get(m)==null){
				_node2succs.put(m, Collections.EMPTY_LIST);
			}
			if(_node2preds.get(m)==null){
				_node2preds.put(m, Collections.EMPTY_LIST);
				_heads.add(m);
			}
		}
	} 
	
	public List<MethodOrMethodContext> getHeads(){			
		return _heads;
	}
	
	public List<MethodOrMethodContext> getTails(){
		return _tails;
	}
	
	public List<MethodOrMethodContext> getPredsOf(MethodOrMethodContext s){		 
		List<MethodOrMethodContext> preds = _node2preds.get(s);
		if(preds==null)
			return Collections.EMPTY_LIST;
		else
			return preds;
	}

	public List<MethodOrMethodContext> getSuccsOf(MethodOrMethodContext s){
		List<MethodOrMethodContext> succs = _node2succs.get(s);
		if(succs==null)
			return Collections.EMPTY_LIST;
		else
			return succs;
	}

	public Iterator<MethodOrMethodContext> iterator(){
		return _node2succs.keySet().iterator();
	}

	public int size() {
		return _node2succs.keySet().size();
	}
}