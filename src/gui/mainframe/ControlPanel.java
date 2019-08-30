package gui.mainframe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import gui.FrameManager;

/**
 * Control panel on Message panel. This class should contain buttons to
 * manipulate with mail e.g. delete/forward/move/response
 *
 */
public class ControlPanel extends JPanel {
	// to manipulate with message it should be checked -> mail manipulations should
	// be done with all checked messages
	// to make control panel know with which messages to operate all checked
	// messages will be added in checked messages array by message panel
	private ArrayList<MessageRowPanel> checkedMessages;
	private JButton refresh;
	// private JButton response;
	private JButton forward;
	private JButton delete;
	private JButton move;

	public ControlPanel() {
		super();
		checkedMessages = new ArrayList<MessageRowPanel>();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(new LineBorder(Color.BLACK));
		refresh = new JButton("refresh");
		JPanel responseAndForward = new JPanel();
		responseAndForward.setLayout(new BoxLayout(responseAndForward, BoxLayout.X_AXIS));

		// response = new JButton("response");
		forward = new JButton("forward");
		forward.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				checkedMessages.get(0).displayMessage(true);
			}
		});
		delete = new JButton("delete");
		delete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (MessageRowPanel messageRow : checkedMessages) {
					try {
						messageRow.delete();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(FrameManager.mainFrame,
								"Not possible to delete message now, try later", "Error Delete Message",
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		move = new JButton("remove to");
		move.setVisible(false);
		this.add(refresh);
		// this.add(response);
		this.add(forward);
		this.add(delete);
		this.add(move);
		// response.setVisible(false);
		forward.setVisible(false);
		delete.setVisible(false);
		Double size = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		this.setBorder(new EmptyBorder(0, 30, 0, 0));
		this.setPreferredSize(new Dimension(size.intValue(), 20));
		this.setMaximumSize(new Dimension(size.intValue(), 20));
		this.setMinimumSize(new Dimension(size.intValue(), 20));
	}

	public void addCheckedMessage(MessageRowPanel messageRow) {
		// add checked message to checked messages array
		checkedMessages.add(messageRow);
		// control button displaying

		// response and forward should be visible only if exactly one message is checked
		// because it's possible to run this operations only for one message
		if (checkedMessages.size() == 1) {
			// response.setVisible(true);
			forward.setVisible(true);
		}
		// delete and move should be visible if at least one message is checked
		// because it's possible to run this operations for one or more messages
		if (checkedMessages.size() > 0) {
			delete.setVisible(true);
			move.setVisible(true);
		}
		// to avoid displaying above buttons in situations when it's not needed (not
		// possible to run this operations)
		// these buttons should be set invisible when

		// check message amount equals 0
		if (checkedMessages.size() == 0) {
			move.setVisible(false);
			delete.setVisible(false);
			// response.setVisible(false);
			forward.setVisible(false);
		}
		// check message amount more than 1
		if (checkedMessages.size() > 1) {
			// response.setVisible(false);
			forward.setVisible(false);
		}
		FrameManager.mainFrame.repaint();

	}

	/**
	 * method to uncheck message
	 * 
	 * @param messageRow message that has been unchecked
	 */
	public void removeCheckedMessage(MessageRowPanel messageRow) {
		// remove already unchecked message from checked messages array
		checkedMessages.remove(messageRow);
		// control button displaying
		if (checkedMessages.size() == 1) {
			// response.setVisible(true);
			forward.setVisible(true);
			delete.setVisible(true);
			FrameManager.mainFrame.repaint();
		} else {
			// response.setVisible(false);
			forward.setVisible(false);
			delete.setVisible(false);
			FrameManager.mainFrame.repaint();
		}
	}

	public void moveMessagesTo(String folder) {
		for (MessageRowPanel messageRow : checkedMessages) {
			messageRow.moveToFolder(folder);
		}
	}

	public void addActionListenerOnRefreshButton(ActionListener actionListener) {
		refresh.addActionListener(actionListener);

	}

	public void addActionListenerOnMoveButton(ActionListener actionListener) {
		move.addActionListener(actionListener);

	}
}
