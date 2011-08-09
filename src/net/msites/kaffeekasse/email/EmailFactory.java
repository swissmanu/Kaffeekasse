package net.msites.kaffeekasse.email;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.Consumption;
import net.msites.kaffeekasse.data.entities.Language;
import net.msites.kaffeekasse.data.entities.OtherCost;

/**
 * Generiert aus einer Abrechnung für jeden Konsumenten, welcher Bezüge gemacht
 * hat, eine detailierte E-Mail in der entsprechenden Sprache.
 * 
 * @author Manuel Alabor
 */
public class EmailFactory {

	private final static DecimalFormat AMOUNT_FORMAT = new DecimalFormat("0.#");
	
	public final static String PATTERN_NAME = "%name%";
	public final static String PATTERN_CONSUMPTIONS = "%consumptions%";
	public final static String PATTERN_OTHERCOSTS = "%othercosts%";
	public final static String PATTERN_TOTAL = "%total%";
	
	public static List<Email> createEmails(Billing billing, Map<Language, String> subjectTranslations, Map<Language, String> textTranslations) {
		List<Email> emails = new ArrayList<Email>(billing.getBillingPositions().size());
		
		for(BillingPosition billingPosition: billing.getBillingPositions()) {
			List<Consumption> consumptions = billingPosition.getConsumptions();
			List<OtherCost> otherCosts = billingPosition.getOtherCosts();
			
			if(consumptions.size() > 0 || otherCosts.size() > 0) {
				String subject = subjectTranslations.get(billingPosition.getConsumer().getLanguage());
				String text = textTranslations.get(billingPosition.getConsumer().getLanguage());
				emails.add(createEmail(billingPosition, billing, subject, text));
			}
		}
		
		return emails;
	}
	
	private static Email createEmail(BillingPosition billingPosition, Billing billing, String subjectTemplate, String textTemplate) {
		/* Vorbereiten: */
		// Daten holen:
		Consumer consumer = billingPosition.getConsumer();
		List<Consumption> consumptions = billingPosition.getConsumptions();
		List<OtherCost> otherCosts = billingPosition.getOtherCosts();
		double total = billingPosition.calculateTotalConsumptions()
					 + billingPosition.calculateTotalOtherCosts();
		
		Collections.sort(consumptions);
		
		// Konsumations-Liste erstellen:
		StringBuffer consumptionsListString = new StringBuffer();
		for(Consumption consumption: consumptions) {
			double amount = consumption.getAmount()
				* billing.getBillingProductPrice(consumption.getProduct()).getPrice();
			
			consumptionsListString.append(" - ");
			consumptionsListString.append(AMOUNT_FORMAT.format(consumption.getAmount()));
			consumptionsListString.append("x ");
			if(consumer.getLanguage() == Language.GERMAN)
				consumptionsListString.append(consumption.getProduct().getName());
			else 
				consumptionsListString.append(consumption.getProduct().getName_en());
			consumptionsListString.append(": ");
			consumptionsListString.append(NumberFormat.getCurrencyInstance().format(amount));
			consumptionsListString.append("\n");
		}
		
		// "Andere Kosten" der Liste anhängen:
		for(OtherCost otherCost: otherCosts) {
			consumptionsListString.append(" - ");
			consumptionsListString.append(otherCost.getText());
			consumptionsListString.append(": ");
			consumptionsListString.append(NumberFormat.getCurrencyInstance().format(otherCost.getAmount()));
			consumptionsListString.append("\n");
		}
		
		/* Einsetzen: */
		String subject = subjectTemplate;
		
		String text = textTemplate.replaceAll(PATTERN_NAME, consumer.getFirstname());
		text = text.replaceAll(PATTERN_TOTAL, NumberFormat.getCurrencyInstance().format(total));
		text = text.replaceAll(PATTERN_CONSUMPTIONS, consumptionsListString.toString());
		
		return new Email(billingPosition.getConsumer(), subject, text);
	}
	
}
