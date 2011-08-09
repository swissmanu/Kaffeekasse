package net.msites.kaffeekasse.ui.components.checkablejlist;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;

import net.msites.kaffeekasse.ui.components.DetailsListCellRenderer;

public class CheckableDetailsListCellRenderer extends DetailsListCellRenderer { 
	
	private final JCheckBox chkCheckbox = new JCheckBox();
	
	public CheckableDetailsListCellRenderer() {
		setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
		
		chkCheckbox.setOpaque(false);
		chkCheckbox.setBorder(BorderFactory.createEmptyBorder());
		chkCheckbox.setFont(lblName.getFont().deriveFont(Font.BOLD));
		lblDetails.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
		
		setLayout(new BorderLayout());			
		add(chkCheckbox, BorderLayout.CENTER);
		add(lblDetails, BorderLayout.SOUTH);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		CheckableJList checkableList = (CheckableJList)list;  // hässlich, aber nötig
		
		if(isSelected) {
			setBackground(list.getSelectionBackground());
			chkCheckbox.setForeground(list.getSelectionForeground());
			lblDetails.setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			chkCheckbox.setForeground(list.getForeground());
			lblDetails.setForeground(list.getForeground());
		}
		
		chkCheckbox.setSelected(checkableList.getModel().isChecked(index));
		setText(value.toString());
		
		return this;
	}
	
	@Override
	protected void setText(String text) {
		chkCheckbox.setText(text);
	}
	
}
