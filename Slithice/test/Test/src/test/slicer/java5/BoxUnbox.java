/**
 * 
 */
package test.slicer.java5;

/**
 * @author bruteforce
 *
 */
public class BoxUnbox {
	static void boxUnbox(){
		int i = 0;
		Integer j = 2; //box
		int x = 3;
		
		int k = i + j;
		i = k + x;
		k++;
	}
 
	public static void main(String[] args) {
		boxUnbox();
	}

}
