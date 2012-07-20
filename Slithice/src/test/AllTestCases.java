package test;


interface AllTestCases{
	// Reaching Definition Analysis 
	static interface RD{
		public final String MAIN_CLASS = "test.cases.RD";
		
		public static String[] INTRAPROC = {
			"<test.cases.RD: void test_stack_rd()>",
			"<test.cases.RD: void test_heap_rd()>",
			"<test.cases.RD: void test_array_dep()>",
			"<test.cases.RD: void test_kill()>",
			"<test.cases.RD: int test_on_entry(int,int)>"
		};
		
		public static String[] INTERPROC = {
			"<test.cases.RD: void test_simple_call()>",	   
			"<test.cases.RD: void test_single_call()>",
			"<test.cases.RD: void test_virtual_call(int)>",
		};
	}
	
	// Reaching Use Analysis 
	static interface RU{
		public final String MAIN_CLASS = "test.cases.RU";
		
		public static String[] INTRAPROC = {
			"<test.cases.RU: void test_stack_ru()>",
			"<test.cases.RU: void test_heap_ru()>",
			"<test.cases.RU: void test_array_ru()>",
			"<test.cases.RU: void test_global()>",
			"<test.cases.RU: int test_on_entry(int,int)>"		 
		};
		 
		public static String[] INTERPROC = {
			"<test.cases.RU: void test_simple_call()>",
			"<test.cases.RU: void test_single_call()>",
			"<test.cases.RU: void test_virtual_call(int)>"
		};
	}
	
	// DependenceQuery 
	static interface DepQuery{
		public final String MAIN_CLASS = "test.cases.DepQuery";
		
		public static String[] CTRL_DEPENDENCE_QUERY = {
			"<test.cases.DepQuery: int test_ctrl_dep1(int)>",
			"<test.cases.DepQuery: int test_ctrl_dep2(int)>",
			"<test.cases.DepQuery: int test_ctrl_dep3(int)>",
			"<test.cases.DepQuery: int test_ctrl_dep4(int)>",
			"<test.cases.DepQuery: int test_ctrl_dep5(int)>",
			"<test.cases.DepQuery: int test_ctrl_dep6(int)>",		 
		};
		
		public static String[] FLOW_DEPENDENCE_QUERY = {
			"<test.cases.DepQuery: int test_flow_dep1(int)>",
			"<test.cases.DepQuery: int test_flow_dep2(int)>",
			"<test.cases.DepQuery: int test_flow_dep3(int)>",
			"<test.cases.DepQuery: int test_flow_dep4(int)>",
			"<test.cases.DepQuery: int test_flow_dep5(int)>",
		};
		
		public static String[] ANTI_DEPENDENCE_QUERY = {
			"<test.cases.DepQuery: int test_anti_dep1(int)>",
			"<test.cases.DepQuery: int test_anti_dep2(int)>",
			"<test.cases.DepQuery: int test_anti_dep3(int)>",
		};
		
		public static String[] OUTPUT_DEPENDENCE_QUERY = {
			"<test.cases.DepQuery: int test_output_dep1(int)>",
			"<test.cases.DepQuery: int test_output_dep2(int)>",
		};
	}
	
	static interface SDGCases{
		public final String[] CLASSES = {	
				"test.cases.SDG1",
				"test.cases.SDG2",
				"test.cases.SDG3",
				"test.cases.SDG4",
				"test.cases.SDG5",
				"test.cases.SDG6",
				"test.cases.SDG7",
				"test.cases.SDG8",
		};
			

		public static String[][] SDG_ENTRIES = {
				{
				   "<test.cases.SDG1: void test1()>",
				   "<test.cases.SDG1: void test2()>",
				   "<test.cases.SDG1: void test3()>",
				   "<test.cases.SDG1: void test4()>",
				   "<test.cases.SDG1: void test5()>",
				   "<test.cases.SDG1: void test6()>",
				   "<test.cases.SDG1: void test7()>",
				},
				{
				   "<test.cases.SDG2: void test1()>",//10
				   "<test.cases.SDG2: void test2()>",
				},
				{
				   "<test.cases.SDG3: void test1()>",	 
				},
				{
				   "<test.cases.SDG4: void test1()>", //16		 
				   "<test.cases.SDG4: void test2()>",
				   "<test.cases.SDG4: void test3()>",
				   "<test.cases.SDG4: void test4()>",
				   "<test.cases.SDG4: void test5()>",
				   "<test.cases.SDG4: void test6()>",//21
				},
				{
				   "<test.cases.SDG5: void main(java.lang.String[])>",	
				},
				{
				   "<test.cases.SDG6: void test1()>",	
				},
				{
				   "<test.cases.SDG7: void test1()>",	
				}
		};
	}
	
	
	
	
	public final String SLICE_MAIN_CLASS = "test.cases.Slice"; 
	
	
	
	
	
	
	

	

	
	
