package net.msites.kaffeekasse.data.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="BILLINGS")
/**
 * Eine Abrechnung besteht aus folgenden Elementen:<br/>
 *  + Abrechnung Billing								{@link Billing}<br/>									
 *  +-+ 1-n Abrechnungspositionen, eine pro Konsument	{@link BillingPosition}<br/>
 *    +-+ 1-n Konsumationen, eine pro Produkt			{@link Consumption}<br/>
 * <br/>
 * So kann eine komplette Abrechnung im Speicher dargestellt werden.<br/>
 * Um programmtechnisch einen einfachen Zugriff auf die Positions zu gewährleisten,
 * sind diese in einer {@link Map} abgelegt (Key: {@link Consumer}).<br/>
 * Damit pBeans2 diese Daten aber auch persistent speichern kann, gibt {@link Billing}
 * per {@link #getBillingPosition(Consumer)} ein Array statt eine {@link Map} aus.<br/>
 * Dementsprechend nimmt auch {@link #setBillingPositions(BillingPosition[])}
 * ein Array entgegen, welches automatisch in die {@link Map} umgefüllt wird.
 * 
 * @author Manuel Alabor
 * @see BillingPosition
 * @see Consumption
 * @see Product
 * @see Consumer
 */
public class Billing extends DataEntity implements Comparable<Billing> {

	@Basic(optional = false)
	private String title;
	@Temporal(TemporalType.DATE)
	@Basic(optional = false)
	private Date date;
	@OneToMany(mappedBy="billing", cascade=CascadeType.ALL)
	private List<BillingProductPrice> billingProductPrices;
	@OneToMany(mappedBy="billing", cascade=CascadeType.ALL)
	private List<BillingPosition> billingPositions;
	
	
	// Konstruktoren -----------------------------------------------------------
	public Billing() {
		billingProductPrices = new ArrayList<BillingProductPrice>();
		billingPositions = new ArrayList<BillingPosition>();
	}
	
	public Billing(String title, Date date) {
		this();
		this.title = title;
		this.date = date;
	}	
	
	
	// Getter- & Setter-Methoden -----------------------------------------------
	public String getTitle() {
		return title;
	}
	
	public Date getDate() {
		return date;
	}
	
	public List<BillingPosition> getBillingPositions() {
		return billingPositions;
	}
	
	public List<BillingProductPrice> getBillingProductPrices() {
		return billingProductPrices;
	}
	
	public void setTitle(String title) {
		this.title = title;
		setChanged();
	}
	
	public void setDate(Date date) {
		this.date = date;
		setChanged();
	}
	
	public void setBillingPositions(List<BillingPosition> billingPositions) {
		this.billingPositions = billingPositions;
		setChanged();
	}
	
	public void setBillingProductPrices(
			List<BillingProductPrice> billingProductPrices) {
		this.billingProductPrices = billingProductPrices;
		setChanged();
	}
	
	
	// BillingPosition-Zugriffe ------------------------------------------------
	@Transient
	public BillingPosition getBillingPosition(Consumer consumer) {
		BillingPosition billingPosition = null;
		
		for(BillingPosition pos: billingPositions) {
			if(pos.getConsumer().equals(consumer)) {
				billingPosition = pos;
				break;
			}
		}
		
		return billingPosition;
	}
	
	public void addBillingPosition(BillingPosition billingPosition) {
		billingPositions.add(billingPosition);
		setChanged();
	}
	
	public void removeBillingPosition(BillingPosition billingPosition) {
		billingPositions.remove(billingPosition);
		setChanged();
	}
	
	
	// BillingProductPrice-Zugriffe --------------------------------------------
	public BillingProductPrice getBillingProductPrice(Product product) {
		BillingProductPrice price = null;
		
		for(BillingProductPrice p: billingProductPrices) {
			if(p.getProduct().equals(product)) {
				price = p;
				break;
			}
		}
		
		return price;
	}
	
	
	// Hilfsfunktionenen -------------------------------------------------------
	/**
	 * Liefert eine Liste mit allen {@link Consumer}'s, welche in dieser
	 * Abrechnung mindestens eine {@link BillingPosition} haben.
	 * 
	 * @return List<Consumer>
	 */
	@Transient
	public List<Consumer> getUsedConsumers() {
		List<Consumer> usedConsumers = new ArrayList<Consumer>();
		
		for(BillingPosition billingPosition: billingPositions) {
			Consumer consumer = billingPosition.getConsumer();
			if(!usedConsumers.contains(consumer)) usedConsumers.add(consumer);
		}
		Collections.sort(usedConsumers);
		
		return usedConsumers;
	}
	
	/**
	 * Liefert eine Liste mit allen {@link Product}'s, welche in dieser
	 * Abrechnung mindestens einmal in einer {@link Consumption} verwendet
	 * wurden.
	 * 
	 * @return
	 */
	@Transient
	public List<Product> getUsedProducts() {
		List<Product> usedProducts = new ArrayList<Product>();
		
		for(BillingPosition billingPosition: billingPositions) {
			for(Consumption consumption: billingPosition.getConsumptions()) {
				Product product = consumption.getProduct();
				if(!usedProducts.contains(product)) usedProducts.add(product);
			}
		}
		Collections.sort(usedProducts);
		
		return usedProducts;
	}
	
	
	// Überschreibungen --------------------------------------------------------
	@Override
	public String toString() {
		return "Billing [" + title + ", " + date + "]";
	}
	
	@Override
	public int compareTo(Billing o) {
		return o.getDate().compareTo(getDate());
	}
	
}
