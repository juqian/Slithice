package jqian.sootex.location;

import soot.jimple.*;
import soot.*;

/**
 * A class to hold the call information
 */
public class InvokeInfo {
    public InvokeInfo(Stmt stmt){       
        this._stmt=stmt;
        
        InvokeExpr invoke = _stmt.getInvokeExpr();
        int _argNum= invoke.getArgCount();       
        _argLocs=new Location[_argNum];
        
        for(int i=0;i<_argNum;i++){
            Value arg = invoke.getArg(i);
            if(arg instanceof Local || 
               arg instanceof NullConstant || 
               arg instanceof NumericConstant || 
               arg instanceof StringConstant ||
               arg instanceof ClassConstant){
                _argLocs[i] = Location.valueToLocation(arg);                
            }else{
                throw new RuntimeException();
            }            
        }  
        
        if(stmt instanceof DefinitionStmt){
        	Value target = ((DefinitionStmt)stmt).getLeftOp();
        	_ret = Location.valueToLocation(target);
        }
        
        //initialize the receiver info
        if(invoke instanceof InstanceInvokeExpr){
            Value base=((InstanceInvokeExpr)invoke).getBase();
            _receiver = Location.valueToLocation(base);    
        }
    }    
   
    /**Return the abstract location of indexed parameter
     * NOTING: the index parameter is not checked.
     */
    public Location[] getArgLocs(){
        return _argLocs;
    }
    
    /**Get access path of the receiver object */
    public Location receiver(){
        return _receiver;
    }
    
    public Location getRetLoc(){
        return _ret;
    }
    
    public InvokeExpr getInvokeExpr(){
        return _stmt.getInvokeExpr();
    }    
    
    public Unit getInvokeStmt(){
        return _stmt;
    }
    	
    
    public String toString(){
    	InvokeExpr invoke = _stmt.getInvokeExpr();
    	SootMethod tgt = invoke.getMethod();
        String ret="";
        if(_receiver!=null){
           ret += _receiver.toString()+"." + tgt.getName();
        }else{
           ret += tgt.getName(); 
        }
        
        ret+="(";
        
        int argNum = invoke.getArgCount();
        for(int i=0;i<argNum;i++){
            if(_argLocs[i]!=null){
                ret+=_argLocs[i].toString();
            }else{
                ret+=invoke.getArg(i).toString();
            }
            
            if(i!=argNum-1){
                ret+=",";
            }
        }
        ret+=")";
        return ret;
    }
    
    public SootMethod getDeclaredCallee(){
        InvokeExpr invoke = _stmt.getInvokeExpr();
    	return invoke.getMethod();
    }
    
    ////////////////////////////////////////////////
    private Location   _receiver;
    private Location   _ret;
    private Location[] _argLocs;    
    private Stmt       _stmt;
}
