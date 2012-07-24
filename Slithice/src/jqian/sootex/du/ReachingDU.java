package jqian.sootex.du;

import java.util.Collection;

import soot.*;
import jqian.sootex.location.*;
import jqian.sootex.util.SootUtils;
import jqian.util.CollectionUtils;


final class ReachingDU{
    private final Unit _stmt;                      //position    
    private final AccessPath _ap;                  //access path
    private final Collection<Location> _locations; //def/use memory locations
    
	@SuppressWarnings("unchecked")
	public ReachingDU(Unit stmt, AccessPath ap, Collection<Location> locations){
	    this._stmt = stmt;
	    this._ap = ap;
	    
	    if(locations.size()>100){	    	 
	    	this._locations = SootUtils.toCompactSet(locations);
	    }
	    else{
	    	this._locations = locations;
	    }
	}   
   
	public ReachingDU(Unit stmt, AccessPath ap, Location loc){
	    this._stmt = stmt;	    
	    this._locations = new SingleList(loc);
	    this._ap = ap;
	}
	
	public String toString(){
	    return "("+SootUtils.getStmtString(_stmt)+","+_ap+","+
	             "["+CollectionUtils.toString(_locations.iterator(), ",")+"])";
	}

	public final Collection<Location> getLocations(){
	    return _locations;
	}
	
	public final AccessPath getAccessPath(){
	    return _ap;
	}	

	public Unit getStmt(){
	    return _stmt;
	}
	
	final static class SingleList extends java.util.AbstractList<Location> {
	    private final Location o;
	    public SingleList(Location o ) { this.o = o; }
	    public final int size() { 
	    	return 1; 
	    }
	    
	    @Override
	    public final boolean contains(final Object other ) { 
	    	return other==o; 
	    }
	    public final Location get( int index ) {
	        if( index != 0 ) {
	            throw new IndexOutOfBoundsException( "Singleton list; index = "+index );
	        }
	        return o;
	    }
	}
}
