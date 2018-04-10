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

import cz.cuni.mff.respefo.ChartBuilder;
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
			chart = new ChartBuilder(ReSpefo.getShell()).setTitle(spectrum.name()).setXAxisLabel("RV (km/s)").setYAxisLabel("relative flux")
					.addSeries(LineStyle.SOLID, "original", ChartBuilder.green, spectrum.getXSeries(), spectrum.getYSeries())
					.addSeries(LineStyle.SOLID, "mirrored", ChartBuilder.blue, Xmdata, Ymdata)
					.adjustRange(1).pack();
	        
	        def = Xmdata[0];
	        
	        ReSpefo.setChart(chart);
	        
	        ReSpefo.getShell().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e)
        		{     
					Chart chart = ReSpefo.getChart();
					
					int s = 1;
					
        			switch (e.keyCode) {
        			case SWT.ARROW_UP:
        			case 'w':
        				for (IAxis i : chart.getAxisSet().getYAxes()) {
    						i.scrollUp();
    					}
        				break;
        			case SWT.ARROW_DOWN:
        			case 's':
        				for (IAxis i : chart.getAxisSet().getYAxes()) {
    						i.scrollDown();
    					}
        				break;
        			case 'a':
        			case SWT.ARROW_LEFT: // arrow left
        				s = -1;
        			case 'd':
        			case SWT.ARROW_RIGHT: // arrow right
        				ILineSeries ser = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
        				IAxis XAxis = chart.getAxisSet().getXAxis(ser.getXAxisId());
        				double q = (XAxis.getRange().upper - XAxis.getRange().lower) / 400;
        				ser.setXSeries(Util.adjustArrayValues(ser.getXSeries(), s * q));
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
        				for (IAxis i : chart.getAxisSet().getYAxes()) {
    						i.zoomIn();
    					}
        				break;
        			case SWT.KEYPAD_2: // NumPad down
        				for (IAxis i : chart.getAxisSet().getYAxes()) {
    						i.zoomOut();
    					}
        				break;
        			case SWT.KEYPAD_4: // NumPad left
        				for (IAxis i : chart.getAxisSet().getXAxes()) {
    						i.zoomOut();
    					}
        				break;
        			case SWT.KEYPAD_6: // NumPad right
        				for (IAxis i : chart.getAxisSet().getXAxes()) {
    						i.zoomIn();
    					}
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

