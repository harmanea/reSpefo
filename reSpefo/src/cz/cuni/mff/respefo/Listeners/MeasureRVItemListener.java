package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxis.Position;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.Util;

public class MeasureRVItemListener implements SelectionListener {
	private static final double c = 299792.458; // speed of light
	private static double def;
	
	public MeasureRVItemListener() {}
	
	@Override
	public void widgetSelected(SelectionEvent event) {
		MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_INFORMATION | SWT.OK);
		
		Spectrum spectrum = Util.importSpectrum();
		
		if (spectrum != null) {
			NumberInputDialog dialog = new NumberInputDialog(ReSpefo.getShell());
			dialog.open();
			double l0 = dialog.getl0();
			double radius = dialog.getRadius();
			
			double[] Xmdata = spectrum.getTrimmedXSeries(l0 - radius, l0 + radius);
			double[] Ymdata = Util.mirrorArray(spectrum.getTrimmedYSeries(l0 - radius, l0 + radius));
			
			if (Xmdata.length == 0 || Ymdata.length == 0) {
	        	mb.setMessage("Selected region doesn't contain any data to be mirrored");
	        	mb.open();
	        	return;
	        }
			
			for (Listener l : ReSpefo.getShell().getListeners(SWT.KeyDown)) {
				ReSpefo.getShell().removeListener(SWT.KeyDown, l);
			}
			
			Chart chart = ReSpefo.getChart();
			
			if (chart != null) {
				chart.dispose();
			}
			chart = new Chart(ReSpefo.getShell(), SWT.NONE);
			
			chart.getTitle().setText(spectrum.name());
	        chart.getAxisSet().getXAxis(0).getTitle().setText("wavelength (Å)");
	        chart.getAxisSet().getYAxis(0).getTitle().setText("relative flux");
	        
	        Color black = new Color(Display.getDefault(), 0, 0, 0);
			Color green = new Color(Display.getDefault(), 0, 255, 0);
			Color blue = new Color(Display.getDefault(), 0, 0, 255);
			Color yellow = new Color(Display.getDefault(), 255, 255, 0);
	        
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
	        original.setXSeries(spectrum.getXSeries());
	        original.setYSeries(spectrum.getYSeries());
	        original.setLineStyle(LineStyle.SOLID);
	        
	        ILineSeries mirrored = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "mirrored");
	        mirrored.setXSeries(Xmdata);
	        mirrored.setYSeries(Ymdata);
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
	        axisset.getXAxis(0).setRange(xAxis2.getRange());
	        axisset.getYAxis(0).setRange(yAxis2.getRange());
	        
	        def = mirrored.getXSeries()[0];
	        
	        ReSpefo.setChart(chart);
	        
