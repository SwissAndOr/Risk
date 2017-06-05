import java.awt.Font;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.scene.control.RadioButton;

public class Main {
	
	public static void main(String[] args) {
		JFrame window = new JFrame("Risk");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		
		JPanel startMenu = new JPanel(new GridLayout(5,1));
		JLabel sliderLabel = new JLabel("Players", SwingConstants.CENTER);
		JSlider slider = new JSlider(2, 6, 3);
		JPanel buttonBox = new JPanel();
		ButtonGroup radioButtons = new ButtonGroup();
		JRadioButton selectionStart = new JRadioButton("Selection", true);
		JRadioButton randomStart = new JRadioButton("Random");
		JRadioButton trueRandomStart = new JRadioButton("True Random");
		JButton newGame = new JButton("New Game");
		JButton loadGame = new JButton("Load Game");
		
		Font medium = startMenu.getFont().deriveFont(15f);
		Font large = startMenu.getFont().deriveFont(20f);
		slider.setPaintLabels(true);
		slider.setLabelTable(slider.createStandardLabels(1));
		sliderLabel.setFont(medium.deriveFont(Font.BOLD));
		selectionStart.setFont(medium);
		randomStart.setFont(medium);
		trueRandomStart.setFont(medium);
		newGame.setFont(large);
		loadGame.setFont(large);
		
		radioButtons.add(selectionStart);
		radioButtons.add(randomStart);
		radioButtons.add(trueRandomStart);
		buttonBox.add(selectionStart);
		buttonBox.add(randomStart);
		buttonBox.add(trueRandomStart);

		selectionStart.setToolTipText("Players select their territories one after the other");
		randomStart.setToolTipText("Players distribute their armies after getting random territories");
		trueRandomStart.setToolTipText("WARNING: ONLY FOR TRUE ALPHAS");
		
		newGame.addActionListener((e) -> {
			window.remove(startMenu);
			int selection = 0;
			selection += randomStart.getModel() == radioButtons.getSelection() ? 1 : 0;
			selection += trueRandomStart.getModel() == radioButtons.getSelection() ? 2 : 0;
			window.add(new Risk(slider.getValue(), selection));
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
		startMenu.add(buttonBox);
		startMenu.add(newGame);
		startMenu.add(loadGame);
		window.add(startMenu);

		window.pack();
		window.setVisible(true);
	}

}
