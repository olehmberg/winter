package de.metanome.algorithms.hyfd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.util.OpenBitSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.metanome.algorithms.hyfd.structures.FDTree;

public class FDTreeTest {
	
	private FDTree fdtree;
	
	@Before
	public void setUp() throws Exception {
		this.fdtree = new FDTree(5, -1);
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		lhs.set(3);
		this.fdtree.addFunctionalDependency(lhs, 2);
	}

	@After
	public void tearDown() throws Exception {
	}
	
/*	@Test
	public void testContainsSpecialization() {
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		assertTrue(this.fdtree.containsFdOrSpecialization(lhs, 2));
	}
*/	
	@Test
	public void testContainsGeneralization() {
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		assertFalse(this.fdtree.containsFdOrGeneralization(lhs, 2));
		lhs.set(3);
		lhs.set(4);
		assertTrue(this.fdtree.containsFdOrGeneralization(lhs, 2));
	}
	
/*	@Test
	public void testGetSpecialization() {
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		OpenBitSet specLhs = new OpenBitSet();
		assertTrue(this.fdtree.getSpecialization(lhs, 2, 0, specLhs));
		OpenBitSet expResult = new OpenBitSet();
		
		expResult.set(0);
		expResult.set(1);
		expResult.set(3);
		assertEquals(expResult, specLhs);
		
	}
*/	
	@Test 
	public void testGetGeneralizationAndDelete() {
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		lhs.set(3);
		lhs.set(4);
		OpenBitSet specLhs = this.fdtree.getFdOrGeneralization(lhs, 2);
		
		OpenBitSet expResult = new OpenBitSet();
		
		expResult.set(0);
		expResult.set(1);
		expResult.set(3);
		assertEquals(expResult, specLhs);
	}
	
/*	@Test
	public void testFilterSpecialization() {
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(3);
		this.fdtree.addFunctionalDependency(lhs, 2);
		
		this.fdtree.filterSpecializations();
		
		OpenBitSet expResult = new OpenBitSet();
		expResult.set(0);
		expResult.set(1);
		expResult.set(3);
		assertFalse(this.fdtree.containsFdOrGeneralization(lhs, 2));
	}
*/	
	@Test
	public void testDeleteGeneralizations() {
		fdtree = new FDTree(4, -1);
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		
		this.fdtree.addFunctionalDependency(lhs, 3);
		lhs.clear(1);
		lhs.set(2);
		this.fdtree.addFunctionalDependency(lhs, 3);
		
		//lhs.set(1);
		//this.fdtree.deleteGeneralizations(lhs, 3, 0);
		//assertTrue(this.fdtree.isEmpty());
	}
	
/*	@Test 
	public void testContainsSpezialization() {
		FDTree fdtree = new FDTree(5);
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(2);
		lhs.set(4);
		fdtree.addFunctionalDependency(lhs, 3);
		lhs.clear(0);
		lhs.set(1);
		fdtree.addFunctionalDependency(lhs, 3);
		
		lhs.clear(2);
		boolean result = fdtree.containsFdOrSpecialization(lhs, 3);
		assertTrue(result);
	}
*/
}
