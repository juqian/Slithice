package jqian.sootex.util.callgraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import jqian.sootex.util.SootUtils;
import jqian.util.dot.DotViewer;
import jqian.util.dot.GrappaGraph;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import att.grappa.GrappaConstants;
import att.grappa.Node;


/**
 * @author bruteforce
 *
 */
public class CallGraphDotter {
	private static Node assureMethodNode(GrappaGraph graph,Node[] method2node,SootMethod m){
		int id = m.getNumber();
		Node node = method2node[id];
		if(node==null){
			String nodeName = m.toString();
	        node = new Node(graph,nodeName);   
	        node.setAttribute(GrappaConstants.FONTSIZE_ATTR,"9");
	        graph.addNode(node);
	        
	        method2node[id] = node;
		}
		
		return node;
	}
	
	static class MethodBox{
		MethodBox(SootMethod m,int depth){
			this.method = m;
			this.depth = depth;
		}
		SootMethod method;
		int depth;
	}
	
	
	public static void dot(CallGraph cg, SootMethod entry, int depth, String dotpath, String file){
		Node[] method2node = new Node[SootUtils.getMethodCount()];
		
		GrappaGraph graph = new GrappaGraph("Call Graph");
    	graph.setAttribute("fontsize","8");

	    Set<SootMethod> processed = new HashSet<SootMethod>();
	    Stack<MethodBox> stack = new Stack<MethodBox>();
	    
	    MethodBox box = new MethodBox(entry,0);
		stack.add(box);
		    
		while (!stack.isEmpty()) {
			box = stack.pop();			
			SootMethod m = box.method;
			int curDepth = box.depth;
			
			Node from = assureMethodNode(graph,method2node,m);
			
			// if already processed or reach depth limitation
			if (!processed.add(m) || curDepth>depth) {
				continue; 
			}
			
			curDepth++;
			for (Iterator<Edge> it = cg.edgesOutOf(m); it.hasNext();) {
				Edge e = (Edge) it.next();
				SootMethod tgt = e.tgt();
				
				Node to= assureMethodNode(graph,method2node,tgt);
				graph.addEdge(new  att.grappa.Edge(graph,from,to));

				if (!processed.contains(tgt)) {
					box = new MethodBox(tgt,curDepth);
					stack.add(box);
				}
			}
		} 
	    
	    //save to dot file	     
        graph.saveToDot(file);
        
        //Convert .dot to .jpg file
        DotViewer dot=new DotViewer(dotpath, file, "jpg");
        dot.dotIt();
	}
}
