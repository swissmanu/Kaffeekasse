package net.msites.kaffeekasse.ui.components.checkablejlist;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import net.msites.kaffeekasse.ui.dialog.chartdialog.ChartTab;

/**
 * Erweitert die {@link JList} so, dass per Checkbox einzelne Elemente der Liste
 * markiert werden können.
 * 
 * @author Manuel Alabor
 * @see AbstractCheckableListModel
 * @see CheckableListCellRenderer
 * @see CheckableDetailsListCellRenderer
 */
public class CheckableJList extends JList {
	
	public CheckableJList() {
		this(new AbstractCheckableListModel() {
			@Override
			public int getSize() { return 0; }
			@Override
			public Object getElementAt(int index) { return null; }
		});
	}
	
	public CheckableJList(CheckableListModel model) {
		super(model);
		initList();
	}
	
	private void initList() {
		setCellRenderer(new CheckableListCellRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getX() <= 20) {
					int index = locationToIndex(e.getPoint());
					
					if(index > -1) {
						boolean checked = getModel().isChecked(index);
						getModel().setChecked(index, !checked);
					}
				}
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SPACE) {
					int index = getSelectedIndex();
					
					if(index > -1) {
						boolean checked = getModel().isChecked(index);
						getModel().setChecked(index, !checked);
					}
				}
			}
		});
	}
	
	// Vereinfachte Zugriffe aufs Model ----------------------------------------
	@Override
	public CheckableListModel getModel() {
		return (CheckableListModel)super.getModel();
	}
	
	public boolean isChecked(Object element) {
		boolean checked = getModel().isChecked(element);
		return checked;
	}
	
	public void setChecked(Object element, boolean checked) {
		getModel().setChecked(element, checked);
	}
	
	public List<Object> getCheckedElements() {
		List<Object> list = getModel().getCheckedElements();
		return list;
	}
	
	public void setModel(CheckableListModel model, boolean checkAllByDefault) {
		super.setModel(model);
		for(int i = 0, l = model.getSize(); i < l; i++) {
			setChecked(model.getElementAt(i), checkAllByDefault);
		}
	}
	
	// Actions -----------------------------------------------------------------
	public static AbstractAction buildSelectAllItemsAction(CheckableJList list) {
		return new SelectAllItemsAction(list);
	}
	
	/**
	 * Ermöglicht das auswählen bez. das entfernen aller Markierungen in einer
	 * {@link CheckableJList} eines {@link ChartTab}'s.<br/>
	 */
	private static class SelectAllItemsAction extends AbstractAction {
		
		private final CheckableJList list;
		
		public SelectAllItemsAction(CheckableJList list) {
			super("Alle/keine auswählen");
			this.list = list;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean allItemsAreChecked = true;
			AbstractCheckableListModel model = (AbstractCheckableListModel)list.getModel();
			
			for(int i = 0, l = model.getSize(); i < l; i++) {
				if(!list.isChecked(model.getElementAt(i))) {
					allItemsAreChecked = false;
					break;
				}
			}
			
			boolean newValue = !allItemsAreChecked;
			for(int i = 0, l = model.getSize(); i < l; i++) {
				list.setChecked(model.getElementAt(i), newValue);
			}
			
			model.fireCompleteContentsChanged(this);
		}
	}
	
//	// Testing -----------------------------------------------------------------
//	public static void main(String[] args) {
//		GUIHelper.useJGoodiesLooks();
//		
//		JFrame frame = new JFrame("CheckableJList");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setSize(500,300);
//		frame.setLocation(200,200);
//		
//		JPanel content = new JPanel(new BorderLayout());
//		content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
//		
//		AbstractCheckableListModel model = new AbstractCheckableListModel() {
//			String[] strings = new String[]{
//					"Dies",
//					"ist",
//					"ein",
//					"Test",
//					"für"
//			};
//			
//			@Override
//			public int getSize() {
//				return strings.length;
//			}
//			
//			@Override
//			public String getElementAt(int index) {
//				return strings[index];
//			}
//		};
//		
//		CheckableJList lstCheckableList = new CheckableJList(model);
//		lstCheckableList.setCellRenderer(new CheckableDetailsListCellRenderer() {
//			@Override
//			public Component getListCellRendererComponent(JList list,
//					Object value, int index, boolean isSelected,
//					boolean cellHasFocus) {
//				super.getListCellRendererComponent(list, value, index, isSelected,
//						cellHasFocus);
//				
//				setDetails("Test");
//				
//				return this;
//			}
//		});
//		content.add(new JScrollPane(lstCheckableList), BorderLayout.CENTER);
//		
//		frame.setContentPane(content);
//		frame.setVisible(true);
//	}
//	
}
