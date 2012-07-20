package jqian.sootex.location;

import java.util.*;

import jqian.Global;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.*;
import soot.util.*;

/**
 * Encode an access paths in the program.
 * An access path always starts with a Location
 * <immutable>
 */
public class AccessPath implements Numberable{
	private static Object UNCLEAR_INDEX = new Object();
	private static Map<Location, AccessPath> _root2AccessPath;
	private static int _count = 0;

	private final Location _root;
	private final AccessPath _father;
	private final int _id;
	private final Object[] _accessList; // accessor can be SootField/Value/UNCLEAR_INDEX
	
	// private List<AccessPath> _extensions = new ArrayList<AccessPath>(5);
	// XXX: Using list to store extensions is too slow
	// Using map will improve efficiency, but some program may fail to run due
	// to OutOfMemoryException
	private Map<Object, AccessPath> _extensions = new HashMap<Object, AccessPath>(5);
    
	protected static void reset(){
		_count = 0;
		//XXX: Use WeakHashMap instead of HashMap from 2008-09-18
		 _root2AccessPath = new WeakHashMap<Location,AccessPath>(); 
	}
	 
    static{
        reset();        
        Global.v().regesiterResetableGlobals(AccessPath.class);
    }
    
    public static void release(SootMethod m){
    	if(!m.hasActiveBody())
    		return;
    	
    	Body b = m.getActiveBody();
    	Collection<Local> locals = b.getLocals();
    	for(Local local: locals){
    		Location root = Location.valueToLocation(local);
    		_root2AccessPath.remove(root);
    	}
    }
    
    /**
     * @note In this method, the null constant, string constant are represent with 
     *       HeapObject. The implicit return object also can be represented with a HeapObject 
     */   
    public static AccessPath valueToAccessPath(SootMethod method,Unit stmt,Value value){//throws Exception{
        AccessPath ap=null;
        //the atoms 
        if(value instanceof Local 
           //||value instanceof NullConstant 
           //||value instanceof StringConstant
        		){   
        	Location loc = Location.valueToLocation(value);
            ap = AccessPath.getByRoot(loc);   
        }        
        else if(value instanceof ParameterRef){
            //Location loc = StackLocation.getLocation(value);          
            //ap=AccessPath.getByRoot(loc);
        	return null;
        }
        
        //handling the identity references
        else if(value instanceof JCaughtExceptionRef               
                || value instanceof ThisRef
                || value instanceof NewExpr){     
            //Location loc = CommonHeapObject.getHeapObject(method,stmt,value);
            //ap=AccessPath.getByRoot(loc);
        	return null;
        }
        //handling the new expressions
        else if( value instanceof NewArrayExpr 
                || value instanceof NewMultiArrayExpr){
            //HeapObject hobj=ArraySpace.getHeapObject(method,stmt,value);           
            //ap=AccessPath.getByRoot(hobj);
        	return null;
        }
        //handling array element references
        else if(value instanceof ArrayRef){
            //may be some problem
            Value base=((ArrayRef)value).getBase(); 
            Local local = (Local)base;
            ap=AccessPath.getByRoot(Location.valueToLocation(local));    
            //ap=ap.appendArrayRef(((ArrayRef)value).getIndex());
            ap=ap.appendArrayRef(); 
        }
        //handling instance field references
        else if(value instanceof InstanceFieldRef){
            Value base=((InstanceFieldRef)value).getBase(); 
            Local local = (Local)base;
            ap=AccessPath.getByRoot(Location.valueToLocation(local));            
            ap=ap.appendFieldRef(((FieldRef)value).getField());
        }
        //handling class field references
        else if(value instanceof StaticFieldRef){
            SootField field=((StaticFieldRef)value).getField();  
            GlobalLocation loc = Location.getGlobalLocation(field);
            ap=AccessPath.getByRoot(loc); 
        }
        //handling cast expression
        else if(value instanceof CastExpr){
            Value base=((CastExpr)value).getOp(); 
            if(base instanceof Local){
            	Location loc = Location.valueToLocation(base);
                ap = AccessPath.getByRoot(loc);               
            }else{
            	return null;
            }
        }
        //handling the access of array length
        //NOTE: the array length access is translated to a special instruction in .class files
        else if(value instanceof LengthExpr){
             //as it return an integer, the expression is left unhandled
        	return null;
        }
        
        return ap;
    }  
    
