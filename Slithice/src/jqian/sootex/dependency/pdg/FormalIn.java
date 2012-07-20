package jqian.sootex.dependency.pdg;

import soot.*;

public class FormalIn extends FormalNode {
	/**
	 * @param index Index of the corresponding parameter. 
	 *              index == -1 is for the receiver, the "this" pointer
	 *              index == -2 is for other implicit patameters, i.e. locations in USE(mc)
	 */
	public FormalIn(MethodOrMethodContext mc,Object binding,int index){
		super(mc,binding);	
		this._paramIndex = index;
	}
	
    public Object clone(){
    	return new FormalIn(_mc,_binding,_paramIndex);
    }
    
    public String toString(){
    	String name = getBindingName();
    	return "#"+_id+" FI "+name;
    }
    
    /**
     * @return >=0  a normal explicit parameter
     *         ==-1 (THIS_INDEX) a receiver parameter
     *         ==-2 (HEAP_INDEX) an implicit heap parameter
     */
    public int getParamIndex(){
    	return _paramIndex;
    }
    
    public static final int THIS_INDEX  = -1;
    public static final int HEAP_INDEX  = -2;
    
    private int _paramIndex;
}
