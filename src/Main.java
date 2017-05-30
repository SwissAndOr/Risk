import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		JFrame window = new JFrame("Risk");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		int players = args.length > 0 ? Math.min(5, Math.max(2, Integer.parseInt(args[0]))) : 3;
		window.add(new Risk(players));
		window.pack();
		window.setVisible(true);
	}

}
