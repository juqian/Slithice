/*  
 * @author Ju Qian{jqian@live.com}
 * @date 2007-6-11
 * @version 0.01
 */
package com.conref.sootUtil.graph;

import java.util.*;

import com.conref.sootUtil.graph.Graph;
import com.conref.sootUtil.graph.GraphEdge;
import com.conref.sootUtil.graph.GraphNode;

import soot.toolkits.graph.*;
/**
 * @author bruteforce
 *
 */
public class GraphHelper {
	public static Collection<?> classify(UndirectedGraph<Object> graph){		
		return graph.findConnectedComponents();
	}
	
	public static Set<?> getReachables(DirectedGraph<Object> graph,Object start){		
		return getReachables(graph, start,null);
	}
	
	public static Set<?> getReachables(DirectedGraph<Object> graph,Object start,Object block){		
		Collection<Object> starts = new ArrayList<Object>(1);
		starts.add(start);
		return getReachables(graph, starts,block);
	}
	
	public static Set<?> getReachables(DirectedGraph<Object> graph, Collection<?> starts,	Object block) {
		Set<Object> results = new HashSet<Object>();
		Stack<Object> stack = new Stack<Object>();
		Set<Object> inStack = new HashSet<Object>();

		stack.addAll(starts);
		inStack.addAll(starts);

		while (!stack.isEmpty()) {
			Object top = stack.pop();
			inStack.remove(top);

			//if meet a block node, can not pass it
			if(block!=null && top==block){
				continue;
			}
			
			if (!results.add(top)) {
				continue;
			}

			List<Object> nexts = graph.getSuccsOf(top);
			if (nexts == null)
				continue;

			for (Iterator<Object> it = nexts.iterator(); it.hasNext();) {
				Object succ = it.next();
				if (!results.contains(succ) && !inStack.contains(succ)) { 
					stack.add(succ);
					inStack.add(succ);
				}
			}
		}

		return results;
	}
	
	public static class LabelProvider{
		public String getLabel(Object o){
			return o.toString();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static Graph toDisplayGraph(DirectedGraph graph, String graphName){
		return toDisplayGraph(graph, graphName, new LabelProvider());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Graph toDisplayGraph(DirectedGraph graph, String graphName, LabelProvider labelProvider){
        Graph cfg = new Graph(graphName);         
        Map<Object,GraphNode> object2Node=new HashMap<Object,GraphNode>(graph.size()*2+1,0.7f);
  
        for (Iterator<Object> it = graph.iterator(); it.hasNext();) {
            Object o = it.next();      
            String label = labelProvider.getLabel(o);
            GraphNode node = new GraphNode(label);    
            cfg.addNode(node);
            object2Node.put(o, node);
        }

        for(Iterator<Object> it = graph.iterator();it.hasNext();){
        	Object from = it.next();
            List lst = graph.getSuccsOf(from);
            Iterator sit = lst.listIterator();
            while (sit.hasNext()) {
            	Object to = sit.next();
                GraphNode fromNode = object2Node.get(from);
                GraphNode toNode = object2Node.get(to);
                cfg.addEdge(new GraphEdge(fromNode,toNode));   
            }
        }  
        
        return cfg;
    }
}
