package jqian.sootex.du;

import java.util.Collection;

import soot.*;
import soot.util.SingletonList;
import jqian.sootex.location.*;
import jqian.sootex.util.SootUtils;
import jqian.util.CollectionUtils;


/**
 * 
 */
final class ReachingDU{
    private Unit _stmt;                      //position    
    private AccessPath _ap;                  //access path
    private Collection<Location> _locations; //def/use memory locations
    
	@SuppressWarnings("unchecked")
	public ReachingDU(Unit stmt, AccessPath ap, Collection<Location> locations){
	    this._stmt = stmt;
	    
	    if(locations.size()>100){	    	 
	    	this._locations = SootUtils.toCompactSet(locations);
	    }
	    else{
	    	this._locations = locations;
	    }
	    
	    this._ap = ap;
	}   
   
	@SuppressWarnings("unchecked")
	public ReachingDU(Unit stmt, AccessPath ap, Location loc){
	    this._stmt = stmt;	    
	    this._locations = new SingletonList(loc);
	    this._ap = ap;
	}
	
	public String toString(){
	    return "("+SootUtils.getStmtString(_stmt)+","+_ap+","+
	             "["+CollectionUtils.toString(_locations.iterator(), ",")+"])";
	}

	public Collection<Location> getLocations(){
	    return _locations;
	}
	
	public AccessPath getAccessPath(){
	    return _ap;
	}	

	public Unit getStmt(){
	    return _stmt;
	}	
    
	public int hashCode(){
	    /*int hashCode = _stmt.hashCode();
	    if(_ap!=null)
	    	hashCode *= _ap.hashCode();
	    
	    if(_locations!=null){
	    	//call hashCode() of collections can be time consuming
	    	//hashCode *= _locations.hashCode();
	    	hashCode *= _locations.size();
	    }
	    
	    return hashCode;*/
		return super.hashCode();
	}

    
	public boolean equals(Object that){
		return super.equals(that);
		/*if(this==that) 
			return true;
 
		ReachingDU du = (ReachingDU)that;
		if(du._stmt==_stmt && du._ap==_ap && du._locations.equals(_locations))
			return true;
		
		return false;*/
	} 
}
