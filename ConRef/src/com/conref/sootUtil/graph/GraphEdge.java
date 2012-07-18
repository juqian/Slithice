package com.conref.sootUtil.graph;

/**
 * A graph edge with attributes
 */
public class GraphEdge extends GraphElement{
	private GraphNode _src;
    private GraphNode _dest;  
    
    public GraphEdge(GraphNode src, GraphNode dest){
        this._src = src;
        this._dest = dest;
    }
    
    public GraphNode src(){
    	return _src;
    }
    
	public GraphNode dest(){
		return _dest;
	}
	
	public String toString(){
		return ""+_src + " --> " + _dest;
	}
}
