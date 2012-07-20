package jqian.sootex.location;

import java.util.*;
import soot.*;
import soot.jimple.*;
import jqian.sootex.Cache;
import jqian.sootex.util.*;


public class CommonInstObject extends InstanceObject{ 	
	private Type _type; 
	private Value _alloc; 
	private HeapField[] _fieldLocs;	
	
    CommonInstObject(SootMethod method,Value alloc,Type type){		
	    this._alloc=alloc;	    
	    this._method=method;
		this._type=type;	
		
	    buildFields();	
	}   
    
    /** Only use for override. */
    public CommonInstObject(){}
    
    /**Return the corresponding field */
	public HeapField getField(final SootField field){
		int index = searchField(field);
		if(index<0)
			return null;		
		else
			return _fieldLocs[index];
	}
	
	public HeapField[] getAllFields(){
	    return _fieldLocs; 
	}	
	
    protected void buildFields(){        
		SootClass cls = ((RefType) _type).getSootClass();
		Collection<SootField> flds = Cache.v().getAllInstanceFields(cls);
		_fieldLocs = new HeapField[flds.size()];

		int i = 0;
		for (SootField f : flds) {
			HeapField heapField = new HeapField(this, f);
			_fieldLocs[i] = heapField;
			i++;
		}   
        
        Arrays.sort(_fieldLocs,FieldComparator._instance);
    }

    public String toString(){
        String str= "(";
		if(_alloc!=null && _alloc instanceof AnyNewExpr){
			str += "N"; 
	    }else{
	    	str += "T"; 
	    }      
        str += getNumber()+"#";	
        
        //String typename=_type.toString();
        //int index=typename.lastIndexOf('.');
        //typename=typename.substring(index+1,typename.length());        
        
        if(_alloc!=null){
        	//str+=typename;
        	str += "@";
            if(_method!=null)
                str+=_method.getName();//+_method.getSignature();
            if(getAllocUnit()!=null)
                str+="_"+SootUtils.getLine(getAllocUnit());
        }else{
        	str+= _type.toString();
        }
        str+=")";
        return str;
    }    
    
    public Type getType(){
        return _type;
    } 
    
    private int searchField(final SootField field) {
    	int low = 0;
    	int high = _fieldLocs.length-1;
    	int fieldNum = field.getNumber();

    	while (low <= high) {
    	    int mid = (low + high) >> 1;
    	
    	    HeapField midVal = _fieldLocs[mid];    	    
    	    int cmp = midVal._field.getNumber() - fieldNum;

    	    if (cmp < 0)
    		low = mid + 1;
    	    else if (cmp > 0)
    		high = mid - 1;
    	    else
    		return mid; // key found
    	}
    	return -(low + 1);  // key not found.
    }
    
    private static class FieldComparator implements Comparator<Object>{
    	static FieldComparator _instance = new FieldComparator();
    	
    	public final int compare(Object arg0, Object arg1) {
    		SootField f1 = null;
    		if(arg0 instanceof HeapField){
    			HeapField n1 = (HeapField)arg0;
    			f1 = n1.getField();
    		}
    		else{
    			f1 = (SootField)arg0;
    		}
    		
    		SootField f2 = null;
    		if(arg1 instanceof HeapField){
    			HeapField n2 = (HeapField)arg1;
    			f2 = n2.getField();
    		}
    		else{
    			f2 = (SootField)arg1;
    		}
    		
    		return f1.getNumber()-f2.getNumber();
    	}
    }
}
