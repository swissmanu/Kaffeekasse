package net.msites.kaffeekasse.ui.components.checkablejlist;

import java.util.List;

import javax.swing.ListModel;

/**
 * @author Manuel Alabor
 * @see AbstractCheckableListModel
 */
public interface CheckableListModel extends ListModel {
	
	public boolean isChecked(Object element);
	
	public boolean isChecked(int index);

	public void setChecked(int index, boolean checked);
	
	public void setChecked(Object element, boolean checked);
	
	public List<Object> getCheckedElements();
	
}
