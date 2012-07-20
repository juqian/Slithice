package jqian.sootex.dependency.pdg;

import jqian.Global;
import soot.*;
import soot.util.*; 

/**
 * Dependence graph node. All put to an ArrayNumberer
 */
public abstract class DependenceNode implements Numberable{
	private static int _count;
    private static Numberer _numberer;
    
    static void reset(){
    	 _count = 0;
    	 _numberer = new ArrayNumberer();
    }
    
    static{
    	reset();
    	Global.v().regesiterResetableGlobals(DependenceNode.class);
    }
    
    protected static Numberer getNumberer(){
    	return _numberer;
    }
    
    protected final int _id;
    protected final MethodOrMethodContext _mc;
    
	/** A dependence node must belong to some method or method context. */
    public DependenceNode(MethodOrMethodContext mc){
        this._id=_count;
        this._mc = mc;
        _count++;
        _numberer.add(this);
    }  
    
    /** Get object binds to this node. A Dependence node can be find out from PDG
     *  with the given binding information.   
     *     For JimpleStmtNode, this is a Unit.
     *     For JavaStmtNode, this is a Integer of line. 
     *     For FormalNode and ActualNode, the binding information can be a Location or SootField, or even a Type
     */
    public abstract Object getBinding();
   
    public abstract Object clone();

    /** Each node has a unique identification number. */
    public int getNumber(){
        return _id;
    }
    
    public void setNumber(int i){}
    
    public MethodOrMethodContext getMethodOrMethodContext(){
    	return _mc;
    }
    
    public int hashCode(){
    	return getNumber();
    }
}
