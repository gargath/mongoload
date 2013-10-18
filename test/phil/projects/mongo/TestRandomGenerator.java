/**
 * 
 */
package phil.projects.mongo;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phil.projects.mongo.RandomGenerator.UniqueStringSaturationException;

/**
 * @author ptaprogg
 *
 */
public class TestRandomGenerator {

	private final String allowedCharacters="abcdefghijklmnopqrstuvwxyz";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link phil.projects.mongo.RandomGenerator#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		RandomGenerator g1 = RandomGenerator.getInstance();
		RandomGenerator g2 = RandomGenerator.getInstance();
		assertTrue("Two instances returned by getInstance() are not equal", g1 == g2);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetTooLongRandomString() {
		RandomGenerator g = RandomGenerator.getInstance();
		@SuppressWarnings("unused")
		String s4 = g.getRandomString(Integer.MAX_VALUE); 
	}
	
	/**
	 * Test method for {@link phil.projects.mongo.RandomGenerator#getRandomString(int)}.
	 */
	@Test
	public void testGetRandomString() {
		RandomGenerator g = RandomGenerator.getInstance();
		String[] testStrings = new String[4];
		testStrings[0] = g.getRandomString(5);
		testStrings[1] = g.getRandomString(10);
		testStrings[2] = g.getRandomString(50);
		testStrings[3] = g.getRandomString(100);

		assertTrue("String length not as expected", testStrings[0].length() == 5);
		assertTrue("String length not as expected", testStrings[1].length() == 10);
		assertTrue("String length not as expected", testStrings[2].length() == 50);
		assertTrue("String length not as expected", testStrings[3].length() == 100);
		for (int idx = 0; idx < testStrings.length; idx++) {
			for (int i = 0; i < testStrings[idx].length(); i++) {
				assertTrue("String contains illegal character", allowedCharacters.contains(testStrings[idx].substring(i, i+1)));
			}
		}
	}

	/**
	 * Test method for {@link phil.projects.mongo.RandomGenerator#getRandomUniqueString(int)}.
	 */
	@Test
	public void testGetRandomUniqueString() {
		
		RandomGenerator g = RandomGenerator.getInstance();
		HashSet<String> strings = new HashSet<String>();
		
		try {
			for (int i = 0; i < 17576; i++) {
				assertTrue("Generator returned non-unique string", strings.add(g.getRandomUniqueString(3)));
			}
		}
		catch (UniqueStringSaturationException usse) {
			return;
		}
		fail("Exception not thrown");		
	}

	/**
	 * Test method for {@link phil.projects.mongo.RandomGenerator#getRandomIntInRange(int)}.
	 */
	@Test
	public void testGetRandomIntInRange() {
		RandomGenerator g = RandomGenerator.getInstance();
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			int randomNumber = g.getRandomIntInRange(50);
			assertTrue("Random number outside of specified range",(randomNumber>=0) && (randomNumber < 50));
			if (i%100000000 == 0) System.out.print(".");
			if (i%500000000 == 0) System.out.print(" ");
		}
		System.out.println();
	}

}
