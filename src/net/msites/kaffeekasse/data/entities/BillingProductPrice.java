package net.msites.kaffeekasse.data.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Basic;

@Entity
@Table(name="BILLING_PRODUCT_PRICES")
/**
 * Ermöglicht es, in einer Abrechnung die Preise eines Produktes fix zu speichern.<br/>
 * Ändert der User nähmlich den Basispreis eines bestehenden Produktes, so darf
 * dieser in alten Abrechnungen nicht angepasst werden.
 * 
 * @author Manuel Alabor
 * @see Product
 * @see Billing
 */
public class BillingProductPrice extends DataEntity implements Comparable<BillingProductPrice> {
	
	@OneToOne(optional = false)
	private Product product;
	@Basic(optional = false)
	private Double price;
	@ManyToOne(optional=false)
	private Billing billing;
	
	
	// Konstruktoren -----------------------------------------------------------
	public BillingProductPrice() {}
	
	public BillingProductPrice(Billing billing, Product product) {
		this(billing, product, product.getPrice());
	}
	
	public BillingProductPrice(Billing billing, Product product, Double price) {
		this.product = product;
		this.price = price;
		this.billing = billing;
	}

	
	// Getter- & Setter-Methoden -----------------------------------------------
	public Product getProduct() {
		return product;
	}

	public Double getPrice() {
		return price;
	}
	
	public Billing getBilling() {
		return billing;
	}
	
	public void setProduct(Product product) {
		this.product = product;
		setChanged();
	}

	public void setPrice(Double price) {
		this.price = price;
		setChanged();
	}
	
	public void setBilling(Billing billing) {
		this.billing = billing;
	}
	
	
	// Hilfsmethoden -----------------------------------------------------------
	@Override
	public int compareTo(BillingProductPrice o) {
		int result = this.getProduct().getName().compareTo(o.getProduct().getName());
		if(result == 0) result = this.getPrice().compareTo(o.getPrice());
		
		return result;
	}
	
}
