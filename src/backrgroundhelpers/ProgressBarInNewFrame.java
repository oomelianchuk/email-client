package backrgroundhelpers;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import actionclasses.ProgressBarAction;
import gui.mainframe.MainFrame;
import gui.mainframe.ProgressBarPanel;

public class ProgressBarInNewFrame implements Runnable {
	private JFrame frame;
	private MainFrame mainFrame;
	private JProgressBar progressBar;
	private JLabel label;
	private ProgressBarAction action;
	private boolean inNewFrame;

	public ProgressBarInNewFrame(ProgressBarAction action, boolean inNewFrame) {
		super();
		this.action = action;
		this.inNewFrame = inNewFrame;
		frame = new JFrame("Progress Terminated");
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		frame.setContentPane(contentPane);
		label = new JLabel("");
		progressBar = new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		progressBar.setStringPainted(true);
		contentPane.add(label);
		contentPane.add(progressBar);
	}

	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public void startInNewFrame() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setPreferredSize(new Dimension(300, 200));
		frame.pack();
		frame.setVisible(true);
		// this.execute();
	}

	public void setFrame(MainFrame frame) {
		this.mainFrame = frame;
	}

	public void startInMainFrame() {
		ProgressBarPanel panel = (ProgressBarPanel) mainFrame.getPanel("progressBar");
		progressBar = panel.getProgressBar2();
		label = panel.getProcessName();
		// this.execute();
	}

	protected void doInBackground() {
		action.action(progressBar, label);
		// return null;
	}

	protected void done() {
		progressBar.setVisible(false);
		label.setVisible(false);
		frame.dispose();
	}

	@Override
	public void run() {
		if (inNewFrame) {
			startInNewFrame();
		} else {
			startInMainFrame();
		}
		doInBackground();
		done();
	}

}
