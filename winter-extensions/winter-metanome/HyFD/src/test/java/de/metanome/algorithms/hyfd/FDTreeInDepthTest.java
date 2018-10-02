package de.metanome.algorithms.hyfd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.util.OpenBitSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.metanome.algorithms.hyfd.structures.FDTree;

public class FDTreeInDepthTest {
	
	private FDTree fdtree;
	
	@Before
	public void setUp() throws Exception {
		this.fdtree = new FDTree(5, -1);
		
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		lhs.set(3);
		this.fdtree.addFunctionalDependency(lhs, 2);
		
		lhs = new OpenBitSet();
		lhs.set(0);
		this.fdtree.addFunctionalDependency(lhs, 2);

		lhs = new OpenBitSet();
		lhs.set(1);
		this.fdtree.addFunctionalDependency(lhs, 2);
		
		lhs = new OpenBitSet();
		lhs.set(1);
		lhs.set(4);
		this.fdtree.addFunctionalDependency(lhs, 2);

		lhs = new OpenBitSet();
		lhs.set(4);
		this.fdtree.addFunctionalDependency(lhs, 2);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testContainsGeneralization() {
		this.fdtree.filterGeneralizations();
		
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		lhs.set(3);
		assertTrue(this.fdtree.containsFd(lhs, 2));
		
		lhs = new OpenBitSet();
		lhs.set(0);
		assertFalse(this.fdtree.containsFd(lhs, 2));
		
		lhs = new OpenBitSet();
		lhs.set(1);
		assertFalse(this.fdtree.containsFd(lhs, 2));
		
		lhs = new OpenBitSet();
		lhs.set(1);
		lhs.set(4);
		assertTrue(this.fdtree.containsFd(lhs, 2));
		
		lhs = new OpenBitSet();
		lhs.set(4);
		assertFalse(this.fdtree.containsFd(lhs, 2));
	}
	
	@Test
	public void testContainsGeneralizationAnFilterDeadEnds() {
		this.fdtree.filterGeneralizations();
		this.fdtree.filterDeadElements();
		
		OpenBitSet lhs = new OpenBitSet();
		lhs.set(0);
		lhs.set(1);
		lhs.set(3);
		assertTrue(this.fdtree.containsFd(lhs, 2));
		
		lhs = new OpenBitSet();
		lhs.set(0);
		assertFalse(this.fdtree.containsFd(lhs, 2));
		
		lhs = new OpenBitSet();
		lhs.set(1);
		assertFalse(this.fdtree.containsFd(lhs, 2));
		
		lhs = new OpenBitSet();
		lhs.set(1);
		lhs.set(4);
		assertTrue(this.fdtree.containsFd(lhs, 2));
		
		lhs = new OpenBitSet();
		lhs.set(4);
		assertFalse(this.fdtree.containsFd(lhs, 2));
	}
	
}
