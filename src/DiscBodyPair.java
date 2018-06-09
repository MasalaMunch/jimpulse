

public class DiscBodyPair {
	
	private final int minBodyIndex, maxBodyIndex;
	
	public DiscBodyPair(int minBodyIndex, int maxBodyIndex) {
		this.minBodyIndex = minBodyIndex;
		this.maxBodyIndex = maxBodyIndex;
	}

	public int getMinBodyIndex() {
		return minBodyIndex;
	}

	public int getMaxBodyIndex() {
		return maxBodyIndex;
	}
	
	public boolean equals(DiscBodyPair other) {
		return (minBodyIndex == other.minBodyIndex
				&& maxBodyIndex == other.maxBodyIndex);
	}
	
	@Override
	public int hashCode() {
		return (minBodyIndex * maxBodyIndex 
				+ minBodyIndex + maxBodyIndex);
	}

}
