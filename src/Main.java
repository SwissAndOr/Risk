import java.awt.GridLayout;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {
	
	public static void main(String[] args) {
		JFrame window = new JFrame("Risk");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		
		JPanel startMenu = new JPanel(new GridLayout(4,1));
		JLabel sliderLabel = new JLabel("Players", SwingConstants.CENTER);
		JSlider slider = new JSlider(2, 6, 3);
		JButton newGame = new JButton("New Game");
		JButton loadGame = new JButton("Load Game");
		
		slider.setPaintLabels(true);
		slider.setLabelTable(slider.createStandardLabels(1));
		sliderLabel.setFont(sliderLabel.getFont().deriveFont(15f));
		newGame.setFont(newGame.getFont().deriveFont(20f));
		loadGame.setFont(loadGame.getFont().deriveFont(20f));
		
		newGame.addActionListener((e) -> {
			window.remove(startMenu);
			window.add(new Risk(slider.getValue()));
			window.pack();
		});
		
		loadGame.addActionListener((e) -> {
			JFileChooser chooser = new JFileChooser(".");
			chooser.setDialogTitle("Risk Game Loader");
			chooser.setFileFilter(new FileNameExtensionFilter("Risk Saves", "save"));
			if (chooser.showOpenDialog(startMenu) == JFileChooser.APPROVE_OPTION) {
				window.remove(startMenu);
				window.add(new Risk(chooser.getSelectedFile()));
				window.pack();
			}
		});
		
		startMenu.add(sliderLabel);
		startMenu.add(slider);
		startMenu.add(newGame);
		startMenu.add(loadGame);
		window.add(startMenu);

		window.pack();
		window.setVisible(true);
	}

}
