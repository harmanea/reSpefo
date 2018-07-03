package cz.cuni.mff.respefo.Listeners;

import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpectrumPrinter;
import cz.cuni.mff.respefo.Util;

public class MeasureRVItemListener implements SelectionListener {
	private class Measurement {
		public Measurement(double l0, double radius, String name, boolean corr) {
			this.l0 = l0;
			this.radius = radius;
			this.name = name;
			this.corr = corr;
		}
		
		double l0;
		double radius;
		String name;
		boolean corr;
	}

	private class Result {
		public Result(double rV, double radius, String category, double l0, String name, String comment) {
			this.rV = rV;
			this.radius = radius;
			this.category = category;
			this.l0 = l0;
			this.name = name;
			this.comment = comment;
		}
		
		double rV;
		double radius;
		String category;
		double l0;
		String name;
		String comment;
	}

	private static final double c = 299792.458; // speed of light (km/s)
	private ArrayList<Measurement> measures;
	private ArrayList<Result> results;
	private ArrayList<Result> corrections;
	private int index;
	
	// for drag and drop
	private boolean drag = false;
	private int prevX;
	
	private int prevTime = 0; // to filter multiple events fired at the same time
	
	private double diff = 0; // to measure rv
	private double deltaRV;

	private void getMeasurements(String[] a, boolean corr) {
		if (a != null) {
			for (String s : a) {
				File f = new File(s);
				try (BufferedReader br = new BufferedReader(new FileReader(f))) {
					String line;
					String[] tokens;
					
					while ((line = br.readLine()) != null) {
						tokens = line.trim().replaceAll(" +", " ").split(" ", 3);
						if (tokens.length < 3) {
							continue;
						} else {
							try {
								double l0 = Double.valueOf(tokens[0]);
								double radius = Double.valueOf(tokens[1]);
								String name = tokens[2];

								measures.add(new Measurement(l0, radius, name, corr));
							} catch (Exception e) {
								continue;
							}
						}
					}
				} catch (IOException e1) {
					MessageBox warning = new MessageBox(ReSpefo.getShell(), SWT.ICON_ERROR | SWT.OK);
					warning.setText("Error loading file: " + s);
					warning.open();
				}
			}
		}
	}

	private void measureNext() {
		if (index < measures.size()) {
			Measurement m = measures.get(index);
			Spectrum spectrum = ReSpefo.getSpectrum();
			
			if (spectrum.getTrimmedXSeries(m.l0 - m.radius, m.l0 + m.radius).length == 0) {
				index++;
				measureNext();
				return;
			}
			
			double[] YSeries = spectrum.getYSeries();
			double[] origXSeries = spectrum.getXSeries();
			double[] XSeries = Util.fillArray(YSeries.length, 1, 1);
			
			double mid = Util.intep(origXSeries, XSeries, new double[] { m.l0 })[0];
			
			double[] mirroredYSeries = spectrum.getTrimmedYSeries(m.l0 - m.radius, m.l0 + m.radius);
			Util.mirrorArray(mirroredYSeries);
			
			int j = Arrays.binarySearch(origXSeries, spectrum.getTrimmedXSeries(m.l0 - m.radius, m.l0 + m.radius)[0]);
			double[] temp = Util.fillArray(mirroredYSeries.length, j, 1);
			
			double[] mirroredXSeries = new double[temp.length];
			for (int i = 0; i < temp.length; i++) {
				mirroredXSeries[i] = 2 * mid - temp[temp.length - 1 - i];
			}

			Chart chart = ReSpefo.getChart();

			if (chart != null) {
				chart.dispose();
			}
			chart = new ChartBuilder(ReSpefo.getShell()).setTitle(m.name + " #" + (index + 1) + " (" + m.l0 + ")").setXAxisLabel("index")
					.setYAxisLabel("relative flux I(λ)")
					.addSeries(LineStyle.SOLID, "original", ChartBuilder.green, XSeries, YSeries)
					.addSeries(LineStyle.SOLID, "mirrored", ChartBuilder.blue, mirroredXSeries, mirroredYSeries).adjustRange(1)
					.build();

			ReSpefo.setChart(chart);
			
			ReSpefo.getChart().getPlotArea().addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDown(MouseEvent arg0) {
					prevX = arg0.x;
					drag = true;
				}

				@Override
				public void mouseUp(MouseEvent arg0) {
					drag = false;
				}
			
			});
			
