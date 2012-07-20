package jqian.sootex.ptsto;

import java.util.*;

import soot.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.*;
import soot.jimple.toolkits.pointer.Union;
import soot.jimple.*;
import soot.util.ArrayNumberer;
import soot.util.Numberable;
import soot.util.Numberer;

/**
 * @author bruteforce
 *
 */
public class PtsToAdapter {
	private final PointsToAnalysis _pta;
	private Map<Object,AllocNode> _obj2node;
	
	public PtsToAdapter(PointsToAnalysis pa){
		_pta = pa;
	}
	
	public PointsToAnalysis getPointsToAnalysis(){
		return _pta;
	} 
	
	private PointsToSet pagReachingObject(AnyNewExpr obj,SparkField f){
		if (_obj2node == null) {
			_obj2node = PAGHelper.buildNewExprToAllocNodeMap((PAG)_pta);
		}
		AllocNode node = _obj2node.get(obj);
		return PAGHelper.getFieldPointsTo(node, f);
	}
	
	public PointsToSet reachingObjects(AnyNewExpr obj,SootField f){
		if(!(f.getType() instanceof RefLikeType)){
			return null;
		}
		
		if(f.isStatic()){
            return _pta.reachingObjects(f);
        } 
		
		if(_pta instanceof PAG){
			return pagReachingObject(obj, f);
		}
		else if(_pta instanceof TypeBasedPointsToAnalysis){
			TypeBasedPointsToAnalysis tpta = (TypeBasedPointsToAnalysis)_pta;
			return tpta.reachingObjects(f);
		}
		
		return null;
	}
	
	public PointsToSet reachingObjectsOfArrayElement(AnyNewExpr arrayObject){
		if(_pta instanceof PAG){
			return pagReachingObject(arrayObject, ArrayElement.v());
		}
		else if(_pta instanceof TypeBasedPointsToAnalysis){
			ArrayType type = (ArrayType)arrayObject.getType();
			Type elementType = type.getArrayElementType();
			TypeBasedPointsToAnalysis tpta = (TypeBasedPointsToAnalysis)_pta;
			return tpta.reachingObjects(elementType);
		}
		
		return null;	 
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Set<Numberable> toHeapNodeSet(PointsToSet ptSt) {
		Set result = null;
		if (ptSt instanceof PointsToSetInternal || ptSt instanceof Union) {
			result = new HashSet();
			toHeapNodeSet(ptSt, result);
		}
		else if(ptSt instanceof TypeBasedPointsToSet){
			result = (TypeBasedPointsToSet)ptSt;			 
		}
		 	
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void toHeapNodeSet(PointsToSet ptSt, Set<Numberable> result) {
		if (ptSt instanceof PointsToSetInternal || ptSt instanceof Union) {
			Set out = result;
			PAGHelper.toAllocNodeSet(ptSt, out);
		}
		else if(ptSt instanceof TypeBasedPointsToSet){
			TypeBasedPointsToSet set = (TypeBasedPointsToSet)ptSt;
			result.addAll(set);
		}
	}
	
	public Set<Numberable> reachingObjects(Local local){
		PointsToSet pset = _pta.reachingObjects(local);
		return toHeapNodeSet(pset);
	}   
	
	public void reachingObjects(Local local, Set<Numberable> result){
		PointsToSet pset = _pta.reachingObjects(local);
		toHeapNodeSet(pset, result);
	}   
	
	public Set<Numberable> reachingObjects(Local v, SootField f){
		PointsToSet pset = _pta.reachingObjects(v,f);		 
		return toHeapNodeSet(pset);
	}  
	
	/** Override this method to define your own collection. */
	public static class AtomicFilter{
		public boolean isAtomic(Type type){
			return false;
		}
	}
	
	public void reachingClosure(Local local,Set<Numberable> out){
		Set<Numberable> starts = reachingObjects(local);
		reachingClosure(starts,out, null);
	}
	
	public Type getAbstractObjectType(Numberable object){
		if(object instanceof Type){
			return (Type)object;
		}
		else if(object instanceof AllocNode){
			return ((AllocNode)object).getType();
		}
		
		return null;
	}
	
	public Numberer getObjectNumberable(){
		ArrayNumberer numberer = null;
		if(_pta instanceof PAG){
			PAG pag = (PAG)_pta;
			numberer = pag.getAllocNodeNumberer();
		}
		else if(_pta instanceof TypeBasedPointsToAnalysis){
			numberer = Scene.v().getTypeNumberer();
		}
		else{
			
		}
		
		return numberer;
	}
	
	/**
	 * @param object assuming the passed in parameter is an abstract object
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<Numberable> reachingObjectOfAllFields(Numberable object){
		Set result = null;
		if(_pta instanceof PAG){
			result = PAGHelper.reachingObjectsOfAllFields((AllocNode)object);			
		}
		else if(_pta instanceof TypeBasedPointsToAnalysis){
			TypeBasedPointsToAnalysis tpta = (TypeBasedPointsToAnalysis)_pta;
			result = tpta.reachingObjectsOfAllFields((Type)object);
		}
		
		return result;
	}
	
	private void reachingClosure(Set<Numberable> starts,Set<Numberable> out, AtomicFilter filter){		
		Stack<Numberable> stack = new Stack<Numberable>();		
		stack.addAll(starts);	
	
	    while(!stack.isEmpty()){
	    	Numberable n = stack.pop();
	    	out.add(n);
	    	
	    	//if is atomic type which can not be expanded
	    	if(filter!=null && filter.isAtomic(getAbstractObjectType(n))){
	    		continue;
	    	}
	    	
	    	Set<Numberable> reach = reachingObjectOfAllFields(n);
			for(Numberable tgt: reach){
	            if(!out.contains(tgt)){
	            	stack.push(tgt);
	            }		
	        }
	    }	    
	}
}
