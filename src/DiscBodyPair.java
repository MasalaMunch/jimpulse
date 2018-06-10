import java.util.Arrays;

public class DiscBodyPair {
	
	private final int bodyIndexA, bodyIndexB;
	private final int hashCode;
	
	public DiscBodyPair(int bodyIndexA, int bodyIndexB) {
		this.bodyIndexA = bodyIndexA;
		this.bodyIndexB = bodyIndexB;
		this.hashCode = hashCode(bodyIndexA, bodyIndexB);
	}

	public int getBodyIndexA() {
		return bodyIndexA;
	}

	public int getBodyIndexB() {
		return bodyIndexB;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DiscBodyPair)
			return hashCode == o.hashCode();
		else
			return false;
	}
	
	public static int hashCode(int bodyIndexA, int bodyIndexB) {
		return bodyIndexA * bodyIndexB + bodyIndexA + bodyIndexB;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(new int[]{bodyIndexA,bodyIndexB});
	}
	
}
