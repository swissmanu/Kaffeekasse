package net.msites.kaffeekasse.ui.dialog;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.Language;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.data.entities.Property;
import net.msites.kaffeekasse.pdf.ConsumptionSheetPDFFactory;
import net.msites.kaffeekasse.ui.components.checkablejlist.AbstractCheckableListModel;
import net.msites.kaffeekasse.ui.components.checkablejlist.CheckableDetailsListCellRenderer;
import net.msites.kaffeekasse.ui.components.checkablejlist.CheckableJList;

public class ConsumptionSheetDialog extends JDialog {

	private final JTextField txtSubTitle = new JTextField();
	private final JComboBox comLanguage = new JComboBox(new String[]{"Deutsch","Englisch"});
	private final CheckableJList lstProductColumns = new CheckableJList(new ProductColumnsListModel());
	private final JSpinner spiColumnWidth = new JSpinner(new SpinnerNumberModel(1,1,5,0.1));
	private final Action actionMoveUp = new MoveColumnAction(-1, "Nach Oben verschieben", GUIImageManager.getInstance().getImageIcon("arrow_up.png"));
	private final Action actionMoveDown = new MoveColumnAction(1, "Nach Unten verschieben", GUIImageManager.getInstance().getImageIcon("arrow_down.png"));
	private final Action actionCreate = new CreateAction();
	private final Action actionCancel = new CancelAction();
	
	private List<ColumnInformation> columnInformation = new ArrayList<ColumnInformation>();
	
