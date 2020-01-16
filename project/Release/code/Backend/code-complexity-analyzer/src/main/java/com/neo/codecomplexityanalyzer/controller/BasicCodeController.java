/*
-------------------------------------------------------------------------------------------------------
--  Date        Sign    History
--  ----------  ------  --------------------------------------------------------------------------------
--  2019-08-06  Sathira  185817, Added CTC functions.
--  2019-08-06  Sahan          , Added CiJava Services Functions
--  ----------  ------  --------------------------------------------------------------------------------
*/
package com.neo.codecomplexityanalyzer.controller;

import java.util.*;

import com.neo.codecomplexityanalyzer.service.serviceimpl.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.neo.codecomplexityanalyzer.model.CiResultModel;
import com.neo.codecomplexityanalyzer.service.ICNCService;

@CrossOrigin(origins = { "http://localhost:1234", "http://localhost:3000" })

@RestController
public class BasicCodeController {
	private static final Logger logger = LogManager.getLogger(BasicCodeController.class);

	// --------------------------------------- General End Points
	// ---------------------------------------------------------------
	// REST service to get the line count
	@GetMapping(path = "/line-count")
	public ResponseEntity<Integer> getLineCount(@RequestHeader("file-path") String filePath) {
		String code = "";
		int lineCount = 0;
		GeneralServiceImpl ccaUtil = new GeneralServiceImpl();

		code = ccaUtil.getSourceCode(filePath);
		lineCount = ccaUtil.findSourceCodeLineCount(code);

		return (new ResponseEntity<Integer>(lineCount, HttpStatus.OK));
	}

	// REST service to get the file type
	@GetMapping(path = "/file-type")
	public ResponseEntity<String> getFileType(@RequestHeader("file-path") String filePath) {
		GeneralServiceImpl ccaUtil = new GeneralServiceImpl();
		String type = ccaUtil.getSourceCodeType(filePath);
		return (new ResponseEntity<String>(type, HttpStatus.OK));
	}

	// REST service to get the source code as an array
	@GetMapping(path = "/get-code")
	public ResponseEntity<String[]> getSourceCode(@RequestHeader("file-path") String filePath) {
		String code;
		int lineCount;
		String[] lineArr;
		GeneralServiceImpl ccaUtil = new GeneralServiceImpl();
		ccaUtil.getSourceCode(filePath);
		code = ccaUtil.getOriginalSourceCode();
		lineCount = ccaUtil.findSourceCodeLineCount(code);
		lineArr = ccaUtil.collectAllSourceCodeLines(code, lineCount);

		return (new ResponseEntity<String[]>(lineArr, HttpStatus.OK));
	}

	@GetMapping(value = "/get-score", produces = { "application/json" })
	public ResponseEntity<?> getSourceCodeFormatted(@RequestHeader("file-path") String filePath) {
		try {

			ResponseClass r1 = new ResponseClass();
			GeneralServiceImpl generalService = new GeneralServiceImpl();

			String fileType = generalService.getSourceCodeType(filePath);
			List<String> errorList = new ArrayList<>();
			if(fileType.equals("java")) {
				JavaSyntaxChecker javaSyntaxChecker = new JavaSyntaxChecker(filePath);
				 errorList = javaSyntaxChecker.check();
			}
			String code = generalService.getSourceCode(filePath);
			r1.setCode(Arrays.asList(
					generalService.collectAllSourceCodeLines(code, generalService.findSourceCodeLineCount(code))));

			CTCServiceImpl cctUtil = new CTCServiceImpl(filePath);
			int itcScore = cctUtil.getIterativeControlScore();
			int controlScore = cctUtil.getControlScore();
			int catchScore = cctUtil.getCatchScore();
			int switchScore = cctUtil.getSwitchScore();

			HashMap<Integer, Integer> m1 = (HashMap<Integer, Integer>) cctUtil.getLineScore();
			String[] lineScoreArray = new String[m1.size()];
			if (!errorList.isEmpty()) {
				Arrays.fill(lineScoreArray, "-");
				r1.setLineScore(Arrays.asList(lineScoreArray));
				r1.setErrorList(errorList);
				r1.setTotalCtcCount("Error");
				r1.setStatusCode("500");
				r1.setErrorMessage("Please fix the errors in the code !");
				return (new ResponseEntity<>(r1, HttpStatus.OK));
			}

			int i = 0;
			for (Map.Entry<Integer, Integer> entry : m1.entrySet()) {
				int value = entry.getValue();
				lineScoreArray[i] = String.valueOf(value);
				++i;
			}
			r1.setLineScore(Arrays.asList(lineScoreArray));
			r1.setTotalCtcCount(String.valueOf((itcScore + controlScore + catchScore + switchScore)));
			r1.setStatusCode("200");
			return (new ResponseEntity<>(r1, HttpStatus.OK));
		} catch (IllegalArgumentException e) {
			ResponseClass responseClass = new ResponseClass();
			responseClass.setErrorMessage("Invalid file or file path !");
			responseClass.setTotalCtcCount("0");
			responseClass.setStatusCode("501");
			return (new ResponseEntity<>(responseClass, HttpStatus.OK));
		} catch (Exception e) {
			logger.log(Level.ERROR, e);
		}
		return null;
	}

