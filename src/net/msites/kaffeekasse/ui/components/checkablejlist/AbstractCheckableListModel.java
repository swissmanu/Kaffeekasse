package net.msites.kaffeekasse.ui.components.checkablejlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;

/**
 * Erweitert das {@link AbstractListModel} so, dass zu jedem Listenelement
 * ein {@link Boolean}-Wert (checked/unchecked) gespeichert werden kann.<br/>
 * Dieser Wert wird von der {@link CheckableJList} verwendet, damit Einträge
 * in der Liste per Checkbox markiert werden können.<br/>
 * <br/>
 * Ein {@link AbstractCheckableListModel} kann nur eine Art von Elementen enthalten.
 * Dementsprechend wurde per Generics die passende Typensicherheit implementiert.
 * 
 * @author Manuel Alabor
 */
public abstract class AbstractCheckableListModel extends AbstractListModel implements CheckableListModel {

	private Map<Object, Boolean> elementStates = new HashMap<Object, Boolean>();
	
	public boolean isChecked(Object element) {
		boolean checked = false;
		
		Boolean savedState = elementStates.get(element);
		if(savedState != null) checked = savedState;
		
		return checked;
	}
	
	public boolean isChecked(int index) {
		return isChecked(getElementAt(index));
	}
	
	public void setChecked(Object element, boolean checked) {
		elementStates.put(element, checked);
	}
	
	public void setChecked(int index, boolean checked) {
		setChecked(getElementAt(index), checked);
		fireContentsChanged(this, index, index);
	}
	
	public List<Object> getCheckedElements() {
		Set<Entry<Object, Boolean>> entries = elementStates.entrySet();
		List<Object> checkedElements = new ArrayList<Object>(entries.size());
		
		for(Entry<Object, Boolean> entry: entries) {
			if(entry.getValue() == true) checkedElements.add(entry.getKey());
		}
		
		return checkedElements;
	}
	
	public void fireCompleteContentsChanged(Object source) {
		fireContentsChanged(source, 0, getSize());
	}
	
}
