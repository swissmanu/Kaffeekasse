package net.msites.kaffeekasse.pdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import net.msites.kaffeekasse.ui.dialog.chartdialog.Chart;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class ChartsPDFFactory extends AbstractPdfFactory {
	
	private final List<Chart> charts;
	
	private final static int CHART_IMAGE_WIDTH = 530;
	private final static int CHART_IMAGE_HEIGHT = 350;
	
	public ChartsPDFFactory(List<Chart> charts) {
		this.charts = charts;
	}
	
	
	// PDFFactory-Implementierung ----------------------------------------------
	@Override
	protected void buildPdf(File pdfFile) throws Exception {		
		/* PDF erzeugen: */
		// Vorbereiten:
		Document document = new Document(PageSize.A4);
		PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
		document.open();
		
		// Titel erstellen:
		document.add(new Paragraph(20f, "Auswertungen Kaffeekasse", FONT_VERDANA_18));
		
		int chartCount = 0;
		for(Chart chart: charts) {
			if(chartCount > 1 && chartCount % 2 == 0) document.newPage();
			chartCount++;
			
			// Titel:
			document.add(new Paragraph(16f, chart.getName(), FONT_VERDANA_12));
			
			// Chart:
			BufferedImage chartImage = new BufferedImage(CHART_IMAGE_WIDTH,CHART_IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
			
			Graphics2D g2 = (Graphics2D)chartImage.getGraphics();
			g2.setColor(Color.white);
			g2.fillRect(0, 0, CHART_IMAGE_WIDTH, CHART_IMAGE_HEIGHT);
			chart.getChart().draw(g2, new Rectangle2D.Double(0,0,CHART_IMAGE_WIDTH,CHART_IMAGE_HEIGHT));
			g2.dispose();
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ImageIO.write(chartImage, "png", bytes);
			
			Image pdfImage = Image.getInstance(bytes.toByteArray());
			document.add(pdfImage);
			
			chartImage = null;
			bytes = null;
		}
        
        // Dokument finalisieren:
		document.close();
	}
	
	
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//
//		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//		DataContainer container = DataContainer.getInstance();
//		for(Billing billing: container.getBillings()) {
//			double total = 0;
//			
//			for(BillingPosition position: billing.getBillingPositions()) {
//				total += position.calculateTotalConsumptions();
//				total += position.calculateTotalOtherCosts();
//			}
//			
//			dataset.addValue(total, "Total", billing.getDate());
//			
//		}
//		
//		final JFreeChart chart = ChartFactory.createBarChart(
//				"",
//				"Datum",
//				"Einnahmen SFr.",
//				dataset,
//				PlotOrientation.VERTICAL,
//				false,
//				true,
//				true);
//		chart.setAntiAlias(true);
//		chart.setTextAntiAlias(true);
//		
//		
//		Map<String, JFreeChart> charts = new HashMap<String, JFreeChart>();
//		charts.put("Total pro Abrechnung", chart);
//		charts.put("Total pro Abrechnung 1", chart);
//		charts.put("Total pro Abrechnung 2", chart);
//		charts.put("Total pro Abrechnung 3", chart);
//		charts.put("Total pro Abrechnung 4", chart);
//		charts.put("Total pro Abrechnung 5", chart);
//		
//		ChartsPDFFactory.createDocument(new File("charts.pdf"), charts);
//	}

}
