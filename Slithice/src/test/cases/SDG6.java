package test.cases;

import java.io.*;

/**
 * A test case which may use a large number library methods.
 */
public class SDG6 {
	 static void test1() {
		 String cmd = "dir";
			try {
				Process process = Runtime.getRuntime().exec(cmd);
				try {
					process.waitFor();
					process.getOutputStream();
					process.getInputStream();
					process.exitValue();
					process.getErrorStream();
					
				} catch (InterruptedException e) {
					System.out.println("dot error.");
				}
			} catch (SecurityException e) {
				System.err.println("Security error!");
				e.printStackTrace(System.err);
			} catch (NullPointerException e) {
				System.err.println("Command is null!");
				e.printStackTrace(System.err);
			} catch (IllegalArgumentException e) {
				System.err.println("Command is empty!");
				e.printStackTrace(System.err);
			} catch (IOException e) {
				System.err.println("IO error!");
				e.printStackTrace(System.err);
			}
		}
		 

		public static void main(String[] args) {
			test1();
		}
}
