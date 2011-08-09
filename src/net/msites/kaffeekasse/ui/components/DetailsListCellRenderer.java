package net.msites.kaffeekasse.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class DetailsListCellRenderer extends JPanel implements ListCellRenderer {
		
	protected final JLabel lblName = new JLabel();
	protected final JLabel lblDetails = new JLabel();
	
	public DetailsListCellRenderer() {
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		
		lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
		lblDetails.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		
		setLayout(new BorderLayout());			
		add(lblName, BorderLayout.CENTER);
		add(lblDetails, BorderLayout.SOUTH);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if(isSelected) {
			this.setBackground(list.getSelectionBackground());
			lblName.setForeground(list.getSelectionForeground());
			lblDetails.setForeground(list.getSelectionForeground());
		} else {
			this.setBackground(list.getBackground());
			lblName.setForeground(list.getForeground());
			lblDetails.setForeground(list.getForeground());
		}
		
		return this;
	}
	
	protected void setText(String text) {
		lblName.setText(text);
	}
	
	protected void setDetails(String details) {
		lblDetails.setText(details);
	}
	
	protected void setIcon(Icon icon) {
		lblName.setIcon(icon);
	}
		
}
