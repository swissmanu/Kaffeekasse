package net.msites.kaffeekasse.data.entities;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Basic;


@Entity
@Table(name="PRODUCTS")
public class Product extends DataEntity implements Comparable<Product> {

	@Basic(optional = false)
	private String name;
	@Basic(optional = false)
	private String name_en;
	@Basic(optional = false)
	private Double price = new Double(0d);
	
	
	// Konstruktoren -----------------------------------------------------------
	public Product() {}
	
	public Product(String name, String name_en, Double price) {
		this.name = name;
		this.name_en = name_en;
		this.price = price;
	}

	// Getter- & Setter-Methoden -----------------------------------------------
	public String getName() {
		return name;
	}
	
	public String getName_en() {
		return name_en;
	}

	public Double getPrice() {
		return price;
	}

	public void setName(String name) {
		this.name = name;
		this.setChanged();
	}
	
	public void setName_en(String nameEn) {
		name_en = nameEn;
		this.setChanged();
	}

	public void setPrice(Double price) {
		this.price = price;
		this.setChanged();
	}
	
	
	// Überschreibungen --------------------------------------------------------
	@Override
	public int compareTo(Product o) {
		int compare = getName().compareTo(o.getName());
		if(compare == 0) compare = Double.compare(getPrice(), o.getPrice());
		
		return compare;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if(obj instanceof Product) {
			Product o = (Product)obj;
			
			equals = o.getName().equals(this.getName())
				&& o.getPrice().equals(this.getPrice());
		}
		
		return equals;
	}
	
	@Override
	public String toString() {
		return "Product [" + name + ", " + price + "]";
	}
	
}
