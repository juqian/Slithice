package jqian.sootex.ptsto;

import java.util.HashSet;
import java.util.Set;

import jqian.util.CollectionUtils;

import soot.PointsToSet;
import soot.Type;
import soot.jimple.ClassConstant;

/**
 *
 */
@SuppressWarnings("serial")
public class TypeBasedPointsToSet extends HashSet<Type> implements PointsToSet {

	/* (non-Javadoc)
	 * @see soot.PointsToSet#isEmpty()
	 */
	public boolean isEmpty() {		 
		return super.isEmpty();
	}

	/* (non-Javadoc)
	 * @see soot.PointsToSet#hasNonEmptyIntersection(soot.PointsToSet)
	 */
	public boolean hasNonEmptyIntersection(PointsToSet other) {
		if(this == other){
			return true;
		}
		
		if(!(other instanceof TypeBasedPointsToSet)){
			throw new RuntimeException("parameter type unsupported.");
		}
		 
		return CollectionUtils.hasInterset(this, (TypeBasedPointsToSet)other);
	}

	/* (non-Javadoc)
	 * @see soot.PointsToSet#possibleTypes()
	 */
	public Set<Type> possibleTypes() {
		throw new RuntimeException("Unimplemented.");
	}

	/* (non-Javadoc)
	 * @see soot.PointsToSet#possibleStringConstants()
	 */
	public Set<String> possibleStringConstants() {
		throw new RuntimeException("Unimplemented.");
	}

	/* (non-Javadoc)
	 * @see soot.PointsToSet#possibleClassConstants()
	 */
	public Set<ClassConstant> possibleClassConstants() {
		throw new RuntimeException("Unimplemented.");
	}
}
