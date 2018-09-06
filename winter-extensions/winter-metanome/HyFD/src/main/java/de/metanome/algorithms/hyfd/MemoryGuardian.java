package de.metanome.algorithms.hyfd;

import java.lang.management.ManagementFactory;

import de.metanome.algorithms.hyfd.structures.FDList;
import de.metanome.algorithms.hyfd.structures.FDSet;
import de.metanome.algorithms.hyfd.structures.FDTree;

public class MemoryGuardian {
	
	private boolean active;
	private final float maxMemoryUsagePercentage = 0.8f;	// Memory usage in percent from which a lattice level should be dropped
	private final float trimMemoryUsagePercentage = 0.7f;	// If data structures must be trimmed, this is the memory percentage that they are trimmed to (trim to less than max memory usage to avoid oscillating trimming)
	private long memoryCheckFrequency;						// Number of allocation events that cause a memory check
	private long maxMemoryUsage;
	private long trimMemoryUsage;
	private long availableMemory;
	private int allocationEventsSinceLastCheck = 0;

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public MemoryGuardian(boolean active) {
		this.active = active;
		this.availableMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		this.maxMemoryUsage = (long)(this.availableMemory * this.maxMemoryUsagePercentage);
		this.trimMemoryUsage = (long)(this.availableMemory * this.trimMemoryUsagePercentage);
		this.memoryCheckFrequency = (long)Math.max(Math.ceil((float)this.availableMemory / 10000000), 10);
	}
	
	public void memoryChanged(int allocationEvents) {
		this.allocationEventsSinceLastCheck += allocationEvents;
	}

	public boolean memoryExhausted(long memory) {
		long memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		return memoryUsage > memory;
	}
	
	public void match(FDSet negCover, FDTree posCover, FDList newNonFDs) {
		if ((!this.active) || (this.allocationEventsSinceLastCheck < this.memoryCheckFrequency))
			return;
		
		if (this.memoryExhausted(this.maxMemoryUsage)) {
//			Logger.getInstance().writeln("Memory exhausted (" + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() + "/" + this.maxMemoryUsage + ") ");
			Runtime.getRuntime().gc();
//			Logger.getInstance().writeln("GC reduced to " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
			
			while (this.memoryExhausted(this.trimMemoryUsage)) {
				int depth = Math.max(posCover.getDepth(), negCover.getDepth()) - 1;
				if (depth < 1)
					throw new RuntimeException("Insufficient memory to calculate any result!");
				
				System.out.print(" (trim to " + depth + ")");
				posCover.trim(depth);
				negCover.trim(depth);
				if (newNonFDs != null)
					newNonFDs.trim(depth);
				Runtime.getRuntime().gc();
			}
		}
		
		this.allocationEventsSinceLastCheck = 0;
	}
}
