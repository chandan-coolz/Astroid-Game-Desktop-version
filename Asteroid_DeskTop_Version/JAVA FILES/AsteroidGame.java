import java.awt.EventQueue;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class AsteroidGame extends JFrame {

	public AsteroidGame() {

		initUI();
	}

	private void initUI() {

		add(new Board());

		setResizable(false);
		pack();

		setTitle("Asteroid");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				JFrame ex = new AsteroidGame();
				ex.setVisible(true);
			}
		});
	}
}
