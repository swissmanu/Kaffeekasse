package net.msites.kaffeekasse.ui.dialog;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.guilibrary.toolbox.MultilineLabel;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.DataContainerEvent;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.ui.components.DetailsListCellRenderer;
import net.msites.kaffeekasse.ui.dialog.editordialog.EditorDialog;
import net.msites.kaffeekasse.ui.dialog.editordialog.ProductEditor;

public class ProductDialog extends JDialog {

	private final JList lstProducts = new JList(new ProductListModel());
	private final Action actionAdd = new AddAction();
	private final Action actionEdit = new EditAction();
	private final Action actionDelete = new DeleteAction();
	private final Action actionClose = new CloseAction();
	
	public ProductDialog(Frame owner) {
		super(owner, "Produkte", true);
		
		setSize(500,400);
		GUIHelper.centerOnOwner(this, owner);
		setResizable(false);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionClose.actionPerformed(new ActionEvent(this,0,"windowClosed"));
			}
		});
		
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
		
		JScrollPane listScroller = new JScrollPane(lstProducts);
		MultilineLabel lblHint = new MultilineLabel(
				"Hinweis: Wird ein Produktpreis nachträglich geändert, hat dies " +
				"keinen Einfluss auf bestehende Abrechnungen!");
		JButton btnAdd = GUIHelper.createListButton(actionAdd);
		JButton btnDelete = GUIHelper.createListButton(actionDelete);
		JButton btnClose = new JButton(actionClose);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(listScroller)
				.addComponent(lblHint)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnAdd)
						.addComponent(btnDelete)
						.addComponent(btnClose)
				)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(listScroller)
						.addComponent(lblHint)
						.addGroup(Alignment.LEADING, layout.createSequentialGroup()
								.addComponent(btnAdd)
								.addComponent(btnDelete)
						)
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
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
		/* lstProducts: */
		DataContainer.getInstance().getDataContainerObservable()
			.addObserver((ProductListModel)lstProducts.getModel());
		
		lstProducts.setCellRenderer(new ProductsListCellRenderer());
		lstProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstProducts.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(lstProducts.getSelectedValue() == null) {
					actionDelete.setEnabled(false);
				} else {
					actionDelete.setEnabled(true);
				}
			}
		});
		
		lstProducts.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && lstProducts.getSelectedIndex() > -1) {
					actionEdit.actionPerformed(new ActionEvent(lstProducts,ActionEvent.ACTION_PERFORMED,"doubleClickEdit"));
				}
			}
		});
		
		/* Actions: */
		actionEdit.setEnabled(false);
		actionDelete.setEnabled(false);
	}
	
	private void initKeyBindings(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Close");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Edit");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "New");
		
		ActionMap actionMap = component.getActionMap();
		actionMap.put("Close", actionClose);
		actionMap.put("Edit", actionEdit);
		actionMap.put("Delete", actionDelete);
		actionMap.put("New", actionAdd);
		
	}
	
	private void cleanUpGui() {
		DataContainer.getInstance().getDataContainerObservable()
			.deleteObserver((ProductListModel)lstProducts.getModel());
	}
	
	// Liste -------------------------------------------------------------------
	private class ProductListModel extends AbstractListModel implements Observer {
		
		@Override
		public Object getElementAt(int index) {
			Product product = DataContainer.getInstance().getProducts().get(index);
			return product;
		}
		
		@Override
		public int getSize() {
			int size = DataContainer.getInstance().getProducts().size();
			return size;
		}
		
		@Override
		public void update(Observable o, Object arg) {
			DataContainerEvent event = (DataContainerEvent)arg;
			
			if(event.getDataEntity() instanceof Product) {
				int currentSelection = lstProducts.getSelectedIndex();
				int size = getSize();
				fireContentsChanged(o, 0, size);
				
				// Auswahl behalten:
				if(currentSelection > getSize()-1) currentSelection--;
				lstProducts.setSelectedIndex(currentSelection);				
				
				// Leer?
				if(size == 0) {
					actionEdit.setEnabled(false);
					actionDelete.setEnabled(false);
				} else {
					actionEdit.setEnabled(true);
					actionDelete.setEnabled(true);
				}
			}
		}
		
	}
	
	private class ProductsListCellRenderer extends DetailsListCellRenderer {
			
		private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
		
		public ProductsListCellRenderer() {
			setIcon(GUIImageManager.getInstance().getImageIcon("package_green.png"));
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			Product product = (Product)value;
			
			setText(product.getName());
			setDetails("Basispreis: " + currencyFormat.format(product.getPrice()));
			
			return this;
		}
		
	}
	
	private Product getSelectedProduct() {
		Object selected = lstProducts.getSelectedValue();
		Product selectedProduct = null;
		
		if(selected != null) selectedProduct = (Product)selected;
		
		return selectedProduct;
	}
	
	// Actions -----------------------------------------------------------------
	private class AddAction extends AbstractAction {
		public AddAction() {
			super(null, GUIImageManager.getInstance().getImageIcon("add.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			EditorDialog editorDialog = new EditorDialog(
					ProductDialog.this,
					new ProductEditor(),
					"Neues Produkt");
			editorDialog.setVisible(true);
			
			if(editorDialog.getDialogResult() == DialogResult.OK) {
				// Speichern:
				Product newProduct = (Product)editorDialog.getData();
				DataContainer.getInstance().saveDataEntity(newProduct);
				lstProducts.setSelectedValue(newProduct, true);
			}
		}
	}
	
	private class EditAction extends AbstractAction {
		public EditAction() {
			super("Bearbeiten");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Product selectedProduct = getSelectedProduct();
			
			if(selectedProduct != null) {
				EditorDialog editorDialog = new EditorDialog(
						ProductDialog.this,
						new ProductEditor(selectedProduct),
						"Produkt bearbeiten");
				editorDialog.setVisible(true);
				
				if(editorDialog.getDialogResult() == DialogResult.OK) {
					// Daten übernehmen:
					Product editedProduct = (Product)editorDialog.getData();
					selectedProduct.setName(editedProduct.getName());
					selectedProduct.setPrice(editedProduct.getPrice());
					DataContainer.getInstance().saveDataEntity(selectedProduct);
				}				
			}
			
		}
	}
	
	private class DeleteAction extends AbstractAction {
		public DeleteAction() {
			super(null, GUIImageManager.getInstance().getImageIcon("delete.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Product selectedProduct = getSelectedProduct();
			
			if(selectedProduct != null) {
				if(DataContainer.getInstance().isUsedInBillings(selectedProduct)) {
					JOptionPane.showMessageDialog(
						ProductDialog.this,
						"Das gewählte Produkt kann nicht gelöscht werden, " +
						"da es mindestens\n" +
						"in einer Abrechnung verwendet wird.",
						"Produkt löschen",
						JOptionPane.ERROR_MESSAGE);
				} else {
					int confirmResult = JOptionPane.showConfirmDialog(
							ProductDialog.this,
							"Möchten Sie das Produkt \"" + selectedProduct.getName() + "\" wirklich löschen?",
							"Produkt löschen",
							JOptionPane.YES_NO_OPTION);
					
					if(confirmResult == JOptionPane.YES_OPTION) {
						DataContainer.getInstance().deleteDataEntity(selectedProduct);
					}					
				}
				
			}
		}
	}
	
	private class CloseAction extends AbstractAction {
		public CloseAction() {
			super("Schliessen");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			cleanUpGui();
			dispose();
		}
	}
	
}
