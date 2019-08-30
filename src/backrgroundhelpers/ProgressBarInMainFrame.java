package backrgroundhelpers;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import actionclasses.BackgroundAction;
import gui.mainframe.MainFrame;
import gui.mainframe.ProgressBarPanel;

public class ProgressBarInMainFrame extends SwingWorker<Void, Void> {
	private MainFrame mainFrame;
	private JProgressBar progressBar;
	private JLabel label;
	private BackgroundAction action;

	public ProgressBarInMainFrame(BackgroundAction action, boolean inNewFrame) {
		super();
		this.action = action;
	}

	public void setFrame(MainFrame frame) {
		this.mainFrame = frame;
	}

	public void startInMainFrame() {
		ProgressBarPanel panel = (ProgressBarPanel) mainFrame.getPanel("progressBar");
		progressBar = panel.getProgressBar2();
		label = panel.getProcessName();
		this.execute();
	}

	protected Void doInBackground() {
		label.setText("in progress");
		action.action(progressBar, label);
		return null;
	}

	protected void done() {
		label.setText("done");
		progressBar.setVisible(false);
		label.setVisible(false);
	}

}
