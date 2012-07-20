/** 
 *  Transform a graph structure in to Grappa Graph and use dot to show it. 
 */

package jqian.util.dot;

import java.io.*;
import java.util.*;

import jqian.util.Utils;
import jqian.util.graph.GraphEdge;
import jqian.util.graph.GraphNode;
import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.GrappaConstants;
import att.grappa.Node;


/**
 *
 */
public class GrappaGraph extends Graph {
	public GrappaGraph(String name){
		super(name);
	}
	
    public GrappaGraph(jqian.util.graph.Graph graph) {
        super(graph.getTitle());
        
    	this.setAttribute("fontsize","9");//new Integer(9));
          
        HashMap<GraphNode, Node> tograppa = new HashMap<GraphNode, Node>(graph.getNodeCount()*2+1,0.7f);
        
        for(GraphNode n: graph.getNodes()){
        	Node node = new Node(this, n.getLabel());    
            node.setAttribute(GrappaConstants.FONTSIZE_ATTR,"9");
            tograppa.put(n, node);
        } 
        
        for(GraphNode from: graph.getNodes()){
        	Collection<GraphEdge> edges = graph.edgesOutOf(from);
        	for(GraphEdge e: edges){
        		Node fromNode = tograppa.get(from);
                Node toNode = tograppa.get(e.dest());
                this.addEdge(new Edge(this,fromNode,toNode)); 
        	}        	 
        }
    } 
    
    public void saveToDot(String file){
        try{   
        	Utils.assureDirectory(file);
        	BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            PrintWriter pw=new PrintWriter(bw,true);
            printGraph(pw);  
    	}
    	catch(Exception e) {			
			e.printStackTrace(System.err);
		}
    }
}
