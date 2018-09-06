package de.metanome.algorithms.hyfd.structures;

public class ClusterIdentifier {

	private final int[] cluster;
	
	public ClusterIdentifier(final int[] cluster) {
		this.cluster = cluster;
	}
	
	public void set(int index, int clusterId) {
		this.cluster[index] = clusterId;
	}
	
	public int get(int index) {
		return this.cluster[index];
	}
	
	public int[] getCluster() {
		return this.cluster;
	}
	
	public int size() {
		return this.cluster.length;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		int index = this.size();
		while (index-- != 0)
			hash = 31 * hash + this.get(index);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ClusterIdentifier))
			return false;
		final ClusterIdentifier other = (ClusterIdentifier) obj;
		
		int index = this.size();
		if (index != other.size())
			return false;
		
		final int[] cluster1 = this.getCluster();
		final int[] cluster2 = other.getCluster();
		
		while (index-- != 0)
			if (cluster1[index] != cluster2[index])
				return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i = 0; i < this.size(); i++) {
			builder.append(this.cluster[i]);
			if (i + 1 < this.size())
				builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
	}
}
