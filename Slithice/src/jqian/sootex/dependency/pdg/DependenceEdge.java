package jqian.sootex.dependency.pdg;


public abstract class DependenceEdge
{
    //dependence edge types
    public static final int STACK_DEP = 0;   
    public static final int GLOBAL_DEP = 1;
    public static final int HEAP_DEP = 2;
    public static final int CTRL_DEP = 3;
    public static final int UNDISTINGUSIHED_DATA_DEP = 4;
    
    private final DependenceNode _from;
    private final DependenceNode _to;    
    
    /** NOTE Here edges are maintained separately from the dependence nodes.*/
    protected DependenceEdge(DependenceNode from,DependenceNode to){
        this._from=from;
        this._to=to;
    }
    
    public final DependenceNode getFrom(){
        return _from;
    }
    
    public final DependenceNode getTo(){
        return _to;
    }
    
    public boolean equals(Object that){
    	if(that.getClass()!=this.getClass())
    		return false;
    	    	
    	DependenceEdge thatEdge = (DependenceEdge)that;
    	if(this._from.equals(thatEdge._from) && this._to.equals(thatEdge._to))
    		return true;
    	
    	return false;
    }
    
    public int hashCode(){
    	long hash = _from.hashCode();
    	hash = hash*_to.hashCode();
    	return (int)hash;
    }
}
