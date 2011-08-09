package net.msites.kaffeekasse.ui.dialog.chartdialog;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.guilibrary.toolbox.MultilineLabel;
import net.msites.kaffeekasse.pdf.ChartsPDFFactory;
import net.msites.kaffeekasse.ui.dialog.DialogResult;
import net.msites.kaffeekasse.ui.dialog.WaitDialog;

public class ChartsDialog extends JDialog {

	private final JTabbedPane tbpCharts = new JTabbedPane();
	private final Action actionAddToPdfQueue = new AddToPDFQueue();
	private final Action actionClose = new CloseAction();
	private final Action actionCreatePdf = new CreatePdfAction();
	
	private final List<Chart> pdfQueue = new ArrayList<Chart>();
	
	
	// Konstruktoren -----------------------------------------------------------
	public ChartsDialog(Frame owner) {
		super(owner, "Auswertungen", true);
		
		setSize(750,550);
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
		
		Runnable task = new Runnable() {
			@Override
			public void run() {
				new TotalPerBillingChartTab().addToTabbedPane(tbpCharts);
				new ProductRevenueChartTab().addToTabbedPane(tbpCharts);
				new TopConsumersChartTab().addToTabbedPane(tbpCharts);
			}
		};
		WaitDialog waitDialog = new WaitDialog(
				ChartsDialog.this,
				"Diagramme erstellen",
				"Diagramme werden erstellt... Bitte warten.",
				task);
		waitDialog.setVisible(true);
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
		
		tbpCharts.setFocusable(false);
		JButton btnAddToPdfQueue = GUIHelper.createListButton(actionAddToPdfQueue);
		JButton btnCreatePdf = GUIHelper.createListButton(actionCreatePdf);
		JButton btnClose = new JButton(actionClose);
		
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(tbpCharts)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnAddToPdfQueue)
						.addComponent(btnCreatePdf)
						.addComponent(btnClose)
				)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(tbpCharts)
						
						
						.addGroup(Alignment.LEADING, layout.createSequentialGroup()
								.addComponent(btnAddToPdfQueue)
								.addComponent(btnCreatePdf)
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
		actionCreatePdf.setEnabled(false);
	}
	
	private void initKeyBindings(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Close");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Edit");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "New");
		
		ActionMap actionMap = component.getActionMap();
		actionMap.put("Close", actionClose);		
	}
	
	private void cleanUpGui() {
		
	}
	
	// Actions -----------------------------------------------------------------
	/**
	 * Schliesst diesen {@link ChartsDialog}.
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
	
	private class CreatePdfAction extends AbstractAction {

		public CreatePdfAction() {
			super("PDF erstellen", GUIImageManager.getInstance().getImageIcon("printer_color.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final PdfQueueDialog dialog = new PdfQueueDialog();
			dialog.setVisible(true);
			
			if(dialog.getDialogResult() == DialogResult.OK) {
				ChartsPDFFactory factory = new ChartsPDFFactory(pdfQueue);
				factory.createPdf(ChartsDialog.this, "Auswertungen.pdf");
				
				pdfQueue.clear();
				actionCreatePdf.setEnabled(false);
			} else {
				if(pdfQueue.size() == 0) actionCreatePdf.setEnabled(false);
			}
		}
	}
	
	private class AddToPDFQueue extends AbstractAction {
		
		public AddToPDFQueue() {
			super("Aktuelles Diagramm zum PDF hinzufügen", GUIImageManager.getInstance().getImageIcon("printer_add.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			ChartTab currentTab = (ChartTab)tbpCharts.getSelectedComponent();
			Chart chart = currentTab.getChart();
			
			if(chart.getChart() != null) {
				String name = JOptionPane.showInputDialog(
						ChartsDialog.this,
						"Bitte geben Sie einen Titel für das Diagramm ein:",
						chart.getName());
				
				if(name != null) {
					chart = new Chart(name, chart.getChart());
					pdfQueue.add(chart);
					actionCreatePdf.setEnabled(true);
				}
			}
		}
		
	}
	
	// Hilfs-Dialoge -----------------------------------------------------------
	/**
	 * Zeigt einen Dialog an, welcher die Reihenfolge der Diagramme für das PDF
	 * anpassen kann.
	 */
	private class PdfQueueDialog extends JDialog {
		
		private final JList lstQueue = new JList();
		private DialogResult dialogResult;
		
		public PdfQueueDialog() {
			super(ChartsDialog.this, "PDF vorbereiten", true);
			setSize(350,400);
			GUIHelper.centerOnOwner(this, ChartsDialog.this);
			setResizable(false);
			
			/* Intro: */
			MultilineLabel lblIntro = new MultilineLabel(
					"Vor dem Erstellen des PDF's haben Sie die Möglichkeit, " +
					"ausgewählte Diagramme zu entfernen oder neu zu ordnen.\n" +
					"Klicken Sie auf \"PDF erstellen\" sobald Sie fertig sind."
					);
			
			/* Buttons: */
			final JButton btnCreate = new JButton(new AbstractAction("PDF erstellen") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialogResult = DialogResult.OK;
					dispose();
				}
			});
			final JButton btnCancel = new JButton(new AbstractAction("Abbrechen") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialogResult = DialogResult.CANCEL;
					dispose();
				}
			});
			final JButton btnUp = GUIHelper.createListButton(new AbstractAction("Nach oben verschieben", GUIImageManager.getInstance().getImageIcon("arrow_up.png")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					moveSelectedItem(-1);
				}
			});
			final JButton btnDown = GUIHelper.createListButton(new AbstractAction("Nach unten verschieben", GUIImageManager.getInstance().getImageIcon("arrow_down.png")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					moveSelectedItem(1);
				}
			});
			final JButton btnDelete = GUIHelper.createListButton(new AbstractAction("Diagramm entfernen", GUIImageManager.getInstance().getImageIcon("delete.png")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					int selected = lstQueue.getSelectedIndex();
					
					pdfQueue.remove(selected);
					lstQueue.updateUI();  // dirty ;)
					
					if(pdfQueue.size() == 0) {
						setEnabled(false);
						btnUp.setEnabled(false);
						btnDown.setEnabled(false);
						btnCreate.setEnabled(false);
					} else {
						if(selected > pdfQueue.size()-1) {
							lstQueue.setSelectedIndex(selected-1);
						}
					}
				}
			});
			
			/* Liste initialisieren: */			
			lstQueue.setModel(new AbstractListModel() {
				@Override
				public int getSize() {
					return pdfQueue.size();
				}
				
				@Override
				public Object getElementAt(int index) {
					return pdfQueue.get(index);
				}
			});
			
			lstQueue.setCellRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
					
					Chart chart = (Chart)value;
					setText(chart.getName());
					setIcon(GUIImageManager.getInstance().getImageIcon("chart_bar.png"));
					
					return this;
				}
			});
			
			lstQueue.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if(lstQueue.getSelectedIndex() == 0) btnUp.setEnabled(false);
					else btnUp.setEnabled(true);
					
					if(lstQueue.getSelectedIndex() == pdfQueue.size()-1) btnDown.setEnabled(false);
					else btnDown.setEnabled(true);
				}
			});
			
			lstQueue.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lstQueue.setSelectedIndex(0);  // geht ok, da der dialog nie ohne einträge geöffnet wird
			
			/* Layout: */
			JPanel gui = new JPanel();
			GroupLayout layout = new GroupLayout(gui);
			gui.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			JScrollPane scpCharts = new JScrollPane(lstQueue);
			
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(lblIntro, 50,50,50)
					.addComponent(scpCharts)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnDelete)
							.addComponent(btnUp)
							.addComponent(btnDown)
							.addComponent(btnCreate)
							.addComponent(btnCancel)
					)
			);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addComponent(lblIntro)
							.addComponent(scpCharts)
							
							.addGroup(Alignment.LEADING, layout.createSequentialGroup()
									.addComponent(btnDelete)
									.addComponent(btnUp)
									.addComponent(btnDown)
							)
							.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
									.addComponent(btnCreate)
									.addComponent(btnCancel)
							)
					)
			);
			
			setContentPane(gui);
		}
		
		public DialogResult getDialogResult() {
			return dialogResult;
		}
		
		private void moveSelectedItem(int direction) {
			int selectedIndex = lstQueue.getSelectedIndex();
			int newIndex = selectedIndex+direction;
			
			if(newIndex >= 0 && newIndex < pdfQueue.size()) {
				Chart old = pdfQueue.set(newIndex, pdfQueue.get(selectedIndex));
				pdfQueue.set(selectedIndex, old);
				lstQueue.updateUI(); // dirty
				lstQueue.setSelectedIndex(newIndex);
			}
		}
		
	}
	
}