    /**
     * Get the AccessPath instance by root pointer. If it is not found, just build
     * it and store it to the map.
     * @param root
     * @return
     */
    public static AccessPath getByRoot(Location root){
        AccessPath ap= _root2AccessPath.get(root);
        if(null==ap){
            ap=new AccessPath(root); 
        }
        return ap;
    }   
    
    //The access path can only be build from an implcit factory
    private AccessPath(Location loc){
        this._root = loc; 
        this._accessList = new Object[0];
        this._father = null;
        this._id=_count;
        
        //register to the factory
        _root2AccessPath.put(loc,this);
        _count++;
    }  
    
    private AccessPath(AccessPath father, Object extraAccessor){
        this._root = father._root;
        
        int fatherAccessors = father._accessList.length;
        _accessList = new Object[fatherAccessors+1];
        System.arraycopy(father._accessList, 0, _accessList, 0, fatherAccessors);
        _accessList[fatherAccessors] = extraAccessor;  
        
        this._father = father;        
        this._id=_count;
        
        _count++;
    }
    
    public int getNumber(){
        return _id;
    }
    
    public void setNumber(int id){}
    
    public final AccessPath getRootModified(final Location newRoot){
        AccessPath ap=getByRoot(newRoot);
        for(Object accessor: _accessList){            
            ap=ap.appendAccessor(accessor);
        }
        return ap;
    }
    
    public final AccessPath appendAccessor(final Object accessor){
        //if found, do not reconstruct it        
        /*for(AccessPath ap: _extensions){ 
        	int index = ap._accessList.length-1;         
            Object ac = ap._accessList[index];
            if(ac==accessor){
               return ap;                
            }                                         
        }
        
        AccessPath ext = new AccessPath(this,accessor);
        _extensions.add(ext);
        
        return ext; */
    	
    	AccessPath ext = _extensions.get(accessor);
    	if(ext==null){
    		ext = new AccessPath(this,accessor);
    		_extensions.put(accessor,ext);
    	}
        
        return ext; 
    } 
    
    public AccessPath appendFieldRef(SootField field){
    	return appendAccessor(field);
    }
    
    /** Append an array reference without distinguish the index. */
    public AccessPath appendArrayRef(){
    	return appendAccessor(UNCLEAR_INDEX);
    }
    
    /**Truncate the access path by the given length*/
    public AccessPath getTruncated(int length){
        if(length<0){
            throw new RuntimeException("Truncate access path error");            
        }
        
        AccessPath cur=this;
        while(cur!=null && cur.length()>length){
            cur=cur._father;
        }
        return cur;
    }
    
    /**Return a sequence of ancestor access paths*/
    public List<AccessPath> getAncestors(){
        List<AccessPath> list=new LinkedList<AccessPath>();
        AccessPath cur=_father;
        while(cur!=null){
            list.add(0,cur);  //add to list head
            cur=cur._father;
        }
        return list;
    }
    
    /** return the length of this access path */
    public int length(){
       return _accessList.length;
    }
    
    public String toString(){
        String str=_root.toString();
        for(Object ac: _accessList){            
            if(ac instanceof SootField){
                str+='.'+((SootField)ac).getName();
            }else{
                str+="[]"; 
            }          
        }
        return str;
    }
    
    /** return the root of this access path  */
    public Location getRoot(){
        return _root;
    }

    /**Check whether the access path contains an array element access*/
    public boolean withArrayElmtAccess(){
        for(Object ac: _accessList){         
            if(!(ac instanceof SootField)){
               return true;
            }          
        }
        return false;
    }
    
    /** Get the declaration type of an access path. 
     *  NOTE: To save memory space, we do not have a type field for the AccessPath,
     *        and for long access paths, accessing its type may have some cost.
     *        For an access path with length in 3, this will not be a problem.
     */
    public Type getDeclareType(){    
        if(_accessList.length==0){
            return _root.getType();
        }
        
        Object lastAccessor = getLastAccessor();       
        if(lastAccessor instanceof SootField){
            return ((SootField)lastAccessor).getType();
        }
        else{// if is a array accessor, return the type of array element 
            Type type = _father.getDeclareType();
            assert(type instanceof ArrayType);
            return ((ArrayType)type).getArrayElementType();           
        }
    }
    
