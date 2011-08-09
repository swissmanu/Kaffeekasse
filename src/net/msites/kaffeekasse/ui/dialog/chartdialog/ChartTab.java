package net.msites.kaffeekasse.ui.dialog.chartdialog;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

public abstract class ChartTab extends JComponent {

	private final ChartCanvas chartCanvas = new ChartCanvas();
	
	private final Chart chart;
	
	public ChartTab(Chart chart) {
		this.chart = chart;
	}
	
	
	// ChartTab-Methoden -------------------------------------------------------
	public abstract void buildChart();
	
	
	// Getter-, Setter- & Hilfsmethoden ----------------------------------------
	protected JComponent getChartCanvas() {
		return chartCanvas;
	}
	
	public Chart getChart() {
		return chart;
	}
	
	/**
	 * Vereinfacht das Hinzufügen dieses {@link ChartTab}'s zu einer
	 * {@link JTabbedPane}.<br/>
	 * {@link Chart#getName()} wird hierbei direkt als Text für den neuen Tab
	 * verwendet.
	 * 
	 * @param tabs
	 */
	public void addToTabbedPane(JTabbedPane tabs) {
		tabs.add(chart.getName(), this);
	}
	
	@Override
	public String getName() {
		return chart.getName();
	}
	
	// Hilfskomponenten --------------------------------------------------------
	protected final class ChartCanvas extends JComponent {
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			super.paintComponent(g2);
			
			if(chart.getChart() != null) {
				chart.getChart().draw(g2, new Rectangle2D.Double(0,0,getWidth(), getHeight()));
			} else {
				TextLayout layout = new TextLayout(
						"< Keine Datenquelle(n) ausgewählt/verfügbar >",
						UIManager.getFont("Label.font"),
						g2.getFontRenderContext());
				Rectangle2D rect = layout.getBounds();
				layout.draw(
						g2,
						(float)(getWidth()-rect.getWidth())/2,
						(float)(getHeight()-rect.getHeight())/2);
			}
		}
		
		@Override
		public void repaint() {
			super.repaint();
		}
		
	}
	
}
