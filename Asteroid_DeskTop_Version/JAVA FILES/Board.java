import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Board extends JPanel implements Runnable, KeyListener,
		MouseListener {

	// audio

	AudioClip shot, explode, playercollide;
	// Images
	Image shipImage, spacebackground, bigAsteroid, mediumAsteroid,
			smallAsteroid;

	// variable to track life
	int lifeLeft;
	boolean isPlayerAlive;

	// variable to check whether player won or not
	boolean isGameWon;

	// The X-coordinate and Y-coordinate of the last click.
	int xpos;
	int ypos;

	// cordinate to start the game
	int rect1xco, rect1yco, rect1width, rect1height;

	Thread thread;
	long startTime, endTime, framePeriod;
	// to avolid flickering
	Dimension dim;
	BufferedImage bufferedImage;
	Graphics g;

	// crfeating ship class object
	Ship ship;
	boolean paused;
	Shot[] shots; // Variable that stores the new array of Shots
	int numShots; // Stores the number of shots in the array
	boolean shooting; // true if the ship is currently shooting

	Asteroid[] asteroids; // the array of asteroids
	int numAsteroids; // the number of asteroids currently in the array
	double astRadius, minAstVel, maxAstVel; // values used to create asteroids
	int astNumHits, astNumSplit;

	// AudioClip shot, explode, playercollide;

	// image ship

	boolean gameover;

	private final int B_WIDTH = 500;
	private final int B_HEIGHT = 500;

	public Board() {

		initBoard();
	}

	private void loadImage() {

		// to load ship image
		URL imgURL = getClass().getClassLoader().getResource("spaceship.gif");
		ImageIcon backgroundIcon = new ImageIcon(imgURL);
		shipImage = backgroundIcon.getImage();

		// to load background image

		URL imgURL1 = getClass().getClassLoader().getResource(
				"spacebackground.png");
		ImageIcon backgroundIcon1 = new ImageIcon(imgURL1);
		spacebackground = backgroundIcon1.getImage();

		// to load bigAsteroid

		URL imgURL2 = getClass().getClassLoader()
				.getResource("bigAsteroid.gif");
		ImageIcon backgroundIcon2 = new ImageIcon(imgURL2);
		bigAsteroid = backgroundIcon2.getImage();

		// to load medium Asteroid

		URL imgURL3 = getClass().getClassLoader().getResource(
				"mediumAsteroid.gif");
		ImageIcon backgroundIcon3 = new ImageIcon(imgURL3);
		mediumAsteroid = backgroundIcon3.getImage();

		// to load small Asteroid

		URL imgURL4 = getClass().getClassLoader().getResource(
				"smallAsteroid.gif");
		ImageIcon backgroundIcon4 = new ImageIcon(imgURL4);
		smallAsteroid = backgroundIcon4.getImage();

	} // loadImage

	public void loadAudio() {

		URL url = this.getClass().getClassLoader().getResource("shot.au");
		shot = Applet.newAudioClip(url);

		URL url1 = this.getClass().getClassLoader().getResource("explode.au");
		explode = Applet.newAudioClip(url1);

		URL url2 = this.getClass().getClassLoader()
				.getResource("playercollide.au");
		playercollide = Applet.newAudioClip(url2);

	} // loadAudio

	private void initBoard() {

		setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
		setDoubleBuffered(true);
		setFocusable(true);

		loadImage();
		loadAudio();
		// intial life of the player is 3
		lifeLeft = 0;
		isPlayerAlive = false;
		isGameWon = false;
		// Assign values to the rectanagle coordinates.
		rect1xco = 170;
		rect1yco = 240;
		rect1width = 150;
		rect1height = 40;
		// */
		ship = new Ship(250, 250, 0, 0.35, 0.98, 0.1, 12);

		paused = true;

		shots = new Shot[41]; // Allocate the space for the array.
		// We allocate enough space to store the maximum number of
		// shots that can possibly be on the screen at one time.
		// 41 is the max because no more than one shot can be fired per
		// frame and shots only last for 40 frames (40 is the value passed
		// in for lifeLeft when shots are created)
		numShots = 0; // no shots on the screen to start with.
		shooting = false; // the ship is not shooting

		numAsteroids = 0;
		astRadius = 40; // values used to create the asteroids
		minAstVel = .5;
		maxAstVel = 5;
		astNumHits = 3;
		astNumSplit = 2;

		gameover = false;

		addMouseListener(this);
		addKeyListener(this);
		startTime = 0L;
		endTime = 0L;
		framePeriod = 25L;
		dim = Toolkit.getDefaultToolkit().getScreenSize();

		bufferedImage = new BufferedImage(B_WIDTH, B_HEIGHT,
				BufferedImage.TYPE_INT_RGB);

		g = bufferedImage.getGraphics();
		;

	} // load initial content

	@Override
	public void addNotify() {
		super.addNotify();
		thread = new Thread(this);
		thread.start();

	} // addNotify

	public void setUpNextLevel() { // starts a new level with one more asteroid

		isPlayerAlive = true;
		lifeLeft = 3;
		// create a new, inactive ship centered on the screen
		// I like .35 for acceleration, .98 for velocityDecay, and
		// .1 for rotationalSpeed. They give the controls a nice feel.
		ship = new Ship(250, 250, 0, .35, .98, .1, 12);

		numShots = 0; // no shots on the screen at beginning of level

		paused = true;
		shooting = false;

		// create an array large enough to hold the biggest number
		// of asteroids possible on this level (plus one because
		// the split asteroids are created first, then the original
		// one is deleted). The level number is equal to the
		// number of asteroids at it's start.
		asteroids = new Asteroid[4 * (int) Math
				.pow(astNumSplit, astNumHits - 1) + 1];
		numAsteroids = 4;
		// create asteroids in random spots on the screen
		asteroids[0] = new Asteroid(0, 0, astRadius, minAstVel, maxAstVel,
				astNumHits, astNumSplit, bigAsteroid);

		for (int i = 1; i < numAsteroids; i++) {
			Double x, y;
			do {
				x = Math.random() * dim.width;
				y = Math.random() * dim.height;

			} while ((x > 120 && x < 370) && (y > 120 && y < 370));

			asteroids[i] = new Asteroid(x, y, astRadius, minAstVel, maxAstVel,
					astNumHits, astNumSplit, bigAsteroid);
		}

	} //

	// update player again
	public void setUpPlayerAgain() { // starts a new level with one more
										// asteroid

		isPlayerAlive = true;
		// create a new, inactive ship centered on the screen
		// I like .35 for acceleration, .98 for velocityDecay, and
		// .1 for rotationalSpeed. They give the controls a nice feel.
		ship = new Ship(250, 250, 0, .35, .98, .1, 12);
		ship.setActive(true);
		numShots = 0; // no shots on the screen at beginning of level

	} //

	@Override
	public void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);

		g.drawImage(spacebackground, 0, 0, this);

		if (!gameover) {
			ship.draw(g, shipImage);

			for (int i = 0; i < numShots; i++) // loop that calls draw() for
												// each shot
			{
				shots[i].draw(g);
			}

			for (int i = 0; i < numAsteroids; i++) {
				asteroids[i].draw(g);
			}

		}

		// game over code

		if (gameover) {

			g.setColor(Color.white);
			Font newFont = new Font("TimesRoman", Font.PLAIN, 40);
			g.setFont(newFont);
			g.drawString("Game Over", 150, 220);
			
			gfx.drawImage(bufferedImage, 0, 0, this);
			
			
		}



		// game over code

		// if player won
		if (isGameWon) {
			g.setColor(Color.white);
			Font newFont = new Font("TimesRoman", Font.PLAIN, 40);
			g.setFont(newFont);
			g.drawString("YOU WON", 150, 220);
			gfx.drawImage(bufferedImage, 0, 0, this);
			
		}



		// if player won

		if (paused) {

			// g.setColor(Color.red); Font newFont = new Font("TimesRoman",
			// Font.PLAIN, 20); g.setFont(newFont);
			// g.drawString("Press Enter To Start", 150, 240);

			// Rectangle's color
			g.setColor(Color.green);
			g.fillRect(rect1xco, rect1yco, rect1width, rect1height);
			g.setColor(Color.white);
			Font newFont = new Font("TimesRoman", Font.PLAIN, 30);
			g.setFont(newFont);
			g.drawString(" P L A Y ", 190, 270);
		}
		g.setColor(Color.cyan);
		g.drawString("Life: " + lifeLeft, 20, 25);
		gfx.drawImage(bufferedImage, 0, 0, this);

		Toolkit.getDefaultToolkit().sync();

	} // paintCompent

	@Override
	public void run() {

		while (true) {

			startTime = System.currentTimeMillis();

			// start next level when all asteroids are destroyed

			if (!isPlayerAlive) {
				if (lifeLeft == 0) {
					setUpNextLevel();

				} else if (lifeLeft > 0) {
					setUpPlayerAgain();
				}
			} else {
				if (numAsteroids <= 0) {
					isGameWon = true;
					paused = true;
					numAsteroids = 0;
					setUpNextLevel();
				}
			}
			if (!paused) {

				ship.move(B_WIDTH, B_HEIGHT);

				// this loop moves each shot and deletes dead shots
				for (int i = 0; i < numShots; i++) {
					shots[i].move(B_WIDTH, B_HEIGHT);
					// removes shot if it has gone for too long
					// without hitting anything
					if (shots[i].getLifeLeft() <= 0) {
						// shifts all the next shots up one
						// space in the array
						deleteShot(i); // SEE NEW METHOD BELOW
						i--; // move the outer loop back one so
						// the shot shifted up is not skipped
					}
				}

				// move asteroids and check for collisions
				updateAsteroids();

				if (shooting && ship.canShoot()) {
					// add a shot on to the array if the ship is shooting
					shots[numShots] = ship.shoot();
					numShots++;
				}

			} // PAUSE IF */

			repaint();
			endTime = System.currentTimeMillis();
			if (framePeriod - (endTime - startTime) > 0) {
				try {

					Thread.sleep(framePeriod - (endTime - startTime));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block

				} // catch
			} // if

		} // while

	} // run

	private void deleteShot(int index) {
		// delete shot and move all shots after it up in the array
		numShots--;
		for (int i = index; i < numShots; i++)
			shots[i] = shots[i + 1];
		shots[numShots] = null;
	}

	private void deleteAsteroid(int index) {
		// delete asteroid and shift ones after it up in the array
		numAsteroids--;
		for (int i = index; i < numAsteroids; i++)
			asteroids[i] = asteroids[i + 1];
		asteroids[numAsteroids] = null;
	}

	private void addAsteroid(Asteroid ast) {
		// adds the asteroid passed in to the end of the array
		asteroids[numAsteroids] = ast;
		numAsteroids++;
	}

	private void updateAsteroids() {

		for (int i = 0; i < numAsteroids; i++) {
			// move each asteroid
			asteroids[i].move(B_WIDTH, B_HEIGHT);

			// check for collisions with the ship, restart the
			// level if the ship gets hit
			if (asteroids[i].shipCollision(ship)) {
				playercollide.play();
				lifeLeft--;
				if (lifeLeft == 0) {
					numAsteroids = 0;
					gameover = true;
					paused = true;

				}
				isPlayerAlive = false;
				return;
			}

			// check for collisions with any of the shots
			for (int j = 0; j < numShots; j++) {
				if (asteroids[i].shotCollision(shots[j])) {
					explode.play(); // play music
					// if the shot hit an asteroid, delete the shot
					deleteShot(j);
					// split the asteroid up if needed
					if (asteroids[i].getTypeAsteroid() == 3) {

						for (int k = 0; k < asteroids[i].getNumSplit(); k++)
							addAsteroid(asteroids[i].createSplitAsteroid(
									minAstVel, maxAstVel, 2, mediumAsteroid));
					} // if for big asteroid of split
					else if (asteroids[i].getTypeAsteroid() == 2) {

						for (int k = 0; k < asteroids[i].getNumSplit(); k++)
							addAsteroid(asteroids[i].createSplitAsteroid(
									minAstVel, maxAstVel, 1, smallAsteroid));
					} // if for big asteroid of split

					// delete the original asteroid
					deleteAsteroid(i);
					j = numShots; // break out of inner loop - it has
					i--;// don’t skip asteroid shifted back into
					// the deleted asteroid's position
					// already been hit, so don’t need to check
				} // if of collision checker
			}// for loop for collision of bullet

			// for collision with other shots
			// don’t skip asteroid shifted back into
			// the deleted asteroid's position

		} // for loop of main

	} // end of function

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {

			// *
			// paused = !paused;
			// if (paused) ship.setActive(false);
			// else ship.setActive(true);
			// */

		} // eneter button
			// else if (paused && !ship.isActive())
			// return;
		else if (e.getKeyCode() == KeyEvent.VK_UP) {
			ship.setAccelerating(true);

		} else if (e.getKeyCode() == KeyEvent.VK_LEFT)
			ship.setTurningLeft(true);
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			ship.setTurningRight(true);
		else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			if (!paused) {
				shot.play();
			}
			shooting = true; // Start shooting when ctrl is pushed
		}

	} // keyPressed

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_UP)
			ship.setAccelerating(false);
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
			ship.setTurningLeft(false);
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			ship.setTurningRight(false);
		else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
			shooting = false; // Stop shooting when ctrl is released
	} // keyReleased

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		// Save the coordinates of the click lke this.

		xpos = e.getX();
		ypos = e.getY();

		// Check if the click was inside the rectangle area.
		if (xpos > rect1xco && xpos < rect1xco + rect1width && ypos > rect1yco
				&& ypos < rect1yco + rect1height) {
			isGameWon=false;
            gameover=false;
			paused = !paused;
			if (paused)
				ship.setActive(false);

			else
				ship.setActive(true);

		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

} // Board
