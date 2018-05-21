
public class DiscBody {
	
	protected double mass, massInverse;
	protected boolean hasInfiniteMass;
	protected double radius;
	protected double posX, posY;
	protected double velX,  velY;
	protected double forceX, forceY;
	protected double accelX, accelY;
	
	// GETTERS
	
	public double getMass() {
		return mass;
	}
	
	public double getMassInverse() {
		return massInverse;
	}
	
	public boolean hasInfiniteMass() {
		return hasInfiniteMass;
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

	// CONSTRUCTOR AND SETTERS

	public DiscBody(double posX, double posY) {
		setMass(1);
		setRadius(40);
		setPosX(posX);
		setPosY(posY);
		setVelX(0);
		setVelY(0);
		setForceX(0);
		setForceY(0);
		if (hasInfiniteMass) {
			setAccelX(0);
			setAccelY(0);			
		}
	}
	
	public void setMass(double mass) {
		this.mass = mass;
		massInverse = 1/mass;
		hasInfiniteMass = (mass == Double.POSITIVE_INFINITY);
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
		if (!hasInfiniteMass)
			setAccelX(forceX*massInverse);
	}

	public void setForceY(double forceY) {
		this.forceY = forceY;
		if (!hasInfiniteMass)
			setAccelY(forceY*massInverse);
	}

	public void setAccelX(double accelX) {
		this.accelX = accelX;
	}

	public void setAccelY(double accelY) {
		this.accelY = accelY;
	}

	// OTHER METHODS
	
	public void advancePos(double timestep) {
		posX += velX*timestep;
		posY += velY*timestep;
	}
	
	public void advanceVel(double timestep) {
		velX += accelX*timestep;
		velY += accelY*timestep;
	}
		
	//TODO: wrong (trademark)
	public double getMinPosY(double timestep) {
		
		return (posY - radius + velY*timestep);
	}
	public double getMaxPosY(double timestep) {
		return (posY + radius + velY*timestep);
	}
}
