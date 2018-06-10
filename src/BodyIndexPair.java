import java.util.Arrays;

public class BodyIndexPair {
	
	private final int minIndex, maxIndex;
	private final int hashCode;
	
	public BodyIndexPair(int i, int j) {
		boolean order = i < j;
		minIndex = order? i:j;
		maxIndex = order? j:i;
		hashCode = i*j + i + j;
	}
	
	public int getMinIndex() {
		return minIndex;
	}
	
	public int getMaxIndex() {
		return maxIndex;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	public boolean equals(BodyIndexPair other) {
		return (minIndex == other.minIndex && maxIndex == other.maxIndex);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BodyIndexPair)
			return equals((BodyIndexPair) o);
		else
			return false;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(new int[]{minIndex, maxIndex});
	}
	
}
