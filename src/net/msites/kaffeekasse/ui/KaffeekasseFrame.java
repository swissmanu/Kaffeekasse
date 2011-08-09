package net.msites.kaffeekasse.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.backup.DatabaseBackup;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.BillingProductPrice;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.data.entities.OtherCost;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.data.entities.Property;
import net.msites.kaffeekasse.pdf.BillingPDFFactory;
import net.msites.kaffeekasse.ui.components.tabs.AbstractEditableTab;
import net.msites.kaffeekasse.ui.components.tabs.BillingTab;
import net.msites.kaffeekasse.ui.components.tabs.WelcomeTab;
import net.msites.kaffeekasse.ui.dialog.BillingDialog;
import net.msites.kaffeekasse.ui.dialog.ConsumerDialog;
import net.msites.kaffeekasse.ui.dialog.ConsumptionSheetDialog;
import net.msites.kaffeekasse.ui.dialog.DialogResult;
import net.msites.kaffeekasse.ui.dialog.EmailDialog;
import net.msites.kaffeekasse.ui.dialog.ProductColumnsDialog;
import net.msites.kaffeekasse.ui.dialog.ProductDialog;
import net.msites.kaffeekasse.ui.dialog.WaitDialog;
import net.msites.kaffeekasse.ui.dialog.chartdialog.ChartsDialog;

/**
 * Hauptfenster
 *
 * @author Manuel Alabor
 */
public class KaffeekasseFrame extends JFrame {

	private final JTabbedPane tbpTabs = new JTabbedPane();
	
	private final Map<ActionKey, Action> actions = new HashMap<ActionKey, Action>();
	
	private enum ActionKey {
		NEW_BILLING, NEW_BILLING_BASED_ON_CURRENT, OPEN_BILLING, CLOSE_TAB,
		SAVE, PDF_CONSUMPTION_SHEET, PDF_BILLING, EMAIL, PRODUCT_COLUMNS,
		EDIT_CONSUMERS, EDIT_PRODUCTS, CHARTS, BACKUP, RESTORE_BACKUP, QUIT
	}
	