	// --------------------------------------- CTC End Points
	// ---------------------------------------------------------------
	@GetMapping(path = "/get-ctc/if")
	public ResponseEntity<Integer> getCTCScore(@RequestHeader("file-path") String filePath) {
		CTCServiceImpl cctUtil = new CTCServiceImpl(filePath);
		return (new ResponseEntity<>(cctUtil.getControlScore(), HttpStatus.OK));
	}

	@GetMapping(path = "/get-ctc/itc")
	public ResponseEntity<Integer> getCTCITCScore(@RequestHeader("file-path") String filePath) {
		CTCServiceImpl cctUtil = new CTCServiceImpl(filePath);
		return (new ResponseEntity<>(cctUtil.getIterativeControlScore(), HttpStatus.OK));
	}

	@GetMapping(path = "/get-ctc/catch")
	public ResponseEntity<Integer> getCTCCatchScore(@RequestHeader("file-path") String filePath) {
		CTCServiceImpl cctUtil = new CTCServiceImpl(filePath);
		return (new ResponseEntity<>(cctUtil.getCatchScore(), HttpStatus.OK));
	}

	@GetMapping(path = "/get-ctc/case")
	public ResponseEntity<Integer> getCTCSwitchScore(@RequestHeader("file-path") String filePath) {
		CTCServiceImpl cctUtil = new CTCServiceImpl(filePath);
		return (new ResponseEntity<>(cctUtil.getSwitchScore(), HttpStatus.OK));
	}

	@GetMapping(path = "/get-ctc")
	public ResponseEntity<HashMap> getCTCTotalScore(@RequestHeader("file-path") String filePath) {
		HashMap<String, String> hashMap = new HashMap<>();
		CTCServiceImpl cctUtil = new CTCServiceImpl(filePath);
		int itcScore = cctUtil.getIterativeControlScore();
		int controlScore = cctUtil.getControlScore();
		int catchScore = cctUtil.getCatchScore();
		int switchScore = cctUtil.getSwitchScore();
		int ctcTotal = itcScore + catchScore + switchScore + controlScore;

		if (controlScore == -1) {
			controlScore = 0;
			hashMap.put("Control Score Error : ", String.valueOf(ctcTotal));
		}

		hashMap.put("ControlScore", String.valueOf(controlScore));
		hashMap.put("ITCScore", String.valueOf(itcScore));
		hashMap.put("CatchScore", String.valueOf(catchScore));
		hashMap.put("SwitchScore", String.valueOf(switchScore));
		hashMap.put("TotalCTCScore", String.valueOf(ctcTotal));

		cctUtil.getLineScore();

		return (new ResponseEntity<>(hashMap, HttpStatus.OK));
	}

	@GetMapping(path = "/get-ctc-line-score")
	public ResponseEntity<String[]> getCTCLineScore(@RequestHeader("file-path") String filePath) {
		CTCServiceImpl cctUtil = new CTCServiceImpl(filePath);
		cctUtil.getIterativeControlScore();
		cctUtil.getControlScore();
		cctUtil.getCatchScore();
		cctUtil.getSwitchScore();
		HashMap<Integer, Integer> m1 = (HashMap<Integer, Integer>) cctUtil.getLineScore();
		String[] lineScoreArray = new String[m1.size()];
		int i = 0;
		for (Map.Entry<Integer, Integer> entry : m1.entrySet()) {
			int value = entry.getValue();
			lineScoreArray[i] = String.valueOf(value);
			++i;
		}
		return (new ResponseEntity<>(lineScoreArray, HttpStatus.OK));
	}

	/*
	 * -----------------------------------------------------------------------------
	 * Ci End Points
	 * -----------------------------------------------------------------------------
	 */

	// This service is to get all the class Names
	@GetMapping(path = "/get-ci/class-names")
	public List<String> getClassNames(@RequestHeader("file-path") String filePath) {
		CiJavaServicesImpl ci = new CiJavaServicesImpl(filePath);
		return ci.getClassNames();
	}

