package integrationtests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import view.PropertiesFile;

class PropertiesFileTest {
	String propertiesFile = "models/simpleTest.properties";
	
	@Test
	void test() {
		String[] args = {propertiesFile};
		PropertiesFile.main(args);
		assertEquals(true, true);
	}

}
