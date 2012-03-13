package net.msites.kaffeekasse.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.data.entities.Property;
import net.msites.kaffeekasse.ui.components.tabbeduserinterface.AbstractEditableTab;
import net.msites.kaffeekasse.ui.components.tabbeduserinterface.TabbedUserInterface;
import net.msites.kaffeekasse.ui.components.tabbeduserinterface.TabbedUserInterfaceDelegate;
import net.msites.kaffeekasse.ui.dialog.WaitDialog;
import net.msites.kaffeekasse.ui.mainframe.actions.BackupAction;
import net.msites.kaffeekasse.ui.mainframe.actions.ChartsAction;
import net.msites.kaffeekasse.ui.mainframe.actions.CloseTabAction;
import net.msites.kaffeekasse.ui.mainframe.actions.EditConsumersAction;
import net.msites.kaffeekasse.ui.mainframe.actions.EditProductsAction;
import net.msites.kaffeekasse.ui.mainframe.actions.EmailAction;
import net.msites.kaffeekasse.ui.mainframe.actions.NewBillingAction;
import net.msites.kaffeekasse.ui.mainframe.actions.NewBillingBasedOnCurrentAction;
import net.msites.kaffeekasse.ui.mainframe.actions.OpenBillingAction;
import net.msites.kaffeekasse.ui.mainframe.actions.PDFBillingSheetAction;
import net.msites.kaffeekasse.ui.mainframe.actions.PDFConsumptationSheetAction;
import net.msites.kaffeekasse.ui.mainframe.actions.ProductColumnsAction;
import net.msites.kaffeekasse.ui.mainframe.actions.QuitAction;
import net.msites.kaffeekasse.ui.mainframe.actions.RestoreBackupAction;
import net.msites.kaffeekasse.ui.mainframe.actions.SaveAction;

/**
 * Hauptfenster
 *
 * @author Manuel Alabor
 */
public class KaffeekasseFrame extends JFrame implements TabbedUserInterface {

	private final TabbedUserInterfaceDelegate tabbedUserInterfaceDelegate = new TabbedUserInterfaceDelegate();
	private final Map<ActionKey, Action> actions = createActions();
	
	private enum ActionKey {
		NEW_BILLING, NEW_BILLING_BASED_ON_CURRENT, OPEN_BILLING, CLOSE_TAB,
		SAVE, PDF_CONSUMPTION_SHEET, PDF_BILLING, EMAIL, PRODUCT_COLUMNS,
		EDIT_CONSUMERS, EDIT_PRODUCTS, CHARTS, BACKUP, RESTORE_BACKUP, QUIT
	}
	
	public KaffeekasseFrame() {
		super("Kaffeekasse");
		
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
	private JComponent buildGui() {
		JPanel gui = new JPanel(new BorderLayout());
		initGui();
		
		JTabbedPane tbpTabs = tabbedUserInterfaceDelegate.getTabs();
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
		tabbedUserInterfaceDelegate.getTabs().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getClickCount() == 2) {
					AbstractEditableTab tab = getCurrentTab();
					if(tab.beforeCloseTab()) {
						// TODO tbpTabs.remove(tab);
					}
				}
			}
		});
		
		// Sicherstellen, dass nur gespeichert werden kann, wenn der aktuelle
		// Tab auch Daten zum speichern hat.
		tabbedUserInterfaceDelegate.getTabs().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				boolean enabled = getCurrentTab() instanceof BillingTab;
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
	
	/**
	 * Creates a {@link Map} with all {@link Action}'s available to this Frame.
	 * {@link ActionKey} is used as {@link Map} key objects.
	 * 
	 * @returns
	 */
	private Map<ActionKey, Action> createActions() {
		Map<ActionKey, Action> actions = new HashMap<KaffeekasseFrame.ActionKey, Action>();
		
		actions.put(ActionKey.NEW_BILLING, new NewBillingAction(this));
		actions.put(ActionKey.NEW_BILLING_BASED_ON_CURRENT, new NewBillingBasedOnCurrentAction(this));
		actions.put(ActionKey.OPEN_BILLING, new OpenBillingAction(this));
		actions.put(ActionKey.SAVE, new SaveAction(this));
		actions.put(ActionKey.CLOSE_TAB, new CloseTabAction(this));
		actions.put(ActionKey.PDF_BILLING, new PDFBillingSheetAction(this));
		actions.put(ActionKey.PDF_CONSUMPTION_SHEET, new PDFConsumptationSheetAction(this));
		actions.put(ActionKey.EMAIL, new EmailAction(this));
		actions.put(ActionKey.EDIT_CONSUMERS, new EditConsumersAction(this));
		actions.put(ActionKey.EDIT_PRODUCTS, new EditProductsAction(this));
		actions.put(ActionKey.PRODUCT_COLUMNS, new ProductColumnsAction(this));
		actions.put(ActionKey.CHARTS, new ChartsAction(this));
		actions.put(ActionKey.BACKUP, new BackupAction(this));
		actions.put(ActionKey.RESTORE_BACKUP, new RestoreBackupAction(this));
		actions.put(ActionKey.QUIT, new QuitAction(this));
		
		return actions; 
	}
	
	// Hilfsfunktionen ---------------------------------------------------------
	public List<Product> getProductColumns() {
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

	// Tabs --------------------------------------------------------------------
	@Override
	public void addTab(String text, AbstractEditableTab tab, boolean select) {
		tabbedUserInterfaceDelegate.addTab(text, tab, select);
	}
	
	@Override
	public AbstractEditableTab getCurrentTab() {
		return tabbedUserInterfaceDelegate.getCurrentTab();
	}
	
	@Override
	public void closeCurrentTab() {
		tabbedUserInterfaceDelegate.closeCurrentTab();
	}

	@Override
	public boolean hasTabs() {
		return tabbedUserInterfaceDelegate.hasTabs();
	}
	
}
