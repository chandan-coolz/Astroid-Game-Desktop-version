

import java.awt.Color;
import java.awt.Graphics;

public class Shot extends Sprite {

	final double shotSpeed = 12; // the speed at which the shots move, in
	// pixels per frame
	double x, y, xVelocity, yVelocity; // variables for movement
	int lifeLeft; // causes the shot to eventually disappear if it doesn’t

	// hit anything

	// constructor
	public Shot(double x, double y, double angle, double shipXVel,
			double shipYVel, int lifeLeft) {
		this.x = x;
		this.y = y;
		// add the velocity of the ship to the shot velocity
		// (so the shot's velocity is relative to the ship's velocity)
		xVelocity = shotSpeed * Math.cos(angle) + shipXVel;
		yVelocity = shotSpeed * Math.sin(angle) + shipYVel;
		// the number of frames the shot will last for before
		// disappearing if it doesn't hit anything
		this.lifeLeft = lifeLeft;
	}

	// gegtter and setters
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public int getLifeLeft() {
		return lifeLeft;
	}

	// getter and setters

	public void draw(Graphics g) {
		g.setColor(Color.yellow); // set shot color
		// draw circle of radius 3 centered at the closest point
		// with integer coordinates (.5 added to x-1 and y-1 for rounding)
		g.fillOval((int) (x + 10), (int) (y +10), 3, 3);
	} // draw

	public void move(int scrnWidth, int scrnHeight) {
		lifeLeft--; // used to make shot disappear if it goes too long
		// without hitting anything
		x += xVelocity; // move the shot
		y += yVelocity;
		if (x < 0) // wrap the shot around to the opposite side of the
			x += scrnWidth; // screen if needed
		else if (x > scrnWidth)
			x -= scrnWidth;
		if (y < 0)
			y += scrnHeight;
		else if (y > scrnHeight)
			y -= scrnHeight;
	} // move

} // shot
