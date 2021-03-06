package net.msites.kaffeekasse.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.BillingProductPrice;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.Consumption;
import net.msites.kaffeekasse.data.entities.Product;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class BillingPDFFactory extends AbstractPdfFactory {
	
	private Billing billing;
	private final static DecimalFormat AMOUNT_FORMAT = new DecimalFormat("0.#");
	private final static NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
	
	public BillingPDFFactory(Billing billing) {
		this.billing = billing;
	}
	
	
	// PDFFactory-Implementierung ----------------------------------------------
	@Override
	protected void buildPdf(File pdfFile) throws Exception {
		/* Daten sammeln: */
		List<Product> products = getPresentProductsAsList(billing);
		List<Consumer> consumers = getPresentConsumersAsList(billing);
		
		float[] columnWidths = new float[products.size() + 4];
		columnWidths[0] = 3f;
		for(int i = 0, l = products.size(); i < l; i++) columnWidths[i+1] = 0.5f;
		for(int i = products.size()+1, l = columnWidths.length; i < l; i++) columnWidths[i] = 1.5f;
		
		/* PDF erzeugen: */
		// Vorbereiten:
		Document document = new Document(PageSize.A4);
		PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
		document.open();
		
		// Titel erstellen:
		document.add(new Paragraph(20f, "Abrechnung", FONT_VERDANA_18));
		document.add(new Paragraph(16f, billing.getTitle(), FONT_VERDANA_12));
		
		// Tabelle erstellen:
		PdfPTable table = new PdfPTable(columnWidths);
		PdfPCell cell;
		table.setWidthPercentage(100f);
        table.setSpacingBefore(20f);
		
        cell = new PdfPCell();
        cell.setBorderWidthLeft(0f);
        cell.setBorderWidthTop(0f);
        table.addCell(cell);
        
        // Produkte hinzuf�gen:
        for(Product product: products) {
        	table.addCell(createVerticalTitleCell(product.getName()));
        }
        
        table.addCell(createVerticalTitleCell("Andere Kosten"));
        table.addCell(createVerticalTitleCell("Total"));
        table.addCell(createVerticalTitleCell("Bezahlt"));
        
        // Konsumenten und zugeh�rige BillingPosition:
        double billingTotal = 0d;
        int rowIndex = 0;
        for(Consumer consumer: consumers) {
        	BillingPosition billingPosition = billing.getBillingPosition(consumer);
        	table.addCell(createTitleCell(consumer.getFullname()));
        	
        	BaseColor backgroundColor = BaseColor.WHITE;
        	if(rowIndex % 2 != 0) backgroundColor = COLOR_LIGHT;
        	
        	for(Product product: products) {
        		Consumption consumption = billingPosition.getConsumption(product);
        		if(consumption != null) {
        			cell = new PdfPCell(new Phrase(AMOUNT_FORMAT.format(consumption.getAmount()),FONT_VERDANA_10));
        			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        			cell.setBackgroundColor(backgroundColor);
        			table.addCell(cell);
        		} else {
        			cell = new PdfPCell(new Phrase(""));
        			cell.setBackgroundColor(backgroundColor);
        			table.addCell(cell);
        		}
        	}
        	
        	// Andere Kosten:
        	double totalOtherCosts = billingPosition.calculateTotalOtherCosts();
        	cell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(totalOtherCosts), FONT_VERDANA_10));
        	cell.setBackgroundColor(backgroundColor);
        	cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        	table.addCell(cell);
        	
        	// Total:
        	double total = totalOtherCosts + billingPosition.calculateTotalConsumptions();
        	billingTotal += total;
        	cell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(total), FONT_VERDANA_10_BOLD));
        	cell.setBackgroundColor(COLOR_MIDDLE);
        	cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        	table.addCell(cell);

        	// Bezahlt:
        	String paid = "";
        	if(billingPosition.isPaid()) paid = "x";
			cell = new PdfPCell(new Phrase(paid));
			cell.setBackgroundColor(backgroundColor);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
        	
        	table.completeRow();
        	rowIndex++;
        }
        document.add(table);
        
        // Total: 
        document.add(new Paragraph(
        		20f,
        		"Abrechnungstotal",
        		FONT_VERDANA_10_BOLD));
        document.add(new Paragraph(
        		CURRENCY_FORMAT.format(billingTotal),
        		FONT_VERDANA_10));
        
        // Produktepreise:
        document.add(new Paragraph(
        		20f,
        		"Verwendete Produktpreise",
        		FONT_VERDANA_10_BOLD));
        
        List<BillingProductPrice> prices = billing.getBillingProductPrices();
        Collections.sort(prices);
        for(BillingProductPrice price: prices) {
        	if(products.contains(price.getProduct())) {
	            document.add(new Paragraph(
	            		price.getProduct().getName() + ": " + CURRENCY_FORMAT.format(price.getPrice()),
	            		FONT_VERDANA_10));        	
        	}
        }
        
        // Disclaimer:
        document.add(new Paragraph(
        		20f,
        		"Diese Abrechnung enth�lt nur die Konsumenten und Produkte, " +
        		"welche w�hrend der entsprechenden Periode aktiv waren resp. " +
        		"bezogen wurden.",
        		FONT_VERDANA_8));
        
        // Dokument finalisieren:
		document.close();
	}
	
	/**
	 * Liefert alle Produkte, welche in einer Abrechnung vorhanden sind als
	 * <code>List</code>.
	 * 
	 * @param billing
	 * @return
	 */
	private List<Product> getPresentProductsAsList(Billing billing) {
		List<Product> products = new ArrayList<Product>();
		
		for(BillingPosition billingPosition: billing.getBillingPositions()) {
			for(Consumption consumption: billingPosition.getConsumptions()) {
				if(!products.contains(consumption.getProduct())) {
					products.add(consumption.getProduct());
				}
			}
		}
		
		Collections.sort(products);
		return products;
	}
	
	/**
	 * Liefert alle Konsumenten, welche in einer Abrechnung vorhanden sind als
	 * <code>List</code>.
	 * 
	 * @param billing
	 * @return
	 */
	private List<Consumer> getPresentConsumersAsList(Billing billing) {
		List<Consumer> consumers = new ArrayList<Consumer>();
		
		for(BillingPosition billingPosition: billing.getBillingPositions()) {
			if(!consumers.contains(billingPosition.getConsumer())) {
				consumers.add(billingPosition.getConsumer());
			}
		}
		
		Collections.sort(consumers);
		return consumers;
	}
	
	private PdfPCell createTitleCell(String text) {
    	PdfPCell cell = new PdfPCell(new Phrase(text, FONT_VERDANA_10_BOLD));
    	cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    	
    	cell.setBackgroundColor(COLOR_DARK);
    	
    	return cell;
	}
	
	private PdfPCell createVerticalTitleCell(String text) {
    	PdfPCell cell = new PdfPCell(new Phrase(text, FONT_VERDANA_10_BOLD));
    	cell.setRotation(90);
    	cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    	
    	cell.setBackgroundColor(COLOR_DARK);
    	
    	return cell;
	}

}
