
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

public class Ship extends Sprite {

	// defining the shape of the ship
	final double[] origFlameXPts = { -6, -23, -6 },
			origFlameYPts = { -3, 0, 3 };
	// ship coordinate

	final int radius = 12; // radius of circle used to approximate the ship

	double x, y, angle, xVelocity, yVelocity, acceleration, velocityDecay,
			rotationalSpeed; // variable used in movement

	boolean turningLeft, turningRight, accelerating, active;
	int[] flameXPts, flameYPts; // store the current location

	int shotDelay, shotDelayLeft; // used to determine the rate of firing

	public Ship(double x, double y, double angle, double acceleration,
			double velocityDecay, double rotationalSpeed, int shotDelay) {

		this.x = x;
		this.y = y;
		this.angle = angle;
		this.acceleration = acceleration;
		this.velocityDecay = velocityDecay;
		this.rotationalSpeed = rotationalSpeed;
		xVelocity = 0; // not moving
		yVelocity = 0;
		turningLeft = false; // not turning
		turningRight = false;
		active = false; // start off paused

		flameXPts = new int[3];
		flameYPts = new int[3];
		this.shotDelay = shotDelay; // # of frames between shots
		shotDelayLeft = 0; // ready to shoot
	} // ship constructor

	// getters and setters

	public void setAccelerating(boolean accelerating) {
		this.accelerating = accelerating; // start or stop accelerating the ship

	}

	public void setTurningLeft(boolean turningLeft) {
		this.turningLeft = turningLeft; // start or stop turning the ship
	}

	public void setTurningRight(boolean turningRight) {
		this.turningRight = turningRight;
	}

	public double getX() {
		return x; // returns the ship’s x location
	}

	public double getY() {
		return y;
	}

	public double getRadius() {
		return radius; // returns radius of circle that approximates the ship
	}

	public void setActive(boolean active) {
		this.active = active; // used when the game is paused or unpaused

	}

	public boolean isActive() {
		return active;
	}

	public boolean canShoot() {
		if (shotDelayLeft > 0) // checks to see if the ship is ready to
			return false; // shoot again yet or if it needs to wait longer
		else
			return true;
	}

	// end of getters and setters

	public void draw(Graphics g, Image shipImage) {
		// rotate the points and translate them to the ship's location by adding
		// x and y and then round them by adding .5 and casting them as integer
		if (accelerating && active) { // draw flame if accelerating

			for (int i = 0; i < 3; i++) {
				flameXPts[i] = (int) (origFlameXPts[i] * Math.cos(angle)
						- origFlameYPts[i] * Math.sin(angle) + x + 13);
				flameYPts[i] = (int) (origFlameXPts[i] * Math.sin(angle)
						+ origFlameYPts[i] * Math.cos(angle) + y + 13);
			}
			g.setColor(Color.red); // set color of flame
			g.fillPolygon(flameXPts, flameYPts, 3); // 3 is # of points
		} // flame if

		// calculate the polygon for the ship, then draw it
		/*
		 * for(int i=0;i<4;i++){ xPts[i]=(int)(origXPts[i]*Math.cos(angle)-
		 * //rotate origYPts[i]*Math.sin(angle)+ x+.5); //translate and round
		 * yPts[i]=(int)(origXPts[i]*Math.sin(angle)+ //rotate
		 * origYPts[i]*Math.cos(angle)+ y+.5); //translate and round }
		 */

		Graphics2D g2d = (Graphics2D) g;
		int spriteHeight = shipImage.getHeight(null);
		int spriteWidth = shipImage.getWidth(null);

		AffineTransform affineTransform = new AffineTransform();
		affineTransform
				.rotate(angle, x + spriteWidth / 2, y + spriteHeight / 2);
		affineTransform.translate(x, y);

		g2d.drawImage(shipImage, affineTransform, null);
		// g.drawImage(shipImage, (int)x, (int)y,null);

		/*
		 * AffineTransform tx = new AffineTransform(); double scalex = 1; double
		 * scaley = 1; tx.scale(scalex, scaley);
		 * 
		 * double dx = Math.sin(angle); double dy = -Math.cos(angle); x+=dx;
		 * y+=dy; tx.translate(x, y); tx.rotate(angle,16,16);
		 * g.drawImage(shipImage, tx, null);
		 */

		// if(active) // active means game is running (not paused)
		// g.setColor(Color.white);
		// else // draw the ship dark gray if the game is paused
		// g.setColor(Color.darkGray);

		// g.drawImage(shipImage, (int)xPts, (int)yPts,null);
		// g.fillPolygon(xPts,yPts,4); // 4 is the number of points

	} // draw

	public void move(int scrnWidth, int scrnHeight) {

		if (shotDelayLeft > 0) // move() is called every frame that the game
			shotDelayLeft--; // is run, so this ticks down the shot delay
		if (turningLeft) // this is backwards from typical polar coordinates
			angle -= rotationalSpeed; // because positive y is downward.
		if (turningRight) // Because of that, adding to the angle is
			angle += rotationalSpeed; // rotating clockwise (to the right)
		if (angle > (2 * Math.PI)) // Keep angle within bounds of 0 to 2*PI
			angle -= (2 * Math.PI);
		else if (angle < 0)
			angle += (2 * Math.PI);
		if (accelerating) { // adds accel to velocity in direction pointed
		// calculates components of accel and adds them to velocity

			xVelocity += acceleration * Math.cos(angle);
			yVelocity += acceleration * Math.sin(angle);
		}
		x += xVelocity; // move the ship by adding velocity to position
		y += yVelocity;
		xVelocity *= velocityDecay; // slows ship down by percentages (velDecay
		yVelocity *= velocityDecay; // should be a decimal between 0 and 1
		if (x < 0) // wrap the ship around to the opposite side of the screen
			x += scrnWidth; // when it goes out of the screen's bounds
		else if (x > scrnWidth)
			x -= scrnWidth;
		if (y < 0)
			y += scrnHeight;
		else if (y > scrnHeight)
			y -= scrnHeight;
	} // move

	public Shot shoot() {
		shotDelayLeft = shotDelay; // set delay till next shot can be fired
		// a life of 40 makes the shot travel about the width of the
		// screen before disappearing
		return new Shot(x, y, angle, xVelocity, yVelocity, 40);
	}

} // ship

