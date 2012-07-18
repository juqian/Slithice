/* 
 * @author Ju Qian{jqian@live.com}
 * @date 2007-7-2
 * @version 0.01
 */
package com.conref.util;

import java.util.Comparator;
import soot.util.*;

/**
 * @author bruteforce
 *
 */
public class NumberableComparator implements Comparator<Numberable> {
	protected NumberableComparator(){}
	private static NumberableComparator _instance = new NumberableComparator();
	
	public static NumberableComparator v(){
		return _instance;
	}
	
	public int compare(Numberable n1, Numberable n2) {
		return n1.getNumber()-n2.getNumber();
	}
}