    public AccessPath getFather(){
        return _father;
    }
  
    /**Check whether the current access path is a prefix of another access path. */
    public boolean isPrefixOf(AccessPath that){
        if(this._root!=that._root){
            return false;
        }
        
        int length = _accessList.length;
        if(length > that._accessList.length){
            return false;
        }        
        
        for(int i=0;i<length;i++){        	
            Object thisAc = _accessList[i];
            Object thatAc = that._accessList[i];
            if(thisAc!=thatAc){
                return false;
            }
        }
        return true;
    }

    public boolean isTruePrefixOf(AccessPath that){
       if(!isPrefixOf(that)){
           return false;
       }else if(this.length()>=that.length()){
           return false;
       }
       return true;
    }
    
 
    
    public static Collection<AccessPath> getAllRootAccessPaths(){
        Set<AccessPath> rootAps = new HashSet<AccessPath>();
        Set<Location> rootPtrs = _root2AccessPath.keySet();
        for(Iterator<Location> it = rootPtrs.iterator();it.hasNext();){
            rootAps.add(_root2AccessPath.get(it.next()));
        }
        return rootAps;
    }
    
    /**Return all currently constructed access paths*/
    public static Collection<AccessPath> getAllAccessPaths(){
        Collection<AccessPath> rootAps = getAllRootAccessPaths();
        List<AccessPath> list=new LinkedList<AccessPath>();
        for(Iterator<AccessPath> it=rootAps.iterator();it.hasNext();){
            AccessPath ap = it.next();
            list.add(ap);
            list.addAll(ap.getAllExtensions());            
        }
        return list;
    }
 
    
    public Collection<AccessPath> getImmediateExtensions(){        
        //return _extensions;
    	return _extensions.values();
    }
    
    /**Recusively get all extensions of the current access path*/
    public List<AccessPath> getAllExtensions(){
        List<AccessPath> list=new LinkedList<AccessPath>();
        for(AccessPath ext: _extensions.values()){          
            list.add(ext); 
            list.addAll(ext.getAllExtensions());
        }
        return list;
    }
    
    /** Get the sequence of accessors. */
    public Object[] getAccessors(){
        return _accessList;
    }
    
    public final Object getLastAccessor(){
    	int index = _accessList.length-1;
    	return _accessList[index];
    }
    
    public AccessPath appendAccessors(List<Object> accessors){
    	AccessPath cur = this;
    	for(Iterator<Object> it=accessors.iterator();it.hasNext();){
    		Object access = it.next();
   			cur = cur.appendAccessor(access);    		 
    	}
    	return cur;
    }
    
    
    /** Get the last accessed field in the accessor list.
     *  This method only work correctly when withArrayElementAccess()==false*/
    public SootField getLastAccessedField(){
        Object accessor = getLastAccessor();
        return (SootField)accessor;
    }
    
    /** Check whether two access path is the same in a limited length. */
    public boolean isPartialyEquals(AccessPath that,int length){
        if(this._root!=that._root)
            return false;
        
        if(_accessList.length<length || that._accessList.length<length)
            throw new RuntimeException("Too big length to compare.");
        
        for(int i=0;i<length;i++){
        	  Object thisAccessor = _accessList[i];
              Object thatAccessor = that._accessList[i];
              if(!thisAccessor.equals(thatAccessor))
                  return false;           
        }
        
        return true;
    }
    
    public boolean match(AccessPath ap){
        if(getLastAccessedField()!=AnyField.v())
            return this==ap;
        else{
            int len=this.length();
            if(len!=ap.length())  return false;
            if(this._father==ap._father)
                return true;
            return false;            
        }
    }
    
    public static boolean isFieldSelector(Object accessor){
    	return accessor!=null && (accessor instanceof SootField);
    }
    
    public static SootField getAccessedField(Object accessor){
    	return (SootField)accessor;
    }
}
