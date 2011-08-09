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
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.Language;
import net.msites.kaffeekasse.ui.components.DetailsListCellRenderer;
import net.msites.kaffeekasse.ui.dialog.editordialog.ConsumerEditor;
import net.msites.kaffeekasse.ui.dialog.editordialog.EditorDialog;

public class ConsumerDialog extends JDialog {

	private final JList lstConsumers = new JList(new ConsumerListModel());
	private final Action actionAdd = new AddAction();
	private final Action actionEdit = new EditAction();
	private final Action actionDelete = new DeleteAction();
	private final Action actionClose = new CloseAction();
	
	
	// Konstruktoren -----------------------------------------------------------
	public ConsumerDialog(Frame owner) {
		super(owner, "Konsumenten", true);
		
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
	
	
	// GUI-Erstellung ----------------------------------------------------------
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
		
		JScrollPane listScroller = new JScrollPane(lstConsumers);
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
		/* lstConsumers: */
		DataContainer.getInstance().getDataContainerObservable().addObserver((ConsumerListModel)lstConsumers.getModel());
		lstConsumers.setCellRenderer(new ConsumerListCellRenderer());
		lstConsumers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstConsumers.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(lstConsumers.getSelectedValue() == null) {
					actionEdit.setEnabled(false);
					actionDelete.setEnabled(false);
				} else {
					actionEdit.setEnabled(true);
					actionDelete.setEnabled(true);
				}
			}
		});
		
		lstConsumers.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && lstConsumers.getSelectedIndex() > -1) {
					actionEdit.actionPerformed(new ActionEvent(lstConsumers,ActionEvent.ACTION_PERFORMED,"doubleClickEdit"));
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
			.deleteObserver((ConsumerListModel)lstConsumers.getModel());
	}
	
	// Konsumentenliste --------------------------------------------------------
	private class ConsumerListModel extends AbstractListModel implements Observer {
		
		@Override
		public Object getElementAt(int index) {
			Consumer consumer = DataContainer.getInstance().getConsumers().get(index);
			return consumer;
		}
		
		@Override
		public int getSize() {
			int size = DataContainer.getInstance().getConsumers().size();
			return size;
		}
		
		@Override
		public void update(Observable o, Object arg) {
			DataContainerEvent event = (DataContainerEvent)arg;
			
			if(event.getDataEntity() instanceof Consumer) {
				int currentSelection = lstConsumers.getSelectedIndex();
				int size = getSize();
				fireContentsChanged(o, 0, size);
				
				// Auswahl behalten:
				if(currentSelection > getSize()-1) currentSelection--;
				lstConsumers.setSelectedIndex(currentSelection);		
				
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
	
	private class ConsumerListCellRenderer extends DetailsListCellRenderer {
		
		public ConsumerListCellRenderer() {
			setIcon(GUIImageManager.getInstance().getImageIcon("user.png"));
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			Consumer consumer = (Consumer)value;
			setText(consumer.getFullname());
			
			String language = "Deutsch";
			if(consumer.getLanguage() == Language.ENGLISH) language = "Englisch";
			setDetails("E-Mail: " + consumer.getEmail() + ", Sprache: " + language);
			
			return this;
		}
		
	}
	
	private Consumer getSelectedConsumer() {
		Object selected = lstConsumers.getSelectedValue();
		Consumer selectedConsumer = null;
		
		if(selected != null) selectedConsumer = (Consumer)selected;
		
		return selectedConsumer;
	}
	
	// Actions -----------------------------------------------------------------
	/**
	 * Zeigt einen {@link EditorDialog} zwecks Hinzufügen eines neuen Konsumenten
	 * an.
	 */
	private class AddAction extends AbstractAction {
		public AddAction() {
			super(null, GUIImageManager.getInstance().getImageIcon("add.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			EditorDialog editorDialog = new EditorDialog(
					ConsumerDialog.this,
					new ConsumerEditor(new Consumer()),
					"Neuer Konsument");
			editorDialog.setVisible(true);
			
			if(editorDialog.getDialogResult() == DialogResult.OK) {
				// Speichern:
				Consumer newConsumer = (Consumer)editorDialog.getData();
				DataContainer.getInstance().saveDataEntity(newConsumer);
				lstConsumers.setSelectedValue(newConsumer, true);
			}
		}
	}
	
	/**
	 * Bearbeitet den aktuell gewählten {@link Consumer} in einem {@link EditorDialog}.
	 */
	private class EditAction extends AbstractAction {
		public EditAction() {
			super("Bearbeiten");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Consumer selectedConsumer = getSelectedConsumer();
			
			if(selectedConsumer != null) {
				EditorDialog editorDialog = new EditorDialog(
						ConsumerDialog.this,
						new ConsumerEditor(selectedConsumer),
						"Konsument bearbeiten");
				editorDialog.setVisible(true);
				
				if(editorDialog.getDialogResult() == DialogResult.OK) {
					// Daten übernehmen:
					Consumer editedConsumer = (Consumer)editorDialog.getData();
					selectedConsumer.setLastname(editedConsumer.getLastname());
					selectedConsumer.setFirstname(editedConsumer.getFirstname());
					selectedConsumer.setEmail(editedConsumer.getEmail());
					selectedConsumer.setLanguage(editedConsumer.getLanguage());
					selectedConsumer.setAddToNewBillings(editedConsumer.getAddToNewBillings());
					
					DataContainer.getInstance().saveDataEntity(selectedConsumer);
				}				
			}
			
		}
	}
	
	/**
	 * Löscht den aktuell gewählten {@link Consumer}.
	 */
	private class DeleteAction extends AbstractAction {
		public DeleteAction() {
			super(null, GUIImageManager.getInstance().getImageIcon("delete.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Consumer selectedConsumer = getSelectedConsumer();
			
			if(selectedConsumer != null) {
				if(DataContainer.getInstance().isUsedInBillings(selectedConsumer)) {
					JOptionPane.showMessageDialog(
						ConsumerDialog.this,
						"Der gewählte Konsument kann nicht gelöscht werden, " +
						"da er/sie mindestens\n" +
						"in einer Abrechnung verwendet wird.",
						"Konsument löschen",
						JOptionPane.ERROR_MESSAGE);
				} else {
					int confirmResult = JOptionPane.showConfirmDialog(
							ConsumerDialog.this,
							"Möchten Sie den Konsumenten \"" + selectedConsumer.getFullname() + "\" wirklich löschen?",
							"Konsument löschen",
							JOptionPane.YES_NO_OPTION);
					
					if(confirmResult == JOptionPane.YES_OPTION) {
						DataContainer.getInstance().deleteDataEntity(selectedConsumer);
						lstConsumers.updateUI();						
					}						
				}
				
			}
		}
	}
	
	/**
	 * Schliesst diesen {@link ConsumerDialog}.
	 */
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
