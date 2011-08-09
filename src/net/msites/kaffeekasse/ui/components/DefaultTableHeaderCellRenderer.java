/**
 * @(#)DefaultTableHeaderCellRenderer.java	1.0 02/24/09
 */
package net.msites.kaffeekasse.ui.components;

import java.awt.Component;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A default cell renderer for a JTableHeader.
 * <P>
 * Extends {@link DefaultTableCellRenderer}.
 * <P>
 * DefaultTableHeaderCellRenderer attempts to provide identical behavior to the
 * renderer which the Swing subsystem uses by default, the Sun proprietary
 * class sun.swing.table.DefaultTableCellHeaderRenderer.
 * <P>
 * To apply any desired customization, DefaultTableHeaderCellRenderer may be
 * suitably extended.
 * 
 * @author Darryl
 */
public class DefaultTableHeaderCellRenderer extends DefaultTableCellRenderer {

   /**
    * Constructs a <code>DefaultTableHeaderCellRenderer</code>.
    * <P>
    * The horizontal alignment and text position are set as appropriate to a
    * table header cell, and the renderer is set to be non-opaque.
    */
   public DefaultTableHeaderCellRenderer() {
      setHorizontalAlignment(CENTER);
      setHorizontalTextPosition(LEFT);
      setOpaque(false);
   }

   /**
    * Returns the default table header cell renderer.
    * <P>
    * The icon is set as appropriate for the header cell of a sorted or
    * unsorted column, and the border appropriate to a table header cell is
    * applied.
    * <P>
    * Subclasses may overide this method to provide custom content or
    * formatting.
    * 
    * @param table the <code>JTable</code>.
    * @param value the value to assign to the header cell
    * @param isSelected This parameter is ignored.
    * @param hasFocus This parameter is ignored.
    * @param row This parameter is ignored.
    * @param column the column of the header cell to render
    * @return the default table header cell renderer
    * 
    * @see DefaultTableCellRenderer#getTableCellRendererComponent(JTable,
    * Object, boolean, boolean, int, int)
    */
   @Override
   public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);
      setIcon(getIcon(table, column));
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      return this;
   }

   /**
    * Overloaded to return an icon suitable to a sorted column, or null if
    * the column is unsorted.
    * 
    * @param table the <code>JTable</code>.
    * @param column the colummn index.
    * @return the sort icon, or null if the column is unsorted.
    */
   protected Icon getIcon(JTable table, int column) {
      SortKey sortKey = getSortKey(table, column);
      if (sortKey != null && sortKey.getColumn() == column) {
         SortOrder sortOrder = sortKey.getSortOrder();
         switch (sortOrder) {
            case ASCENDING:
               return UIManager.getIcon("Table.ascendingSortIcon");
            case DESCENDING:
               return UIManager.getIcon("Table.descendingSortIcon");
         }
      }
      return null;
   }

   protected SortKey getSortKey(JTable table, int column) {
      RowSorter<?> rowSorter = table.getRowSorter();
      if (rowSorter == null) {
         return null;
      }

      List<?> sortedColumns = rowSorter.getSortKeys();
      if (sortedColumns.size() > 0) {
         return (SortKey) sortedColumns.get(0);
      }
      return null;
   }
}
