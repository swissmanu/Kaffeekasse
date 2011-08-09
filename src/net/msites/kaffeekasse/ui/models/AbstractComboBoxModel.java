package net.msites.kaffeekasse.ui.models;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

public abstract class AbstractComboBoxModel implements ComboBoxModel {

	private Object selectedItem = null;
	private List<ListDataListener> listDataListeners = new ArrayList<ListDataListener>(2);
	
	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		this.selectedItem = anItem;
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listDataListeners.add(l);
	}
	
	@Override
	public void removeListDataListener(ListDataListener l) {
		listDataListeners.remove(l);
	}
	
	
	
	@Override
	public abstract Object getElementAt(int index);

	@Override
	public abstract int getSize();

}
