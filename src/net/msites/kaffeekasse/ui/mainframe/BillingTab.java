package net.msites.kaffeekasse.ui.mainframe;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.Consumption;
import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.ui.components.BetterJTable;
import net.msites.kaffeekasse.ui.components.VerticalTableHeaderCellRenderer;
import net.msites.kaffeekasse.ui.components.tabbeduserinterface.AbstractEditableTab;
import net.msites.kaffeekasse.ui.dialog.OtherCostsDialog;

/**
 * Beinhaltet eine angepasste {@link JTable} sowie ein passendes
 * {@link TableModel} zur Bearbeitung einer Abrechnung ({@link Billing}).
 * 
 * @author Manuel Alabor
 */
public class BillingTab extends AbstractEditableTab {

	private final JTextField txtTitle = new JTextField();
	private final JFormattedTextField txtDate = new JFormattedTextField(DATE_FORMAT);
	private final BillingJTable tblBilling = new BillingJTable();
	private final JLabel lblTotals = new JLabel(GUIImageManager.getInstance().getImageIcon("calculator.png"));
	
	private final OtherCostsAction actionOtherCosts = new OtherCostsAction();
	private final RemoveConsumerAction actionRemoveConsumer = new RemoveConsumerAction();
	private final AddConsumerAction actionAddConsumer = new AddConsumerAction();
	private final AddProductAction actionAddProduct = new AddProductAction();
	
	
	private Billing billing;
	private List<Consumer> consumers;
	private List<Product> products;
	
