package net.msites.kaffeekasse.data;

import java.util.Observable;

import net.msites.kaffeekasse.data.entities.DataEntity;

/**
 * Wird vom {@link Observable} des {@link DataContainer}'s zur Kommunikation mit
 * den entsprechenden {@link Observer}'s verwendet.
 * 
 * @author Manuel Alabor
 */
public class DataContainerEvent {

	private Action action;
	private DataEntity dataEntity;
	
	public enum Action {
		INSERT, UPDATE, DELETE
	}
	
	
	// Konstruktoren -----------------------------------------------------------
	public DataContainerEvent(Action action, DataEntity dataEntity) {
		this.action = action;
		this.dataEntity = dataEntity;
	}
	
	
	// Getter-Methoden ---------------------------------------------------------
	public Action getAction() {
		return action;
	}
	
	public DataEntity getDataEntity() {
		return dataEntity;
	}
	
}
