package net.msites.kaffeekasse.data.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class DataEntity implements Serializable { 
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Transient
	private boolean changed = false;
	@Transient
	private boolean surpressChangeFlag = false;
	
	
	// Konstruktoren -----------------------------------------------------------
	public DataEntity() {}
	
	
	// Getter- & Setter-Methoden -----------------------------------------------
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
		setChanged();
	}
	
	
	// Changed/Not changed -----------------------------------------------------
	public void setChanged() {
		setChanged(true);
	}
	
	public void setChanged(boolean changed) {
		if(!surpressChangeFlag) this.changed = changed;
	}
	
	public boolean hasChanged() {
		return changed;
	}
	
	public void setSurpressChangeFlag(boolean surpressChangeFlag) {
		this.surpressChangeFlag = surpressChangeFlag;
	}
	
	
	// Hilfsunktionen ----------------------------------------------------------
	/**
	 * Konvertiert einen {@link Integer}-Wert nach {@link Boolean}.
	 * 
	 * @param integer
	 * @return
	 */
	protected boolean convertIntToBoolean(Integer integer) {
		boolean result = false;
		
		if(integer != null && integer >= 1) {
			result = true;
		}
		
		return result;
	}
	
	/**
	 * Konvertiert einen {@link Boolean}-Wert nach {@link Integer}.
	 * 
	 * @param bool
	 * @return
	 */
	protected Integer convertBooleanToInt(Boolean bool) {
		Integer result = 0;
		
		if(bool != null && bool.booleanValue() == true) {
			result = 1;
		}
		
		return result;
	}
	
	
	// Überschreibungen --------------------------------------------------------
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if(obj instanceof DataEntity) {
			DataEntity other = (DataEntity)obj;
			equals = (getId() == other.getId());
		}
		
		return equals;
	}
	
	@Override
	public String toString() {
		return "DataEntity [id: " + getId() + "]";
	}
	
}
