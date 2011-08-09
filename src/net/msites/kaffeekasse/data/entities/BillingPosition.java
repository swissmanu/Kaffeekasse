package net.msites.kaffeekasse.data.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="BILLING_POSITIONS")
public class BillingPosition extends DataEntity {

	@OneToOne(optional = false)
	private Consumer consumer;
	@OneToMany(mappedBy="billingPosition", cascade=CascadeType.ALL)
	private List<Consumption> consumptions;
	@OneToMany(mappedBy="billingPosition", cascade=CascadeType.ALL)
	private List<OtherCost> otherCosts;
	private Integer paid = 0;
	@ManyToOne(optional = false)
	private Billing billing;
	
	
	// Konstruktoren -----------------------------------------------------------
	public BillingPosition() {
		this.consumptions = new ArrayList<Consumption>();
		this.otherCosts = new ArrayList<OtherCost>();
	}
	
	public BillingPosition(Billing billing, Consumer consumer, List<Consumption> consumptions, List<OtherCost> otherCosts, boolean paid) {
		this.consumer = consumer;
		this.consumptions = consumptions;
		this.otherCosts = otherCosts;
		this.paid = convertBooleanToInt(paid);
		this.billing = billing;
	}
	
	
	// Getter- & Setter-Methoden -----------------------------------------------
	public Consumer getConsumer() {
		return consumer;
	}
	
	public List<Consumption> getConsumptions() {
		return consumptions;
	}
	
	public List<OtherCost> getOtherCosts() {
		return otherCosts;
	}
	
	public boolean isPaid() {
		return convertIntToBoolean(paid);
	}
	
	public Billing getBilling() {
		return billing;
	}
	
	public void setConsumer(Consumer consumer) {
		this.consumer = consumer;
		setChanged();
	}
	
	public void setConsumptions(List<Consumption> consumptions) {
		this.consumptions = consumptions;
		setChanged();
	}
	
	public void setOtherCosts(List<OtherCost> otherCosts) {
		this.otherCosts = otherCosts;
		setChanged();
	}
	
	public void setPaid(boolean paid) {
		this.paid = convertBooleanToInt(paid);
		setChanged();
	}
	
	public void setBilling(Billing billing) {
		this.billing = billing;
	}
	
	
	// Consumption-Zugriff -----------------------------------------------------
	@Transient
	public Consumption getConsumption(Product product) {
		Consumption consumption = null;
		
		for(Consumption c: consumptions) {
			if(c.getProduct().equals(product)) {
				consumption = c;
				break;
			}
		}
		
		return consumption;
	}
	
	public void addConsumption(Consumption consumptation) {
		consumptions.add(consumptation);
		setChanged();
	}
	
	public boolean removeConsumption(Consumption consumption) {
		boolean result = consumptions.remove(consumption);
		setChanged();
		
		return result;
	}
	
	/**
	 * Berechnet den Totalbetrag aller {@link Consumption}'s dieser
	 * {@link BillingPosition}.
	 * 
	 * @return {@link Double}
	 */
	public Double calculateTotalConsumptions() {
		Double total = 0d;
		
		for(Consumption c: consumptions) {
			BillingProductPrice price = billing.getBillingProductPrice(c.getProduct());
			total += c.getAmount() * price.getPrice();
		}
		
		return total;
	}
	
	
	// OtherCost-List-Zugriff --------------------------------------------------
	public void addOtherCost(OtherCost otherCost) {
		otherCosts.add(otherCost);
	}
	
	public boolean removeOtherCost(OtherCost otherCost) {
		boolean result = otherCosts.remove(otherCost);
		setChanged();
		
		return result;
	}
	
	/**
	 * Berechnet den Totalbetrag aller "Anderer Kosten" dieser
	 * {@link BillingPosition}.
	 * 
	 * @return
	 */
	public Double calculateTotalOtherCosts() {
		Double total = 0d;
		
		for(OtherCost otherCost: otherCosts) {
			total += otherCost.getAmount();
		}
		
		return total;
	}
	
	
	// Überschreibungen --------------------------------------------------------
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if(obj instanceof BillingPosition) {
			BillingPosition other = (BillingPosition)obj;
			if(getConsumer().equals(other.getConsumer())
					&& getConsumptions().size() == other.getConsumptions().size()) {
				equals = true;
			}
		}
		
		return equals;
	}
	
	@Override
	public void setChanged() {
		super.setChanged();
		billing.setChanged();
	}
	
}