	public KaffeekasseFrame() {
		super("Kaffeekasse");
		
		initActions();
		
		setIconImage(GUIImageManager.getInstance().getImage("book.png"));
		setSize(800,600);
		GUIHelper.centerOnScreen(this);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actions.get(ActionKey.QUIT).actionPerformed(new ActionEvent(this,0,"windowClosed"));
			}
		});
		
		setContentPane(buildGui());
		setJMenuBar(buildMenuBar());
		
		/* Initiales caching: */
		Runnable task = new Runnable() {
			@Override
			public void run() {
				DataContainer.getInstance().getConsumers();
			}
		};
		WaitDialog waitDialog = new WaitDialog(
				this,
				"Kaffeekasse",
				"Datenbank wird gestartet... Bitte warten.",
				task);
		waitDialog.setVisible(true);
	}
	
	
	// GUI-Erstellung ----------------------------------------------------------
	private void initActions() {
		actions.put(ActionKey.NEW_BILLING, new NewBillingAction());
		actions.put(ActionKey.NEW_BILLING_BASED_ON_CURRENT, new NewBillingBasedOnCurrentAction());
		actions.put(ActionKey.OPEN_BILLING, new OpenBillingAction());
		actions.put(ActionKey.SAVE, new SaveAction());
		actions.put(ActionKey.CLOSE_TAB, new CloseTabAction());
		actions.put(ActionKey.PDF_BILLING, new PDFBillingSheetAction());
		actions.put(ActionKey.PDF_CONSUMPTION_SHEET, new PDFConsumptationSheetAction());
		actions.put(ActionKey.EMAIL, new EmailAction());
		actions.put(ActionKey.EDIT_CONSUMERS, new EditConsumersAction());
		actions.put(ActionKey.EDIT_PRODUCTS, new EditProductsAction());
		actions.put(ActionKey.PRODUCT_COLUMNS, new ProductColumnsAction());
		actions.put(ActionKey.CHARTS, new ChartsAction());
		actions.put(ActionKey.BACKUP, new BackupAction());
		actions.put(ActionKey.RESTORE_BACKUP, new RestoreBackupAction());
		actions.put(ActionKey.QUIT, new QuitAction());
	}
	
	private JComponent buildGui() {
		JPanel gui = new JPanel(new BorderLayout());
		initGui();
		
		tbpTabs.setFocusable(false);
		tbpTabs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		gui.add(buildToolbar(), BorderLayout.NORTH);
		gui.add(tbpTabs, BorderLayout.CENTER);
		
		return gui;
	}
	
	private void initGui() {
		/* Tabs: */
		// Willkommens-Tab erstellen:
		addTab("Willkommen!", new WelcomeTab(), false);
		
		// Einen MausListener installieren, welcher den aktuellen Tab per
		// Doppelklick schliessen kann.
		tbpTabs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getClickCount() == 2) {
					AbstractEditableTab tab = getSelectedEditableTab();
					if(tab.beforeCloseTab()) {
						tbpTabs.remove(tab);
					}
				}
			}
		});
		
		// Sicherstellen, dass nur gespeichert werden kann, wenn der aktuelle
		// Tab auch Daten zum speichern hat.
		tbpTabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				boolean enabled = getSelectedEditableTab() instanceof BillingTab;
				actions.get(ActionKey.NEW_BILLING_BASED_ON_CURRENT).setEnabled(enabled);
				actions.get(ActionKey.SAVE).setEnabled(enabled);
				actions.get(ActionKey.CLOSE_TAB).setEnabled(enabled);
				actions.get(ActionKey.PDF_BILLING).setEnabled(enabled);
				actions.get(ActionKey.EMAIL).setEnabled(enabled);
			}
		});
		
		/* Actions: */
		// Der erste Tab ist immer der Willkommens-Tab. Hier kann nicht
		// gespeichert werden. Demnach die entsprechende Action deaktivieren.
		actions.get(ActionKey.SAVE).setEnabled(false);
		actions.get(ActionKey.CLOSE_TAB).setEnabled(false);
		actions.get(ActionKey.NEW_BILLING_BASED_ON_CURRENT).setEnabled(false);
		actions.get(ActionKey.PDF_BILLING).setEnabled(false);
		actions.get(ActionKey.EMAIL).setEnabled(false);
	}
	
	private JMenuBar buildMenuBar() {
		JMenuBar menu = new JMenuBar();
		
		JMenu mnuFile = new JMenu("Datei");
		mnuFile.add(actions.get(ActionKey.NEW_BILLING));
		mnuFile.add(actions.get(ActionKey.NEW_BILLING_BASED_ON_CURRENT));
		mnuFile.add(actions.get(ActionKey.OPEN_BILLING));
		mnuFile.addSeparator();
		mnuFile.add(actions.get(ActionKey.CLOSE_TAB));
		mnuFile.addSeparator();
		mnuFile.add(actions.get(ActionKey.SAVE));
		mnuFile.addSeparator();
		
		JMenu mnuPDF = new JMenu("PDF erstellen");
		mnuPDF.add(actions.get(ActionKey.PDF_BILLING));
		mnuPDF.add(actions.get(ActionKey.PDF_CONSUMPTION_SHEET));
		
		mnuFile.add(mnuPDF);
		mnuFile.add(actions.get(ActionKey.EMAIL));
		mnuFile.addSeparator();
		mnuFile.add(actions.get(ActionKey.QUIT));
		menu.add(mnuFile);
		
		JMenu mnuEdit = new JMenu("Bearbeiten");
		mnuEdit.add(actions.get(ActionKey.EDIT_CONSUMERS));
		mnuEdit.add(actions.get(ActionKey.EDIT_PRODUCTS));
		mnuEdit.addSeparator();
		mnuEdit.add(actions.get(ActionKey.PRODUCT_COLUMNS));
		menu.add(mnuEdit);
		
		JMenu mnuTools = new JMenu("Extras");
		mnuTools.add(actions.get(ActionKey.CHARTS));
		mnuTools.addSeparator();
		mnuTools.add(actions.get(ActionKey.BACKUP));
		mnuTools.add(actions.get(ActionKey.RESTORE_BACKUP));
		menu.add(mnuTools);
		
		return menu;
	}
	
	private JToolBar buildToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setRollover(true);
		toolbar.setFloatable(false);
		
		toolbar.add(buildToolbarButton(actions.get(ActionKey.NEW_BILLING)));
		toolbar.add(buildToolbarButton(actions.get(ActionKey.NEW_BILLING_BASED_ON_CURRENT)));
		toolbar.add(buildToolbarButton(actions.get(ActionKey.OPEN_BILLING)));
		toolbar.add(buildToolbarButton(actions.get(ActionKey.SAVE)));
		toolbar.addSeparator();
		toolbar.add(buildPrintMenuButton());
		toolbar.add(buildToolbarButton(actions.get(ActionKey.EMAIL)));
		toolbar.add(buildToolbarButton(actions.get(ActionKey.CHARTS)));
		
		return toolbar;
	}
	
	private JButton buildToolbarButton(Action action) {
		JButton btnButton = new JButton(action);
		
		btnButton.setFocusable(false);
		btnButton.setToolTipText(btnButton.getText());
		btnButton.setText("");
		
		return btnButton;
	}
	
	private JButton buildPrintMenuButton() {
		JButton btnPrint = new JButton();
		btnPrint.setIcon(GUIImageManager.getInstance().getImageIcon("printer_color.png"));
		btnPrint.setToolTipText("PDF's erstellen");
		btnPrint.setFocusable(false);
		final JPopupMenu mnuPrint = new JPopupMenu();
		mnuPrint.add(actions.get(ActionKey.PDF_BILLING));
		mnuPrint.add(actions.get(ActionKey.PDF_CONSUMPTION_SHEET));
		
		btnPrint.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Component source = e.getComponent();
				Point pos = source.getLocation();
				
				pos = SwingUtilities.convertPoint(source.getParent(), pos, source);
				
				int x = pos.x;
				int y = source.getY() + source.getHeight();
				
				mnuPrint.show(e.getComponent(), x, y);
			}
		});
		
		return btnPrint;
	}
	
	private void addTab(String text, AbstractEditableTab tab, boolean select) {
		tbpTabs.addTab(text, tab);
		if(select) tbpTabs.setSelectedIndex(tbpTabs.getComponentCount()-1);
	}
	
	private AbstractEditableTab getSelectedEditableTab() {
		AbstractEditableTab tab = (AbstractEditableTab)tbpTabs.getSelectedComponent();
		return tab;
	}
	
	// Hilfsfunktionen ---------------------------------------------------------
	private List<Product> getProductColumns() {
		/* lstColumnInformation: */
		Property property = DataContainer.getInstance().getProperty(DataContainer.PROPERTY_KEY_BILLING_PRODUCT_COLUMNS);
		List<Product> productColumns = new ArrayList<Product>();
		
		/* ColumnInformation's zurück holen: */
		if(property != null) {
			String[] columns = property.getValue().split(";");
			
			for(String column: columns) {
				Long id = Long.parseLong(column);
				
				for(Product product: DataContainer.getInstance().getProducts()) {
					if(product.getId().equals(id)) {
						productColumns.add(product);
						break;
					}
				}
			}
		}
		
		return productColumns;
	}
	
	// Actions -----------------------------------------------------------------
	/**
	 * Erstellt eine neue Abrechnung
	 */
	private class NewBillingAction extends AbstractAction {
		
		public NewBillingAction() {
			super("Neue Abrechnung", GUIImageManager.getInstance().getImageIcon("table.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					Billing newBilling = new Billing("Neue Abrechnung", new Date());
					
					List<Consumer> consumers = new ArrayList<Consumer>();
					for(Consumer consumer: DataContainer.getInstance().getConsumers())
						if(consumer.getAddToNewBillings()) consumers.add(consumer);
					List<Product> products = getProductColumns();
					List<BillingProductPrice> prices = new ArrayList<BillingProductPrice>();
					for(Product product: DataContainer.getInstance().getProducts())
						prices.add(new BillingProductPrice(newBilling, product));
					
					newBilling.setBillingProductPrices(prices);
					newBilling.setChanged(false);
					
					BillingTab editor = new BillingTab(newBilling, consumers, products);
					addTab("< neue Abrechnung >", editor, true);
				}
			};
			
			WaitDialog wait = new WaitDialog(KaffeekasseFrame.this, "Neue Abrechnung", "Daten werden geladen... Bitte warten.", task);
			wait.setVisible(true);
		}
	}
	
	/**
	 * Bestehende Abrechnung öffnen
	 */
	private class OpenBillingAction extends AbstractAction {
		
		public OpenBillingAction() {
			super("Abrechnung öffnen", GUIImageManager.getInstance().getImageIcon("folder_table.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			BillingDialog billingDialog = new BillingDialog(KaffeekasseFrame.this);
			billingDialog.setVisible(true);
			
			if(billingDialog.getDialogResult() == DialogResult.OK) {
				Billing selectedBilling = billingDialog.getSelectedBilling();
				
				addTab(
						selectedBilling.getTitle(),
						new BillingTab(
								selectedBilling,
								selectedBilling.getUsedConsumers(),
								getProductColumns()),
						true
						);
			}
		}
	}
	
	/**
	 * Aktuelle Abrechnung speichern
	 */
	private class SaveAction extends AbstractAction {
		
		public SaveAction() {
			super("Speichern", GUIImageManager.getInstance().getImageIcon("disk.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					getSelectedEditableTab().save();
				}
			};
			
			WaitDialog waitDialog = new WaitDialog(
					KaffeekasseFrame.this,
					"Speichern",
					"Daten werden gespeichert... Bitte warten.",
					task);
			waitDialog.setVisible(true);
		}
	}
	
	/**
	 * Erstellt aufgrund der aktuellen Abrechnung eine neue.<br/>
	 * Wurde eine Person nicht als Bezahlt markiert, wird der entsprechende
	 * Betrag auf die neue Abrechnung übernommen.
	 */
	private class NewBillingBasedOnCurrentAction extends AbstractAction {
		
		public NewBillingBasedOnCurrentAction() {
			super("Neue Abrechnung (basierend auf aktueller)", GUIImageManager.getInstance().getImageIcon("table_lightning.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					DataEntity data = getSelectedEditableTab().getData();
					if(data != null && data instanceof Billing) {
						Billing oldBilling = (Billing)data;
						Billing newBilling = new Billing("Neue Abrechnung", new Date());
						
						List<Consumer> consumers = new ArrayList<Consumer>();
						for(Consumer consumer: DataContainer.getInstance().getConsumers())
							if(consumer.getAddToNewBillings()) consumers.add(consumer);
						List<Product> products = getProductColumns();
						List<BillingProductPrice> prices = new ArrayList<BillingProductPrice>();
						for(Product product: DataContainer.getInstance().getProducts())
							prices.add(new BillingProductPrice(newBilling, product));
						
						newBilling.setBillingProductPrices(prices);
						
						for(BillingPosition oldBillingPosition: oldBilling.getBillingPositions()) {
							if(!oldBillingPosition.isPaid()) {
								BillingPosition newBillingPosition = new BillingPosition();
								newBillingPosition.setBilling(newBilling);
								newBillingPosition.setConsumer(oldBillingPosition.getConsumer());
								
								Double oldConsumptionsTotal = oldBillingPosition.calculateTotalConsumptions();
								
								for(OtherCost oldOtherCost: oldBillingPosition.getOtherCosts()) {
									OtherCost newOtherCost = new OtherCost(
											newBillingPosition,
											oldOtherCost.getText(),
											oldOtherCost.getAmount());
									newBillingPosition.addOtherCost(newOtherCost);
								}
								
								if(oldConsumptionsTotal > 0d) {
									OtherCost oldConsumptationsOtherCost = new OtherCost(
											newBillingPosition,
											"Austehende Konsumationen von \"" + oldBilling.getTitle() + "\"",
											oldConsumptionsTotal);
									newBillingPosition.addOtherCost(oldConsumptationsOtherCost);
								}
								
								if(newBillingPosition.getOtherCosts().size() > 0) {
									newBilling.addBillingPosition(newBillingPosition);
								}
							}
						}
						
						newBilling.setChanged(false);
						addTab("< neue Abrechnung >", new BillingTab(newBilling, consumers, products), true);
					}
				}
			};
			
			WaitDialog waitDialog = new WaitDialog(
					KaffeekasseFrame.this,
					"Neue Abrechnung",
					"Neue Abrechnung wird erstellt... Bitte warten.",
					task);
			waitDialog.setVisible(true);
		}
	}
	
	
	/**
	 * Erstellt ein PDF aus der aktuellen Abrechnung.
	 */
	private class PDFBillingSheetAction extends AbstractAction {
		
		public PDFBillingSheetAction() {
			super("Abrechnung", GUIImageManager.getInstance().getImageIcon("table_gear.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			AbstractEditableTab editableTag = getSelectedEditableTab();
			DataEntity data = editableTag.getData();
			
			if(data instanceof Billing) {
				BillingPDFFactory factory = new BillingPDFFactory((Billing)data);
				factory.createPdf(KaffeekasseFrame.this, "Abrechnung.pdf");
			}
		}
	}
	
	/**
	 * Erstellt eine Strichliste für den Pausenraum als PDF.
	 */
	private class PDFConsumptationSheetAction extends AbstractAction {
		
		public PDFConsumptationSheetAction() {
			super("Strichliste", GUIImageManager.getInstance().getImageIcon("table_edit.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK ));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			new ConsumptionSheetDialog(KaffeekasseFrame.this).setVisible(true);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));
		}
	}
	
	/**
	 * Generiert persönliche E-Mails für alle Konsumenten in der aktuellen
	 * Abrechnung.
	 */
	private class EmailAction extends AbstractAction {
		
		public EmailAction() {
			super("Abrechnungs-E-Mails erstellen", GUIImageManager.getInstance().getImageIcon("email_start.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			AbstractEditableTab editableTag = getSelectedEditableTab();
			DataEntity data = editableTag.getData();
			
			if(data instanceof Billing) {
				EmailDialog emailDialog = new EmailDialog(KaffeekasseFrame.this, (Billing)data);
				emailDialog.setVisible(true);
			}
		}
	}
	
	private class ProductColumnsAction extends AbstractAction {
		
		public ProductColumnsAction() {
			super("Produktespalten anpassen", GUIImageManager.getInstance().getImageIcon("table_column.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			new ProductColumnsDialog(KaffeekasseFrame.this).setVisible(true);
		}
	}
	
	
	/**
	 * Öffnet den {@link ConsumerDialog}.
	 */
	private class EditConsumersAction extends AbstractAction {
		
		public EditConsumersAction() {
			super("Konsumenten", GUIImageManager.getInstance().getImageIcon("user.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			new ConsumerDialog(KaffeekasseFrame.this).setVisible(true);
		}
	}
	
	/** 
	 * Öffnet den {@link ProductDialog}.
	 */
	private class EditProductsAction extends AbstractAction {
		
		public EditProductsAction() {
			super("Produkte", GUIImageManager.getInstance().getImageIcon("package_green.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			new ProductDialog(KaffeekasseFrame.this).setVisible(true);
		}
	}
	
	/** 
	 * Beendet die Applikation.
	 */
	private class QuitAction extends AbstractAction {
		
		public QuitAction() {
			super("Beenden");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK ));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			DataContainer.getInstance().cleanUpDataContainer();
			dispose();
			
			System.exit(0);
		}
	}
	
	/**
	 * Erstellt ein Backup der Datenbank.
	 */
	private class BackupAction extends AbstractAction {
		
		public BackupAction() {
			super("Datenbank-Backup erstellen", GUIImageManager.getInstance().getImageIcon("pill.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK ));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			/* Keine Abrechnungen oder anderes offen?: */
			if(tbpTabs.getTabCount() > 1) {
				JOptionPane.showMessageDialog(
						KaffeekasseFrame.this,
						"Bitte schliessen und/oder speichern Sie vor dem Erstellen eines Datenbank-Backups vorgängig\n" +
						"alle offenen Abrechnungen.\n" +
						"Somit kann eine vollständige Sicherung der Daten gewährleistet werden.",
						"Datenbank-Backup erstellen",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			/* Vorbereiten: */
			// Dateinamen:
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm");
			String filename = dateFormat.format(new Date()) + DatabaseBackup.BACKUP_FILE_EXTENSION;
			
			// Backup-Ordner:
			File backupFolder = new File("backups\\");
			if(!backupFolder.exists()) backupFolder.mkdir();
			
			/* FileChooser: */
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(
					new FileFilter() {
						@Override
						public String getDescription() {
							return "Kaffeekasse Datenbank-Backup";
						}
						@Override
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(DatabaseBackup.BACKUP_FILE_EXTENSION)
								|| f.isDirectory();
						}
					});
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle("Datenbank-Backup erstellen");
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setCurrentDirectory(backupFolder);
			fileChooser.setSelectedFile(new File(filename));
			int result = fileChooser.showSaveDialog(KaffeekasseFrame.this);
			
			/* Backup erstellen, falls "Save" geklickt: */
			if(result == JFileChooser.APPROVE_OPTION) {
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							DatabaseBackup.createBackup(fileChooser.getSelectedFile(), true);
							JOptionPane.showMessageDialog(
									KaffeekasseFrame.this,
									"Die Datenbank wurde erfolgreich gesichert.",
									"Datenbank-Backup erstellen",
									JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(
									KaffeekasseFrame.this,
									"Ein Fehler ist aufgetreten!\n" +
									"Die Datenbank konnte nicht gesichert werden.",
									"Datenbank-Backup erstellen",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				
				WaitDialog waitDialog = new WaitDialog(
						KaffeekasseFrame.this,
						"Datenbank-Backup erstellen",
						"Backup wird erstellt... Bitte warten.",
						task);
				waitDialog.setVisible(true);
			}
		}
	}
	
	/**
	 * Stellt ein Datenbankbackup wieder her.
	 */
	private class RestoreBackupAction extends AbstractAction {
		
		public RestoreBackupAction() {
			super("Datenbank-Backup wiederherstellen");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK ));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			/* Keine Abrechnungen oder anderes offen?: */
			if(tbpTabs.getTabCount() > 1) {
				JOptionPane.showMessageDialog(
						KaffeekasseFrame.this,
						"Bitte schliessen Sie vor dem Wiederherstellen eines Datenbank-Backups vorgängig\n" +
						"alle offenen Abrechnungen.\n",
						"Datenbank-Backup wiederherstellen",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			/* Vorbereiten: */
			// Backup-Ordner:
			File backupFolder = new File("backups\\");
			if(!backupFolder.exists()) backupFolder.mkdir();
			
			/* FileChooser: */
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(
					new FileFilter() {
						@Override
						public String getDescription() {
							return "Kaffeekasse Datenbank-Backup";
						}
						@Override
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(DatabaseBackup.BACKUP_FILE_EXTENSION)
									|| f.isDirectory();
						}
					});
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle("Datenbank-Backup wiederherstellen");
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setCurrentDirectory(backupFolder);
			int result = fileChooser.showOpenDialog(KaffeekasseFrame.this);
			
			if(result == JFileChooser.APPROVE_OPTION) {
				result = JOptionPane.showConfirmDialog(
						KaffeekasseFrame.this,
						"Sind sie sicher, dass Sie das Backup mit dem Namen \"" +
						fileChooser.getSelectedFile().getName() + "\" wiederherstellen möchten?\n" +
						"Alle momentan vorhandenen Daten (Abrechnungen, Konsumenten usw.) gehen damit " +
						"unwiederruflich verloren!",
						"Datenbank-Backup wiederherstellen",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE
						);
				
				if(result == JOptionPane.YES_OPTION) {
					Runnable task = new Runnable() {
						@Override
						public void run() {
							try {
								DatabaseBackup.restoreBackup(fileChooser.getSelectedFile());
								JOptionPane.showMessageDialog(
										KaffeekasseFrame.this,
										"Die Datenbank wurde erfolgreich wiederhergestellt.",
										"Datenbank-Backup wiederherstellen",
										JOptionPane.INFORMATION_MESSAGE);
							} catch (Exception e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(
										KaffeekasseFrame.this,
										"Ein Fehler ist aufgetreten!\n" +
										"Die Datenbank konnte nicht wiederhergestellt werden.",
										"Datenbank-Backup wiederherstellen",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					};
					
					WaitDialog waitDialog = new WaitDialog(
							KaffeekasseFrame.this,
							"Datenbank-Backup wiederherstellen",
							"Backup wird wiederhergestellt... Bitte warten.",
							task);
					waitDialog.setVisible(true);
				}
			}
		}
	}
	
	/**
	 * Schliesst den aktuellen Tab, ausser, der "Willkommen"-Tab ist ausgewählt.
	 */
	private class CloseTabAction extends AbstractAction {
		
		public CloseTabAction() {
			super("Schliessen");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK ));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int index = tbpTabs.getSelectedIndex();
			if(index > 0) {
				boolean ok = getSelectedEditableTab().beforeCloseTab();
				if(ok) {
					tbpTabs.removeTabAt(index);
				}
			}
		}
	}
	
	/**
	 * Zeigt den Dialog mit den verschiedenen Diagrammen an.
	 */
	private class ChartsAction extends AbstractAction {
		
		public ChartsAction() {
			super("Auswertungen", GUIImageManager.getInstance().getImageIcon("chart_bar.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK ));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			new ChartsDialog(KaffeekasseFrame.this).setVisible(true);
		}
	}
	
}
