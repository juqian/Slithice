/*  
 * @author Ju Qian{jqian@live.com}
 * @date 2006-12-22
 * @version 0.01
 */
package com.conref.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *  
 */
public class CollectionUtils {
	public static String toString(Iterator<?> it, String separator) {
		StringBuffer buf = new StringBuffer();
		while (it.hasNext()) {
		
			Object m = it.next();
			
			buf.append(m);
			if (it.hasNext()) {
				buf.append(separator);
			}

			}
		return buf.toString();
	}
	
	public static String toString(Object[] objects, String separator) {
		StringBuffer buf = new StringBuffer();
		int length = objects.length;
		for (int i = 0; i < length; i++) {
			buf.append(objects[i]);
			if (i < length - 1) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}

	public static class ElementMatcher {
		protected boolean match(Object obj) {
			return false;
		}
	}

	public static Collection<Object> search(Iterator<?> it,
			ElementMatcher matcher) {
		List<Object> st = new LinkedList<Object>();
		for (; it.hasNext();) {
			Object obj = it.next();
			if (matcher.match(obj)) {
				st.add(obj);
			}
		}
		return st;
	}

	public static boolean hasInterset(Set<?> a, Set<?> b) {
		if (a.size() > b.size()) {
			Set<?> tmp = a;
			a = b;
			b = tmp;
		}

		for (Object o : a) {
			if (b.contains(o)) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasInterset(Collection<?> a, Collection<?> b) {
		if (a.size() > b.size()) {
			Collection<?> tmp = a;
			a = b;
			b = tmp;
		}

		Set<?> set = new HashSet<Object>(b);
		for (Object o : a) {
			if (set.contains(o)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return the intersected original set, all a new set. The operation is
	 * determined by set size, and proportion of set size.
	 */
	public static Set<?> intersectWith(Set<?> workingset,
			final Set<?> intersected) {
		final int TIMES = 20;

		if (workingset.size() > intersected.size() * TIMES) {
			Set<?> newSt = (Set<?>) ((HashSet<?>) intersected).clone();
			newSt.retainAll(workingset);
			return newSt;
		} else {
			workingset.retainAll(intersected);
			return workingset;
		}
	}

	@SuppressWarnings("unchecked")
	public static Set<?> copy(final Set<?> from) {
		Set<Object> newSt = null;
		if (from instanceof HashSet) {
			newSt = (Set<Object>) ((HashSet<?>) from).clone();
		} else if (from instanceof TreeSet) {
			newSt = (Set<Object>) ((TreeSet<?>) from).clone();
		} else {
			newSt = new HashSet<Object>();
			newSt.addAll(from);
		}

		return newSt;
	}

	public static Set<?> intersect(final Set<?> a, final Set<?> b) {
		final int TIMES = 20;

		Set<?> small, big;
		// make sure a is the smaller set
		if (a.size() > b.size() * TIMES) {
			small = b;
			big = a;
		} else {
			small = a;
			big = b;
		}

		Set<?> newSt = copy(small);
		newSt.retainAll(big);
		return newSt;
	}

	public static int getHashSetInitCapacity(int elementCount) {
		return 1 + (elementCount + 1) * 4 / 3;
	}
}
