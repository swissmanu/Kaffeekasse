package net.msites.kaffeekasse.ui.components.tabbeduserinterface;

import javax.swing.JComponent;

import net.msites.kaffeekasse.data.entities.DataEntity;

public abstract class AbstractEditableTab extends JComponent {

	/**
	 * Ermittelt ob sich die Daten in diesem Tab geändert haben.
	 * 
	 * @return true/false
	 */
	public abstract boolean hasChanged();
	
	/**
	 * Ermittelt ob dieser Tab speichern kann.
	 * 
	 * @return
	 */
	public abstract boolean canSave();
	
	/**
	 * Gibt das momentan zu bearbeitende {@link DataEntity} dieses Tabs zurück.
	 * 
	 * @return
	 */
	public abstract DataEntity getData();
	
	/**
	 * Veranlasst diesen Tab, seinen Inhalt zu speichern.
	 */
	public abstract void save();
	
	/**
	 * Wird vor dem Schliessen des Tabs aufgerufen.<br/>
	 * Der Tab selber kann hier entscheiden, ob er geschlossen werden darf
	 * (Returnwert <code>true</code>) oder ob er offen bleiben muss
	 * (Returnwert <code>false</code>).
	 * 
	 * @return true/false
	 */
	public abstract boolean beforeCloseTab();
	
}
