package net.msites.kaffeekasse.ui.dialog;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.guilibrary.toolbox.MultilineLabel;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.data.entities.Property;
import net.msites.kaffeekasse.ui.components.checkablejlist.AbstractCheckableListModel;
import net.msites.kaffeekasse.ui.components.checkablejlist.CheckableDetailsListCellRenderer;
import net.msites.kaffeekasse.ui.components.checkablejlist.CheckableJList;

public class ProductColumnsDialog extends JDialog {

	private final CheckableJList lstProductColumns = new CheckableJList(new ProductColumnsListModel());
	private final Action actionMoveUp = new MoveColumnAction(-1, "Nach Oben verschieben", GUIImageManager.getInstance().getImageIcon("arrow_up.png"));
	private final Action actionMoveDown = new MoveColumnAction(1, "Nach Unten verschieben", GUIImageManager.getInstance().getImageIcon("arrow_down.png"));
	private final Action actionCreate = new CreateAction();
	private final Action actionCancel = new CancelAction();
	
	private List<Product> products = new ArrayList<Product>();
	
	public ProductColumnsDialog(Window owner) {
		super(owner);
		
		setModal(true);
		setTitle("Produktespalten anpassen");
		setSize(500,400);
		GUIHelper.centerOnOwner(this, owner);
		setResizable(false);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionCancel.actionPerformed(new ActionEvent(this,0,"windowClosed"));
			}
		});
		
		/* GUI erstellen: */
		setContentPane(buildGui());
	}
	
	/**
	 * Setzt die {@link Component}'s des GUI's zusammen und gibt diese zusammengefasst
	 * in einer {@link JComponent} zurück.
	 * 
	 * @return
	 */
	private JComponent buildGui() {
		JPanel gui = new JPanel();
		GroupLayout layout = new GroupLayout(gui);
		gui.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		initKeyBindings(gui);
		initGui();
		
		MultilineLabel lblIntro = new MultilineLabel(
				"Hier können Sie die Reihenfolge der Produktspalten für die " +
				"Abrechnungsansicht verändern. Beachten Sie, dass die etwaige neue " +
				"Reihenfolge erst beim nächsten öffnen einer Abrechnung verwendet " +
				"wird."
				);
		JScrollPane listScroller = new JScrollPane(lstProductColumns);
		JButton btnUp = GUIHelper.createListButton(actionMoveUp);
		JButton btnDown = GUIHelper.createListButton(actionMoveDown);
		JButton btnCreate = new JButton(actionCreate);
		JButton btnClose = new JButton(actionCancel);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(lblIntro, 50,50,50)
				.addComponent(listScroller)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnUp)
						.addComponent(btnDown)
						.addComponent(btnCreate)
						.addComponent(btnClose)
						)
				);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(lblIntro)
						.addComponent(listScroller)
						.addGroup(Alignment.LEADING, layout.createSequentialGroup()
								.addComponent(btnUp)
								.addComponent(btnDown)
								)
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(btnCreate)
								.addComponent(btnClose)
								)
						)
				);

		return gui;
	}
	
	/**
	 * Initialisiert das GUI.
	 */
	private void initGui() {		
		/* lstColumnInformation: */
		loadColumnInformation();
		
		lstProductColumns.setCellRenderer(new ColumnInformationListCellRenderer());
		lstProductColumns.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Object selectedItem = lstProductColumns.getSelectedValue();
				
				if(selectedItem != null) {
					actionMoveDown.setEnabled(true);
					actionMoveUp.setEnabled(true);					
				} else {
					actionMoveDown.setEnabled(false);
					actionMoveUp.setEnabled(false);					
				}
			}
		});
		
		
		/* Deaktivieren: */
		actionMoveDown.setEnabled(false);
		actionMoveUp.setEnabled(false);
	}
	
	private void initKeyBindings(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Create");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Close");
		
		ActionMap actionMap = component.getActionMap();
		actionMap.put("Create", actionCreate);
		actionMap.put("Close", actionCancel);
	}
	
	// Liste -------------------------------------------------------------------
	private class ProductColumnsListModel extends AbstractCheckableListModel {
		
		@Override
		public Product getElementAt(int index) {
			return products.get(index);
		}
		
		@Override
		public int getSize() {
			return products.size();
		}
		
	}
	
	private class ColumnInformationListCellRenderer extends CheckableDetailsListCellRenderer {
		
		private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			Product productColumn = (Product)value;
			setText(productColumn.getName());
			setDetails("Preis: " + currencyFormat.format(productColumn.getPrice()));
			
			return this;
		}
		
	}
	
	
	// Persistenz --------------------------------------------------------------
	private void saveColumnInformation() {
		/* lstColumnInformation: */
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for(Product productColumn: products) {
			if(lstProductColumns.isChecked(productColumn)) {
				if(!first) buffer.append(";");
				buffer.append(productColumn.getId());
				first = false;
			}
		}
		
		Property property = DataContainer.getInstance().getProperty(DataContainer.PROPERTY_KEY_BILLING_PRODUCT_COLUMNS);
		if(property != null) {
			property.setValue(buffer.toString());
		} else {
			property = new Property(DataContainer.PROPERTY_KEY_BILLING_PRODUCT_COLUMNS, buffer.toString());
		}
		
		DataContainer.getInstance().putProperty(property);
	}
	
	private void loadColumnInformation() {
		/* lstColumnInformation: */
		Property property = DataContainer.getInstance().getProperty(DataContainer.PROPERTY_KEY_BILLING_PRODUCT_COLUMNS);
		List<Product> products = DataContainer.getInstance().getProducts();
		
		/* ColumnInformation's zurück holen: */
		if(property != null) {
			String[] columns = property.getValue().split(";");
			
			for(String column: columns) {
				Long id = Long.parseLong(column);
				
				for(Product product: products) {
					if(product.getId().equals(id)) {
						this.products.add(product);
						lstProductColumns.setChecked(product, true);
					}
				}
			}
			
			for(Product product: products) {
				if(!this.products.contains(product)) this.products.add(product);
			}
		}
	}
	
	
	// Actions -----------------------------------------------------------------
	private class CreateAction extends AbstractAction {
		public CreateAction() {
			super("Speichern");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			saveColumnInformation();
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction {
		public CancelAction() {
			super("Abbrechen");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	
	private class MoveColumnAction extends AbstractAction {
		
		private int direction;
		
		public MoveColumnAction(int direction, String text, ImageIcon icon) {
			super(text, icon);
			this.direction = direction;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int currentIndex = lstProductColumns.getSelectedIndex();
			int newIndex = currentIndex + direction;
			
			if(newIndex >= 0 && newIndex < lstProductColumns.getModel().getSize()) {
				Product productCol = products.remove(currentIndex);
				products.add(newIndex, productCol);
				lstProductColumns.updateUI();
				lstProductColumns.setSelectedIndex(newIndex);
			}
		}
	}
	
}
