package jqian.sootex.util;

import jqian.sootex.util.graph.GraphHelper;
import jqian.sootex.util.graph.GraphHelper.LabelProvider;
import jqian.util.graph.Graph;
import jqian.util.graph.GraphEdge;
import jqian.util.graph.GraphNode;

import java.util.*;

import soot.*;
import soot.toolkits.graph.*;
import soot.util.*;
 

public class CFGViewer {
	protected DirectedGraph<Unit> _graph;
    protected SootMethod _method;
    
    public CFGViewer(SootMethod m,DirectedGraph<Unit> graph){
        this._graph = graph;
        this._method = m;
    }
     
    public Graph makeJimpleCFG(){
    	LabelProvider labelProvider = new LabelProvider(){
    		int index = 0;
    		public String getLabel(Object o){
    			Unit n = (Unit) o;   
    			index ++;
    			return "n"+index+": "+SootUtils.getStmtString(n);    			 
    		}
    	};
    		
    	Graph cfg = GraphHelper.toDisplayGraph(_graph, "Jimple CFG", labelProvider);
    	return cfg;
	}    
     
    public Graph makeJavaCFG(){
    	Graph cfg = new Graph("Java CFG");
        Body body = _method.getActiveBody();
        Chain<Unit> ch = body.getUnits();
    	Set<Integer> lineNumbers= new HashSet<Integer>();
        
    	for(Unit n: ch){
    		int line = SootUtils.getLine(n); 
    	    lineNumbers.add(new Integer(line));
    	}
    	
    	Map<Integer,GraphNode> line2Node = new HashMap<Integer,GraphNode>(lineNumbers.size()*2+1,0.7f);
    	for(Integer line: lineNumbers){
    		GraphNode node = new GraphNode(line.toString());
    	    line2Node.put(line,node);
    	} 
        
        for(Iterator<Unit> it = _graph.iterator();it.hasNext();) {
            Unit from = (Unit) it.next();
            List<Unit> lst = _graph.getSuccsOf(from);
            
            for(Unit to: lst){
                int fromLine=SootUtils.getLine(from);
                int toLine=SootUtils.getLine(to);
                
                if(fromLine==toLine){
                	//drop reflexive edges
                }
                else{
                	 GraphNode fromNode=(GraphNode)line2Node.get(fromLine);
                     GraphNode toNode=(GraphNode)line2Node.get(toLine);
                     cfg.addEdge(new GraphEdge(fromNode,toNode));   
                }
            }
        }    
        
        return cfg;
    }
}