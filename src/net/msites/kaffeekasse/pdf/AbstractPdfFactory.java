package net.msites.kaffeekasse.pdf;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.msites.kaffeekasse.ui.dialog.WaitDialog;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;

/**
 * Vorlage für Factory-Klassen zur Erstellung von PDF-Dokumenten.
 * 
 * @author Manuel Alabor
 */
public abstract class AbstractPdfFactory {

	public final static String FONT_NAME = "Tahoma";
	
	public final static Font FONT_VERDANA_8 = FontFactory.getFont(FONT_NAME, 8f);
	public final static Font FONT_VERDANA_10 = FontFactory.getFont(FONT_NAME, 10f);
	public final static Font FONT_VERDANA_10_BOLD = FontFactory.getFont(FONT_NAME, 10f, Font.BOLD);
	public final static Font FONT_VERDANA_12 = FontFactory.getFont(FONT_NAME, 12f);
	public final static Font FONT_VERDANA_18 = FontFactory.getFont(FONT_NAME, 18f, Font.BOLD);
	
	public final static BaseColor COLOR_DARK = new BaseColor(170,161,154);
	public final static BaseColor COLOR_MIDDLE = new BaseColor(200,193,188);
	public final static BaseColor COLOR_LIGHT = new BaseColor(227,223,219);
	
	/**
	 * Muss von der ableitenden Factory implementiert werden. Dient zum erstellen
	 * des effetkiven PDF's und wird von {@link #buildPDF()} aufgerufen.
	 * 
	 * @param pdfFile
	 */
	protected abstract void buildPdf(File pdfFile) throws Exception;
	
	/**
	 * Gleicher Ablauf wie {@link #createPdf(Window, File)}, jedoch wird vorab
	 * die Möglichkeit geboten, in einem {@link JFileChooser} die Zieldatei für
	 * das PDF auszuwählen.
	 * 
	 * @param guiOwner
	 * @param initialFilename
	 * @return
	 * @see #createPdf(Window, File)
	 */
	public File createPdf(Window guiOwner, String initialFilename) {
		File pdfFile = selectTargetPdfFile(guiOwner, initialFilename);
		
		if(pdfFile != null) pdfFile = createPdf(guiOwner, pdfFile);
		
		return pdfFile;
	}
	
	/**
	 * Erstellt die Datei <code>pdfFile</code> und schreibt in diese das generierte
	 * PDF.<br/>
	 * <code>guiOwner</code> wird benötigt, damit das GUI der Applikation per
	 * {@link WaitDialog} während der PDF-Generierung blockiert werden kann.
	 * 
	 * @param guiOwner
	 * @param pdfFile
	 * @return
	 */
	public File createPdf(final Window guiOwner, final File pdfFile) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					buildPdf(pdfFile);
					
					int result = JOptionPane.showConfirmDialog(
							guiOwner,
							"Das PDF wurde erfolgreich erstellt!\n" +
							"Möchten Sie es jetzt öffnen?",
							"PDF erstellen",
							JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.YES_OPTION) {
						Runtime.getRuntime().exec(
								"rundll32 url.dll,FileProtocolHandler \"" +
								pdfFile.getAbsolutePath() + "\""
								);	
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							guiOwner,
							"Das PDF konnte nicht erstellt werden!",
							"PDF erstellen",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		
		WaitDialog waitDialog = new WaitDialog(
				guiOwner,
				"PDF erstellen",
				"PDF wird erstellt... Bitte warten.",
				task);
		waitDialog.setVisible(true);
		
		return pdfFile;
	}
	
	/**
	 * Zeigt einen {@link JFileChooser} zur speicherung eines neuen PDF's an.
	 * 
	 * @param guiOwner
	 * @param initialFilename
	 * @return
	 */
	private File selectTargetPdfFile(Window guiOwner, String initialFilename) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF Dateien", "pdf"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("PDF speichern unter");
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setSelectedFile(new File(initialFilename));
		int result = fileChooser.showSaveDialog(guiOwner);
		
		File resultFile = null;
		if(result == JFileChooser.APPROVE_OPTION) {
			resultFile = fileChooser.getSelectedFile();
		}
		
		return resultFile;
	}
	
}
