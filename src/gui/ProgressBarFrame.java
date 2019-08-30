package gui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class ProgressBarFrame extends JFrame {
	private JLabel label;
	private JProgressBar progressBar;

	public ProgressBarFrame() {
		super("Progress Terminated");
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setContentPane(contentPane);
		label = new JLabel("not started yet");
		progressBar = new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		progressBar.setStringPainted(true);
		contentPane.add(label);
		contentPane.add(progressBar);
	}

	public JLabel getLabel() {
		return label;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
}
