
public class DiscBodyPair {
	
	private DiscBody bodyA, bodyB;
	
	public DiscBody getBodyA() {
		return bodyA;
	}

	public DiscBody getBodyB() {
		return bodyB;
	}

	public DiscBodyPair(DiscBody a, DiscBody b) {
		bodyA = a;
		bodyB = b;
	}
	
	public PointPair[] getPointPairs() {
		return new PointPair[] {}; //TODO
	}
	
}
