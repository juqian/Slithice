package jqian.sootex.dependency.pdg;

import java.util.Comparator;

public class DependenceEdgeComparator implements Comparator<DependenceEdge>  {
	protected DependenceEdgeComparator(){}
	@SuppressWarnings("rawtypes")
	private static Comparator _instance = new DependenceEdgeComparator();
	
	@SuppressWarnings("rawtypes")
	public static Comparator v(){
		return _instance;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(DependenceEdge arg0, DependenceEdge arg1) {
		return 0;
	}
}
