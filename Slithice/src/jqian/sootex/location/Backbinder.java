package jqian.sootex.location;

import java.util.*;

import soot.*;
import soot.jimple.*;

/**
 * Build a map between actuals and formals(no map on extended aprameters)
 */
public class Backbinder {
	 /**
	  * @param actualCallee is the actual callee, not the declared callee
	  */
	public Backbinder(InvokeInfo invoke,SootMethod actualCallee) {
        getActualFormalMap(invoke,actualCallee,_afMap);
    }	 
	 
	@SuppressWarnings("unchecked")
	public static Map<Value,Value> createParmArgMap(Unit invoke,SootMethod callee){	
		InvokeExpr expr = ((Stmt)invoke).getInvokeExpr();
    	SootMethod declaredCallee = expr.getMethod();
    	String signature = declaredCallee.getSubSignature();
    	String tgtSignature = callee.getSubSignature();
    	
    	//implicit call
    	if(!signature.equals(tgtSignature)){
    		return Collections.EMPTY_MAP;
    	}
    		
    	Map<Value,Value> map = new HashMap<Value,Value>();
    	Body body = callee.getActiveBody();
    	 
    	//map for this
        if(!callee.isStatic()){
        	Value thiz = body.getThisLocal();
        	Value receiver = ((InstanceInvokeExpr)expr).getBase();
        	map.put(thiz,receiver);
        	map.put(receiver,thiz);
        }
        
        //map for parameters
        int paramCount = callee.getParameterCount();
        for(int i=0;i<paramCount;i++){
        	Value param = body.getParameterLocal(i);
        	Value arg = expr.getArg(i);
        	map.put(param,arg);
            map.put(arg,param);       
        }
        return map;
	}
   
    protected void getActualFormalMap(InvokeInfo invoke, SootMethod actualCallee,Map<Location,Location> out){
    	String tgtSignature = actualCallee.getSubSignature();
    	SootMethod declaredCallee = invoke.getDeclaredCallee();
    	String signature = declaredCallee.getSubSignature();
    	
    	//implicit call
    	if(!signature.equals(tgtSignature)){
    		return;
    	}
    		
    	//map for this
        if(!actualCallee.isStatic()){
        	Location thisPtr = Location.getThisPointer(actualCallee);
        	Location receiver=invoke.receiver();
        	out.put(thisPtr,receiver);
        	out.put(receiver,thisPtr);
        }
        //map for parameters       
        Location[] args = invoke.getArgLocs();     
        int argNum = args.length;
        for(int i=0;i<argNum;i++){
        	Local p = actualCallee.getActiveBody().getParameterLocal(i);               
            Location param = Location.valueToLocation(p);
        	
            out.put(param,args[i]);
            out.put(args[i],param);                      
        }
        
        //map for return
        if(actualCallee.getReturnType() instanceof RefLikeType){
            Location formalRet = Location.methodToRet(actualCallee);
			Location actualRet = invoke.getRetLoc();
			if (formalRet != null && actualRet != null) {
				out.put(formalRet, actualRet);
				out.put(actualRet, formalRet);
			}
        }
    }

    /** Backbind a parameter based access path to an argument based access path
     *  @return CAN return null if ap is an access path on parameter while the 
     *          return value is not used, 
     */
    public AccessPath backbind(AccessPath ap) {
        Location param=ap.getRoot();
        //do not perform mapping for globals
        if(param instanceof GlobalLocation){
            return ap;
        }else{
            Location arg = _afMap.get(param);
            if(arg==null)
            	return null;
            else
            	return ap.getRootModified(arg);
        }
    }
    
    public Location backbind(Location param){    	
        return _afMap.get(param);
    }
       
    public void backbindAccessPaths(Collection<AccessPath> aps,Collection<AccessPath> out) {       
        for(Iterator<AccessPath> it=aps.iterator();it.hasNext();){
            AccessPath formal = it.next();
            AccessPath actual = backbind(formal);
            if(actual!=null)
            	out.add(actual);
        }
    }   

    //////////////////////////////////////////////////////////
    private Map<Location,Location> _afMap=new HashMap<Location,Location>();
}
