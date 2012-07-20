package jqian.sootex.dependency.pdg;

import jqian.sootex.util.SootUtils;
import soot.*;

/**
 *
 */
public class JimpleStmtNode extends DependenceNode{
    public JimpleStmtNode(MethodOrMethodContext mc,Unit unit){
    	super(mc);
        _stmt=unit;
    }
    
    /**Get the corresponding statement.*/
    public Unit getStmt(){
        return _stmt;
    }
    
    public JavaStmtNode toJavaStmtNode(){
        return new JavaStmtNode(_mc,SootUtils.getLine(_stmt));
    }
    
    public boolean equals(Object that){
    	DependenceNode thatNode = (DependenceNode)that;
    	if(thatNode.getNumber()!=this.getNumber())
    		return false;
    	
        JimpleStmtNode thatStmt=(JimpleStmtNode)that;
        if(this._stmt==thatStmt._stmt)
            return true;
        else
            return false;
    }
    
    public int hashCode(){
        return _stmt.hashCode();
    }
    
    public Object clone(){
        return new JimpleStmtNode(_mc,_stmt);
    }
    
    public String toString(){
        return SootUtils.getStmtString(_stmt);
    }
    
    public Object getBinding(){
    	return _stmt;
    }
    ///////////////////////////////////////////////
    private Unit _stmt;   
}
