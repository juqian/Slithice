package jqian.sootex;

import java.util.*;

import jqian.Global;
import jqian.sootex.util.SootUtils;
import soot.*;
import soot.jimple.toolkits.callgraph.TopologicalOrderer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Cache {
	private static Cache _instance = new Cache();	
	private Cache(){
		Global.v().regesiterResetableGlobals(Cache.class);
	}
	public static Cache v(){		
		return _instance;
	}    
    
	private Map<SootClass,Set> _class2fields = new HashMap<SootClass,Set>();
	private List _topoOrder;
		
	 /** 
     * Get the possible instance fields of a class, including fields declared in the class
     * definition and fields of super classes.
     */
	public Set<SootField> getAllInstanceFields(SootClass cls){
    	Set fields = _class2fields.get(cls);
    	if(fields==null){
    		fields = SootUtils.findAllInstanceFields(cls);
    		_class2fields.put(cls, fields);    		
    	}
        
        return fields;
    }
    
	public List<MethodOrMethodContext> getTopologicalOrder() {
		if(_topoOrder==null){
			if(Scene.v().hasCallGraph()){
				TopologicalOrderer orderer = new TopologicalOrderer(Scene.v().getCallGraph());
				orderer.go();
				_topoOrder = orderer.order();  
			}
			else{
				_topoOrder = Collections.EMPTY_LIST;
			}
		}		 
		
		return _topoOrder;
	}
	

    
    /** For analysis reset. */
    public static void reset(){
    	_instance = new Cache();
    }
}