			ReSpefo.getChart().getPlotArea().addMouseMoveListener(new MouseMoveListener() {
				
				@Override
				public void mouseMove(MouseEvent arg0) {
					if (drag) {				
						Chart chart = ReSpefo.getChart();
						ILineSeries ser = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
						Range XRange = chart.getAxisSet().getXAxis(ser.getXAxisId()).getRange();
						
						double change = ((arg0.x - prevX) * (XRange.upper - XRange.lower)) / ReSpefo.getChart().getPlotArea().getBounds().width;
						diff += change;
						
						ser.setXSeries(Util.adjustArrayValues(ser.getXSeries(), change));
						
						chart.redraw();
						
						prevX = arg0.x;
					}
				}
			});
			
			diff = 0;
		} else {
			Chart chart = ReSpefo.getChart();

			if (chart != null) {
				chart.dispose();
			}
			Spectrum spectrum = ReSpefo.getSpectrum();
			
			double[] X = new double[measures.size()];
			for (int i = 0; i < X.length; i++) {
				X[i] = measures.get(i).l0;
			}
			double Y[] = Util.intep(spectrum.getXSeries(), spectrum.getYSeries(), X);
			
			chart = new ChartBuilder(ReSpefo.getShell()).setTitle("Press ENTER to finish").setXAxisLabel("wavelength (Å)")
					.setYAxisLabel("relative flux I(λ)")
					.addSeries(LineStyle.SOLID, "original", ChartBuilder.green, spectrum.getXSeries(), spectrum.getYSeries())
					.addSeries(LineStyle.NONE, "measurements", ChartBuilder.pink, X, Y).adjustRange()
					.build();

			ReSpefo.setChart(chart);
			
			Util.clearListeners();
			
			ReSpefo.getShell().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.time == prevTime) {
						return;
					} else {
						prevTime = e.time;
					}
					
					switch (e.keyCode) {
					case SWT.BS:
						if (results.size() > 0) {
							results.remove(results.size() - 1);
						}
						index--;
						Util.clearListeners();
						ReSpefo.getShell().addKeyListener(new MeasureRVKeyAdapter());
						ReSpefo.getShell().addKeyListener(new MeasureRVKeyAdapterNoRepeat());
						measureNext();
						break;
						
					case 'n':
						index--;
						Util.clearListeners();
						ReSpefo.getShell().addKeyListener(new MeasureRVKeyAdapter());
						ReSpefo.getShell().addKeyListener(new MeasureRVKeyAdapterNoRepeat());
						measureNext();
						break;
						
					case SWT.CR:
						printResults();
							
						Chart chart = ReSpefo.getChart();

						if (chart != null) {
							chart.dispose();
						}

						ReSpefo.setChart(null);
						ReSpefo.setSpectrum(null);

						Util.clearListeners();
						break;
					}
				}
			});

		}
	}
	
	private void printResults() {
		try (PrintWriter writer = new PrintWriter(
				ReSpefo.getFilterPath() + File.separator + ReSpefo.getSpectrum().name() + ".rv")) {
			
			String format = " %1.10E";
			writer.println(String.format(format, ReSpefo.getSpectrum().getX(0)));
			ILineSeries ser = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries("original");
			writer.println(String.format(format, ser.getXSeries()[1] - ser.getXSeries()[0]));
			
			// polynomial coefficients
			writer.println(String.format(format, (double) 0));
			writer.println(String.format(format, (double) 0));
			writer.println(String.format(format, (double) 0));
			writer.println(String.format(format, (double) 0));
			writer.println(String.format(format, (double) 1));
			
			// header
			writer.println("rv\tradius\tnull\tcategory\tlambda\tname\tcomment");
			
			double sum = 0;
			for (Result r : results) {
				writer.println(r.rV + "\t"
							+ r.radius + "\t"
							+ (double) 0 + "\t"
							+ r.category + "\t"
							+ r.l0 + "\t"
							+ r.name + "\t"
							+ r.comment);
				
				sum += r.rV;
			}
			double average = sum / results.size();
			writer.println("Average RV: " + average);
			writer.println();
			
			sum = 0;
			for (Result r : corrections) {
				writer.println(r.rV + "\t"
							+ r.radius + "\t"
							+ (double) 0 + "\t"
							+ r.category + "\t"
							+ r.l0 + "\t"
							+ r.name + "\t"
							+ r.comment);
				
				sum += r.rV;
			}
			average = sum / corrections.size();
			writer.println("Average RV: " + average);
			
		} catch (FileNotFoundException e) {
			MessageBox warning = new MessageBox(ReSpefo.getShell(), SWT.ICON_ERROR | SWT.OK);
			warning.setText("Error occured while printing results.");
			warning.open();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent event) {

		MeasureRVDialog dialog = new MeasureRVDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			return;
		}

		Util.clearListeners();
		
		String s = dialog.getSpectrum();
		String[] measurements = dialog.getMeasurements();
		String[] corrections = dialog.getCorrections();

		measures = new ArrayList<>();
		getMeasurements(measurements, false);
		getMeasurements(corrections, true);

		Spectrum spectrum = Util.importSpectrum(s);
		if (spectrum == null) {
			return;
		}

		if (spectrum.size() < 2) {
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setText("Spectrum has less than two points.");
			mb.open();
			return;
		}

		
		double[] XSeries = spectrum.getXSeries();
		double[] YSeries = spectrum.getYSeries();
		for (int i = 0; i < YSeries.length; i++) {
			if (Double.isNaN(YSeries[i])) {
				YSeries[i] = 1;
			}
		}
		//Spectrum s = new Spectrum(XSeries, YSeries, spectrum.name());
		//ReSpefo.setSpectrum(s);
		
		deltaRV = ((XSeries[1] - XSeries[0]) * c) / (XSeries[0] * 3);
		
		double[] newXSeries = new double[XSeries.length * 3 - 2];
		newXSeries[0] = XSeries[0];
		for (int i = 1; i < newXSeries.length; i++) {
			newXSeries[i] = newXSeries[i - 1] * (1 + deltaRV / c);
			if (newXSeries[i] > XSeries[XSeries.length - 1]) {
				newXSeries = Arrays.copyOf(newXSeries, i);
				break;
			}
		}
		
		double[] newYSeries = Util.intep(XSeries, YSeries, newXSeries);
		
		ReSpefo.setSpectrum(new Spectrum(newXSeries, newYSeries, spectrum.name()));
		
		results = new ArrayList<>();
		this.corrections = new ArrayList<>();
		index = 0;

		ReSpefo.getShell().addKeyListener(new MeasureRVKeyAdapter());
		ReSpefo.getShell().addKeyListener(new MeasureRVKeyAdapterNoRepeat());

		measureNext();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}
	
	private class MeasureRVKeyAdapter extends KeyAdapter {

		public void keyPressed(KeyEvent e) {			
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
			case SWT.ARROW_LEFT:
				s = -1;
			case 'd':
			case SWT.ARROW_RIGHT:
				ILineSeries ser = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
				IAxis XAxis = chart.getAxisSet().getXAxis(ser.getXAxisId());
				double q = (XAxis.getRange().upper - XAxis.getRange().lower) / 400;
				ser.setXSeries(Util.adjustArrayValues(ser.getXSeries(), s * q));
				diff += s * q;
				break;

			case '+':
			case 16777259:
			case 49:
				chart.getAxisSet().zoomIn();
				break;
			case '-':
			case 16777261:
			case 47:
				chart.getAxisSet().zoomOut();
				break;

			case '8':
			case SWT.KEYPAD_8:
				for (IAxis i : chart.getAxisSet().getYAxes()) {
					i.zoomIn();
				}
				break;

			case '2':
			case SWT.KEYPAD_2:
				for (IAxis i : chart.getAxisSet().getYAxes()) {
					i.zoomOut();
				}
				break;

			case '4':
			case SWT.KEYPAD_4:
				for (IAxis i : chart.getAxisSet().getXAxes()) {
					i.zoomOut();
				}
				break;

			case '6':
			case SWT.KEYPAD_6:
				for (IAxis i : chart.getAxisSet().getXAxes()) {
					i.zoomIn();
				}
				break;
			}
			chart.redraw();
		}
	}
	
	private class MeasureRVKeyAdapterNoRepeat extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.time == prevTime) {
				return;
			} else {
				prevTime = e.time;
			}
			
			switch (e.keyCode) {
			case SWT.ESC:
			case 'm':
				index++;
				measureNext();
				break;
				
			case 'n':
				if (index > 0) {
					index--;
					measureNext();
				}
				break;

			case SWT.BS:
				if (results.size() > 0) {
					results.remove(results.size() - 1);
				}
				if (index > 0) {
					index--;
					measureNext();
				}
				break;

			case SWT.CR:
			case SWT.INSERT:
				Measurement m = measures.get(index);
				MeasurementInputDialog dialog = new MeasurementInputDialog(ReSpefo.getShell(), m.corr);
				String result = dialog.open();
				if (result != null) {
					double l0 = m.l0;
					double rV = deltaRV * (diff / 2);
					double radius = m.radius;
					String category = result;
					String name = m.name;
					String comment = dialog.getComment();

					if (m.corr) {
						corrections.add(new Result(rV, radius, category, l0, name, comment));
					} else {
						results.add(new Result(rV, radius, category, l0, name, comment));
					}
					index++;
					measureNext();
				}
				break;
			}
		}
	}

	private class MeasurementInputDialog extends Dialog {
		private String value;
		private String comment = "";
		private boolean corr;

		public MeasurementInputDialog(Shell parent) {
			super(parent, 0);
			corr = false;
		}
		
		public MeasurementInputDialog(Shell parent, boolean corr) {
			super(parent, 0);
			this.corr = corr;
		}

		public String getComment() {
			return comment;
		}

		public String open() {
			Shell parent = getParent();
			Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
			shell.setText("Confirm measurement");

			GridLayout layout = new GridLayout(2, false);
			shell.setLayout(layout);

			Label labelOne = new Label(shell, SWT.LEFT);
			labelOne.setText("Category:");
			labelOne.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

			Text textOne = new Text(shell, SWT.SINGLE | SWT.BORDER);
			textOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			if (corr) {
				textOne.setText("corr");
				textOne.setEnabled(false);
			} else {
				textOne.setText("");
			}

			Label labelTwo = new Label(shell, SWT.LEFT);
			labelTwo.setText("Comment:");
			labelTwo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

			Text textTwo = new Text(shell, SWT.SINGLE | SWT.BORDER);
			textTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			textTwo.setText("");

			final Button buttonConfirm = new Button(shell, SWT.PUSH | SWT.CENTER);
			buttonConfirm.setText("Confirm");
			buttonConfirm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			if (!corr) {
				buttonConfirm.setEnabled(false);
			}

			Button buttonCancel = new Button(shell, SWT.PUSH | SWT.CENTER);
			buttonCancel.setText("Cancel");
			buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			shell.setDefaultButton(buttonConfirm);

			if (!corr) {
				textOne.addListener(SWT.Modify, new Listener() {
					public void handleEvent(Event event) {
						if (!textOne.getText().equals("")){
							buttonConfirm.setEnabled(true);
						} else {
							buttonConfirm.setEnabled(false);
						}
					}
				});
			}

			buttonConfirm.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					value = textOne.getText();
					comment = textTwo.getText();
					shell.dispose();
				}
			});

			buttonCancel.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					shell.dispose();
				}
			});

			shell.pack();
			shell.open();
			Display display = parent.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			return value;
		}
	}
	
	private class MeasureRVDialog extends Dialog {
		private Shell parent;
		private boolean status;
		
		private String s;
		private String[] itemsOne, itemsTwo;
		
		public MeasureRVDialog(Shell parent) {
			super(parent, 0);
			this.parent = parent;
			status = false;
		}
		
		public String getSpectrum() {
			return s;
		}
		
		public String[] getMeasurements() {
			return itemsOne;
		}
		
		public String[] getCorrections() {
			return itemsTwo;
		}
		
		public boolean open() {
			Display display = parent.getDisplay();
			Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
			shell.setText("Measure radial velocity");
			
			// Part one
			Composite comp1 = new Composite(shell, SWT.NONE);
	        Label one = new Label(comp1, SWT.LEFT);
	        one.setText("Select spectrum to measure");
	        one.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	        Text textOne = new Text(comp1, SWT.BORDER);
	        textOne.setText("");
	        textOne.setEnabled(false);
	        Button buttonOne = new Button(comp1, SWT.PUSH | SWT.CENTER);
	        buttonOne.setText("...");

	        // Part two
	        Composite comp2 = new Composite(shell, SWT.NONE);
	        Label two = new Label(comp2, SWT.LEFT);
	        two.setText("Select .stl file(s) with measurements");
	        two.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	        List listOne = new List(comp2, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
	        Composite buttonCompOne = new Composite(comp2, SWT.NONE);
	        Button buttonTwoOne = new Button(buttonCompOne, SWT.PUSH | SWT.CENTER);
	        buttonTwoOne.setText("Add");
	        Button buttonTwoTwo = new Button(buttonCompOne, SWT.PUSH | SWT.CENTER);
	        buttonTwoTwo.setText("Remove");

	        // Part three
	        Composite comp3 = new Composite(shell, SWT.NONE);
	        Label three = new Label(comp3, SWT.LEFT);
	        three.setText("Select .stl file(s) with corrections");
	        three.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	        List listTwo = new List(comp3, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

	        Composite buttonCompTwo = new Composite(comp3, SWT.NONE);
	        Button buttonThreeOne = new Button(buttonCompTwo, SWT.PUSH | SWT.CENTER);
	        buttonThreeOne.setText("Add");
	        Button buttonThreeTwo = new Button(buttonCompTwo, SWT.PUSH | SWT.CENTER);
	        buttonThreeTwo.setText("Remove");
	        
	        // Part four
	        Composite comp4 = new Composite(shell, SWT.NONE);
	        Button buttonFourOne = new Button(comp4, SWT.PUSH | SWT.CENTER);
	        buttonFourOne.setText("Ok");
	        buttonFourOne.setEnabled(false);
	        Button buttonFourTwo = new Button(comp4, SWT.PUSH | SWT.CENTER);
	        buttonFourTwo.setText("  Cancel  ");
	        

	        // Layout stuff
	        GridLayout layout = new GridLayout(1, false);
	        layout.marginBottom = 15;
	        layout.marginLeft = 15;
	        layout.marginRight = 15;
	        layout.marginTop = 15;
	        layout.verticalSpacing = 10;
	        shell.setLayout(layout);
	        
	        layout = new GridLayout(2, false);
	        comp1.setLayout(layout);
	        comp1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	        comp2.setLayout(layout);
	        comp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        comp3.setLayout(layout);
	        comp3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        
	        layout = new GridLayout(2, true);
	        comp4.setLayout(layout);
	        comp4.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));
	        layout = new GridLayout(1, true);
	        layout.marginWidth = 0;
	        layout.marginHeight = 0;
	        buttonCompOne.setLayout(layout);
	        buttonTwoOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        buttonTwoTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        
	        layout = new GridLayout(1, true);
	        layout.marginWidth = 0;
	        layout.marginHeight = 0;
	        buttonCompTwo.setLayout(layout);
	        buttonThreeOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        buttonThreeTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        
	        textOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        buttonOne.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
	        listOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        listTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        buttonCompOne.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
	        buttonCompTwo.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
	        buttonFourOne.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
	        buttonFourTwo.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
	        
	        // Listeners
			buttonOne.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event e) {
					String s = Util.openFileDialog(Util.Spectrum);
					
					if (s != null) {
						textOne.setText(s);
						textOne.setSelection(textOne.getText().length());
						
						if (listOne.getItemCount() > 0) {
							buttonFourOne.setEnabled(true);
						}
					}
				}

			});
			
			buttonTwoOne.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event e) {
					String s = Util.openFileDialog(Util.Stl);
					
					if (s != null) {
						listOne.add(s);
						
						if (!textOne.getText().equals("")) {
							buttonFourOne.setEnabled(true);
						}
					}
				}

			});
			
			buttonTwoTwo.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event e) {
					if (listOne.getSelectionIndex() != -1) {
						listOne.remove(listOne.getSelectionIndex());
						
						if (listOne.getItemCount() == 0) {
							buttonFourOne.setEnabled(false);
						}
					}
				}

			});
			
			buttonThreeOne.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event e) {
					String s = Util.openFileDialog(Util.Stl);
					
					if (s != null) {
						listTwo.add(s);
					}
				}

			});
			
			buttonThreeTwo.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event e) {
					if (listTwo.getSelectionIndex() != -1) {
						listTwo.remove(listTwo.getSelectionIndex());
					}
				}

			});
			
			buttonFourOne.addListener(SWT.Selection, new Listener() {
				
				@Override
				public void handleEvent(Event arg0) {
					status = true;
					itemsOne = listOne.getItems();
					itemsTwo = listTwo.getItems();
					s = textOne.getText();
					shell.dispose();
				}
			});
			
			buttonFourTwo.addListener(SWT.Selection, new Listener() {
				
				@Override
				public void handleEvent(Event arg0) {
					status = false;
					shell.dispose();
				}
			});
			
			// Pack and open
			shell.pack();
			shell.open();
			shell.setSize(600, 700);
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			
			return status;
		}
	}
}
