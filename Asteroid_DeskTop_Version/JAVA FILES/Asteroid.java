
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

public class Asteroid extends Sprite {

	double x, y, xVelocity, yVelocity, radius;
	int numSplit;
	int typeAsteroid; // 3:big ;2:small ;1:small
	Image asteroidImage;

	public Asteroid(double x, double y, double radius, double minVelocity,
			double maxVelocity, int typeAsteroid, int numSplit,
			Image asteroidImage) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.typeAsteroid = typeAsteroid; // number of shots left to destroy it
		this.numSplit = numSplit; // number of smaller asteroids it
		// breaks up into when shot
		// calculates a random direction and a random
		// velocity between minVelocity and maxVelocity
		double vel = minVelocity + Math.random() * (maxVelocity - minVelocity), dir = 2
				* Math.PI * Math.random(); // random direction
		xVelocity = vel * Math.cos(dir);
		yVelocity = vel * Math.sin(dir);

		this.asteroidImage = asteroidImage;

	} // maxVelocity
		// getter and setter

	public int getNumSplit() {
		return numSplit;
	}

	// getter and setters

	public int getTypeAsteroid() {
		return typeAsteroid;
	}

	@Override
	public void move(int scrnWidth, int scrnHeight) {
		x += xVelocity; // move the asteroid
		y += yVelocity;
		// wrap around code allowing the asteroid to go off the screen
		// to a distance equal to its radius before entering on the
		// other side. Otherwise, it would go halfway off the sceen,
		// then disappear and reappear halfway on the other side
		// of the screen.
		if (x < 0 - radius)
			x += scrnWidth + 2 * radius;
		else if (x > scrnWidth + radius)
			x -= scrnWidth + 2 * radius;
		if (y < 0 - radius)
			y += scrnHeight + 2 * radius;
		else if (y > scrnHeight + radius)
			y -= scrnHeight + 2 * radius;
	} // move

	
	public void draw(Graphics g) {

		g.setColor(Color.darkGray); // set color for the asteroid

		// draw the asteroid centered at (x,y)
		/*
		 * g.fillOval((int) (x - radius + .5), (int) (y - radius + .5), (int) (2
		 * * radius), (int) (2 * radius));
		 */
		g.drawImage(asteroidImage, (int) (x - radius + .5),
				(int) (y - radius + .5), null);

	}

	public boolean shipCollision(Ship ship) {
		// Use the distance formula to check if the ship is touching this
		// asteroid: Distance^2 = (x1-x2)^2 + (y1-y2)^2 ("^" denotes
		// exponents). If the sum of the radii is greater than the
		// distance between the center of the ship and asteroid, they are
		// touching.
		// if (shipRadius + asteroidRadius)^2 > (x1-x2)^2 + (y1-y2)^2,
		// then they have collided.
		// It does not check for collisions if the ship is not active
		// (the player is waiting to start a new life or the game is paused).
		if (Math.pow(radius + ship.getRadius(), 2) > Math.pow(ship.getX() - x,
				2) + Math.pow(ship.getY() - y, 2)
				&& ship.isActive()) {
			return true;
		}

		return false;
	} // shipCollision

	public boolean shotCollision(Shot shot) {
		// Same idea as shipCollision, but using shotRadius = 0
		if (Math.pow(radius, 2) > Math.pow(shot.getX() - x, 2)
				+ Math.pow(shot.getY() - y, 2))
			return true;
		return false;
	} // shotcollision

	public Asteroid createSplitAsteroid(double minVelocity, double maxVelocity,
			int typeAsteroid, Image asteroidImage) {
		// when this asteroid gets hit by a shot, this method is called
		// numSplit times by AsteroidsGame to create numSplit smaller
		// asteroids. Dividing the radius by sqrt(numSplit) makes the
		// sum of the areas taken up by the smaller asteroids equal to
		// the area of this asteroid. Each smaller asteroid has one
		// less hit left before being completely destroyed.
		return new Asteroid(x, y, radius / Math.sqrt(numSplit), minVelocity,
				maxVelocity, typeAsteroid, numSplit, asteroidImage);
	} // createSplitAsteroid

} // Asteroid
