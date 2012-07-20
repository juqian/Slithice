package soot.jimple.spark.solver;

import java.util.*;
import soot.jimple.spark.pag.*;

/**
 * XXX: This is a FIX version of soot.jimple.spark.solver.TopoSorter.
 * When the PAG chain is too long, the old version can be stack overflowed.
 * We use a user defined stack instead of recursive call.
 *
 */
public class TopoSorter {
    /** Actually perform the topological sort on the PAG. */
    public void sort() {
        for(Iterator it = pag.getVarNodeNumberer().iterator(); it.hasNext(); ) {
            dfsVisit( (VarNode) it.next() );
        }
        visited = null;
    }
    public TopoSorter( PAG pag, boolean ignoreTypes ) {
        this.pag = pag;
        this.ignoreTypes = ignoreTypes;
        //this.visited = new NumberedSet( pag.getVarNodeNumberer() );
        this.visited = new HashSet<Node>();
    }
    
    /* End of public methods. */
    /* End of package methods. */

    protected boolean ignoreTypes;
    protected PAG pag;
    protected int nextFinishNumber = 1;
    protected HashSet<Node> visited;
    
    
    protected void dfsVisit( VarNode x ) {        
    	Stack<Node> stack = new Stack<Node>();
    	stack.push(x);
    	
    	while(!stack.isEmpty()){
    		VarNode node = (VarNode)stack.peek();
    		
    		if( visited.contains( node ) ){
    			node.setFinishingNumber( nextFinishNumber++ );
                stack.pop();
                continue;
    		}
    		
            visited.add( node );   		
           
            Node[] succs = pag.simpleLookup( node );
            for( int i = 0; i < succs.length; i++ ) {
                if( ignoreTypes 
                || pag.getTypeManager().castNeverFails(
                        node.getType(), succs[i].getType() ) ) {
                	VarNode succNode = (VarNode) succs[i];
                	if(!visited.contains(succNode)){
                		stack.push(succNode);                		
                	}                    
                }
            }            
    	}
    }
}
