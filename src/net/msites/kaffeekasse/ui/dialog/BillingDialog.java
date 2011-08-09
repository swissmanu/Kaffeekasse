package net.msites.kaffeekasse.ui.dialog;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
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
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.DataContainerEvent;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.ui.components.DetailsListCellRenderer;

public class BillingDialog extends JDialog {

	private final JList lstBillings = new JList(new BillingListModel());
	private final Action actionOpen = new OpenAction();
	private final Action actionDelete = new DeleteAction();
	private final Action actionClose = new CloseAction();
	
	private DialogResult dialogResult = DialogResult.CANCEL;
	private Billing selectedBilling = null;
	
	public BillingDialog(Frame owner) {
		super(owner, "Abrechnungen", true);
		
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
		
		JScrollPane listScroller = new JScrollPane(lstBillings);
		JButton btnDelete = GUIHelper.createListButton(actionDelete);
		JButton btnOpen = new JButton(actionOpen);
		JButton btnClose = new JButton(actionClose);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(listScroller)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnDelete)
						.addComponent(btnOpen)
						.addComponent(btnClose)
				)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(listScroller)
						
						.addGroup(Alignment.LEADING, layout.createSequentialGroup()
								.addComponent(btnDelete)
						)
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(btnOpen)
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
		/* lstBillings: */
		DataContainer.getInstance().getDataContainerObservable()
			.addObserver((BillingListModel)lstBillings.getModel());
		
		lstBillings.setCellRenderer(new BillingListCellRenderer());
		lstBillings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstBillings.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(lstBillings.getSelectedValue() == null) {
					actionDelete.setEnabled(false);
					actionOpen.setEnabled(false);
				} else {
					actionDelete.setEnabled(true);
					actionOpen.setEnabled(true);
				}
			}
		});
		
		lstBillings.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && lstBillings.getSelectedIndex() > -1) {
					actionOpen.actionPerformed(new ActionEvent(lstBillings,ActionEvent.ACTION_PERFORMED,"doubleClickEdit"));
				}
			}
		});
		
		/* Actions: */
		actionDelete.setEnabled(false);
		actionOpen.setEnabled(false);
	}
	
	private void initKeyBindings(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Close");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Open");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
		
		ActionMap actionMap = component.getActionMap();
		actionMap.put("Open", actionOpen);
		actionMap.put("Close", actionClose);
		actionMap.put("Delete", actionDelete);	
	}
	
	private void cleanUpGui() {
		DataContainer.getInstance().getDataContainerObservable()
			.deleteObserver((BillingListModel)lstBillings.getModel());
	}
	
	/**
	 * Liefert nach Schliessen des Dialogs die ausgewählte Abrechnung.<br/>
	 * Wurde der Dialog per "Abbrechen" geschlossen, wird hier lediglich
	 * <code>null</code> zurückgegeben.
	 * 
	 * @return
	 */
	public Billing getSelectedBilling() {
		return selectedBilling;
	}
	
	public DialogResult getDialogResult() {
		return dialogResult;
	}
	
	// Liste -------------------------------------------------------------------
	private class BillingListModel extends AbstractListModel implements Observer {
		
		@Override
		public Object getElementAt(int index) {
			Billing billing = DataContainer.getInstance().getBillings().get(index);
			return billing;
		}
		
		@Override
		public int getSize() {
			int size = DataContainer.getInstance().getBillings().size();
			return size;
		}
		
		@Override
		public void update(Observable o, Object arg) {
			DataContainerEvent event = (DataContainerEvent)arg;
			
			if(event.getDataEntity() instanceof Billing) {
				int currentSelection = lstBillings.getSelectedIndex();
				int size = getSize();
				fireContentsChanged(o, 0, size);
				
				// Auswahl behalten:
				if(currentSelection > getSize()-1) currentSelection--;
				lstBillings.setSelectedIndex(currentSelection);
				
				if(size == 0) {
					actionOpen.setEnabled(false);
					actionDelete.setEnabled(false);
				} else {
					actionOpen.setEnabled(true);
					actionDelete.setEnabled(true);
				}
			}
		}
		
	}
	
	private class BillingListCellRenderer extends DetailsListCellRenderer {
		
		private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
		
		public BillingListCellRenderer() {
			setIcon(GUIImageManager.getInstance().getImageIcon("table.png"));
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			Billing billing = (Billing)value;
			
			setText(billing.getTitle());
			setDetails("Datum: " + DATE_FORMAT.format(billing.getDate()));
			
			return this;
		}
		
	}
	
	private Billing getSelectedBillingFromList() {
		Object selected = lstBillings.getSelectedValue();
		Billing selectedBilling = null;
		
		if(selected != null) selectedBilling = (Billing)selected;
		
		return selectedBilling;
	}
	
	// Actions -----------------------------------------------------------------
	private class OpenAction extends AbstractAction {
		public OpenAction() {
			super("Öffnen");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Billing selectedBilling = getSelectedBillingFromList();
			
			if(selectedBilling != null) {
				BillingDialog.this.selectedBilling = selectedBilling;
				BillingDialog.this.dialogResult = DialogResult.OK;
				cleanUpGui();
				dispose();
			}
			
		}
	}
	
	private class DeleteAction extends AbstractAction {
		public DeleteAction() {
			super(null, GUIImageManager.getInstance().getImageIcon("delete.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Billing selectedBilling = getSelectedBillingFromList();
			
			if(selectedBilling != null) {
				int confirmResult = JOptionPane.showConfirmDialog(
						BillingDialog.this,
						"Möchten Sie die Abrechnung \"" + selectedBilling.getTitle() + "\" wirklich löschen?",
						"Abrechnung löschen",
						JOptionPane.YES_NO_OPTION);
				
				if(confirmResult == JOptionPane.YES_OPTION) {
					DataContainer.getInstance().deleteDataEntity(selectedBilling);
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
			BillingDialog.this.dialogResult = DialogResult.CANCEL;
			BillingDialog.this.selectedBilling = null;
			cleanUpGui();
			dispose();
		}
	}
	
}
