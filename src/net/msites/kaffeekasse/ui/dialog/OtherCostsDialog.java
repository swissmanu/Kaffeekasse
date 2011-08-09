package net.msites.kaffeekasse.ui.dialog;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

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
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.OtherCost;
import net.msites.kaffeekasse.ui.components.DetailsListCellRenderer;
import net.msites.kaffeekasse.ui.dialog.editordialog.EditorDialog;
import net.msites.kaffeekasse.ui.dialog.editordialog.OtherCostEditor;

public class OtherCostsDialog extends JDialog {

	private final JList lstOtherCosts = new JList(new OtherCostsListModel());
	private final Action actionAdd = new AddAction();
	private final Action actionEdit = new EditAction();
	private final Action actionDelete = new DeleteAction();
	private final Action actionClose = new CloseAction();
	
	private BillingPosition billingPosition;
	
	public OtherCostsDialog(Window owner, BillingPosition billingPosition) {
		super(owner);
		
		this.billingPosition = billingPosition;
		
		setModal(true);
		setTitle("Andere Kosten für " + billingPosition.getConsumer().getFullname());
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
		
		JScrollPane listScroller = new JScrollPane(lstOtherCosts);
		JButton btnAdd = GUIHelper.createListButton(actionAdd);
		JButton btnDelete = GUIHelper.createListButton(actionDelete);
		JButton btnClose = new JButton(actionClose);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(listScroller)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnAdd)
						.addComponent(btnDelete)
						.addComponent(btnClose)
				)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(listScroller)
						
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
		/* lstOtherCosts: */
		lstOtherCosts.setCellRenderer(new OtherCostListCellRenderer());
		lstOtherCosts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstOtherCosts.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(lstOtherCosts.getSelectedValue() == null) {
					actionEdit.setEnabled(false);
					actionDelete.setEnabled(false);
				} else {
					actionEdit.setEnabled(true);
					actionDelete.setEnabled(true);
				}
			}
		});
		
		lstOtherCosts.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && lstOtherCosts.getSelectedIndex() > -1) {
					actionEdit.actionPerformed(new ActionEvent(lstOtherCosts,ActionEvent.ACTION_PERFORMED,"doubleClickEdit"));
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
	
	// Liste -------------------------------------------------------------------
	private class OtherCostsListModel extends AbstractListModel {
		
		@Override
		public Object getElementAt(int index) {
			OtherCost otherCost = billingPosition.getOtherCosts().get(index);
			return otherCost;
		}
		
		@Override
		public int getSize() {
			int size = billingPosition.getOtherCosts().size();
			return size;
		}
		
	}
	
	private class OtherCostListCellRenderer extends DetailsListCellRenderer {
		
		private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
		
		public OtherCostListCellRenderer() {
			super();
			setIcon(GUIImageManager.getInstance().getImageIcon("money.png"));
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			OtherCost otherCost = (OtherCost)value;
			setText(otherCost.getText());
			setDetails("Betrag: " + currencyFormat.format(otherCost.getAmount()));
			
			return this;
		}
		
	}
	
	private OtherCost getSelectedOtherCost() {
		Object selected = lstOtherCosts.getSelectedValue();
		OtherCost selectedOtherCost = null;
		
		if(selected != null) selectedOtherCost = (OtherCost)selected;
		
		return selectedOtherCost;
	}
	
	// Actions -----------------------------------------------------------------
	private class AddAction extends AbstractAction {
		public AddAction() {
			super(null, GUIImageManager.getInstance().getImageIcon("add.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			EditorDialog editorDialog = new EditorDialog(
					OtherCostsDialog.this,
					new OtherCostEditor(new OtherCost()),
					"Neue andere Kosten");
			editorDialog.setVisible(true);
			
			if(editorDialog.getDialogResult() == DialogResult.OK) {
				// Speichern:
				OtherCost newOtherCost = (OtherCost)editorDialog.getData();
				newOtherCost.setBillingPosition(billingPosition);
				billingPosition.addOtherCost(newOtherCost);
				
				lstOtherCosts.updateUI();  // dirty :-P
				lstOtherCosts.setSelectedValue(newOtherCost, true);
			}
		}
	}
	
	private class EditAction extends AbstractAction {
		public EditAction() {
			super("Bearbeiten");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			OtherCost selectedOtherCost = getSelectedOtherCost();
			
			if(selectedOtherCost != null) {
				EditorDialog editorDialog = new EditorDialog(
						OtherCostsDialog.this,
						new OtherCostEditor(selectedOtherCost),
						"Andere Kosten bearbeiten");
				editorDialog.setVisible(true);
				
				if(editorDialog.getDialogResult() == DialogResult.OK) {
					// Daten übernehmen:
					OtherCost editedOtherCost = (OtherCost)editorDialog.getData();
					selectedOtherCost.setText(editedOtherCost.getText());
					selectedOtherCost.setAmount(editedOtherCost.getAmount());
					DataContainer.getInstance().saveDataEntity(editedOtherCost);
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
			OtherCost selectedOtherCost = getSelectedOtherCost();
			
			if(selectedOtherCost != null) {
				int confirmResult = JOptionPane.showConfirmDialog(
						OtherCostsDialog.this,
						"Möchten Sie die Kosten \"" + selectedOtherCost.getText() + "\" wirklich löschen?",
						"Andere Kosten löschen",
						JOptionPane.YES_NO_OPTION);
				
				if(confirmResult == JOptionPane.YES_OPTION) {
					billingPosition.removeOtherCost(selectedOtherCost);
					lstOtherCosts.updateUI(); // dirty :-P
					
					if(lstOtherCosts.getModel().getSize() == 0) {
						this.setEnabled(false);
						actionEdit.setEnabled(false);
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
			dispose();
		}
	}
	
}
