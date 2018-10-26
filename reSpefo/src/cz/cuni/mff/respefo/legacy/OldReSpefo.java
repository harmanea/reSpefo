package cz.cuni.mff.respefo.legacy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

import cz.cuni.mff.respefo.Util;

import org.swtchart.LineStyle;

public class OldReSpefo {
	private static Chart chart;
	private static boolean scatter;

	public static void main(String[] args)  {
		OldSpectrum s = null;
		
		/*
		if (args.length < 1) {
			System.out.println("Nezadal jsi zadny parametr.");
			System.exit(1);
		} else if (!(new File(args[0]).exists())) {
			System.out.println("Nemohu najit soubor se zadanym jmenem.");
			System.exit(1);
		} else if (args.length > 2) {
			try {
				double from = Double.valueOf(args[1]);
				double to = Double.valueOf(args[2]);
				s = SpectrumBuilder.importFromASCIIFile(args[0], from, to);
			} catch (Exception e) {
				System.out.println("Druhy a treti parametr nejsou platna cisla. Spektrum bude nacteno cele.");
				s = SpectrumBuilder.importFromASCIIFile(args[0]);
			}
		} else {
			s = SpectrumBuilder.importFromASCIIFile(args[0]);
		}
		
		
		if (s == null) {
			System.out.println("Spektrum se nepodarilo nacist. Mozna neni ve spravnem formatu?");
			System.exit(1);
		}
		*/
		
		s = SpectrumBuilder.importFromASCIIFile("/home/adam/test.txt");
		
		Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("reSpefo");
        shell.setSize(1200, 800);
        shell.setLayout(new FillLayout());
        
        double l0 = 6562.817;
        int id = createChart2(shell, s, l0, 10);
        
        scatter = false;
        
        shell.addKeyListener(new KeyAdapter()
        {
        		public void keyPressed(KeyEvent e)
        		{     
        			switch (e.keyCode) {
        			case 'w':
        			case SWT.ARROW_UP: // arrow up
        				chart.getAxisSet().getYAxis(0).scrollUp();
        				chart.getAxisSet().getYAxis(id).scrollUp();
        				break;
        			case 'a':
        			case SWT.ARROW_LEFT: // arrow left
        				ILineSeries mirroredl = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
        				double q = (chart.getAxisSet().getXAxis(id).getRange().upper - chart.getAxisSet().getXAxis(id).getRange().lower) / 200;
        				mirroredl.setXSeries(Util.adjustArrayValues(mirroredl.getXSeries(), -q));
        				break;
        			case 's':
        			case SWT.ARROW_DOWN: // arrow down
        				chart.getAxisSet().getYAxis(0).scrollDown();
        				chart.getAxisSet().getYAxis(id).scrollDown();
        				break;
        			case 'd':
        			case SWT.ARROW_RIGHT: // arrow right
        				ILineSeries mirroredr = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
        				double w = (chart.getAxisSet().getXAxis(id).getRange().upper - chart.getAxisSet().getXAxis(id).getRange().lower) / 200;
        				mirroredr.setXSeries(Util.adjustArrayValues(mirroredr.getXSeries(), w));
        				break;
        			/*case SWT.SPACE: // space
        				chart.getAxisSet().adjustRange();
        				break;*/
        			case '+': // +
        				chart.getAxisSet().zoomIn();
        				break;
        			case '-': // -
        				chart.getAxisSet().zoomOut();
        				break;
        			case SWT.KEYPAD_8: // NumPad up
        				chart.getAxisSet().getYAxis(0).zoomIn();
        				chart.getAxisSet().getYAxis(id).zoomIn();
        				break;
        			case SWT.KEYPAD_2: // NumPad down
        				chart.getAxisSet().getYAxis(0).zoomOut();
        				chart.getAxisSet().getYAxis(id).zoomOut();
        				break;
        			case SWT.KEYPAD_4: // NumPad left
        				chart.getAxisSet().getXAxis(0).zoomOut();
        				chart.getAxisSet().getXAxis(id).zoomOut();
        				break;
        			case SWT.KEYPAD_6: // NumPad right
        				chart.getAxisSet().getXAxis(0).zoomIn();
        				chart.getAxisSet().getXAxis(id).zoomIn();
        				break;
        			case 'l':
        				ILineSeries Series = (ILineSeries) chart.getSeriesSet().getSeries("series");
        				if (scatter) {
        					Series.setSymbolType(PlotSymbolType.NONE);
        					Series.setLineStyle(LineStyle.SOLID);
        					scatter = false;
        				} else {
        					Series.setSymbolType(PlotSymbolType.CIRCLE);
            				Series.setLineStyle(LineStyle.NONE);
            				scatter = true;
        				}
        				break;
        			case SWT.CR:
        				ILineSeries m = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
        				ILineSeries o = (ILineSeries) chart.getSeriesSet().getSeries("original");
        				double c = 299792.458; // speed of light
        				double l = l0 + (m.getXSeries()[0] - o.getXSeries()[0]);
        				System.out.println("299792.458 * (" + l + " - " + l0 + ")) / " + l0); // RV = c/l0 (l - l0)
        				System.out.println((c * (l - l0)) / l0);
        				break;
        			}
        			chart.redraw();
        		}
        });
        
        shell.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent arg0) {
				if (arg0.count > 0) {
					chart.getAxisSet().zoomIn();
				} else {
					chart.getAxisSet().zoomOut();
				}
				chart.redraw();
			}
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
	}
	
	static public void createChart(Composite parent, OldSpectrum s) {

        chart = new Chart(parent, SWT.NONE);

        chart.getTitle().setText(s.name());
        chart.getAxisSet().getXAxis(0).getTitle().setText("wavelength (Å)");
        chart.getAxisSet().getYAxis(0).getTitle().setText("relative flux");

        ILineSeries scatterSeries = (ILineSeries) chart.getSeriesSet()
                .createSeries(SeriesType.LINE, "series");
        scatterSeries.setLineStyle(LineStyle.SOLID);
        scatterSeries.setXSeries(s.getXSeries());
        scatterSeries.setYSeries(s.getYSeries());
        
        IAxisSet axisset = chart.getAxisSet();
        
        Color black = new Color(Display.getDefault(), 0, 0, 0);
        chart.setBackground(black);
        chart.setBackgroundInPlotArea(black);
        axisset.getXAxis(0).getGrid().setForeground(black);
        axisset.getYAxis(0).getGrid().setForeground(black);
        
        Color green = new Color(Display.getDefault(), 0, 255, 0);
        scatterSeries.setSymbolSize(1);
        scatterSeries.setSymbolColor(green);
        scatterSeries.setSymbolType(PlotSymbolType.NONE);
        scatterSeries.setLineColor(green);
        
        Color yellow = new Color(Display.getDefault(), 255, 255, 0);
        axisset.getXAxis(0).getTick().setForeground(yellow);
        axisset.getYAxis(0).getTick().setForeground(yellow);
        chart.getTitle().setForeground(yellow);
        axisset.getXAxis(0).getTitle().setForeground(yellow);
        axisset.getYAxis(0).getTitle().setForeground(yellow);
        
        chart.getLegend().setVisible(false);
        
        chart.getAxisSet().adjustRange();
    }
	
	static public int createChart2(Composite parent, OldSpectrum s, double middle, double radius) {

		Color black = new Color(Display.getDefault(), 0, 0, 0);
		Color green = new Color(Display.getDefault(), 0, 255, 0);
		Color blue = new Color(Display.getDefault(), 0, 0, 255);
		Color yellow = new Color(Display.getDefault(), 255, 255, 0);
		
        chart = new Chart(parent, SWT.NONE);

        chart.getTitle().setText(s.name());
        chart.getAxisSet().getXAxis(0).getTitle().setText("wavelength (Å)");
        chart.getAxisSet().getYAxis(0).getTitle().setText("relative flux");

        // create second Y axis
        int axisId = chart.getAxisSet().createYAxis();
        IAxis yAxis2 = chart.getAxisSet().getYAxis(axisId);
        yAxis2.getTick().setVisible(false);
        yAxis2.getTitle().setVisible(false);
        yAxis2.getGrid().setForeground(black);
        int axisId2 = chart.getAxisSet().createXAxis();
        IAxis xAxis2 = chart.getAxisSet().getXAxis(axisId2);
        xAxis2.getTick().setVisible(false);
        xAxis2.getTitle().setVisible(false);
        xAxis2.getGrid().setForeground(black);
        
        // create line series
        ILineSeries original = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "original");
        original.setXSeries(s.getTrimmedXSeries(middle - radius, middle + radius));
        original.setYSeries(s.getTrimmedYSeries(middle - radius, middle + radius));
        original.setLineStyle(LineStyle.SOLID);
        
        ILineSeries mirrored = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "mirrored");
        mirrored.setXSeries(s.getTrimmedXSeries(middle - radius, middle + radius));
        mirrored.setYSeries(Util.mirrorArray(s.getTrimmedYSeries(middle - radius, middle + radius)));
        mirrored.setLineStyle(LineStyle.SOLID);
        
        // assign series to second Y axis
        mirrored.setYAxisId(axisId);
        mirrored.setXAxisId(axisId2);
        
        IAxisSet axisset = chart.getAxisSet();
        
        chart.setBackground(black);
        chart.setBackgroundInPlotArea(black);
        axisset.getXAxis(0).getGrid().setForeground(black);
        axisset.getYAxis(0).getGrid().setForeground(black);
        
        
        original.setSymbolSize(1);
        original.setSymbolColor(green);
        original.setSymbolType(PlotSymbolType.NONE);
        original.setLineColor(green);
        
        
        mirrored.setSymbolSize(1);
        mirrored.setSymbolColor(blue);
        mirrored.setSymbolType(PlotSymbolType.NONE);
        mirrored.setLineColor(blue);
        
        
        axisset.getXAxis(0).getTick().setForeground(yellow);
        axisset.getYAxis(0).getTick().setForeground(yellow);
        chart.getTitle().setForeground(yellow);
        axisset.getXAxis(0).getTitle().setForeground(yellow);
        axisset.getYAxis(0).getTitle().setForeground(yellow);
        
        chart.getLegend().setVisible(false);
        
        chart.getAxisSet().adjustRange();
        
        return axisId2;
    }
	
}