package gui.newaccountdialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Checkboxes for POP, IMAP, SMTP
 */
public class CheckBoxPanel extends JPanel {
	private HashMap<String, JCheckBox> checkBoxes;

	public CheckBoxPanel() {
		checkBoxes = new HashMap<String, JCheckBox>();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(new EmptyBorder(0, 0, 20, 50));
	}

	public void addCheckBox(String checkBoxLabelName) {
		JCheckBox checkBox = new JCheckBox(checkBoxLabelName);
		checkBox.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(checkBox);
		checkBoxes.put(checkBoxLabelName, checkBox);
	}

	public JCheckBox getCheckBox(String checkBoxLabelName) {
		return checkBoxes.get(checkBoxLabelName);
	}

	public void addEnableActionOnCheckBox(String checkBoxLabelName, JTextField... fields) {
		JCheckBox checkBox = checkBoxes.get(checkBoxLabelName);
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (checkBox.isSelected()) {
					for (JTextField field : fields) {
						field.setEditable(true);
					}
				} else {
					for (JTextField field : fields) {
						field.setEditable(false);
					}
				}
			}
		});
	}
}
