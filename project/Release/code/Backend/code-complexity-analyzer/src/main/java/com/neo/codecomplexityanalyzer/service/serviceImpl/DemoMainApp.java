package com.neo.codecomplexityanalyzer.service.serviceImpl;

import java.util.HashMap;

public class DemoMainApp {
	public static void main(String[] args) {

		CiCppServicesImpl ci = new CiCppServicesImpl(
				"/home/sahan/Documents/My Documents/SLIIT/SPM/Code-complexity-analyzer/project/Release/code/Backend/code-complexity-analyzer/src/main/resources/sampleData/CppInheritanceSample.cpp");

		CiJavaServicesImpl ciJ = new CiJavaServicesImpl(
				"/home/sahan/Documents/My Documents/SLIIT/SPM/Code-complexity-analyzer/project/Release/code/Backend/code-complexity-analyzer/src/main/resources/sampleData/InheritanceSample.java");

		ci.getAncestorClasses("Person");
//		for (String string : ci.getAncestorClasses("TA")) {
//			System.out.print(string);
//		}
//		for (String string : ciJ.getAnsestorClassNames("SportsCar")) {
//			System.out.println(string);
//		}

//		HashMap<String, String> map = ci.getClassMapping();
//		for (String name : map.keySet()) {
//			String key = name.toString();
//			String value = map.get(name).toString();
//			System.out.println(key + " " + value);
//		}
		// System.out.println("Count Returned : " + count);
	}

}