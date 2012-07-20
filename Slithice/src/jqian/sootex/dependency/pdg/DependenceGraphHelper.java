package jqian.sootex.dependency.pdg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.toolkits.graph.DirectedGraph;
import jqian.sootex.CFGProvider;
import jqian.sootex.HammockCFGProvider;
import jqian.sootex.dependency.pdg.builder.SDGBuilder;
import jqian.sootex.du.DUBuilder;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.sideeffect.ISideEffectAnalysis;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.graph.GraphHelper;
import jqian.util.dot.DotViewer;
import jqian.util.dot.GrappaGraph;
import jqian.util.graph.Graph;
import jqian.util.jgraphx.GraphViewer;
import att.grappa.Edge;
import att.grappa.GrappaConstants;
import att.grappa.Node;

/**
 *
 */
public class DependenceGraphHelper {
	public static class LabelProvider {
		public String getLabel(DependenceNode node) {
			return node.toString();
		}

		public String getName(DependenceNode node) {
			if (node instanceof JavaStmtNode || node instanceof JimpleStmtNode) {
				return node.getNumber() + ":" + node.toString();
			}
			return node.toString();
		}
	}

	private static String getReasonString(DataDependenceEdge dataDep) {
		Object reason = dataDep.getReason();
		if (reason != null) {
			String label = "";
			if (reason instanceof SootField) {
				label = SootUtils.getShortFieldString((SootField) reason);
			} else if (reason instanceof Type) {
				label = reason.toString() + "[x]";
			} else {
				label = reason.toString();
			}

			if (label.length() > 20)
				label = label.substring(0, 20) + "...";

			return label;
		}

		return null;
	}

	public static GrappaGraph toGrappaGraph(PDG pdg) {
		return toGrappaGraph(pdg, null);
	}
	
	public static GrappaGraph toGrappaGraph(PDG pdg, Collection<DependenceNode> highlightedNodes) {
		LabelProvider fetcher = new LabelProvider();
		return toGrappaGraph(pdg, null, highlightedNodes, null, fetcher);
	}

	public static GrappaGraph toGrappaGraph(PDG pdg, Collection<DependenceNode> highlightedNodes, LabelProvider fetcher) {
		return toGrappaGraph(pdg, null, highlightedNodes, null, fetcher);
	} 

	/**
	 * @param highlightedNodes  This set can be null
	 */
	protected static GrappaGraph toGrappaGraph(PDG pdg, GrappaGraph depGraph,
			Collection<DependenceNode> highlightedNodes, Map<DependenceNode,Node> dep2grappa,
			LabelProvider lableFetcher) {
		if (depGraph == null) {
			depGraph = new GrappaGraph("Dependence Graph");
			depGraph.setMenuable(true);
			depGraph.setEditable(true);
			depGraph.setAttribute("fontsize", "8");
			//depGraph.setNodeAttribute("style","rounded");//"ellipse");
			// depGraph.setAttribute("fontstyle","bold");
			// depGraph.setNodeAttribute("shape","box");//"ellipse");
			// depGraph.setNodeAttribute("style","filled");
			// depGraph.setNodeAttribute("color","beige");//"darkgreen"
			// depGraph.setNodeAttribute("tip","A Node"); //"An Edge"
			// depGraph.setEdgeAttribute("instances","1");
		}

		Map<DependenceNode,Node> depNode2GrappaNode = new HashMap<DependenceNode,Node>(pdg.getNodes().size() * 2 + 1, 0.7f);

		// map each dependence graph node to grappa grah node
		for (DependenceNode depNode : pdg.getNodes()) {
			String name = lableFetcher.getName(depNode);
			String label = lableFetcher.getLabel(depNode);
			Node grappaNode = new Node(depGraph, name);
			grappaNode.setAttribute(GrappaConstants.LABEL_ATTR, label);
			//grappaNode.setAttribute("shape","box");//"ellipse");
			//grappaNode.setAttribute("labelfontsize","10");

			if (depNode instanceof FormalNode || depNode instanceof ActualNode) {
				//grappaNode.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
				grappaNode.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
				grappaNode.setAttribute(GrappaConstants.FILLCOLOR_ATTR, "gray");
				if (dep2grappa != null)
					dep2grappa.put(depNode, grappaNode);
			} else if (depNode instanceof EntryNode || depNode instanceof CallNode) {
				if (dep2grappa != null)
					dep2grappa.put(depNode, grappaNode);
			} else if (depNode instanceof JavaStmtNode) {
				if (dep2grappa != null && ((JavaStmtNode) depNode).isCallSite())
					dep2grappa.put(depNode, grappaNode);
			}

			if (highlightedNodes != null && highlightedNodes.contains(depNode)) {
				grappaNode.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
				grappaNode.setAttribute(GrappaConstants.FILLCOLOR_ATTR, "yellow");
			}

			grappaNode.setAttribute(GrappaConstants.FONTSIZE_ATTR, "8");
			depNode2GrappaNode.put(depNode, grappaNode);
		}

		// map each dependence edge to grappa graph edge
		for (DependenceNode depNode : pdg.getNodes()) {
			Node grappaFrom = (Node) depNode2GrappaNode.get(depNode);
			Collection<?> edges = pdg.edgesOutOf(depNode);
			if (edges != null) {
				for (Iterator<?> egIt = edges.iterator(); egIt.hasNext();) {
					DependenceEdge edge = (DependenceEdge) egIt.next();
					DependenceNode to = edge.getTo();
					Node grappaTo = (Node) depNode2GrappaNode.get(to);
					Edge grappaEdge = new Edge(depGraph,grappaFrom, grappaTo);

					setGrappaEdgeProperty(edge, grappaEdge);
					depGraph.addEdge(grappaEdge);
				}
			}
		}
		return depGraph;
	}

