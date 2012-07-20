package jqian.sootex.util.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorTree;

/**
 *
 */
@SuppressWarnings("unchecked")
public class DominatorTreeGraph implements DirectedGraph<DominatorNode>{	 
	DominatorTree tree;
	
	public DominatorTreeGraph(DominatorTree tree){
		this.tree = tree;
	}

	public List<DominatorNode> getHeads() {
		List<DominatorNode> list = new ArrayList<DominatorNode>(1);
		list.add(tree.getHead());
		return list;
	}
	
	public List<DominatorNode> getTails() {
		return tree.getTails();
	}

	public List<DominatorNode> getPredsOf(DominatorNode s) {
		List<DominatorNode> list = new ArrayList<DominatorNode>(1);
		list.add(s.getParent());
		return list;
	}

	public List<DominatorNode> getSuccsOf(DominatorNode s) {
		return s.getChildren();
	}

	public int size() {
		return tree.size();
	}

	public Iterator<DominatorNode> iterator() {
		return tree.iterator();
	}
}
