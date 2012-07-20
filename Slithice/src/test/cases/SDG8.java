package test.cases;


/** Test variables types of location abstractions for summary edge construction  */
public class SDG8 {
	static class Int{
		int i;
	}
	
	static Int ONE;
	
	static{
		ONE = new Int();
		ONE.i = 1;
	}
	
	public static void main(String[] args) {
		test1();
	}
	
	private static void test1(){
		Int ix = new Int();
		Int iy = new Int();
		ix.i = 0;
		iy.i = 1;
		
	    ix = add(ix, iy);
	    ix.i++;
	    
	    int[] z = new int[2];
	    inc(z);
	}
 
	private static Int add (Int a, Int b){
		Int r = new Int();
		r.i = a.i + b.i;
	    return r;
	}
	
	private static void inc(int[] a){
		a[0]++;
	}
}
