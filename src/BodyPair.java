

public class BodyPair {
	
	private final DiscBody bodyA, bodyB;
	private final int hashCode;
	
	public BodyPair(DiscBody a, DiscBody b) {
		bodyA = a;
		bodyB = b;
		hashCode = a.hashCode() * b.hashCode()
				   + a.hashCode() + b.hashCode();
	}
	
	public boolean equals(BodyPair other) {
		return (bodyA == other.bodyA && bodyB == other.bodyB)
			   || (bodyB == other.bodyA && bodyA == other.bodyB);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BodyPair)
			return equals((BodyPair) o);
		return false;
	}
		
	public DiscBody getBodyA() {
		return bodyA;
	}
	
	public DiscBody getBodyB() {
		return bodyB;
	}
	
}
