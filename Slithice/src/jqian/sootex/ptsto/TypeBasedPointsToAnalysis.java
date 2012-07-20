package jqian.sootex.ptsto;

import java.util.*;

import jqian.Global;
import jqian.sootex.Cache;
import jqian.util.CollectionUtils;
import jqian.util.Utils;
import soot.AnySubType;
import soot.ArrayType;
import soot.Context;
import soot.Hierarchy;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.Type;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.options.Options;
import soot.util.Numberer;

/**
 * Type-based points-to analysis
 */
public class TypeBasedPointsToAnalysis implements PointsToAnalysis {
	private static TypeBasedPointsToAnalysis _instance;
	protected static void reset(){
		_instance = null;
	}
	
	static{
		Global.v().regesiterResetableGlobals(TypeBasedPtsToQuery.class);
	}
	
	public static TypeBasedPointsToAnalysis v(boolean allReachable){
    	if(_instance==null || (!_instance._allReachable && allReachable)){
    		_instance = new TypeBasedPointsToAnalysis(allReachable);
    	}
    	
    	return _instance;
    }	
	
	private final TypeBasedPointsToSet[] _type2ptsto;
	private final Type OBJECT_TYPE = Scene.v().getObjectType();
	private final boolean _allReachable;
	private Numberer _nodeNumberer;
	
	/**
	 * @param allReachable Should all methods considered reachable?
	 */
	private TypeBasedPointsToAnalysis(boolean allReachable){    	
        if(allReachable){
        	//Date startTime = new Date();
            //SootUtils.jimplify();	 
    		//Date endTime=new Date();	
    		//Global.v().out.println(">> jimplify all classes in " + Utils.getTimeConsumed(startTime,endTime));
    		
        	// construct a call graph to jimplify all necessary methods, load all necessary types
    		if(!Scene.v().hasCallGraph()){
    			Options.v().setPhaseOption("cg","verbose:false");
    			Options.v().setPhaseOption("cg","all-reachable:true");
    			CallGraphBuilder cg = new CallGraphBuilder( DumbPointerAnalysis.v() );
    			//CallGraphBuilder cg = new CallGraphBuilder();
    			cg.build();
    			Scene.v().setCallGraph(cg.getCallGraph());
    		}    	
        }
        else{
        	// parse all reachable methods by building a CHA call graph
        	if(!Scene.v().hasCallGraph() && !Scene.v().hasPointsToAnalysis()){
        		Date startTime = new Date();        		
        	    CallGraphBuilder cg = new CallGraphBuilder( DumbPointerAnalysis.v() );
        	    cg.build();
        	    Scene.v().setCallGraph(cg.getCallGraph());
        	    Date endTime=new Date();	
        	    Global.v().out.println(">> jimplify reachable classes and build CHA call graph in " + Utils.getTimeConsumed(startTime,endTime));
        	}
        }
        
        _nodeNumberer = Scene.v().getTypeNumberer();     
        _type2ptsto = new TypeBasedPointsToSet[_nodeNumberer.size()+1];
        _allReachable = allReachable;
    }
	
	public Numberer getObjectNumberer(){
		return _nodeNumberer;
	}
	

	/* (non-Javadoc)
	 * @see soot.PointsToAnalysis#reachingObjects(soot.Local)
	 */
	public PointsToSet reachingObjects(Local l) {
		return reachingObjects(l.getType());
	}

	/* (non-Javadoc)
	 * @see soot.PointsToAnalysis#reachingObjects(soot.Context, soot.Local)
	 */
	public PointsToSet reachingObjects(Context c, Local l) {
		return reachingObjects(l.getType());
	}

	/* (non-Javadoc)
	 * @see soot.PointsToAnalysis#reachingObjects(soot.SootField)
	 */
	public PointsToSet reachingObjects(SootField f) {
		return reachingObjects(f.getType());
	}

	/* (non-Javadoc)
	 * @see soot.PointsToAnalysis#reachingObjects(soot.Local, soot.SootField)
	 */
	public PointsToSet reachingObjects(Local l, SootField f) {
		return reachingObjects(f.getType());
	}

	/* (non-Javadoc)
	 * @see soot.PointsToAnalysis#reachingObjects(soot.Context, soot.Local, soot.SootField)
	 */
	public PointsToSet reachingObjects(Context c, Local l, SootField f) {
		return reachingObjects(f.getType());
	}

	/* (non-Javadoc)
	 * @see soot.PointsToAnalysis#reachingObjects(soot.PointsToSet, soot.SootField)
	 */
	public PointsToSet reachingObjects(PointsToSet s, SootField f) {
		return reachingObjects(f.getType());
	}
	
	/* (non-Javadoc)
	 * @see soot.PointsToAnalysis#reachingObjectsOfArrayElement(soot.PointsToSet)
	 */
	public PointsToSet reachingObjectsOfArrayElement(PointsToSet s) {
		if(s instanceof TypeBasedPointsToSet){
			TypeBasedPointsToSet ptsto = new TypeBasedPointsToSet();
			for(Type t: (TypeBasedPointsToSet)s){
				if(t instanceof ArrayType){
					ArrayType at = (ArrayType)t;
					Type p = at.getElementType();
					TypeBasedPointsToSet tgts = reachingObjects(p);					
					ptsto.addAll(tgts);
				}
			}
			
			return ptsto;
		}
		else{
			throw new RuntimeException("Parameter of types other than TypeBasedPointsToSet are not acceptable.");
		}		 
	}
    
