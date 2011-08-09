package net.msites.kaffeekasse.ui.components.checkablejlist;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CheckableListCellRenderer extends JCheckBox implements ListCellRenderer { 
	
	public CheckableListCellRenderer() {
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		CheckableJList checkableList = (CheckableJList)list;  // hässlich, aber nötig
		
		if(isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		
		setSelected(checkableList.getModel().isChecked(index));
		setText(value.toString());
		
		return this;
	}
	
}
