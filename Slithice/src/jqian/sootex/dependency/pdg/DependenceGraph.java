package jqian.sootex.dependency.pdg;

import java.util.*;
import soot.toolkits.graph.*; 

/**
 *
 */
public interface DependenceGraph{   
    public Collection<DependenceNode> getNodes();
    public int getEdgeCount();
    public Collection<DependenceEdge> edgesOutOf(DependenceNode node);
    public Collection<DependenceEdge> edgesInto(DependenceNode node);
    
    /**Get a graph compatible with the DirectedGraph interface*/
    public DirectedGraph<DependenceNode> toDirectedGraph();    
}
