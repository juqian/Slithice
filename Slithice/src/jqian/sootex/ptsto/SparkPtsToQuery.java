package jqian.sootex.ptsto;

import java.util.*;
import jqian.sootex.location.*;
import soot.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.*;
import soot.util.*;

/**
 * Query points-to relations using spark framework.
 * Spark assumes all pointer can points to null which is not explicitly shown.
 */
public class SparkPtsToQuery implements IPtsToQuery{
    private final InstanceObject[] _alloc2obj;    
    private AllocNode[]  _obj2alloc;  
	
    public SparkPtsToQuery(){
    	PAG _pa = (PAG)Scene.v().getPointsToAnalysis();
        int allocNum = _pa.getNumAllocNodes();
        _alloc2obj = new InstanceObject[allocNum+1];                 
        _obj2alloc = new AllocNode[allocNum*2];   //maybe not enough

        ArrayNumberer numberer=_pa.getAllocNodeNumberer();
        //FIX 2007-06-20  "i<allocNum" => "i<=allocNum"
        for(int i=1;i<=allocNum;i++){//0 - null pointer
            AllocNode node=(AllocNode)numberer.get(i);                
            InstanceObject loc= InstanceObject.makeInstObject(node);
    	    _alloc2obj[i]=loc;
    	    if(loc!=null){
                setHeapObj2NodeMap(loc,node);
    	    }
        }  
    }
    
    private final void setHeapObj2NodeMap(final InstanceObject hObj,final AllocNode node){
        int hId=hObj.getNumber();
        if(hId >= _obj2alloc.length-1){    
            int oldSz = _obj2alloc.length;
            int newSz = oldSz * 2;            
            while(hId >= newSz-1){
            	newSz *= 2;
            }
            
            AllocNode[] tmp = new AllocNode[newSz];
            for (int i = 0; i < oldSz; i++) {
                tmp[i] = _obj2alloc[i];
            }  
            _obj2alloc=tmp;
        }
        _obj2alloc[hId]=node;
    }
   
    private final PointsToSet getPointTos(final Location ptr){
    	PAG pa = (PAG)Scene.v().getPointsToAnalysis();
    	PointsToSet result = null;
    	
    	try{
    		if(ptr instanceof StackLocation){
                Value v = ((StackLocation)ptr).getValue();                 
                assert(v instanceof Local); 
                result = pa.reachingObjects((Local)v);                            
            }
            else if(ptr instanceof GlobalLocation){
                SootField field = ((GlobalLocation)ptr).getSootField();
                result = pa.reachingObjects(field);              
            }
            else if(ptr instanceof HeapLocation){
                InstanceObject hobj = ((HeapLocation)ptr).getWrapperObject();            
                AllocNode node= _obj2alloc[hobj.getNumber()];
                AllocDotField field=null;
                
                if(ptr instanceof ArrayElmt){
                	field=node.dot(ArrayElement.v());
                }
                else if(ptr instanceof HeapField){                
                	field=node.dot(((HeapField)ptr).getField());
                }
                
                if(field!=null)
                	result = field.getP2Set();               
            } 
    	}
    	catch(Exception e){
    		pa.hashCode();
    	}
        
    	return result;
    }
    
    private static final int CACHE_SIZE = 11;
    private final Location[] cachedLocs = new Location[CACHE_SIZE];
    @SuppressWarnings("unchecked")
	private final Set<InstanceObject>[] cachedPtsTos = new Set[CACHE_SIZE];
    private int cacheTail = 0;


    public Set<InstanceObject> getPointTos(SootMethod m, final Unit stmt, final Location ptr){
    	if(!ptr.isPointer())
    		return Collections.emptySet(); 
    	
    	// search in cache
      	for(int i=0; i<CACHE_SIZE; i++){
    		if(cachedLocs[i]==ptr){
    			return cachedPtsTos[i];
    		}
    	}
      	    	
    	Set<InstanceObject> pt2Set = new HashSet<InstanceObject>();
    	
    	if(ptr instanceof MethodRet){
    		//collect points-to information from return pointers
            MethodRet retPtr = (MethodRet)ptr;  
            Collection<Location> sources = retPtr.getValueSource();                
            for(Location loc: sources){                   
                if(loc!=null){          
                    Set<InstanceObject> s = getPointTos(m, stmt,loc);
                    pt2Set.addAll(s);
                }                
            }
        }
    	else{
    		PointsToSet p2St = getPointTos(ptr);
    		if(p2St!=null)
    			pt2SetToHeapObjSet(p2St,pt2Set);
    		//else
    		//	pt2Set.add(Location.UNKNOWN);
    	}
    	
    	// update cache
    	cachedLocs[cacheTail] = ptr;
    	cachedPtsTos[cacheTail] = pt2Set;
    	cacheTail = (cacheTail+1)%CACHE_SIZE;
    	
    	return pt2Set;
    }

	
    ////////////////////// Private part ///////////////////////////////////////
    private final class PtSetVisitor extends P2SetVisitor{
    	final Set<InstanceObject> _hset;
    	
        public PtSetVisitor(final Set<InstanceObject> hSet){
            this._hset=hSet;
        }
        public void visit(final Node n)  {
            assert(n instanceof AllocNode);
            
        	InstanceObject obj=_alloc2obj[n.getNumber()];
        	if(obj!=null)   _hset.add(obj);                                  
        }
        
        public boolean getReturnValue(){
            return true;
        }
    }
    
    private final void pt2SetToHeapObjSet(final PointsToSet ptSt,final Set<InstanceObject> out){
        if(ptSt instanceof PointsToSetInternal){
            PointsToSetInternal ptStInternal=(PointsToSetInternal)ptSt;
            PtSetVisitor visitor = new PtSetVisitor(out);
            ptStInternal.forall(visitor);
        }
        else{
            throw new RuntimeException("Unsupported type: "+ptSt.getClass());         
        }
     }
}