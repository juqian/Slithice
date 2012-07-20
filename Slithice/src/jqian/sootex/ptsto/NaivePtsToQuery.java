package jqian.sootex.ptsto;

import java.util.*;
import jqian.sootex.location.InstanceObject;
import jqian.sootex.location.Location;
import jqian.sootex.location.UnknownArraySpace;
import jqian.sootex.location.UnknownInstObject;
import soot.*;


/**
 * A dumb points-to relation query by type.
 * Model heap by an unknown object and an unknown array
 */
public class NaivePtsToQuery implements IPtsToQuery{
	public NaivePtsToQuery(){ }  
    
    private Set<InstanceObject> getPointTos(Type type){
    	Set<InstanceObject> pt2Set = new HashSet<InstanceObject>();
    	
    	if(type.equals(Scene.v().getObjectType())){
    		// can point to every kind of object
    		pt2Set.add(UnknownArraySpace.v());    		 
    		pt2Set.add(UnknownInstObject.v());
    	}
    	else if(type instanceof ArrayType){
    		pt2Set.add(UnknownArraySpace.v());
    	}
    	else{
    		pt2Set.add(UnknownInstObject.v());
    	}   
    	
    	return pt2Set;
    }
    
    public Set<InstanceObject> getPointTos(SootMethod m, Unit stmt,Location ptr){
    	if(!ptr.isPointer())
    		return Collections.emptySet();
    	
    	Type type = ptr.getType();
    	return getPointTos(type);
    }
}