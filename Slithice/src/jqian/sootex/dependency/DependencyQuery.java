package jqian.sootex.dependency;

import java.util.*;

import jqian.sootex.CFGProvider;
import jqian.sootex.du.IGlobalDUQuery;
import jqian.sootex.du.IReachingDUQuery;
import jqian.sootex.location.Location;
import jqian.sootex.util.SootUtils;

import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

/**
 * XXX: This class do not cache data, so, could be very slow if reenter.
 */
public class DependencyQuery implements IDependencyQuery {
	private IGlobalDUQuery _duQuery;
	private CFGProvider _cfgProvider;
	private Map<Unit,Collection<Unit>>[] _method2ctrlDepMap;
	
	@SuppressWarnings("unchecked")
	public DependencyQuery(IGlobalDUQuery duQuery, CFGProvider cfgProvider){
		this._duQuery = duQuery;
		this._cfgProvider = cfgProvider;
		this._method2ctrlDepMap = new Map[SootUtils.getMethodCount()];
	}
	
	public void release(SootMethod m){
		int mId = m.getNumber();
		_duQuery.releaseQuery(m);
		_method2ctrlDepMap[mId] = null;
	}
	
	public Collection<Unit> getCtrlDependences(SootMethod m, Unit u){
		int mId = m.getNumber();
		Map<Unit,Collection<Unit>> map = _method2ctrlDepMap[mId];
		if(map==null){
			UnitGraph cfg = _cfgProvider.getCFG(m);
			map = DependencyHelper.calcCtrlDependences(cfg);
			_method2ctrlDepMap[mId] = map;
		}
		
		Collection<Unit> result = map.get(u);
		return result;
	}
	
	/** Get Write->Read dependences. */
	public Collection<Unit> getWRDependences(SootMethod m, Unit u){
		IReachingDUQuery rd = _duQuery.getRDQuery(m);
		IReachingDUQuery ru = _duQuery.getRUQuery(m);
		Collection<Location> usedLocations = ru.getDULocations(u);
		Collection<Unit> defs = rd.getReachingDUSites(u, null, usedLocations);
		return defs;
	}
	
	/** Get Read->Write dependences. */
	public Collection<Unit> getRWDependences(SootMethod m, Unit u){
		IReachingDUQuery rd = _duQuery.getRDQuery(m);
		IReachingDUQuery ru = _duQuery.getRUQuery(m);
		Collection<Location> defLocations = rd.getDULocations(u);
		Collection<Unit> uses = ru.getReachingDUSites(u, null, defLocations);
		return uses;
	}
	
	/** Get Write->Write dependences. */
	public Collection<Unit> getWWDependences(SootMethod m, Unit u){
		IReachingDUQuery rd = _duQuery.getRDQuery(m);
		Collection<Location> defLocations = rd.getDULocations(u);
		Collection<Unit> defs = rd.getReachingDUSites(u, null, defLocations);
		return defs;
	}
	
	public Collection<Unit> getAllDependences(SootMethod m, Unit u){
		Collection<Unit> cd = getCtrlDependences(m, u);
		Collection<Unit> rwd = getRWDependences(m, u);
		Collection<Unit> wrd = getWRDependences(m, u);
		Collection<Unit> wwd = getWWDependences(m, u);
		Collection<Unit> dep = new HashSet<Unit>();
		dep.addAll(cd);
		dep.addAll(rwd);
		dep.addAll(wrd);
		dep.addAll(wwd);
		return dep;
	}
}
