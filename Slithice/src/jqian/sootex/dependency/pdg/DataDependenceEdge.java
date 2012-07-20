package jqian.sootex.dependency.pdg;

/**
 * A data dependence edge with the reason information that cause the dependence.
 */
public class DataDependenceEdge extends DependenceEdge {
    private Object _reason;
    
	/**
	 * @param reason  The dependence reason can be a Location, a SootField, a Type, or a collection of things. 
	 */
    public DataDependenceEdge(DependenceNode from,DependenceNode to,Object reason){
        super(from,to);
        this._reason = reason;
    }
    
    public String toString(){
        return getFrom()+" -> "+getTo()+"["+_reason+"]";
    }
    
    public Object getReason(){
        return _reason;
    }
    
    public boolean equals(Object that){
    	if(that.getClass()!=this.getClass())
    		return false;
    	
    	DataDependenceEdge edge = (DataDependenceEdge)that;
    	if(this._reason==edge._reason && super.equals(that))
    		return true;
    	return false;
    }
    
    public int hashCode(){
    	long hash = super.hashCode();
    	if(_reason!=null) 
    		hash *= _reason.hashCode();
    	return (int)hash;
    }
}