	static void setGrappaEdgeProperty(DependenceEdge edge, Edge grappaEdge) {
		if (edge instanceof CtrlDependenceEdge) {
			grappaEdge.setAttribute(GrappaConstants.STYLE_ATTR, "bold");
		} else if (edge instanceof DataDependenceEdge) {
			DataDependenceEdge dataDep = (DataDependenceEdge) edge;
			String label = getReasonString(dataDep);
			if (label != null) {
				grappaEdge.setAttribute(GrappaConstants.FONTSIZE_ATTR, "9");
				grappaEdge.setAttribute(GrappaConstants.LABEL_ATTR, label);
			}

			if (edge instanceof SummaryEdge) {
				grappaEdge.setAttribute(GrappaConstants.STYLE_ATTR, "dotted");
			}
		}
	}

	
	public static GrappaGraph toGrappaGraph(SDG sdg) {
		return toGrappaGraph(sdg, null);
	}

	public static GrappaGraph toGrappaGraph(SDG sdg, Collection<DependenceNode> highlightedNodes) {
		LabelProvider fetcher = new LabelProvider();
		return toGrappaGraph(sdg, highlightedNodes, fetcher);
	}

	public static GrappaGraph toGrappaGraph(SDG sdg, Collection<DependenceNode> highlighted, LabelProvider labelFetcher) {
		GrappaGraph sdgGraph = new GrappaGraph("SDG");
		sdgGraph.setMenuable(true);
		sdgGraph.setEditable(true);
		sdgGraph.setAttribute("fontsize", "8");
		//sdgGraph.setNodeAttribute("shape","box");//"ellipse");

		Map<DependenceNode,Node> dep2grappa = new HashMap<DependenceNode,Node>();
		for (PDG pdg: sdg.getPDGs()) {		 
			toGrappaGraph(pdg, sdgGraph, highlighted, dep2grappa, labelFetcher);
		}

		for (Map.Entry<DependenceNode, Collection<DependenceEdge>> entry : sdg.getExtraEdges()) {
			DependenceNode node = entry.getKey();
			Node to = (Node) dep2grappa.get(node);

			Collection<DependenceEdge> inEdges = entry.getValue();
			for (DependenceEdge e : inEdges) {
				DependenceNode fromNode = e.getFrom();
				Node from = (Node) dep2grappa.get(fromNode);

				Edge grappaEdge = new Edge(sdgGraph, from, to);

				setGrappaEdgeProperty(e, grappaEdge);
				sdgGraph.addEdge(grappaEdge);
			}
		}

		return sdgGraph;
	}
	
	
    public static void dotSlice(DependenceGraph sdg, Set<DependenceNode> slice, String dotToolPath, 
    		                    String dotfile, boolean displayed){
    	GrappaGraph sliceGraph = null;
    	if(sdg instanceof PDG){
    		sliceGraph = DependenceGraphHelper.toGrappaGraph((PDG)sdg, slice);
    	}
    	else if(sdg instanceof SDG){
    		sliceGraph = DependenceGraphHelper.toGrappaGraph((SDG)sdg, slice);
    	}     
    	sliceGraph.saveToDot(dotfile);
    	
    	DotViewer dotView = new DotViewer(dotToolPath,dotfile);
	    dotView.dotIt();
	    
	    if(displayed){
	    	dotView.view(); 
	    }
    } 
    
    
    public static void dotDependenceGraph(DependenceGraph depGraph, String dotToolPath, String dotfile, boolean displayed){
    	GrappaGraph graph = null;
    	if(depGraph instanceof PDG){
    		graph = DependenceGraphHelper.toGrappaGraph((PDG)depGraph);
    	}
    	else if(depGraph instanceof SDG){
    		graph = DependenceGraphHelper.toGrappaGraph((SDG)depGraph);
    	}   
    	graph.saveToDot(dotfile);
    	
    	DotViewer dotView = new DotViewer(dotToolPath,dotfile);
	    dotView.dotIt();
	    
	    if(displayed){
	    	dotView.view(); 
	    }
	}
    
	public static void showDependenceGraphByJGraph(DependenceGraph depGraph, String title){
		DirectedGraph<DependenceNode> dgraph = depGraph.toDirectedGraph(); 
		Graph graph = GraphHelper.toDisplayGraph(dgraph, title);   
		GraphViewer frame = new GraphViewer(graph);
		frame.setSize(1000, 700);
		frame.setVisible(true);
	}
	
	public static SDG constructSDG(IPtsToQuery ptsto, HeapAbstraction heapAbstraction, 
			ISideEffectAnalysis se, SootMethod entry, DepGraphOptions opts, boolean verbose, int javaLibDepth){
    	Collection<SootMethod> entries = new ArrayList<SootMethod>(1);
		entries.add(entry);

		// build def-use analysis
		CFGProvider cfgProvider = new HammockCFGProvider();
		DUBuilder rdb = new DUBuilder(cfgProvider, ptsto, heapAbstraction, se, false);
 
		SDGBuilder sdgBuilder = new SDGBuilder(entries,
				opts, cfgProvider, ptsto, heapAbstraction, rdb.getGlobalDUQuery(), se, verbose, javaLibDepth); 
 
		// initialize SDG builder
		sdgBuilder.preBuild();
		sdgBuilder.buildAll();
		sdgBuilder.postBuild();

		SDG sdg = sdgBuilder.getSDG();
		sdg.buildSummaryEdges(entry);
		sdg.connectPDGs(); 
     
		// clean unnecessary info in points-to analysis
		SootUtils.cleanPAG();
		
		// force clean, the following statements just notice that these should be released
		// they are redundant here
		se = null;
		rdb = null;
		sdgBuilder = null;
		
        return sdg;
	}
}
