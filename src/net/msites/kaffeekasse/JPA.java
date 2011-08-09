package net.msites.kaffeekasse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.BillingProductPrice;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.Consumption;
import net.msites.kaffeekasse.data.entities.Language;
import net.msites.kaffeekasse.data.entities.Product;

public class JPA {

	/**
	 * @param args
	 */
	@SuppressWarnings({"unchecked","unused"})
	public static void main(String[] args) {
		// Create the EntityManager
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("kaffeekasse");
		EntityManager em = factory.createEntityManager();
		
		em.getTransaction().begin();
		em.persist(new Consumer("Carmen", "Ackermann", "carmen.ackermann-goeldi@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Manuel", "Alabor", "manuel.alabor@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Renato", "Altenburger", "renato.altenburger@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Ravi", "Anthamola", "ravikumar.anthamola@credit-suisse.com", Language.ENGLISH,true ));
		em.persist(new Consumer("Gerry", "Arnold", "gerhard.arnold@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Roger", "Begert", "roger.w.begert@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Patrick", "Belk", "patrick.belk@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Josip", "Budzaki", "josip.budzaki@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Mario", "Clavadetscher", "mario.clavadetscher@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Mario", "Eugster", "mario.eugster@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Hermann", "Gloor", "hermann.gloor@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Christoph", "Glutz", "christoph.glutz@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Satish", "Kessettiv", "satish.k.kessettiv@credit-suisse.com", Language.ENGLISH,true ));
		em.persist(new Consumer("Corsin", "Marques", "corsin.marques@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Jeremias", "Messmer", "jeremias.messmer@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Sandro", "Muggli", "sandro.muggli@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Pius", "Pally", "pius.pally@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Daniel", "Rensch", "daniel.rensch@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Marcel", "Schnoz", "marcel.schnoz@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Lars", "Schröder", "lars.schroeder@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Stefan", "Ort", "stefan.ort@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Roger", "Steinmann", "roger.steinmann@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Lorenz", "Tanner", "lorenz.tanner@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Cindy", "Weber", "cindy.weber@credit-suisse.com", Language.GERMAN,true ));
		em.persist(new Consumer("Madhusudhan", "Yalamuri", "madhusudhan.yalamuri@credit-suisse.com", Language.ENGLISH,true ));
		em.persist(new Product("Tee / Milch", "Tea / Milk", 0.5d));
		em.persist(new Product("Kaffee", "Coffee", 0.8d));
		em.persist(new Product("Glacé", "Ice Cream", 1d));
		em.persist(new Product("Schokolade (klein)", "Chocolate (small)", 0.4d));
		em.persist(new Product("Schokolade (gross)", "Chocolate (large", 0.9d));
		em.persist(new Product("Getränk", "Beverage", 1.2d));
		em.persist(new Product("RedBull", "RedBull", 1.7d));
		em.persist(new Product("Parkplatz", "Parking", 5d));
		em.getTransaction().commit();
		
		Query q = em.createQuery("select c from Consumer c");
		List<Consumer> consumers = q.getResultList();
		q = em.createQuery("select p from Product p");
		List<Product> products = q.getResultList();
		q = em.createQuery("select b from Billing b");
		List<Billing> billings = q.getResultList();
		
		
		
		Billing billing = new Billing();
		billing.setTitle("Testabrechnung");
		billing.setDate(new Date());
		
		List<BillingProductPrice> productPrices = new ArrayList<BillingProductPrice>();
		for(Product p: products) productPrices.add(new BillingProductPrice(billing, p));
		billing.setBillingProductPrices(productPrices);
		
		List<BillingPosition> billingPositions = new ArrayList<BillingPosition>();
		BillingPosition pos = new BillingPosition();
		pos.setBilling(billing);
		pos.setConsumer(consumers.get(0));
		pos.setPaid(true);
		
		List<Consumption> consumptions = new ArrayList<Consumption>();
		consumptions.add(new Consumption(pos, products.get(0), 1d));
		pos.setConsumptions(consumptions);
		
		billingPositions.add(pos);
		billing.setBillingPositions(billingPositions);
		
		
		
		em.getTransaction().begin();
		em.persist(billing);
		em.getTransaction().commit();
		
		em.close();
		factory.close();
	}

}
