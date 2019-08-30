package gui.mainframe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * This class represents labels for messages's short informations (message rows)
 */
public class MessagesTopLine extends JPanel {
	JCheckBox checkAll;

	public MessagesTopLine() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(new LineBorder(Color.BLACK));
		checkAll = new JCheckBox();
		checkAll.setBorder(new EmptyBorder(10, 10, 10, 10));

		// subject label
		JLabel subjectLabel = new JLabel("subject");
		subjectLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// sender label
		JLabel senderLabel = new JLabel("sender");
		senderLabel.setBorder(new EmptyBorder(10, 165, 10, 415));

		// date label
		JLabel dateLabel = new JLabel("recieve date");
		dateLabel.setBorder(new EmptyBorder(10, 10, 10, 30));

		// make message top line be always displayed till the end of the frame
		Double size = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		this.setPreferredSize(new Dimension(size.intValue(), 20));
		this.setMaximumSize(new Dimension(size.intValue(), 20));
		this.setMinimumSize(new Dimension(size.intValue(), 20));
		this.add(checkAll);
		this.add(subjectLabel);
		this.add(senderLabel);
		this.add(dateLabel);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		int w = getWidth();
		int h = getHeight();
		Color color1 = Color.white;
		Color color2 = Color.gray;
		GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
	}
}
