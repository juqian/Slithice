package jqian.sootex.du;

import java.util.*;

import jqian.sootex.location.AccessPath;
import jqian.sootex.location.Location;
import soot.*;


/**
 * Reaching def/use query
 */
public interface IReachingDUQuery {   
	public Collection<Location> getDULocations(Unit u);
	
    /**Get the reaching definition or use sites of the given locations 
     * @param loc  The location whose definition or use sites are queried
     * @param ap   The access path of the queried location (This parameter is optional)
     */
    public Collection<Unit> getReachingDUSites(Unit stmt, AccessPath ap, Location loc); 
    public Collection<Unit> getReachingDUSites(Unit stmt, AccessPath ap, Collection<Location> locs);
}
 