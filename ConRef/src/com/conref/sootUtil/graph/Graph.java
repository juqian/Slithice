package com.conref.sootUtil.graph;

import java.util.*;

/** General graph structure for display. 
 *  Used as an intermediate representation to separate from 'dot' graph 'jgraph' graph, and etc.
 */
public class Graph extends GraphElement{
	private String _title;
    private Collection<GraphNode> _nodes;    
    private Map<GraphNode,Collection<GraphEdge>> _edgesOut;     //edges out of each node
    private Map<GraphNode,Collection<GraphEdge>> _edgesIn;      //edges into each node
 
    private int _edgeCount;
  
    public Graph(String title){
        this._title = title;
        
        _nodes =  new HashSet<GraphNode>();
        _edgesOut = new HashMap<GraphNode,Collection<GraphEdge>>();     
        _edgesIn = new HashMap<GraphNode,Collection<GraphEdge>>();     
    }   
    
    public String getTitle(){
    	return _title;
    }   
    
    public void setTitle(String title){
    	this._title = title;
    }
    
    public Collection<GraphNode> getNodes(){
        return _nodes;
    }  
  
     public boolean addNode(GraphNode node){
         return _nodes.add(node);
    }    
     
     public boolean addEdge(GraphEdge edge) {
    	GraphNode from = edge.src();
    	GraphNode to = edge.dest();
    	
    	boolean exist = true;
    	Collection<GraphEdge> outs = _edgesOut.get(from);
    	if(outs==null){
    		outs = createEdgeSet();
    		_edgesOut.put(from,outs);
    		exist = false; 
    	}
    	
    	Collection<GraphEdge> ins = _edgesIn.get(to);
    	if(ins==null){
    		ins = createEdgeSet();
    		_edgesIn.put(to,ins);
    		exist = false;
    	}
    	
    	exist = exist && outs.contains(edge); 
    	if(!exist){
    		outs.add(edge);
    		ins.add(edge);
    		_edgeCount++;
    	}     	
    	
        return !exist;
    }   
   
    public int getEdgeCount(){   
        return _edgeCount++;
    }
    
    public int getNodeCount(){   
        return _nodes.size();
    }
    
    public Collection<GraphEdge> edgesInto(GraphNode node){
    	Collection<GraphEdge> edges = _edgesIn.get(node);
    	if(edges!=null)
    		return edges;
    	else
    		return Collections.emptySet();
    }
    
    public Collection<GraphEdge> edgesOutOf(GraphNode node){
    	Collection<GraphEdge> edges = _edgesOut.get(node);
    	if(edges!=null)
    		return edges;
    	else
    		return Collections.emptySet();
    }  
    
    public String toString(){
        StringBuffer str = new StringBuffer("Graph: "+_title+"\n");
        for(GraphNode node: _nodes){
        	str.append("\n\nNode: "+node.toString());
        	str.append("\nIn edges ");
        	Collection<GraphEdge> ins = _edgesIn.get(node);
        	if(ins!=null){
        		str.append("("+ins.size()+")\n");
        		edgesToString(ins, str);
        	}
        	else{
        		str.append("(0)\n");
        	}
        	
        	str.append("\nOut edges:");
        	Collection<GraphEdge> outs = _edgesOut.get(node);
        	if(outs!=null){
        		str.append("("+outs.size()+")\n");
        		edgesToString(outs, str);
        	}
        	else{
        		str.append("(0)\n");
        	}
        }
        
        return str.toString();
    }
    
    private Set<GraphEdge> createEdgeSet(){
    	return new HashSet<GraphEdge>();
    }
    
    private void edgesToString(Collection<GraphEdge> edges, StringBuffer str){
    	for(GraphEdge e: edges){
    		str.append("   " + e + "\n");         
    	}    	
    }
}