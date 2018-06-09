import java.util.Arrays;

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
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DiscBodyPair)
			return equals((DiscBodyPair) o);
		else
			return false;
	}
	
	private boolean equals(DiscBodyPair other) {
		return (minBodyIndex == other.minBodyIndex
				&& maxBodyIndex == other.maxBodyIndex);
	}
	
	@Override
	public int hashCode() {
		return (minBodyIndex * maxBodyIndex 
				+ minBodyIndex + maxBodyIndex);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(new int[]{minBodyIndex, maxBodyIndex});
	}
	
}
