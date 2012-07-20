package jqian.sootex.util.graph;

import java.util.*;
import soot.toolkits.graph.*;

/**
 */
public abstract class BreathFirstSearch<N> {     
    private DirectedGraph<N> _graph;
    private N _start;
    private boolean _searchAll;
    
	/** Check whether an object on the DirectedGraph match the search condition. */
	public abstract boolean match(N obj);
	
    public BreathFirstSearch(DirectedGraph<N> graph, N start,boolean searchAll){
    	this._graph = graph;
    	this._start = start;
    	this._searchAll = searchAll;
    }
    
    public Collection<N> search(){
    	Collection<N> results = new HashSet<N>();
    	LinkedList<N> queue = new LinkedList<N>();
    	queue.add(_start);
    	
    	while(queue.size()>0){
    		N top = queue.removeFirst();
    		if(match(top)){
    			results.add(top);
    			if(!_searchAll) break;    				
    		}
    		
    		Collection<N> nexts = _graph.getSuccsOf(top);
    		queue.addAll(nexts);    		
    	}
    	
    	return results;
    }
}
