package net.msites.kaffeekasse.data.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="PROPERTIES")
public class Property extends DataEntity {

	@Column(unique = true, nullable = false, updatable = false)
	private String key;
	@Basic(optional = false)
	@Column(length = 1000)
	private String value;
	
	// Konstruktoren -----------------------------------------------------------
	public Property() {}
	
	public Property(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	
	// Getter- & Setter-Methoden -----------------------------------------------
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
		setChanged();
	};
	
	public void setKey(String key) {
		this.key = key;
		setChanged();
	}
	
	
	// Überschreibungen --------------------------------------------------------
	@Override
	public String toString() {
		return "Property [" + key + "=" + value + "]";
	}
	
}
