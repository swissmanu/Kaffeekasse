package net.msites.kaffeekasse.data.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Basic;

@Entity
@Table(name="CONSUMPTIONS")
public class Consumption extends DataEntity implements Comparable<Consumption> {

	@OneToOne(optional = false)
	private Product product;
	@Basic(optional = false)
	private Double amount = new Double(0d);
	@ManyToOne(optional = false)
	private BillingPosition billingPosition;
	
	// Konstruktoren -----------------------------------------------------------
	public Consumption() {}
	
	public Consumption(BillingPosition billingPosition, Product product, Double amount) {
		this.billingPosition = billingPosition;
		this.product = product;
		this.amount = amount;
	}

	public Product getProduct() {
		return product;
	}

	public Double getAmount() {
		return amount;
	}
	
	public BillingPosition getBillingPosition() {
		return billingPosition;
	}

	public void setProduct(Product product) {
		this.product = product;
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
		return "Consumptation [" + amount.toString() + "x " + product.getName() + "]";
	}
	
	@Override
	public int compareTo(Consumption o) {
		return getProduct().compareTo(o.getProduct());
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if(obj instanceof Consumption) {
			Consumption other = (Consumption)obj;
			
			if(getProduct().equals(other.getProduct())
					&& getAmount().equals(other.getAmount())) {
				equals = true;
			}
		}
		
		return equals;
	}
	
	@Override
	public void setChanged() {
		super.setChanged();
		billingPosition.setChanged();
	}
	
}