	        ReSpefo.getShell().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e)
        		{     

					Chart chart = ReSpefo.getChart();
					
        			switch (e.keyCode) {
        			case SWT.ARROW_UP:
        			case 'w':
        				chart.getAxisSet().getYAxis(0).scrollUp();
        				chart.getAxisSet().getYAxis(axisId2).scrollUp();
        				break;
        			case SWT.ARROW_DOWN:
        			case 's':
        				chart.getAxisSet().getYAxis(0).scrollDown();
        				chart.getAxisSet().getYAxis(axisId2).scrollDown();
        				break;
        			case 'a':
        			case SWT.ARROW_LEFT: // arrow left
        				ILineSeries left = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
        				double q = (chart.getAxisSet().getXAxis(axisId2).getRange().upper - chart.getAxisSet().getXAxis(axisId2).getRange().lower) / 200;
        				left.setXSeries(Util.adjustArrayValues(left.getXSeries(), -q));
        				break;
        			case 'd':
        			case SWT.ARROW_RIGHT: // arrow right
        				ILineSeries right = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
        				double w = (chart.getAxisSet().getXAxis(axisId2).getRange().upper - chart.getAxisSet().getXAxis(axisId2).getRange().lower) / 200;
        				right.setXSeries(Util.adjustArrayValues(right.getXSeries(), w));
        				break;
        			case '+': // +
        			case 16777259:
        			case 49:
        				chart.getAxisSet().zoomIn();
        				break;
        			case '-': // -
        			case 16777261:
        			case 47:
        				chart.getAxisSet().zoomOut();
        				break;
        			case SWT.KEYPAD_8: // NumPad up
        				chart.getAxisSet().getYAxis(0).zoomIn();
        				chart.getAxisSet().getYAxis(axisId2).zoomIn();
        				break;
        			case SWT.KEYPAD_2: // NumPad down
        				chart.getAxisSet().getYAxis(0).zoomOut();
        				chart.getAxisSet().getYAxis(axisId2).zoomOut();
        				break;
        			case SWT.KEYPAD_4: // NumPad left
        				chart.getAxisSet().getXAxis(0).zoomOut();
        				chart.getAxisSet().getXAxis(axisId2).zoomOut();
        				break;
        			case SWT.KEYPAD_6: // NumPad right
        				chart.getAxisSet().getXAxis(0).zoomIn();
        				chart.getAxisSet().getXAxis(axisId2).zoomIn();
        				break;
        			case SWT.CR:
        				ILineSeries m = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
        				ILineSeries o = (ILineSeries) chart.getSeriesSet().getSeries("original");
        				double l = l0 + (m.getXSeries()[0] - def);
        				mb.setMessage(c + " * (" + l + " - " + l0 + ")) / " + l0 + '\n' + ((c * (l - l0)) / l0) + " km/s"); // RV = c/l0 (l - l0)
        				mb.open();
        				break;
        			}
        			chart.redraw();
        		}
			});
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}
	
	public class NumberInputDialog extends Dialog {
		  double l0;
		  double radius;
		  
		  public double getl0() {
			  return l0;
		  }
		  
		  public double getRadius() {
			  return radius;
		  }

		  public NumberInputDialog(Shell parent) {
		    super(parent);
		  }

		  public NumberInputDialog(Shell parent, int style) {
		    super(parent, style);
		  }

		  public boolean open() {
		    Shell parent = getParent();
		    final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		    shell.setText("NumberInputDialog");

		    shell.setLayout(new GridLayout(2, true));

		    Label label1 = new Label(shell, SWT.NULL);
		    label1.setText("l0:");
		    
		    final Text text1 = new Text(shell, SWT.SINGLE | SWT.BORDER);
		    
		    Label label2 = new Label(shell, SWT.NULL);
		    label2.setText("radius:");

		    final Text text2 = new Text(shell, SWT.SINGLE | SWT.BORDER);

		    final Button buttonOK = new Button(shell, SWT.PUSH);
		    buttonOK.setText("Ok");
		    buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		    text1.addListener(SWT.Modify, new Listener() {
		      public void handleEvent(Event event) {
		        try {
		          l0 = new Double(text1.getText());
		          buttonOK.setEnabled(true);
		        } catch (Exception e) {
		          buttonOK.setEnabled(false);
		        }
		      }
		    });
		    
		    text2.addListener(SWT.Modify, new Listener() {
			      public void handleEvent(Event event) {
			        try {
			          radius = new Double(text2.getText());
			          buttonOK.setEnabled(true);
			        } catch (Exception e) {
			          buttonOK.setEnabled(false);
			        }
			      }
			    });

		    buttonOK.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event event) {
		        shell.dispose();
		      }
		    });
		    
		    shell.addListener(SWT.Traverse, new Listener() {
		      public void handleEvent(Event event) {
		        if(event.detail == SWT.TRAVERSE_ESCAPE)
		          event.doit = false;
		      }
		    });

		    text1.setText("6562.817");
		    text2.setText("10");
		    shell.pack();
		    shell.open();

		    Display display = parent.getDisplay();
		    while (!shell.isDisposed()) {
		      if (!display.readAndDispatch())
		        display.sleep();
		    }

		    return true;
		  }
	}
}

