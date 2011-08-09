package net.msites.kaffeekasse.data.entities;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Basic;

@Entity
@Table(name="CONSUMERS")
public class Consumer extends DataEntity implements Comparable<Consumer>  {
	
	@Basic(optional = false)
	private String firstname;
	@Basic(optional = false)
	private String lastname;
	@Basic(optional = false)
	private String email;
	@Enumerated
	@Basic(optional = false)
	private Language language = Language.GERMAN;
	@Basic(optional = false)
	private Integer addToNewBillings = 0;
	
	// Konstruktoren -----------------------------------------------------------
	public Consumer() {}
	
	public Consumer(String firstname, String lastname, String email, Language language, Boolean addToNewBillings) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.language = language;
		this.addToNewBillings = convertBooleanToInt(addToNewBillings);
	}
	
	
	// Getter- & Setter-Methoden -----------------------------------------------
	public String getFirstname() {
		return firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public String getEmail() {
		return email;
	}
	
	public Language getLanguage() {
		return language;
	}
	
	public Boolean getAddToNewBillings() {
		return convertIntToBoolean(addToNewBillings);
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
		this.setChanged();
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
		this.setChanged();
	}
	
	public void setEmail(String email) {
		this.email = email;
		this.setChanged();
	}
	
	public void setLanguage(Language language) {
		this.language = language;
		this.setChanged();
	}
	
	public void setAddToNewBillings(Boolean addToNewBillings) {
		this.addToNewBillings = convertBooleanToInt(addToNewBillings);
	}
	
	
	// Hilfsmethoden -----------------------------------------------------------
	@Transient
	public String getFullname() {
		return getLastname() + " " + getFirstname();
	}
	
	@Override
	public int compareTo(Consumer o) {
		int compare = getLastname().compareTo(o.getLastname());
		if(compare == 0) compare = getFirstname().compareTo(o.getFirstname());
		
		return compare;
	}
	
	@Override
	public String toString() {
//		return "Consumer [" + firstname + ", " + lastname + ", " + email + ", " + language + "]";
		return getFullname();
	}
	
}