	/*
	 * This service will give the names of the ancestor class Names when a class
	 * Name is given
	 * 
	 * @RequestHeader file-path
	 * 
	 * @RequestParam nameOfTheClass
	 */
	@GetMapping(path = "/get-ci/ansestors")
	public List<String> getNamesOfTheAncestorClasses(@RequestHeader("file-path") String filePath,
			@RequestParam String nameOfTheClass) {
		CiJavaServicesImpl ci = new CiJavaServicesImpl(filePath);
		return ci.getAnsestorClassNames(nameOfTheClass);
	}

	// This service is giving a console log as a briefing of all the details
	@GetMapping(path = "/get-ci/class-level-data")
	public void getClassLevelDetails(@RequestHeader("file-path") String filePath) {
		CiJavaServicesImpl ci = new CiJavaServicesImpl(filePath);
		ci.getClassLevelDetails();
	}

	/*
	 * This service will give the relationship of all the classes with the names in
	 * HashMap
	 */
	@GetMapping(path = "/get-ci/class-map")
	public Map<String, String> getClassMap(@RequestHeader("file-path") String filePath) {
		CiJavaServicesImpl ci = new CiJavaServicesImpl(filePath);
		return ci.getClassMapping();
	}

	// This service is to give the number of ancestor classes of a given class name
	@GetMapping(path = "/get-ci/num-of-ancestors")
	public int getNumberOfAncestors(@RequestHeader("file-path") String filePath, @RequestParam String nameOfTheClass) {
		CiJavaServicesImpl ci = new CiJavaServicesImpl(filePath);
		return ci.getNumberOfAnsestors(nameOfTheClass);
	}

	// This service is to give the complexity of classes separately on a map
	@GetMapping(path = "/get-ci/complexity-map")
	public Map<String, Integer> getComplexityOfAllClasses(@RequestHeader("file-path") String filePath) {
		CiJavaServicesImpl ci = new CiJavaServicesImpl(filePath);
		return ci.complexityOfAllClassesDueToInheritance();
	}

	// This method is for future usages
	@GetMapping(path = "/get-ci/conneted-points")
	public void getStronglyConnectedPoints(@RequestHeader("file-path") String filePath) {
		CiJavaServicesImpl ci = new CiJavaServicesImpl(filePath);
		ci.identifyStronglyConnectedClasses();
	}

	@GetMapping(path = "/get-ci/by-line", produces = "application/json")
	public Map<Integer, CiResultModel> getClassNameLineNumber(@RequestHeader("file-path") String filePath) {
		CiJavaServicesImpl ci = new CiJavaServicesImpl(filePath);
		GeneralServiceImpl gs = new GeneralServiceImpl();
		if ("java".equals(gs.getSourceCodeType(filePath))) {
			return ci.getClassNameIndexByLineNumber();
		} else if ("cpp".equals(gs.getSourceCodeType(filePath))) {
			CiCppServicesImpl ciCpp = new CiCppServicesImpl(filePath);
			return ciCpp.getCiCppDetailsWithLineNumbers();
		} else {
			return null;
		}
	}

	/*
	 * ------------------------ Ci in c++ End Points ------------------------
	 */

	/*
	 * This service end point is to get all the classes in the cpp file
	 */
	@GetMapping(path = "/get-ci/cpp/all-classes")
	public List<String> getAllClassesInTheCode(@RequestHeader("file-path") String filePath) {
		CiCppServicesImpl ciC = new CiCppServicesImpl(filePath);
		return ciC.getAllClassNames();
	}

	/*
	 * This service is end point to get all the ancestors of all the classes
	 */
	@GetMapping(path = "/get-ci/cpp/ancestor-classes")
	public List<String> getAncestorClassesInTheCode(@RequestHeader("file-path") String filePath,
			@RequestParam String claasName) {
		CiCppServicesImpl ciC = new CiCppServicesImpl(filePath);
		return ciC.getAncestorClasses(claasName);
	}

	/*
	 * This service is to get all the classes with the number of ancestor classes in
	 * a Hash Map
	 */
	@GetMapping(path = "/get-ci/cpp/all-classes-with-ancestors")
	public Map<String, Integer> getAllClassesWithNumOfAncestors(@RequestHeader("file-path") String filePath) {
		CiCppServicesImpl ciC = new CiCppServicesImpl(filePath);
		return ciC.getAncestorCountMapForAllClasses();
	}