	public ConsumptionSheetDialog(Window owner) {
		super(owner);
		
		setModal(true);
		setTitle("Strichliste als PDF erstellen");
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
		
		
		JLabel lblSubTitle = new JLabel("Untertitel");
		JLabel lblLanguage = new JLabel("Sprache");
		
		JScrollPane listScroller = new JScrollPane(lstProductColumns);
		JLabel lblWidth = new JLabel("Spaltenbreite");
		lblWidth.setLabelFor(spiColumnWidth);
		JButton btnUp = GUIHelper.createListButton(actionMoveUp);
		JButton btnDown = GUIHelper.createListButton(actionMoveDown);
		JButton btnCreate = new JButton(actionCreate);
		JButton btnClose = new JButton(actionCancel);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSubTitle)
						.addComponent(txtSubTitle)
						)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblLanguage)
						.addComponent(comLanguage)
						)
				.addComponent(listScroller)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnUp)
						.addComponent(btnDown)
						
						.addComponent(lblWidth)
						.addComponent(spiColumnWidth)
						
						.addComponent(btnCreate)
						.addComponent(btnClose)
						)
				);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(lblSubTitle)
								.addComponent(txtSubTitle)
								)
						.addGroup(layout.createSequentialGroup()
								.addComponent(lblLanguage)
								.addComponent(comLanguage)
								)
						.addComponent(listScroller)
						.addGroup(Alignment.LEADING, layout.createSequentialGroup()
								.addComponent(btnUp)
								.addComponent(btnDown)
								.addComponent(lblWidth)
								.addComponent(spiColumnWidth,50,50,50)
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
		/* txtSubTitle: */
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
		txtSubTitle.setText(dateFormat.format(new Date()));
		
		/* lstConsumers: */
		loadSheetSettings();
		lstProductColumns.setCellRenderer(new ColumnInformationListCellRenderer());
		
		lstProductColumns.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Object selectedItem = lstProductColumns.getSelectedValue();
				
				if(selectedItem != null) {
					spiColumnWidth.setEnabled(true);
					actionMoveDown.setEnabled(true);
					actionMoveUp.setEnabled(true);
					
					ColumnInformation col = (ColumnInformation)selectedItem;
					spiColumnWidth.setValue(new Double(col.getColumnWidth()));					
				}
			}
		});
		
		/* spiColumnWidth: */
		spiColumnWidth.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Object selectedItem = lstProductColumns.getSelectedValue();
				Object value = spiColumnWidth.getValue();
				
				if(selectedItem != null && value != null) {
					Double doubleValue = (Double)value;
					ColumnInformation colInfo = (ColumnInformation)selectedItem;
					colInfo.setColumnWidth(new Float(doubleValue));
					
					ProductColumnsListModel model = (ProductColumnsListModel)lstProductColumns.getModel();
					model.fireUpdate(columnInformation.indexOf(colInfo));
				}
			}
		});
		
		
		
		/* Deaktivieren: */
		spiColumnWidth.setEnabled(false);
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
		public ColumnInformation getElementAt(int index) {
			return columnInformation.get(index);
		}
		
		@Override
		public int getSize() {
			return columnInformation.size();
		}
		
		public void fireUpdate(int index) {
			fireContentsChanged(this, index, index);
		}
		
	}
	
	private class ColumnInformationListCellRenderer extends CheckableDetailsListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			ColumnInformation col = (ColumnInformation)value;
			setText(col.getProduct().getName());
			setDetails("Preis: " +  NumberFormat.getCurrencyInstance().format(col.getProduct().getPrice()) +
					", Spaltenbreite: " + col.getColumnWidth());
			
			return this;
		}
	}
	
	
	// Persistenz --------------------------------------------------------------
	private void saveSheetSettings() {
		/* lstColumnInformation: */
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for(ColumnInformation col: columnInformation) {
			if(lstProductColumns.isChecked(col)) {
				if(!first) buffer.append(";");
				buffer.append(col.getProduct().getId());
				buffer.append(",");
				buffer.append(col.getColumnWidth());
				
				first = false;
			}
		}
		
		Property property = DataContainer.getInstance().getProperty(DataContainer.PROPERTY_KEY_CONSUMPTATION_SHEET_COLUMNS);
		if(property != null) {
			property.setValue(buffer.toString());
		} else {
			property = new Property(DataContainer.PROPERTY_KEY_CONSUMPTATION_SHEET_COLUMNS, buffer.toString());
		}
		
		DataContainer.getInstance().putProperty(property);
		
		
		/* comLanguage: */
		String language = "de";
		if(comLanguage.getSelectedIndex() == 1) language = "en";
		property = DataContainer.getInstance().getProperty(DataContainer.PROPERTY_KEY_CONSUMPTATION_SHEET_LANGUAGE);
		if(property != null) {
			property.setValue(language);
		} else {
			property = new Property(DataContainer.PROPERTY_KEY_CONSUMPTATION_SHEET_LANGUAGE, language);
		}
		
		DataContainer.getInstance().putProperty(property);
	}
	
	private void loadSheetSettings() {
		/* lstColumnInformation: */
		Property property = DataContainer.getInstance().getProperty(DataContainer.PROPERTY_KEY_CONSUMPTATION_SHEET_COLUMNS);
		List<Product> products = DataContainer.getInstance().getProducts();
		
		/* ColumnInformation's zurück holen: */
		if(property != null) {
			String[] columns = property.getValue().split(";");
			
			for(String column: columns) {
				String[] data = column.split(",");
				Long id = Long.parseLong(data[0]);
				Float colWidth = Float.parseFloat(data[1]);
				
				for(Product product: products) {
					if(product.getId().equals(id)) {
						ColumnInformation colInfo = new ColumnInformation(
								product, colWidth);
						columnInformation.add(colInfo);
						lstProductColumns.setChecked(colInfo, true);
					}
				}
			}
		}

		/* Restliche Produkte der Liste hinzufügen: */
		for(Product product: products) {
			boolean found = false;
			for(ColumnInformation col: columnInformation) {
				if(col.getProduct().equals(product)) {
					found = true;
					break;
				}
			}
			
			if(!found) columnInformation.add(new ColumnInformation(
					product,
					1f
					));
		}
		
		/* comLanguage: */
		Property language = DataContainer.getInstance().getProperty(DataContainer.PROPERTY_KEY_CONSUMPTATION_SHEET_LANGUAGE);
		if(language != null) {
			if(language.getValue().equals("de")) {
				comLanguage.setSelectedIndex(0);
			} else {
				comLanguage.setSelectedIndex(1);
			}
		}
		
	}
	
	
	// Hilfsklassen ------------------------------------------------------------
	private class ColumnInformation {
		
		private Product product;
		private float columnWidth;
		
		public ColumnInformation(Product product, float columnWidth) {
			this.product = product;
			this.columnWidth = columnWidth;
		}
		
		public Product getProduct() {
			return product;
		}
		
		public float getColumnWidth() {
			return columnWidth;
		}
		
		public void setColumnWidth(float columnWidth) {
			this.columnWidth = columnWidth;
		}
		
	}
	
	
	// Actions -----------------------------------------------------------------
	private class CreateAction extends AbstractAction {
		public CreateAction() {
			super("PDF erstellen");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			/* Vorbereiten: */
			saveSheetSettings();
			
			String subtitle = txtSubTitle.getText();
			
			Language language = Language.GERMAN;
			if(comLanguage.getSelectedIndex() == 1) language = Language.ENGLISH;
			
			List<Float> widths = new ArrayList<Float>(); 
			List<Product> products = new ArrayList<Product>();
			List<Consumer> consumers = new ArrayList<Consumer>();
			
			for(Consumer c: DataContainer.getInstance().getConsumers()) {
				if(c.getAddToNewBillings()) consumers.add(c);
			}
			for(ColumnInformation colInfo: columnInformation) {
				if(lstProductColumns.isChecked(colInfo)) {
					products.add(colInfo.getProduct());
					widths.add(colInfo.getColumnWidth());
				}
			}
			
			float[] columnWidths = new float[widths.size() + 1];
			columnWidths[0] = 3f;  // Namensspalte
			for(int i = 0, l = widths.size(); i < l; i++) columnWidths[i+1] = widths.get(i);
			
			
			/* Generieren: */
			ConsumptionSheetPDFFactory factory = new ConsumptionSheetPDFFactory(
					subtitle, language, consumers, products, columnWidths);
			File pdf = factory.createPdf(ConsumptionSheetDialog.this, "Strichliste.pdf");
			
			if(pdf != null) dispose();
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
				ColumnInformation col = columnInformation.remove(currentIndex);
				columnInformation.add(newIndex, col);
				lstProductColumns.updateUI();
				lstProductColumns.setSelectedIndex(newIndex);
			}
		}
	}
	
}
