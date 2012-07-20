/* This is a JOE CHIEN file. 
 * Thank you for reading, using and distributing it.
 * @author Joe Chien{owqian@gmail.com}
 * @date 2007-6-22
 * @version 0.01
 */
package test.slicer.classic;

/**
 * @author bruteforce
 *
 */
public class SlicingCase {
	public static void main(String[] args) {
		test1();
	}
	
	/** A modification of the case in Horwitz-Reps-Binkley's paper:
	 *  Interprocedural slicing using dependence graphs. in TOPLAS 1990. */
	private static void test1(){
		int sum = 0;
		int i = 1;
		while(i < 11){
			sum = A(sum, i);
			i = i + 1;
		}	
		
		int x = sum + i;
		x++;
	}
	
	private static int A(int x, int y){
	    x = add(x, y);
	    y = increment(y);
	    y++;
	    return x;
	}
	
	private static int add (int a, int b){
	    return a + b;
	}
	
	private static int increment(int z){
		return add(z, 1);
	}
}