	/*
	 * This service is will give the relationship between each of the classes in a
	 * Map
	 */
	@GetMapping(path = "/get-ci/cpp/all-classes-with-inherited-classes")
	public Map<String, String> getClassesWithTheInheritedClasses(@RequestHeader("file-path") String filePath) {
		CiCppServicesImpl ciC = new CiCppServicesImpl(filePath);
		return ciC.getClassMapping();
	}
	/*
	 * This service is to get a brief idea on the classes that are in the file
	 */

	@GetMapping(path = "/get-ci/cpp/brief")
	public void getBriefDescriptionAsLog(@RequestHeader("file-path") String filePath) {
		CiCppServicesImpl ciC = new CiCppServicesImpl(filePath);
		ciC.identifyClassStructure();
	}

	/*
	 * -----------------------------------------------------------------------------
	 */

	// --------------------------------------- CNC End Points
	// ---------------------------------------------------------------
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(path = "/get-cnc/cnc-score")
	public ResponseEntity</*Integer*/?> getCNCScore(@RequestHeader("file-path") String filePath) {
		ICNCService cncService = new CNCServiceImpl(filePath);
		return (new ResponseEntity</*Integer*/>(cncService.getScore(), HttpStatus.OK));
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(path = "/get-cnc/nested-if")
	public ResponseEntity<?> getCNCNestedIfScore(@RequestHeader("file-path") String filePath) {
		ICNCService cncService = new CNCServiceImpl(filePath);
		return (new ResponseEntity<>(cncService.getNestedIfControlScore(), HttpStatus.OK));
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(path = "/get-cnc/nested-for")
	public ResponseEntity<?> getCNCNestedForScore(@RequestHeader("file-path") String filePath) {
		ICNCService cncService = new CNCServiceImpl(filePath);
		return (new ResponseEntity<>(cncService.getNestedForScore(), HttpStatus.OK));
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(path = "/get-cnc/nested-While")
	public ResponseEntity</*Integer*/?> getCNCNestedWhileScore(@RequestHeader("file-path") String filePath) {
		ICNCService cncService = new CNCServiceImpl(filePath);
		return (new ResponseEntity</*Integer*/>(cncService.getNestedWhileScore(), HttpStatus.OK));
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(path = "/get-cnc/nested-do-While")
	public ResponseEntity</*Integer*/?> getCNCNestedDoWhileScore(@RequestHeader("file-path") String filePath) {
		ICNCService cncService = new CNCServiceImpl(filePath);
		return (new ResponseEntity</*Integer*/>(cncService.getNestedDoWhileScore(), HttpStatus.OK));
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(path = "/get-cr/recursive-java")
	public ResponseEntity<?> getCRScoreJava(@RequestHeader("file-path") String filePath) {
		CrServicesImpl crService = new CrServicesImpl(filePath);
		return (new ResponseEntity<>(crService.getControlScore(), HttpStatus.OK));
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(path = "/get-cr/recursive-cpp")
	public ResponseEntity<?> getCRScoreCpp(@RequestHeader("file-path") String filePath) {
		CrServicesImpl crService = new CrServicesImpl(filePath);
		return (new ResponseEntity<>(crService.getControlScoreInCpp(), HttpStatus.OK));
	}
	/*
	 * -----------------------------------------------------------------------------
	 * 
	 * -----------------------------------------------------------------------------
	 * 
	 */

	// --------------------------------------- Cs End Points
	// ---------------------------------------------------------------
	@GetMapping(path = "/get-cs")
	public ResponseEntity<int[]> getCsScore(@RequestHeader("file-path") String filePath) {
		CsServicesImpl cs = new CsServicesImpl();
		GeneralServiceImpl gs = new GeneralServiceImpl();
		String sourceCode = gs.getSourceCode(filePath);
		int[] csValueArray = cs.getAllCsValues(sourceCode);
		return (new ResponseEntity<int[]>(csValueArray, HttpStatus.OK));
	}

}

class ResponseClass {
	private String totalCtcCount;
	private List<String> errorList;
	private List<String> lineScore;
	private List<String> code;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private String errorMessage;

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	private String statusCode;

	public List<String> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<String> errorList) {
		this.errorList = errorList;
	}

	public String getTotalCtcCount() {
		return totalCtcCount;
	}

	public void setTotalCtcCount(String totalCtcCount) {
		this.totalCtcCount = totalCtcCount;
	}

	public List<String> getCode() {
		return code;
	}

	public void setCode(List<String> code) {
		this.code = code;
	}

	public List<String> getLineScore() {
		return lineScore;
	}

	public void setLineScore(List<String> lineScore) {
		this.lineScore = lineScore;
	}
}