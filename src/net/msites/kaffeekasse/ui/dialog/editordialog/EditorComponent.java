package net.msites.kaffeekasse.ui.dialog.editordialog;

import java.awt.Dimension;

import javax.swing.JComponent;

import net.msites.kaffeekasse.data.entities.DataEntity;

public abstract class EditorComponent extends JComponent {

	public EditorComponent() {
		setPreferredSize(new Dimension(300,0));
	}
	
	/**
	 * Überprüft die Eingabekompionenten dieses {@link EditorComponent}
	 * 
	 * @return TRUE = Alles OK, FALSE = Fehler
	 */
	public abstract boolean validateData();
	
	/**
	 * Überträgt die Werte aus den Eingabekomponenten in den internen {@link DataEntity}-
	 * Speicher des {@link EditorComponent}'s.<br/>
	 * Anschliessend kann per {@link #getData()} dieses {@link DataEntity} geholt 
	 * werden.<br/>
	 * <br/>
	 * Diese Methode wird im Normalfall vom {@link EditorDialog} aufgerufen. Sie
	 * muss nicht explizit an anderer Stelle aufgerufen werden.
	 * 
	 * @see DataEntity
	 * @see #getData()
	 * @see EditorDialog
	 */
	public abstract void writeData();
	
	/**
	 * Liefert die Daten aus dem {@link EditorComponent} in Form eines {@link DataEntity}'s.<br/>
	 * ACHTUNG! Das {@link DataEntity} wird keine aktuellen Werte enthalten, solange
	 * zuvor nicht {@link #writeData()} aufgerufen wurde!
	 * 
	 * @return {@link DataEntity}
	 * @see DataEntity
	 * @see #writeData()
	 */
	public abstract DataEntity getData();
	
}
