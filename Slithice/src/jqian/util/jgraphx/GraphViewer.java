package jqian.util.jgraphx;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import jqian.util.graph.Graph;
import jqian.util.graph.GraphEdge;
import jqian.util.graph.GraphNode;

import com.mxgraph.layout.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class GraphViewer extends JFrame
{
	private static final long serialVersionUID = -2707712944901661771L;

	public GraphViewer(Graph graph)
	{
		super(graph.getTitle());
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		mxGraph mxgraph = new mxGraph();
		Object parent = mxgraph.getDefaultParent();

		mxgraph.setAutoOrigin(true);
		mxgraph.setCellsEditable(false);
		mxgraph.setCellsResizable(false);
		mxgraph.setCellsBendable(false);
		mxgraph.setAllowDanglingEdges(false);
		mxgraph.setSplitEnabled(false);
		mxgraph.setKeepEdgesInForeground(false);
		mxgraph.setKeepEdgesInBackground(true);
				
		mxgraph.getModel().beginUpdate();
		
		try
		{
			Map<GraphNode,Object> toJGraph = new HashMap<GraphNode, Object>(graph.getNodeCount()*2+1,0.7f);
			for(GraphNode n: graph.getNodes()){
				Object v = mxgraph.insertVertex(parent, null, n.getLabel(), 0, 0, 150, 30);
				toJGraph.put(n, v);
			}
			
			for(GraphNode from: graph.getNodes()){
				Collection<GraphEdge> edges = graph.edgesOutOf(from);
	        	for(GraphEdge e: edges){
	        		Object fromNode = toJGraph.get(from);
	                Object toNode = toJGraph.get(e.dest());
	                mxgraph.insertEdge(parent, null, "", fromNode, toNode);
	        	}
	        }
			
			mxGraphLayout layout = new mxHierarchicalLayout(mxgraph);
			layout.execute(parent);
		
			//mxGraphLayout layout = new mxStackLayout(mxgraph, true, 25);
			//layout.execute(parent);			

			//mxCircleLayout layout = new mxCircleLayout(mxgraph, 100.0);
			//layout.execute(parent);
			
			//mxGraphLayout layout = new mxFastOrganicLayout(mxgraph);
			//layout.execute(parent);
		}
		finally
		{
			mxgraph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(mxgraph);
		graphComponent.setCenterPage(true);
		graphComponent.getViewport().setOpaque(false);
		graphComponent.setOpaque(true);
		graphComponent.setBackground(Color.decode("#FFFFFF"));
	 
		getContentPane().add(graphComponent);
	}
	
	/** for test */
	GraphViewer(String title)
	{
		super(title);

		mxGraph mxgraph = new mxGraph();
		Object parent = mxgraph.getDefaultParent();

		mxgraph.setAutoOrigin(true);
		mxgraph.setCellsEditable(false);
		mxgraph.setCellsResizable(false);
		mxgraph.setCellsBendable(false);
		mxgraph.setAllowDanglingEdges(false);
		mxgraph.setSplitEnabled(false);
		mxgraph.setKeepEdgesInForeground(false);
		mxgraph.setKeepEdgesInBackground(true);
		
		mxgraph.getModel().beginUpdate();
		
		try
		{
			Object v1 = mxgraph.insertVertex(parent, null, "Hello", 20, 20, 80, 30);
			Object v2 = mxgraph.insertVertex(parent, null, "World!", 240, 150, 80, 30);
			//Object v1 = graph.insertVertex(parent, null, "Hello", 0, 0, 80, 30);
			//Object v2 = graph.insertVertex(parent, null, "World!", 0, 0, 80, 30);
			mxgraph.insertEdge(parent, null, "Edge", v1, v2);
			
			mxHierarchicalLayout layout = new mxHierarchicalLayout(mxgraph);
			layout.execute(parent);

		}
		finally
		{
			mxgraph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(mxgraph);
		getContentPane().add(graphComponent);
	}

	public static void main(String[] args)
	{
		GraphViewer frame = new GraphViewer("Hello, World!");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

}
