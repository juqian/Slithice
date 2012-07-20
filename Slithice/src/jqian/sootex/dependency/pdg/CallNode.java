package jqian.sootex.dependency.pdg;

import soot.*;

/**
 *
 */
public class CallNode extends DependenceNode {
	public CallNode(MethodOrMethodContext mc,Unit callsite,SootMethod callee){
    	super(mc);
        this._callsite = callsite;
        this._callee = callee;        
    }
    
    /**Get the corresponding statement.*/
    public Unit getCallSite(){
        return _callsite;
    }
    
    public boolean equals(Object obj){
    	if(!(obj instanceof CallNode)){
    		return false;
    	}
    	
        CallNode that=(CallNode)obj;
        if(this._mc==that._mc &&
           this._callsite==that._callsite &&
           this._callee==that._callee)
            return true;
        else
            return false;
    }
    
    public int hashCode(){
        return _callsite.hashCode()*_callee.getNumber();
    }
    
    public Object clone(){
        return new CallNode(_mc,_callsite,_callee);
    }
    
    public String toString(){
    	String out = "CALL ";
    	String clsName = _callee.getDeclaringClass().getName();
    	int index = clsName.lastIndexOf('.');
    	clsName = clsName.substring(index+1);
    	
    	out += "#"+_id+" "+clsName+"."+_callee.getName()+"()";    	
        return out;
    }
    
    //FIXME: Dangerous binding object here. Different CallNode may bind to the same SootMethod
    public Object getBinding(){
    	return _callee;
    }
    
    ///////////////////////////////////////////////
    protected Unit _callsite;   
    protected SootMethod _callee;
}
