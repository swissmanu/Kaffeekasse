package net.msites.kaffeekasse.data;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import net.msites.kaffeekasse.data.DataContainerEvent.Action;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.data.entities.Property;

/**
 * Der {@link DataContainer} stellt eine Vermittlungsstelle zwischen dem JPA
 * (Java Persistence API) und der restlichen Applikation dar.<br/>
 * Er selber ist als Singleton implementiert und kann über die statische Methode
 * {@link #getInstance()} abgerufen werden.<br/>
 * <br/>
 * Per {@link #getDataContainerObservable()} kann ein {@link Observable}
 * angefordert werden, welches einem {@link Observer} das beobachten des
 * {@link DataContainer}'s auf Änderungen ermöglicht.<br/>
 * Bei einer versendeten Benachrichtigung wird jeweils ein {@link DataContainerEvent}
 * verwendet.
 * 
 * @author Manuel Alabor
 */
public class DataContainer {
	
	private static DataContainer instance = null;
	private EntityManagerFactory factory = null;
	
	private final DataContainerObservable dataContainerObservable = new DataContainerObservable();

	public final static String PROPERTY_KEY_BILLING_PRODUCT_COLUMNS = "billing.productcolumns";
	public final static String PROPERTY_KEY_CONSUMPTATION_SHEET_LANGUAGE = "pdf.consumption_sheet.language";
	public final static String PROPERTY_KEY_CONSUMPTATION_SHEET_COLUMNS = "pdf.consumption_sheet_columns";
	public final static String PROPERTY_KEY_EMAIL_SUBJECT_GERMAN = "email.german.subject";
	public final static String PROPERTY_KEY_EMAIL_TEXT_GERMAN = "email.german.text";
	public final static String PROPERTY_KEY_EMAIL_SUBJECT_ENGLISH = "email.english.subject";
	public final static String PROPERTY_KEY_EMAIL_TEXT_ENGLISH = "email.english.text";
	
	
	// Singleton ---------------------------------------------------------------
	public static DataContainer getInstance() {
		if(instance == null) {
			instance = new DataContainer();
		}
		return instance;
	}
	
	
	// Konstruktoren -----------------------------------------------------------
	private DataContainer() {
		initDataContainer();
	}
	
	
	// DataContainer -----------------------------------------------------------
	public void initDataContainer() {
		if(factory == null) factory = Persistence.createEntityManagerFactory("kaffeekasse");
	}
	
	public void cleanUpDataContainer() {
		factory.close();
		factory = null;
	}
	
	
	// Consumers ---------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public List<Consumer> getConsumers() {
		EntityManager em = getEntityManager();
		Query q = em.createQuery("select c from Consumer c order by c.lastname, c.firstname");
		List<Consumer> consumers = q.getResultList();
		em.close();
		
		return consumers;
	}
	
	// Products ----------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public List<Product> getProducts() {
		EntityManager em = getEntityManager();
		Query q = em.createQuery("select p from Product p order by p.name");
		List<Product> products = q.getResultList();
		em.close();
		
		return products;
	}
		
	// Billings ----------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public List<Billing> getBillings() {
		EntityManager em = getEntityManager();
		Query q = em.createQuery("select b from Billing b order by b.date desc, b.title");
		List<Billing> billings = q.getResultList();
		em.close();
		
		return billings;
	}
	
	/**
	 * Prüft, ob der Konsument in irgend einer Abrechnung bereits verwendet wird.
	 * 
	 * @param consumer
	 * @return
	 */
	public boolean isUsedInBillings(Consumer consumer) {
		boolean result = false;
		EntityManager em = getEntityManager();
		
		Query q = em.createQuery("select count(b.id) from BillingPosition b where b.consumer.id=:consumer");
		q.setParameter("consumer", consumer.getId());
		
		Number n = (Number)q.getSingleResult();
		result = n.intValue() > 0;
		
		return result;
	}
	
	/**
	 * Prüft, ob das Produkt in irgend einer Abrechnung bereits verwendet wird.
	 * 
	 * @param product
	 * @return
	 */
	public boolean isUsedInBillings(Product product) {
		boolean result = false;
		EntityManager em = getEntityManager();
		
		Query q = em.createQuery("select count(c.id) from Consumption c where c.product.id=:product");
		q.setParameter("product", product.getId());
		
		Number n = (Number)q.getSingleResult();
		result = n.intValue() > 0;
		
		return result;
	}
	
	// Properties --------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public Property getProperty(String key) {
		Property result = null;
		
		EntityManager em = getEntityManager();
		Query q = em.createQuery("select p from Property p where p.key=:key");
		q.setParameter("key", key);
		List<Property> properties = q.getResultList();
		em.close();
		
		for(Property property: properties) {
			if(property.getKey().equals(key)) {
				result = property;
				break;
			}
		}
		
		return result;
	}
	
	public void putProperty(Property property) {
		EntityManager em = getEntityManager();
		
		em.getTransaction().begin();
		em.merge(property);
		em.getTransaction().commit();
		
		em.close();
	}
	
	// Datenmanipulation -------------------------------------------------------
	/**
	 * Speichert eine {@link DataEntity} in den persistenten Speicher.<br/>
	 * Hat die {@link DataEntity} bereits einen ID definiert, wird ein update
	 * durchgeführt. Ansonsten wird die {@link DataEntity} mit einem insert
	 * gespeichert.
	 * 
	 * @param dataEntity
	 */
	public void saveDataEntity(DataEntity dataEntity) {
		DataContainerEvent.Action action;
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		
		if(dataEntity.getId() != null) {
			em.merge(dataEntity);
			action = Action.UPDATE;
		} else{
			em.persist(dataEntity);
			action = Action.INSERT;
		}
		dataEntity.setChanged(false);
		
		em.getTransaction().commit();
		
		/* Observers informieren: */
		dataContainerObservable.setChanged();
		dataContainerObservable.notifyObservers(
				new DataContainerEvent(action, dataEntity)
				);
	}
	
	/**
	 * Löscht eine {@link DataEntity} im persistenten Speicher.
	 * 
	 * @param dataEntity
	 */
	public void deleteDataEntity(DataEntity dataEntity) {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		
		dataEntity = em.merge(dataEntity);
		em.remove(dataEntity);
		
		em.getTransaction().commit();
		
		/* Observers informieren: */
		dataContainerObservable.setChanged();
		dataContainerObservable.notifyObservers(
				new DataContainerEvent(DataContainerEvent.Action.DELETE, dataEntity)
				);
	}
	
	
	// Hilfsmethoden -----------------------------------------------------------
	/**
	 * Liefert einen {@link EntityManager}.<br/>
	 * ACHTUNG! Der {@link EntityManager} muss "von hand" geschlossen werden!
	 * 
	 * @return {@link EntityManager}
	 */
	private EntityManager getEntityManager() {
		EntityManager em = factory.createEntityManager();
		return em;
	}
	
	
	// Delegates ---------------------------------------------------------------	
	/**
	 * Liefert ein {@link Observable}, welches das Beobachten des {@link DataContainer}'s
	 * ermöglicht.
	 * 
	 * @return {@link Observable}
	 */
	public Observable getDataContainerObservable() {
		return dataContainerObservable;
	}
	
	private class DataContainerObservable extends Observable {
		public void setChanged() {
			super.setChanged();
		}
	}
	
}
