package test.slicer.classic;

/** A modification of the case in Horwitz-Reps-Binkley's paper:
 *  Interprocedural slicing using dependence graphs. in TOPLAS 1990. */
public class Howitz90Example {
	public static void main(String[] args){
		int sum = 0;
		int i = 1;
		while(i < 11){
			sum = A(sum, i);
			i = i + 1;
		}
		
		int ret = sum + i;
		ret++;
	}
	
	private static int A(int x, int y){
	    x = Add.add(x, y);
	    y = Increase.increment(y);
	    return x;
	}
}
