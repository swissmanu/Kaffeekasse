package net.msites.kaffeekasse.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.List;

import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.Language;
import net.msites.kaffeekasse.data.entities.Product;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ConsumptionSheetPDFFactory extends AbstractPdfFactory {
	
	private String subtitle;
	private Language language;
	private List<Consumer> consumers;
	private List<Product> products;
	private float[] columnWidths;
	
	public ConsumptionSheetPDFFactory(String subtitle, Language language, List<Consumer> consumers, List<Product> products, float[] columnWidths) {
		this.subtitle = subtitle;
		this.language = language;
		this.consumers = consumers;
		this.products = products;
		this.columnWidths = columnWidths;
	}
	
	
	// PDFFactory-Implementierung ----------------------------------------------
	@Override
	protected void buildPdf(File pdfFile) throws Exception {
		/* PDF erzeugen: */
		// Vorbereiten:
		Document document = new Document(PageSize.A4.rotate());
		PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
		document.open();
		
		// Titel erstellen:
		document.add(new Paragraph(20f, "Personal Consumptions", FONT_VERDANA_18));
		document.add(new Paragraph(16f, subtitle, FONT_VERDANA_12));
		
		// Tabelle erstellen:
		PdfPTable table = new PdfPTable(columnWidths);
		PdfPCell cell;
		table.setWidthPercentage(100f);
        table.setSpacingBefore(20f);
		
        cell = new PdfPCell();
        cell.setBorderWidthLeft(0f);
        cell.setBorderWidthTop(0f);
        table.addCell(cell);
        
        // Produkte hinzufügen:
        for(Product product: products) table.addCell(createProductHeaderCell(product, language));
        table.completeRow();
        table.setHeaderRows(1);
        
        // Konsumenten hinzufügen:
        for(int i = 0, l = consumers.size(); i < l; i++) {
        	Consumer consumer = consumers.get(i);
        	cell = new PdfPCell(new Phrase(consumer.getFullname(), FONT_VERDANA_10_BOLD));
        	cell.setVerticalAlignment(Element.ALIGN_CENTER);
        	
        	if(i % 2 == 0) cell.setBackgroundColor(COLOR_DARK);
        	else cell.setBackgroundColor(BaseColor.WHITE);
        	
        	cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        	table.addCell(cell);
        	
        	cell = new PdfPCell();
        	if(i % 2 == 0) cell.setBackgroundColor(COLOR_LIGHT);
        	else cell.setBackgroundColor(BaseColor.WHITE);
        	for(int j = 1, h = columnWidths.length; j < h; j++) {
        		table.addCell(cell);
        	}
        	
        	table.completeRow();
        }
		
        // Abschliessen:
		document.add(table);
		document.add(createKittyHint());
		document.close();
	}
	
	private PdfPCell createProductHeaderCell(Product product, Language language) {
		PdfPCell cell = new PdfPCell();
		
		cell.setBackgroundColor(COLOR_DARK);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
		
		String nameString;
		if(language == Language.GERMAN) nameString = product.getName();
		else nameString = product.getName_en();
		
		Phrase name = new Phrase(nameString + "\n", FONT_VERDANA_10_BOLD);
		Phrase price = new Phrase(NumberFormat.getCurrencyInstance().format(product.getPrice()), FONT_VERDANA_8);
		
		Paragraph p = new Paragraph();
		p.setAlignment(Element.ALIGN_CENTER);
		p.setLeading(12f);
		p.add(name);
		p.add(price);
		
		cell.addElement(p);
		
		return cell;
	}
	
	private Paragraph createKittyHint() {
		Chunk p = new Chunk("If you're not on the list, please use our cash kitty! Thank you.", FontFactory.getFont("Verdana", 10f, Font.BOLD, BaseColor.RED));
		Paragraph para = new Paragraph(30f, p);
		
		return para;
	}
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//		ArrayList<Consumer> consumers = new ArrayList<Consumer>();
//		ArrayList<Product> products = new ArrayList<Product>();
//		
//		consumers.add(new Consumer("Carmen", "Ackermann", "carmen.ackermann-goeldi@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Manuel", "Alabor", "manuel.alabor@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Renato", "Altenburger", "renato.altenburger@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Ravi", "Anthamola", "ravikumar.anthamola@credit-suisse.com", Language.ENGLISH, true));
//		consumers.add(new Consumer("Gerry", "Arnold", "gerhard.arnold@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Roger", "Begert", "roger.w.begert@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Patrick", "Belk", "patrick.belk@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Josip", "Budzaki", "josip.budzaki@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Mario", "Clavadetscher", "mario.clavadetscher@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Mario", "Eugster", "mario.eugster@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Hermann", "Gloor", "hermann.gloor@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Christoph", "Glutz", "christoph.glutz@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Satish", "Kessettiv", "satish.k.kessettiv@credit-suisse.com", Language.ENGLISH, true));
//		consumers.add(new Consumer("Corsin", "Marques", "corsin.marques@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Jeremias", "Messmer", "jeremias.messmer@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Sandro", "Muggli", "sandro.muggli@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Pius", "Pally", "pius.pally@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Daniel", "Rensch", "daniel.rensch@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Marcel", "Schnoz", "marcel.schnoz@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Lars", "Schröder", "lars.schroeder@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Stefan", "Ort", "stefan.ort@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Roger", "Steinmann", "roger.steinmann@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Lorenz", "Tanner", "lorenz.tanner@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Cindy", "Weber", "cindy.weber@credit-suisse.com", Language.GERMAN, true));
//		consumers.add(new Consumer("Madhu", "Yalamuri", "madhusudhan.yalamuri@credit-suisse.com", Language.ENGLISH, true));
//
//		products.add(new Product("Tee / Milch", 0.5d));
//		products.add(new Product("Kaffee", 0.8d));
//		products.add(new Product("Glacé", 1d));
//		products.add(new Product("Schokolade (klein)", 0.4d));
//		products.add(new Product("Schokolade (gross)", 0.9d));
//		products.add(new Product("Getränk", 1.2d));
//		products.add(new Product("RedBull", 1.7d));
////		products.add(new Product("Parkplatz", 5d));
//		
//		
//		float[] widths = {
//				3f,
//				2f,
//				3.5f,
//				1f,
//				1.6f,
//				1.6f,
//				3.5f,
//				2f
//		};
//		
//		
//		
//		new ConsumptionSheetPDFFactory().createSheet(consumers, products, widths);
//	}

}