    /**Get a collection of objects that pointer of type <code>type</code> can point to. */ 
    public TypeBasedPointsToSet reachingObjects(Type type){
    	int tId = type.getNumber();
    	TypeBasedPointsToSet ptsto = _type2ptsto[tId];    	
    	if(ptsto!=null){
    		return ptsto;
    	}    	
    	
    	ptsto = toConcreteTypes(type);
    	_type2ptsto[tId] = ptsto;
     
        return ptsto;
    }    
    
    // Object o = new String[1]
    private TypeBasedPointsToSet toConcreteTypes(Type type){
    	TypeBasedPointsToSet typeSet = new TypeBasedPointsToSet();
    	
    	// Object type can also reference to arrays
    	if(type==OBJECT_TYPE){
    		Numberer numberer = Scene.v().getTypeNumberer();
    		int count = numberer.size();
    		for(int i=1; i<=count; i++){
    			Type t = (Type)numberer.get(i);
    			if(t instanceof RefType || t instanceof ArrayType){
    				typeSet.add(t);
    			}
    			else if(t instanceof AnySubType){
    				
    			}
    		}
    	}
    	else if(type instanceof RefType){
        	SootClass cls = ((RefType)type).getSootClass();
        	if(cls.isConcrete()){
        		typeSet.add(type);
        	}
        	
        	Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        	Collection<?> classes;
        	if(cls.isInterface()){
        		classes = hierarchy.getImplementersOf(cls);
        	}
        	else{
        		classes = hierarchy.getSubclassesOf(cls);
        	}        	
         
        	for(Iterator<?> it=classes.iterator();it.hasNext();){
        		SootClass subCls = (SootClass)it.next();
        		if(subCls.isConcrete()){
        			Type subType = subCls.getType();
        			typeSet.add(subType);
        		}
        	}            	
        }
    	else if(type instanceof ArrayType){
        	//handle the possibility: Object[] array = new String[]
        	Type elmtType = ((ArrayType)type).getArrayElementType();
        	if(elmtType instanceof PrimType){
        		typeSet.add(type);
        	}
        	else if(elmtType instanceof RefType){
        		// The base of an array type does not necessary to be concrete
        		// add the current type
        		typeSet.add(type);
        		
        		// any sub type for the element type
        		SootClass cls = ((RefType)elmtType).getSootClass();
        		Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        		Collection<SootClass> subClassesAndInterfaces; 
        		if(cls.isInterface()){
        			List<SootClass> interfaces = hierarchy.getSubinterfacesOf(cls);
        			List<SootClass> implementations = hierarchy.getImplementersOf(cls);
        			subClassesAndInterfaces = new ArrayList<SootClass>(interfaces);
        			subClassesAndInterfaces.addAll(implementations);
        		}
        		else{
        			subClassesAndInterfaces = hierarchy.getSubclassesOf(cls);
        		} 
                
            	Collection<Type> elementSubTypes = new HashSet<Type>();   
            	for(SootClass c: subClassesAndInterfaces){            		 
            		elementSubTypes.add(c.getType());
            	}    
            	
        		for(Type subType: elementSubTypes){  
        			// Only existing ArrayType(s) are considered. Avoid creating new types
        			// which may cause type exploding
            		ArrayType at = subType.getArrayType();
            		if(at!=null)
            			typeSet.add(at);
            	}    
        	}
        	else if(elmtType instanceof ArrayType){
        		TypeBasedPointsToSet concreteTypes = toConcreteTypes(elmtType);
        		for(Type t: concreteTypes){        		 
            		Type concreteType = t.getArrayType();
            		if(concreteType!=null){
            			typeSet.add(concreteType);
            		}            		
            	}   
        	}
        }
     
    	return typeSet;
    }
    
    /** Check if type a and type b have a common sub type.  */
    public boolean mayAliased(Type a, Type b){
    	TypeBasedPointsToSet pt2setA = reachingObjects(a);
    	TypeBasedPointsToSet pt2setB = reachingObjects(b);    	
    	return CollectionUtils.hasInterset(pt2setA, pt2setB);
    }
    
    /** 
     * @param assume node should be a concrete type representing an abstract object. 
     *        interfaces and abstract classes do not have reaching objects
     */
    public TypeBasedPointsToSet reachingObjectsOfAllFields(Type node){
    	TypeBasedPointsToSet result = new TypeBasedPointsToSet();
        if(node instanceof RefType){
        	SootClass cls = ((RefType)node).getSootClass();
        	if(cls.isConcrete()){
        		Set<SootField> fields = Cache.v().getAllInstanceFields(cls);             
                for(SootField f: fields){
                	TypeBasedPointsToSet p2set = reachingObjects(f.getType());
                	result.addAll(p2set);
                }
        	}        	
        }
        else if(node instanceof ArrayType){
        	Type elementType = ((ArrayType)node).getArrayElementType();            	
        	TypeBasedPointsToSet p2set = reachingObjects(elementType);            	
        	result.addAll(p2set);
        }
        
        return result;
    }
}
