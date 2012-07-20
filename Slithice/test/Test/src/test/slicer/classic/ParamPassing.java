package test.slicer.classic;

public class ParamPassing {
	static Object m1(Object o, int i, String s, Class<?> c){
		return o;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		m1(null,0,"a", ParamPassing.class);
	}

}
