package net.msites.kaffeekasse.data.entities;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name="OTHER_COSTS")
public class OtherCost extends DataEntity {
	
	@Basic(optional = true)
	private String text;
	@Basic(optional = false)
	private Double amount = new Double(0d);
	@ManyToOne(optional = false)
	private BillingPosition billingPosition;
	
	// Konstruktoren -----------------------------------------------------------
	public OtherCost() {}
	
	public OtherCost(BillingPosition billingPosition, String text, Double amount) {
		this.billingPosition = billingPosition;
		this.text = text;
		this.amount = amount;
	}

	
	// Getter- & Setter-Methoden -----------------------------------------------
	public String getText() {
		return text;
	}

	public Double getAmount() {
		return amount;
	}

	public BillingPosition getBillingPosition() {
		return billingPosition;
	}
	
	public void setText(String text) {
		this.text = text;
		setChanged();
	}

	public void setAmount(Double amount) {
		this.amount = amount;
		setChanged();
	}
	
	public void setBillingPosition(BillingPosition billingPosition) {
		this.billingPosition = billingPosition;
	}
	
	
	// Überschreibungen --------------------------------------------------------
	@Override
	public String toString() {
		return "OtherCost [" + text + ", " + amount + "]";
	}
	
	@Override
	public void setChanged() {
		super.setChanged();
		if(billingPosition != null) billingPosition.setChanged();
	}
	
}
