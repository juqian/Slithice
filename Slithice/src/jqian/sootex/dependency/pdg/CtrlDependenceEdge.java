package jqian.sootex.dependency.pdg;

/**
 * A control dependence edge.
 */
public class CtrlDependenceEdge extends DependenceEdge {
    public CtrlDependenceEdge(DependenceNode from,DependenceNode to){
        super(from,to);
    }
    
    public boolean equals(Object that){
    	if(that.getClass()!=this.getClass())
    		return false;
    	
    	return super.equals(that);
    }
    
    public int hashCode(){
    	return super.hashCode();
    }
}
