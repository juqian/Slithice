package jqian.sootex.ptsto;

import java.util.Set;

import jqian.sootex.location.InstanceObject;
import jqian.sootex.location.Location;
import soot.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;

/**
 * Query points-to relations using both spark and type-based points-to analysis. 
 * For methods reachable from the main entry, use spark query; otherwise, use type-based query.
 */
public class CombinedPtsToQuery implements IPtsToQuery{
	private SparkPtsToQuery spark;
	private TypeBasedPtsToQuery typebased;
	private ReachableMethods _reachFromMain;

	public CombinedPtsToQuery(SparkPtsToQuery spark, TypeBasedPtsToQuery typebased, ReachableMethods reachFromMain){
		this.spark = spark;
		this.typebased = typebased; 
		this._reachFromMain = reachFromMain;
    }  

	public Set<InstanceObject> getPointTos(SootMethod m, Unit stmt, Location ptr) {
		if(_reachFromMain.contains(m)){
			 return spark.getPointTos(m, stmt, ptr);
		 }
		 else{
			 return typebased.getPointTos(m, stmt, ptr);
		 }
	}
}