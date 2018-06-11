

public class DiscBody {
	
	private static int nextHashCode = 0;
	
	private double mass, massInverse;
	private double radius;
	private double posX, posY;
	private double velX, velY;
	private double forceX, forceY;
	private double accelX, accelY;
	private final int hashCode;
	
	public DiscBody(double posX, double posY) {
		setMass(1);
		setRadius(40);
		setPosX(posX);
		setPosY(posY);
		setVelX(0);
		setVelY(0);
		setForceX(0);
		setForceY(0);
		hashCode = nextHashCode++;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	public double getMass() {
		return mass;
	}
	
	public double getMassInverse() {
		return massInverse;
	}
		
	public double getRadius() {
		return radius;
	}
	
	public double getPosX() {
		return posX;
	}

	public double getPosY() {
		return posY;
	}

	public double getVelX() {
		return velX;
	}

	public double getVelY() {
		return velY;
	}

	public double getForceX() {
		return forceX;
	}

	public double getForceY() {
		return forceY;
	}

	public double getAccelX() {
		return accelX;
	}

	public double getAccelY() {
		return accelY;
	}

	public void setMass(double mass) {
		this.mass = mass;
		massInverse = 1/mass;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	public void setVelX(double velX) {
		this.velX = velX;
	}

	public void setVelY(double velY) {
		this.velY = velY;
	}

	public void setForceX(double forceX) {
		this.forceX = forceX;
		setAccelX(forceX*massInverse);
	}

	public void setForceY(double forceY) {
		this.forceY = forceY;
		setAccelY(forceY*massInverse);
	}

	public void setAccelX(double accelX) {
		this.accelX = accelX;
	}

	public void setAccelY(double accelY) {
		this.accelY = accelY;
	}

	public void advance(double timestep) {
		posX += velX*timestep;
		posY += velY*timestep;
		velX += accelX*timestep;
		velY += accelY*timestep;
	}
	
	public double getBound(double timestep, double axisX, double axisY, boolean boundType) {
		
		final double pos = axisX*posX + axisY*posY;
		final double posChange = timestep*(axisX*velX + axisY*velY);
		
		if (posChange > 0) {
			if (boundType)
				return pos + radius + posChange;
			else
				return pos - radius;
		}
		else {
			if (boundType)
				return pos + radius;
			else
				return pos - radius + posChange;
		}
		
	}
	
}
