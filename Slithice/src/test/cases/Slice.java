package test.cases;


public class Slice {
	int sf;
	
	public static void main(String[] args){
		Slice s = new Slice();
		
		test1();
		test2();
		test3();
		test4();		
		test5();
		test6();
		
		s.test7();
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
	
	/** Only control dependence. */
	private static void test2(){
		int x = 0;
		x++;
	}
	
	/** Test pre-execution slicing and post-execution slicing. */
	private static void test3(){
		int a = 0;
		int b = 1;
		
		a = a + b;
		b = a;
	}
	
	/** Test slicing criterion in a complex statement. */
	private static void test4(){
		int m = 0;
		int n = 1;
		int p = 2;
		
		m = m + n; n = m; m = m + p;
		int q = m;
	}
	
	
    static class C{
    	static int global;
    	int f;
    }
    
    //test slicing on global
    private static void test5(){
    	int i = 0;
    	int j = 1;
    	
    	C.global = i + j;
    	
    	j = C.global;
    	
    	int x = C.global;
    }
    
    
    public static void test6(){
    	int i = 0;
    	int j = 1;
    	
    	C c = new C();
    	c.f = i + j;
    	
    	j = c.f;
    	
    	int x = c.f;
    }
    
    public void test7(){
    	int i = 0;
    	int j = 1;    	
    	
    	sf = i + j;
    	
    	j = sf;
    	
    	int x = this.sf;
    }
}
