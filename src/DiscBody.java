
public class DiscBody {
	
	private double mass, massInverse;
	private double radius;
	private double posX, posY;
	private double velX, velY;
	private double forceX, forceY;
	private double accelX, accelY;
	private double sapParaMinBound, sapParaMaxBound;
	private double sapPerpMinBound, sapPerpMaxBound;
	
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

	public double getSapParaMinBound() {
		return sapParaMinBound;
	}

	public double getSapParaMaxBound() {
		return sapParaMaxBound;
	}

	public double getSapPerpMinBound() {
		return sapPerpMinBound;
	}

	public double getSapPerpMaxBound() {
		return sapPerpMaxBound;
	}

	public DiscBody(double posX, double posY) {
		setMass(1);
		setRadius(40);
		setPosX(posX);
		setPosY(posY);
		setVelX(0);
		setVelY(0);
		setForceX(0);
		setForceY(0);
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

	public void updateSapBounds(double timestep, double sapAxisX, double sapAxisY) {
		
		final double sapParaPos = posX*sapAxisX + posY*sapAxisY;
		final double sapParaPosChange = timestep*(velX*sapAxisX + velY*sapAxisY);
		
		if (sapParaPosChange > 0) {
			sapParaMinBound = sapParaPos - radius;
			sapParaMaxBound = sapParaPos + radius + sapParaPosChange;
		}
		else { // sapParaPosChange <= 0
			sapParaMinBound = sapParaPos - radius + sapParaPosChange;
			sapParaMaxBound = sapParaPos + radius;
		}
		
		final double sapPerpPos = posX*sapAxisY - posY*sapAxisX;
		final double sapPerpPosChange = timestep*(velX*sapAxisY - velY*sapAxisX);
		
		if (sapPerpPosChange > 0) {
			sapPerpMinBound = sapPerpPos - radius;
			sapPerpMaxBound = sapPerpPos + radius + sapPerpPosChange;
		}
		else { // sapPerpPosChange <= 0
			sapPerpMinBound = sapPerpPos - radius + sapPerpPosChange;
			sapPerpMaxBound = sapPerpPos + radius;
		}
		
	}

}
