package net.msites.kaffeekasse.ui.components.tabbeduserinterface;

import net.msites.kaffeekasse.ui.mainframe.WelcomeTab;


public interface TabbedUserInterface {

	/**
	 * Adds a new {@link AbstractEditableTab}.
	 * 
	 * @param text
	 * @param tab
	 * @param select
	 */
	public void addTab(String text, AbstractEditableTab tab, boolean select);

	/**
	 * Returns the currently selected {@link AbstractEditableTab}.
	 * 
	 * @return
	 */
	public AbstractEditableTab getCurrentTab();

	/**
	 * Closes the currently selected Tab, calls the {@link AbstractEditableTab#beforeCloseTab()}
	 * callback before.
	 * 
	 * @see AbstractEditableTab#beforeCloseTab()
	 */
	public void closeCurrentTab();

	/**
	 * Checks if tabs are present.<br/>
	 * Hint: The first tab (mostly the {@link WelcomeTab} gets ignored.
	 * 
	 * @return
	 */
	public boolean hasTabs();

}