	//////////////////////// PDG Construction ////////////////////////////
	public final String PDG_MAIN_CLASS = "test.dependency.cases.PDG";
	
	public static String[] PDG = {
		"<test.cases.PDG: void test_param_passing()>",
		"<test.cases.PDG: void test_reflection()>",
	};
	
	
	
	
	//////////////////// Local Slicing Test Cases ///////////////////////
	public static String[] SLICING_START={	   
		   "<test.cases.Slice: void test1()>",
		   "<test.cases.Slice: int A(int,int)>",
		   "<test.cases.Slice: void test2()>",		
		   "<test.cases.Slice: void test3()>",
		   "<test.cases.Slice: void test4()>",
		   "<test.cases.Slice: void test5()>",
		   "<test.cases.Slice: void test6()>",
		   "<test.cases.Slice: void test7()>",
		   "<test.cases.Slice: void main(java.lang.String[])>",
	};
	
	
	
	/////////////////////////////////////////////////////////////////////
	
	public final String[] FIELDS = {
		"<test.cases.Slice$C: int global>",
		"<test.cases.Slice$C: int f>",
	};
	
   
   
   public static String[] RD_HEAP_STRONG_UPDATE = {	   
	   "<test.cases.PDG: void test_kill_2()>",
	   "<test.cases.PDG: void test_kill_3()>",
	   "<test.cases.PDG: void test_kill_4()>",
	   "<test.cases.PDG: void test_kill_5()>",
	   "<test.cases.PDG: void test_kill_6()>",
	   "<test.cases.PDG: void test_kill_7()>",
	   "<test.cases.PDG: void test_kill_8()>"
   };   
   
   public static String[] RD_HEAP_INTERPROC_STRONG_UPDATE = {
	   "<test.cases.PDG: void test_interproc_kill_1()>",
	   "<test.cases.PDG: void test_interproc_kill_2()>",
	   "<test.cases.PDG: void test_interproc_kill_3()>",
	   "<test.cases.PDG: void test_interproc_kill_4()>"
   };
   
   public static String[] RU_BAISC={
	   "<test.cases.ExPDG: void simple()>",
	   "<test.cases.ExPDG: void multi_path()>",
	   "<test.cases.ExPDG: void loop()>",
	   "<test.cases.ExPDG: void test_relative_update()>",
	   "<test.cases.ExPDG: void test_update_entry(test.slicing.cases.ExPDGCase$Node)>"
   };
   
   public static String[] RU2_INTERPROC={
	   "<test.cases.ExPDG: void method_gen()>",
	   "<test.cases.ExPDG: void method_kill()>"	   
   };
   
   public static String[] PDG_USE ={
	   "<test.cases.ExPDG: void test_method_use1(int,test.slicing.cases.ExPDGCase$Node)>",
	   "<test.cases.ExPDG: void test_method_use2()>",    
	   "<test.cases.ExPDG: void test_method_use3(test.slicing.cases.ExPDGCase$Node)>",    
	   "<test.cases.ExPDG: void test_method_use4()>",    
	   "<test.cases.ExPDG: void test_method_use5()>"
   };
   
   
   
   public static String[] PDG_METHODS = {
	   "<test.cases.PDG3: int test1(int,test.slicing.cases.PDG3$Node)>",
	   "<test.cases.PDG3: void test2()>",
   }; 
   
   
   
   public static String[] METHODS={
	   "<test.cases.SDG4: int foreach2()>",//21
   };
   //"<test.slicing.cases.ExPDGCase: void use1(test.slicing.cases.ExPDGCase$Node)>"
   
   
   static interface SideEffect{
	   public static String TESTED_APP = "test.cases.SideEffect";
		
	   public static String FIELD_SCANER_CASES[] = {
			"<test.cases.SideEffect: void simpleTest()>",
			"<test.cases.SideEffect: void directRecursionTest()>",
			"<test.cases.SideEffect: void indirectRecursionTest()>",
			"<test.cases.SideEffect: void iterativeComputingTest()>"};
		
	   public static String SIDE_EFFECT_CASES[] = {
			"<test.cases.SideEffect: void simpleSideEffectTest(test.cases.Node)>",
			"<test.cases.SideEffect: void arrayAccessTest()>",
			"<test.cases.SideEffect: void crossThreadTest()>",
			"<test.cases.SideEffect: void test7()>"};
   }
}
