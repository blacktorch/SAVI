package savi.unitTesting;

import java.util.ArrayList;
import java.util.List;

import jason.asSyntax.Literal;
import savi.jason_processing.Perception;

public class PerceptionUnitTest {
	/**
	 * Perform unit testing on the Perception class.
	 * @return
	 */
	public static boolean unitTest(boolean verbose) {
		boolean testOK = true;
		
		// Make a new perception with null type
		List<Double> perceptParameters = new ArrayList<Double>();
		perceptParameters.add(1.0);
		perceptParameters.add(2.0);
		String perceptName = new String("test");
		String perceptType = null;
		long versionID = 8;
		Perception perceptTest = new Perception(perceptName, perceptType, versionID, perceptParameters);
		
		// Check the parameters of the new Perception
		boolean testResult = perceptTest.getPerceptionName().equals(perceptName);
		UnitTester.reportResult("Percept class - Name test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = (perceptTest.getPerceptionType() == null);
		UnitTester.reportResult("Percept class - Type null test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = (perceptTest.getVersionID() == versionID);
		UnitTester.reportResult("Percept class - VersionID test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = perceptTest.getParameters().equals(perceptParameters);
		UnitTester.reportResult("Percept class - Parameter test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = perceptTest.getParameters().equals(perceptParameters);
		UnitTester.reportResult("Percept class - Parameter test", testResult, verbose);
		testOK = testOK && testResult;

		// Make new version of the perception, with a type
		perceptType = new String("someType");
		Perception perceptTestType = new Perception(perceptName, perceptType, versionID, perceptParameters);
		testResult = perceptTestType.getPerceptionType().equals(perceptType);
		UnitTester.reportResult("Percept class - Type (not null) test", testResult, verbose);
		testOK = testOK && testResult;
		
		// Test copy constructor without the type specified
		Perception perceptTestCopy = new Perception(perceptTest);
		
		// Check the parameters of the new copied Perception
		testResult = perceptTest.getPerceptionName().equals(perceptTestCopy.getPerceptionName());
		UnitTester.reportResult("Percept class - Name after copy test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = (perceptTestCopy.getPerceptionType() == null);
		UnitTester.reportResult("Percept class - Type null after copy test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = (perceptTest.getVersionID() == perceptTestCopy.getVersionID());
		UnitTester.reportResult("Percept class - VersionID after copy test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = perceptTest.getParameters().equals(perceptTestCopy.getParameters());
		UnitTester.reportResult("Percept class - Parameter after copy test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = perceptTest.equals(perceptTestCopy);
		UnitTester.reportResult("Percept class - Equals after copy with null test", testResult, verbose);
		testOK = testOK && testResult;
		
				
		// Test copy constructor with type specified
		Perception perceptTestCopyType = new Perception(perceptTestType);
		
		// Check the parameters of the new copied Perception
		testResult = (perceptTestType.getPerceptionName().equals(perceptTestCopyType.getPerceptionName()));
		UnitTester.reportResult("Percept class - Name after copy with type test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = (perceptTestType.getPerceptionType().equals(perceptTestCopyType.getPerceptionType()));
		UnitTester.reportResult("Percept class - Type null after copy with type test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = (perceptTestType.getVersionID() == perceptTestCopyType.getVersionID());
		UnitTester.reportResult("Percept class - VersionID after copy with type test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = perceptTestType.getParameters().equals(perceptTestCopyType.getParameters());
		UnitTester.reportResult("Percept class - Parameter after copy with type test", testResult, verbose);
		testOK = testOK && testResult;
		
		testResult = perceptTestType.equals(perceptTestCopyType);
		UnitTester.reportResult("Percept class - Equals after copy with type test", testResult, verbose);
		testOK = testOK && testResult;
				
		// Test compare name
		Perception perceptTestAnotherName = new Perception(perceptName + "different", perceptType, versionID, perceptParameters);
		testResult = (perceptTest.comparePerceptionName(perceptTest) && perceptTest.comparePerceptionName(perceptTestCopy) && !perceptTest.comparePerceptionName(perceptTestAnotherName));
		UnitTester.reportResult("Percept class - Perception name comparison test", testResult, verbose);
		testOK = testOK && testResult;
		
		// Test compare type - null case
		testResult = ((perceptTest.getPerceptionType() == null) && perceptTest.comparePerceptionType(perceptTest) && perceptTest.comparePerceptionType(perceptTestCopy) && !perceptTest.comparePerceptionType(perceptTestAnotherName));
		UnitTester.reportResult("Percept class - Perception type comparison (null case) test", testResult, verbose);
		testOK = testOK && testResult;
		
		// Test compare type - not null case
		Perception perceptTestTypeAnotherType = new Perception(perceptName, perceptType + "different", versionID, perceptParameters);
		testResult = (perceptTestType.comparePerceptionType(perceptTestType) && perceptTestType.comparePerceptionType(perceptTestCopyType) && !perceptTest.comparePerceptionType(perceptTestTypeAnotherType));
		UnitTester.reportResult("Percept class - Perception type comparison (not null) test", testResult, verbose);
		testOK = testOK && testResult;
		
		// Test perceptLost()
		Perception perceptLostTest = new Perception(perceptTest);
		perceptLostTest.perceptionLost();
		testResult = perceptLostTest.getPerceptionName().equals(perceptTest.getPerceptionName() + "lost");
		UnitTester.reportResult("Percept class - Perception lost test", testResult, verbose);
		testOK = testOK && testResult;

		// Get Literal test
		testResult = (perceptLostTest.getLiteral().equals(Literal.parseLiteral(new String("testlost(1.0,2.0)"))) && perceptTest.getLiteral().equals(Literal.parseLiteral(new String("test(1.0,2.0)"))));
		UnitTester.reportResult("Percept class - Perception lost test", testResult, verbose);
		testOK = testOK && testResult;
		
		// Test difference and similarity (similarity uses difference calculation)
		boolean differenceZeroWithSelf = (perceptTest.getDifference(perceptTest) == 0.0);
		boolean differenceOneFromLost = (perceptTest.getDifference(perceptLostTest) == 1.0);
		boolean differenceOneType = (perceptTest.getDifference(perceptTestTypeAnotherType) == 1.0);
		boolean similarWithSelf = perceptTest.isSimilar(perceptTest);
		boolean differentFromLost = !perceptTest.isSimilar(perceptLostTest);
		boolean differentType = !perceptTest.isSimilar(perceptTestTypeAnotherType);
				
		List<Double> similarList = new ArrayList<Double>();
		similarList.add(1.01);
		similarList.add(2.0);
		boolean slightlySimilar = perceptTest.isSimilar(new Perception(perceptTest.getPerceptionName(), perceptTest.getPerceptionType(), 0, similarList));
		
		List<Double> differentList = new ArrayList<Double>();
		differentList.add(2.0);
		differentList.add(2.0);
		boolean notSimilar = !perceptTest.isSimilar(new Perception(perceptTest.getPerceptionName(), perceptTest.getPerceptionType(), 0, differentList));

		testResult = (differenceZeroWithSelf && differenceOneFromLost && differenceOneType && similarWithSelf && differentFromLost && differentType && slightlySimilar && notSimilar);
		UnitTester.reportResult("Percept class - Similarity test", testResult, verbose);
		testOK = testOK && testResult;
		
		// Return the final result
		return testOK;
	}
}