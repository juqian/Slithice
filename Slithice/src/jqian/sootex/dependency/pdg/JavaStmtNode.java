package jqian.sootex.dependency.pdg;

import soot.*;
/**
 */
public class JavaStmtNode extends DependenceNode {
    public JavaStmtNode(MethodOrMethodContext mc,int line){
    	super(mc);
        this._line=line;
    }
    
    public int getLine(){
        return _line;
    }
    
    public Object clone(){
        return new JavaStmtNode(_mc,_line);
    }
    
    public String toString(){
        return "["+_line+"]";
    }
    
    public Object getBinding(){
    	return new Integer(_line);
    }
    
    public void setCallSiteFlag(boolean isCallSite){
    	this._hasCall = isCallSite;
    }
    
    public boolean isCallSite(){
    	return _hasCall;
    }
    
    ///////////////////////////////////
    private int _line;
    private boolean _hasCall;
}