	private final static String DATE_FORMAT_PATTERN = "dd.MM.yyyy";
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);
	private final static NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
	
	
	/**
	 * @param billing
	 * @param consumers
	 * @param products
	 */
	public BillingTab(Billing billing, List<Consumer> consumers, List<Product> products) {
		this.billing = billing;
		this.consumers = consumers;
		this.products = products;
		
		initGui();
		buildGui();
	}
	
	
	// GUI-Erstellung ----------------------------------------------------------
	/**
	 * Setzt das GUI zusammen.
	 */
	private void buildGui() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(true);
		setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		
		JLabel lblTitle = new JLabel("Titel");
		JLabel lblDate = new JLabel("Datum");
		JScrollPane table = new JScrollPane(tblBilling);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTitle)
						.addComponent(txtTitle)
						.addComponent(lblDate)
						.addComponent(txtDate)
						)		
				.addComponent(table)
				.addComponent(lblTotals)
				);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(lblTitle)
						.addComponent(txtTitle)
						.addComponent(lblDate)
						.addComponent(txtDate, 100,100,100)
						)
				.addComponent(table)
				.addComponent(lblTotals)
				);
		
	}
	
	/**
	 * Initialisiert das GUI.
	 */
	private void initGui() {
		/* Tabelle initialisieren & anpassen: */
		// Allgemeines:
		tblBilling.setModel(new BillingTableModel(billing));
		
		TableColumnModel columns = tblBilling.getColumnModel();
		VerticalTableHeaderCellRenderer verticalHeaderRenderer = new VerticalTableHeaderCellRenderer();
		for(int i = 0, l = columns.getColumnCount(); i < l; i++) {
			columns.getColumn(i).setHeaderRenderer(verticalHeaderRenderer);			
		}
		
		// Consumer-Spalte:
		columns.getColumn(0).setCellRenderer(new ConsumerTableCellRenderer());
		columns.getColumn(0).setPreferredWidth(150);
		
		// Andere Kosten- & Totalspalte:
		CurrencyTableCellRenderer currencyCellRenderer = new CurrencyTableCellRenderer();
		columns.getColumn(columns.getColumnCount()-3).setCellRenderer(currencyCellRenderer);
		columns.getColumn(columns.getColumnCount()-2).setCellRenderer(currencyCellRenderer);
		
		// Product-Spalten:
		AmountTableCellRenderer amountCellRenderer = new AmountTableCellRenderer();
		AmountTableCellEditor amountCellEditor = new AmountTableCellEditor();
		columns.getColumn(columns.getColumnCount()-1).setHeaderRenderer(verticalHeaderRenderer);
		for(int i = 0, l = products.size(); i < l; i++) {
			TableColumn column = columns.getColumn(i+1);
			column.setPreferredWidth(30);
			column.setCellRenderer(amountCellRenderer);
			column.setCellEditor(amountCellEditor);
		}
		
		// Bezahlt-Spalte:
		TableColumn paidColumn = columns.getColumn(columns.getColumnCount()-1); 
		paidColumn.setCellRenderer(new CheckBoxTableCellRenderer());
		paidColumn.setCellEditor(new CheckBoxTableCellEditor());
		paidColumn.setPreferredWidth(30);
		
		/* Textfelder: */
		txtTitle.setFont(txtTitle.getFont().deriveFont(Font.BOLD));
		txtTitle.setText(billing.getTitle());
		txtTitle.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) { billing.setTitle(txtTitle.getText()); }
			@Override
			public void insertUpdate(DocumentEvent e) { billing.setTitle(txtTitle.getText()); }
			@Override
			public void changedUpdate(DocumentEvent e) { billing.setTitle(txtTitle.getText()); }
		});
		
		try {
			txtDate.setText(txtDate.getFormatter().valueToString(billing.getDate()));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		/* Abrechnungstotal: */
		lblTotals.setFont(lblTotals.getFont().deriveFont(Font.BOLD));
		recalculateBillingTotals();
	}
	
	// BillingJTable & BillingTableModel ---------------------------------------
	/**
	 * Angepasste {@link JTable}.<br/>
	 * Ansteuerung per Tastatur wird so modifiziert, dass per Tabulatur usw.
	 * jeweils zur nächsten Produkte-Zelle gesprungen wird.
	 */
	private class BillingJTable extends BetterJTable {
		
		public BillingJTable() {
			// Anpassung:
			setRowHeight(20);
			setOpaque(true);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			// KeyListener:
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					boolean consume = false;
					
					switch(e.getKeyCode()) {
					case KeyEvent.VK_RIGHT :
					case KeyEvent.VK_ENTER :
						selectNextProductCell();
						consume = true;
						break;
					case KeyEvent.VK_TAB :
						if(e.getModifiers() == KeyEvent.SHIFT_MASK) {
							selectPreviousProductCell();
						} else {
							selectNextProductCell();
						}
						consume = true;
						break;
					case KeyEvent.VK_LEFT :
						selectPreviousProductCell();
						consume = true;
						break;
					}
					
					if(consume) {
						if(getCellEditor() != null) getCellEditor().stopCellEditing();
						e.consume();
					}
				}
			});
			
			// MouseListener:
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					int firstColWidth = tblBilling.getColumnModel().getColumn(0).getWidth();
					
					if(e.getButton() == 3) {  // nicht cross-platform tauglich, aber egal
						int row = rowAtPoint(e.getPoint());
						if(row != -1) setRowSelectionInterval(row, row);
						
						Consumer selectedConsumer = getSelectedConsumer();
						if(selectedConsumer != null) {
							actionOtherCosts.putValue(Action.NAME,
									"\"Andere Kosten\" für " +
									selectedConsumer.getFullname() +
									" anzeigen...");
							actionRemoveConsumer.putValue(Action.NAME,
									selectedConsumer.getFullname() +
									" entfernen...");
							
							JPopupMenu popup = new JPopupMenu();
							popup.add(actionOtherCosts);
							popup.add(actionRemoveConsumer);
							popup.addSeparator();
							popup.add(actionAddConsumer);
							popup.add(actionAddProduct);
							
							popup.show(BillingJTable.this, e.getX(), e.getY());
						}
						
						
					} else if(e.getX() <= firstColWidth && e.getClickCount() == 2) {
						actionOtherCosts.actionPerformed(
								new ActionEvent(BillingJTable.this, 0, "doubleclick"));
					} 
				}
				
			});
		}
		
		@Override
		public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
			if(columnIndex == 0) columnIndex++;
			super.changeSelection(rowIndex, columnIndex, toggle, extend);
		}
		
		private void selectNextProductCell() {
			int currentRow = getSelectedRow();
			int currentCol = getSelectedColumn();
			int totalRows = getRowCount();
			int totalCols = getColumnCount();
			int firstProductCol = 1;
			int lastProductCol = totalCols - 4;
			int nextRow;
			int nextCol;
			
			if(currentCol >= lastProductCol) {
				nextRow = currentRow + 1;
				if(nextRow >= totalRows) nextRow = 0;
				nextCol = firstProductCol;
			} else {
				nextRow = currentRow;
				nextCol = currentCol + 1;
			}
			
			changeSelection(nextRow, nextCol, false, false);
		}
		
		private void selectPreviousProductCell() {
			int currentRow = getSelectedRow();
			int currentCol = getSelectedColumn();
			int totalRows = getRowCount();
			int totalCols = getColumnCount();
			int firstProductCol = 1;
			int lastProductCol = totalCols - 4;
			int nextRow;
			int nextCol;
			
			if(currentCol <= firstProductCol) {
				nextRow = currentRow - 1;
				if(nextRow < 0) nextRow = totalRows - 1;
				nextCol = lastProductCol;
			} else {
				nextRow = currentRow;
				nextCol = currentCol - 1;
			}
			
			changeSelection(nextRow, nextCol, false, false);
		}
		
		/**
		 * Veranlasst die Neuberechnung des Totals auf der Zeile <code>row</code>.
		 * 
		 * @param row
		 */
		public void recalculateTotal(int row) {
			((BillingTableModel)getModel()).recalculateTotal(row);
		}
		
		public Consumer getSelectedConsumer() {
			Consumer consumer = null;
			int selectedRow = getSelectedRow();
			
			if(selectedRow >= 0 && selectedRow < consumers.size()) {
				consumer = consumers.get(selectedRow);
			}
			
			return consumer;
		}
		
	}
	
	/**
	 * Passendes {@link TableModel} zur {@link BillingJTable}.
	 */
	private class BillingTableModel extends AbstractTableModel {
		
		private Billing billing;
		
		public BillingTableModel(Billing billing) {
			this.billing = billing;
		}
		
		@Override
		public int getColumnCount() {
			int count = products.size() + 4;  // Consumers + n*Products + Anderes + Total + Bezahlt
			return count;
		}
		
		@Override
		public int getRowCount() {
			return consumers.size();  // Consumers
		}
		
		@Override
		public String getColumnName(int column) {
			String title = "";
			
			if(column > 0 && column <= products.size()) {
				title = products.get(column-1).getName();
			} else if(column == getColumnCount()-3) {
				title = "Andere Kosten";
			} else if(column == getColumnCount()-2) {
				title = "Total";
			} else if(column == getColumnCount()-1) {
				title = "Bezahlt";
			}
			
			return title;
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object value = "";
			
			if(columnIndex == 0) {
				/* Konsumenten-Spalte: */
				value = consumers.get(rowIndex);
				
			} else if(columnIndex >= 1 && columnIndex < getColumnCount()-3) {
				/* Produkte-Spalten: */
				Consumer consumer = consumers.get(rowIndex);
				Product product = products.get(columnIndex-1);
				
				BillingPosition billingPosition = this.billing.getBillingPosition(consumer);
				if(billingPosition != null) {
					Consumption consumptation = billingPosition.getConsumption(product);
					if(consumptation != null) {
						value = consumptation.getAmount();
					}
				}
				
			} else if(columnIndex == getColumnCount()-3) {
				/* Andere Kosten-Spalte: */
				Double totalOtherCosts = 0d;
				Consumer consumer = consumers.get(rowIndex);
				
				BillingPosition billingPosition = this.billing.getBillingPosition(consumer);
				if(billingPosition != null) {
					totalOtherCosts = billingPosition.calculateTotalOtherCosts();
				}
				
				value = totalOtherCosts;
				
			} else if(columnIndex == getColumnCount()-2) {
				/* Totalspalte: */
				Double total = 0d;
				Consumer consumer = consumers.get(rowIndex);
				
				BillingPosition billingPosition = this.billing.getBillingPosition(consumer);
				if(billingPosition != null) {
					total = billingPosition.calculateTotalConsumptions();
					total += billingPosition.calculateTotalOtherCosts();
				}
				
				value = total;
			} else if(columnIndex == getColumnCount()-1) {
				/* Bezahlt-Spalte: */
				Boolean paid = false;
				Consumer consumer = consumers.get(rowIndex);
				BillingPosition billingPosition = this.billing.getBillingPosition(consumer);
				
				if(billingPosition != null) {
					paid = (Boolean)billingPosition.isPaid();
				}
				
				value = paid;
			}
			
			return value;
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			boolean editable = true;
			if(columnIndex == 0 || columnIndex == getColumnCount()-3 || columnIndex == getColumnCount()-2) editable = false;
			
			return editable;
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			String stringValue = aValue.toString();
			Consumer consumer = consumers.get(rowIndex);
			BillingPosition billingPosition = billing.getBillingPosition(consumer);
			
			if(billingPosition == null) {
				billingPosition = new BillingPosition();
				billingPosition.setBilling(billing);
				billingPosition.setConsumer(consumer);
				billing.addBillingPosition(billingPosition);
			}
			
			if(columnIndex != getColumnCount()-1) {
				/* Produkt-Spalten: */
				Product product = products.get(columnIndex-1);
				
				Consumption consumption = billingPosition.getConsumption(product);
				if(consumption == null) {
					consumption = new Consumption();
					consumption.setBillingPosition(billingPosition);
					consumption.setProduct(product);
					billingPosition.addConsumption(consumption);
				}
				
				if(stringValue.equals("")) {
					billingPosition.removeConsumption(consumption);
					billingPosition.setChanged();
					
					if(billingPosition.getConsumptions().size() == 0
							&& billingPosition.getOtherCosts().size() == 0) {
						billing.removeBillingPosition(billingPosition);
					}
				} else {
					consumption.setAmount(Double.parseDouble(stringValue));
					consumption.setChanged();
				}
				
			} else {
				/* Bezahlt-Spalte: */
				billingPosition.setPaid(Boolean.parseBoolean(stringValue));
			}

			BillingTab.this.billing.setChanged();  // dirty ;)
			recalculateBillingTotals();
		}
		
		public void recalculateTotal(int row) {
			fireTableCellUpdated(row, getColumnCount()-2);
		}
		
		public void rowRemoved(int index) {
			fireTableRowsDeleted(index, index);
		}
		
		public void rowAdded(int index) {
			fireTableRowsInserted(index, index);
		}
		
	}
	
	// Hilfsklassen für JTable -------------------------------------------------
	/**
	 * Renderer für {@link Consumer}-Spalte.
	 */
	private class ConsumerTableCellRenderer extends JLabel implements TableCellRenderer {
		public ConsumerTableCellRenderer() {
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setIcon(GUIImageManager.getInstance().getImageIcon("user.png"));
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if(value != null) {
				Consumer consumer = (Consumer)value;
				setText(consumer.getFullname());
			}
			return this;
		}
		
		/**
		 * Dirty Fix ;)
		 */
		@Override
		public boolean isOpaque() {
			return true;
		}
	}
	
	/**
	 * Renderer für {@link Product}-Bezugs-Spalte.
	 */
	private class AmountTableCellRenderer extends DefaultTableCellRenderer {
		
		private final DecimalFormat format = new DecimalFormat("0.#");
		
		public AmountTableCellRenderer() {
			setHorizontalAlignment(SwingConstants.CENTER);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			String text = "";
			if(value != null && value instanceof Double) {
				Double d = (Double)value;
				text = format.format(d);
			}
			
			setText(text);
			return this;
		}
	}

	/**
	 * Renderer für {@link Product}-Bezugs-Spalte.
	 */
	private class AmountTableCellEditor extends DefaultCellEditor {
		
		public AmountTableCellEditor() {
			super(new JTextField());
			((JTextField)getComponent()).setHorizontalAlignment(SwingConstants.CENTER);
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			JTextField editor = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
			
			editor.setSelectionStart(0);
			editor.setSelectionEnd(editor.getText().length());
			
			return editor;
		}
		
		@Override
		public boolean stopCellEditing() {
			boolean result = super.stopCellEditing();
			
			if(result) {
				((BillingJTable)tblBilling).recalculateTotal(tblBilling.getSelectedRow());
			}
			
			return result;
		}

	}
	
	/**
	 * Renderer für Total-Spalte.
	 */
	private class CurrencyTableCellRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setText(CURRENCY_FORMAT.format(value));
			
			return this;
		}
		
	}
	
	/**
	 * Renderer für Bezahlt-Spalte (Boolean-Wert wird mittels {@link JCheckBox}
	 * dargestellt.
	 */
	private class CheckBoxTableCellRenderer extends JCheckBox implements TableCellRenderer {
		
		public CheckBoxTableCellRenderer() {
			setHorizontalAlignment(SwingConstants.CENTER);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setSelected((Boolean)value);
			
			if(isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
			
			return this;
		}
	}
	
	/**
	 * Editor für Bezahlt-Spalte (Boolean-Wert wird mittels {@link JCheckBox}
	 * bearbeitet)
	 */
	private class CheckBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor {
		
		private final JCheckBox chkEditor = new JCheckBox();
		
		public CheckBoxTableCellEditor() {
			chkEditor.setHorizontalAlignment(SwingConstants.CENTER);
			chkEditor.setFocusable(false);
			chkEditor.setBorder(null);
			setBorder(null);
			
			chkEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stopCellEditing();
				}
			});
		}
		
		@Override
		public Object getCellEditorValue() {
			return (Boolean)chkEditor.isSelected();
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			chkEditor.setBackground(table.getSelectionBackground());
			chkEditor.setForeground(table.getSelectionForeground());
			
			chkEditor.setSelected((Boolean)value);
			
			return chkEditor;
		}
		
	}
	
	// Berechnung Abrechnungstotal ---------------------------------------------
	private void recalculateBillingTotals() {
		double total = 0d;
		double totalPaid = 0d;
		double pending = 0d;
		
		for(BillingPosition billingPosition: billing.getBillingPositions()) {
			double positionTotal = billingPosition.calculateTotalConsumptions()
								 + billingPosition.calculateTotalOtherCosts();
			total += positionTotal;
			if(billingPosition.isPaid()) totalPaid += positionTotal;
		}
		
		pending = total - totalPaid;
		
		if(pending == 0) {
			lblTotals.setIcon(GUIImageManager.getInstance().getImageIcon("accept.png"));
		} else {
			lblTotals.setIcon(GUIImageManager.getInstance().getImageIcon("calculator.png"));
		}
		
		lblTotals.setText(
				"Abrechnungstotal: " + CURRENCY_FORMAT.format(total) + 
				", davon bezahlt: " + CURRENCY_FORMAT.format(totalPaid) + 
				". Ausstehend: " + CURRENCY_FORMAT.format(total - totalPaid));
	}
	
	
	// EditableTab-Implementierung ---------------------------------------------
	@Override
	public boolean hasChanged() {
		return true;
	}
	
	@Override
	public boolean canSave() {
		return true;
	}
	
	@Override
	public DataEntity getData() {
		return billing;
	}
	
	@Override
	public void save() {
		/* Daten übernehmen: */
		// Titel & Datum müssen übernommen werden. Die restlichen Informationen
		// hat das BillingTableModel während dem bearbeiten bereits in die 
		// DataEntity geschrieben.
		billing.setTitle(txtTitle.getText());
		try {
			billing.setDate(DATE_FORMAT.parse(txtDate.getText()));			
		} catch(ParseException e) {
			e.printStackTrace();
		}
		
		/* Speichern: */
		DataContainer.getInstance().saveDataEntity(billing);
	}
	
	public boolean beforeCloseTab() {
		boolean close = true;
		
		if(billing.hasChanged()) {
			int result = JOptionPane.showConfirmDialog(
					SwingUtilities.getWindowAncestor(this),
					"Möchten Sie die Änderungen an dieser Abrechnung speichern?",
					"Änderungen speichern",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			
			switch(result) {
			case JOptionPane.YES_OPTION :
				save();
				break;
			case JOptionPane.CANCEL_OPTION :
				close = false;
				break;
			}
		}
		
		return close;
	}
	
	
	// Actions -----------------------------------------------------------------
	/**
	 * Zeigt den {@link OtherCostsDialog} für den aktuell in der Tabelle
	 * ausgewählten {@link Consumer} an.
	 */
	private class OtherCostsAction extends AbstractAction {
		
		public OtherCostsAction() {
			super("", GUIImageManager.getInstance().getImageIcon("money.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Consumer consumer = tblBilling.getSelectedConsumer();
			
			BillingPosition billingPosition = billing.getBillingPosition(consumer);
			boolean isNewPosition = false;
			if(billingPosition == null) {
				billingPosition = new BillingPosition();
				billingPosition.setBilling(billing);
				billingPosition.setConsumer(consumer);
				isNewPosition = true;
			}
			
			OtherCostsDialog otherCostsDialog = new OtherCostsDialog(
					SwingUtilities.getWindowAncestor(BillingTab.this),
					billingPosition);
			otherCostsDialog.setVisible(true);
			
			if(isNewPosition) {
				if(billingPosition.getOtherCosts().size() > 0) {
					billing.addBillingPosition(billingPosition);
				}
			}
			
			tblBilling.updateUI();  // Dirty ;)
			recalculateBillingTotals();
		}
	}
	
	/**
	 * Entfernt den aktuell gewählten {@link Consumer} aus der aktuell 
	 * bearbeiteten Abrechnung.
	 */
	private class RemoveConsumerAction extends AbstractAction {
		
		public RemoveConsumerAction() {
			super("", GUIImageManager.getInstance().getImageIcon("user_delete.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Consumer selectedConsumer = tblBilling.getSelectedConsumer();
			int selectedRow = tblBilling.getSelectedRow();
			
			int result = JOptionPane.showConfirmDialog(
					SwingUtilities.getWindowAncestor(BillingTab.this),
					"Möchten Sie " + selectedConsumer.getFullname() + " wirklich " +
					"aus dieser Abrechnung entfernen?\n" +
					"Alle erfassten Daten innerhalb dieser Abrechnung werden damit " +
					"unwiederruflich gelöscht!",
					selectedConsumer.getFullname() + " entfernen?",
					JOptionPane.YES_NO_OPTION);
			
			if(result == JOptionPane.YES_OPTION) {
				BillingPosition billingPosition = billing.getBillingPosition(selectedConsumer);
				consumers.remove(selectedConsumer);
				billing.removeBillingPosition(billingPosition);
				
				((BillingTableModel)tblBilling.getModel()).rowRemoved(selectedRow);
				recalculateBillingTotals();
			}
		}
	}
	
	private class AddConsumerAction extends AbstractAction {
		
		public AddConsumerAction() {
			super("Konsument hinzufügen...", GUIImageManager.getInstance().getImageIcon("user_add.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			List<Consumer> allConsumers = DataContainer.getInstance().getConsumers();
			List<Consumer> possibleConsumers = new ArrayList<Consumer>();
			
			for(Consumer consumer: allConsumers) {
				if(!consumers.contains(consumer)) possibleConsumers.add(consumer);
			}
			
			if(possibleConsumers.size() > 0) {
				Object selected = JOptionPane.showInputDialog(
						SwingUtilities.getWindowAncestor(BillingTab.this),
						"Bitte wählen Sie den hinzuzufügenden Konsumenten aus und" +
						"klicken Sie auf \"OK\".",
						"Konsument hinzufügen",
						JOptionPane.PLAIN_MESSAGE,
						null,
						possibleConsumers.toArray(),
						null);
				
				if(selected != null) {
					Consumer selectedConsumer = (Consumer)selected;
					consumers.add(selectedConsumer);
					Collections.sort(consumers);
					
					int index = consumers.indexOf(selectedConsumer);
					((BillingTableModel)tblBilling.getModel()).rowAdded(index);
					tblBilling.setRowSelectionInterval(index, index);
				}
			} else {
				JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor(BillingTab.this),
						"Alle möglichen Konsumenten wurden dieser Abrechnung " +
						"bereits hinzugefügt.\n\n" +
						"Klicken Sie in der Symbolleiste auf \"Konsumenten anzeigen\" falls " +
						"Sie einen komplett\n" +
						"neuen Konsumenten erfassen möchten und wiederholen Sie " +
						"den Vorgang.",
						"Konsument hinzufügen",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	private class AddProductAction extends AbstractAction {
		
		public AddProductAction() {
			super("Produkt hinzufügen...", GUIImageManager.getInstance().getImageIcon("package_green_add.png"));
			setEnabled(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			List<Product> allProducts = DataContainer.getInstance().getProducts();
			List<Product> possibleProducts = new ArrayList<Product>();
			
			for(Product product: allProducts) {
				if(!products.contains(product)) possibleProducts.add(product);
			}
			
			if(possibleProducts.size() > 0) {
				Object selected = JOptionPane.showInputDialog(
						SwingUtilities.getWindowAncestor(BillingTab.this),
						"Bitte wählen Sie das hinzuzufügenden Produkt aus und" +
						"klicken Sie auf \"OK\".",
						"Produkt hinzufügen",
						JOptionPane.PLAIN_MESSAGE,
						null,
						possibleProducts.toArray(),
						null);
				
				if(selected != null) {
					Product selectedProduct = (Product)selected;
					products.add(selectedProduct);
					Collections.sort(products);
					
					@SuppressWarnings("unused")
					int index = products.indexOf(selectedProduct);
				}
			} else {
				JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor(BillingTab.this),
						"Alle möglichen Produkte wurden dieser Abrechnung " +
						"bereits hinzugefügt.\n\n" +
						"Klicken Sie in der Symbolleiste auf \"Produkte anzeigen\" falls " +
						"Sie ein komplett\n" +
						"neues Produkt erfassen möchten und wiederholen Sie " +
						"den Vorgang.",
						"Produkt hinzufügen",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
}