package actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import filewriters.XMLFileManager;
import gui.FrameManager;
import gui.mainframe.MainFrame;

public class ChangeLookAndFeelWhileProgramRunTask implements ActionListener {
	String lookAndFeel;

	public ChangeLookAndFeelWhileProgramRunTask(String lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			// change look and feel
			UIManager.setLookAndFeel(lookAndFeel);
			SwingUtilities.updateComponentTreeUI(FrameManager.mainFrame);
			// save theme settings
			new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings")).changeLookAndFeel(lookAndFeel);
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					FrameManager.getLanguageProperty("error.unsupportedTheme"),
					FrameManager.getLanguageProperty("error.title.unsupportedTheme"), JOptionPane.ERROR_MESSAGE);
		}		
	}

}
