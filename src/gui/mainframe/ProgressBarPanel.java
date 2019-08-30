package gui.mainframe;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class ProgressBarPanel extends JPanel {
	private JProgressBar progressBar2;
	private JLabel processName;

	public ProgressBarPanel() {
		super();
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(Box.createVerticalGlue());
		processName = new JLabel();
		processName.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.add(processName);
		progressBar2 = new JProgressBar();
		progressBar2.setStringPainted(true);
		progressBar2.setMinimum(0);
		progressBar2.setMaximum(100);
		this.add(progressBar2);

		this.add(Box.createVerticalGlue());
	}

	public JProgressBar getProgressBar2() {
		return progressBar2;
	}

	public JLabel getProcessName() {
		return processName;
	}

}
