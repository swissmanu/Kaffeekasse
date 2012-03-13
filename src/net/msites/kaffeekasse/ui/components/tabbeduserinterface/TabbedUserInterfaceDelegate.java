package net.msites.kaffeekasse.ui.components.tabbeduserinterface;

import javax.swing.JTabbedPane;

/**
 * 
 * 
 * @author Manuel Alabor
 */
public class TabbedUserInterfaceDelegate implements TabbedUserInterface {

	private JTabbedPane tbpTabs;
	
	public TabbedUserInterfaceDelegate() {
		tbpTabs = new JTabbedPane();
	}
	
	/**
	 * Returns the {@link JTabbedPane} instance of this
	 * {@link TabbedUserInterface}. 
	 * 
	 * @return
	 */
	public JTabbedPane getTabs() {
		return tbpTabs;
	}

	@Override
	public void addTab(String text, AbstractEditableTab tab, boolean select) {
		tbpTabs.addTab(text, tab);
		if(select) tbpTabs.setSelectedIndex(tbpTabs.getComponentCount()-1);
	}
	
	@Override
	public AbstractEditableTab getCurrentTab() {
		AbstractEditableTab tab = (AbstractEditableTab)tbpTabs.getSelectedComponent();
		return tab;
	}
	
	@Override
	public void closeCurrentTab() {
		int index = tbpTabs.getSelectedIndex();
		if(index > 0) {
			boolean ok = getCurrentTab().beforeCloseTab();
			if(ok) {
				tbpTabs.removeTabAt(index);
			}
		}
	}

	@Override
	public boolean hasTabs() {
		return tbpTabs.getTabCount() > 1;
	}

}
