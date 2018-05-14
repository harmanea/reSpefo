package cz.cuni.mff.respefo.Listeners;

import java.awt.MouseInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
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
		public Measurement(double l0, double radius, String name) {
			this.l0 = l0;
			this.radius = radius;
			this.name = name;
		}
		
		double l0;
		double radius;
		String name;
	}

	private class Result {
		public Result(double rV, double radius, int category, double l0, String name, String comment) {
			this.rV = rV;
			this.radius = radius;
			this.category = category;
			this.l0 = l0;
			this.name = name;
			this.comment = comment;
		}
		
		double rV;
		double radius;
		int category;
		double l0;
		String name;
		String comment;
	}

	private static final double c = 299792.458; // speed of light (km/s)
	private ArrayList<Measurement> measures;
	private ArrayList<Result> results;
	private int index;
	
	// for drag and drop
	private boolean drag = false;
	private int prevX;
	
	private int prevTime = 0; // to filter multiple events fired at the same time
	
	private double diff = 0; // to measure rv

	private ArrayList<Measurement> getMeasurements() {
		FileDialog dialog = new FileDialog(ReSpefo.getShell(), SWT.OPEN);
		dialog.setText("Import .stl File");
		dialog.setFilterPath(ReSpefo.getFilterPath());

		String[] filterNames = new String[] { "Stl Files" };
		String[] filterExtensions = new String[] { "*.stl" };

		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);

		String s = dialog.open();

		if (s != null && Paths.get(s).getParent() != null) {
			ReSpefo.setFilterPath(Paths.get(s).getParent().toString());
		}

		if (s == null) {
			return null;
		}

		File f = new File(s);

		ArrayList<Measurement> ret = new ArrayList<>();

		try (BufferedReader b = new BufferedReader(new FileReader(f))) {
			String line;
			String[] tokens;

			while ((line = b.readLine()) != null) {
				tokens = line.trim().replaceAll(" +", " ").split(" ", 3);
				if (tokens.length < 3) {
					continue;
				} else {
					try {
						double l0 = Double.valueOf(tokens[0]);
						double radius = Double.valueOf(tokens[1]);
						String name = tokens[2];

						ret.add(new Measurement(l0, radius, name));
					} catch (Exception e) {
						continue;
					}
				}
			}

		} catch (FileNotFoundException e) {
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setMessage("Couldn't open file.");
			mb.open();
			return null;
		} catch (IOException e) {
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setMessage("Couldn't read file.");
			mb.open();
			return null;
		}

		if (ret.size() == 0) {
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setMessage("Nothing to measure in the file. It might be corrupt.");
			mb.open();
			return null;
		}

		return ret;
	}

	private void measureNext() {
		if (index < measures.size()) {
			Measurement m = measures.get(index);
			Spectrum spectrum = ReSpefo.getSpectrum();
			
			double[] XSeries = spectrum.getXSeries();
			double[] YSeries = spectrum.getYSeries();
			
			double middle = m.l0;
			double radius = m.radius;
			
			double[] temp = spectrum.getTrimmedXSeries(middle - radius, middle + radius);
			double[] mirroredYSeries = spectrum.getTrimmedYSeries(middle - radius, middle + radius);
			
			double[] mirroredXSeries = new double[temp.length];
			for (int i = 0; i < temp.length; i++) {
				mirroredXSeries[i] = 2 * m.l0 - temp[temp.length - 1 - i];
			}
			
			Util.mirrorArray(mirroredYSeries);

			Chart chart = ReSpefo.getChart();

			if (chart != null) {
				chart.dispose();
			}
			chart = new ChartBuilder(ReSpefo.getShell()).setTitle(m.name).setXAxisLabel("wavelength (Å)")
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
			
			chart = new ChartBuilder(ReSpefo.getShell()).setTitle(spectrum.name()).setXAxisLabel("wavelength (Å)")
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
						measureNext();
						break;
						
					case 'n':
						index--;
						Util.clearListeners();
						ReSpefo.getShell().addKeyListener(new MeasureRVKeyAdapter());
						measureNext();
						break;
						
					case SWT.CR:
						printResults();
						MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
						mb.setMessage("Measure more?");

						if (mb.open() == SWT.YES) {
							widgetSelected(null);

						} else {
							measures = null;
							
							Chart chart = ReSpefo.getChart();

							if (chart != null) {
								chart.dispose();
							}

							ReSpefo.setChart(null);
							ReSpefo.setSpectrum(null);

							Util.clearListeners();
						}
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
			
			writer.println("rv\tradius\tnull\tcategory\tlambda\tname\tcomment");
			
			for (Result r : results) {
				writer.println(r.rV + "\t"
							+ r.radius + "\t"
							+ (double) 0 + "\t"
							+ r.category + "\t"
							+ r.l0 + "\t"
							+ r.name + "\t"
							+ r.comment);
			}
		} catch (FileNotFoundException e) {
			MessageBox warning = new MessageBox(ReSpefo.getShell(), SWT.ICON_ERROR | SWT.OK);
			warning.setText("Error occured while printing results.");
			warning.open();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent event) {

		new FilesInputDialog(ReSpefo.getShell()).open();
		
		Util.clearListeners();

		if (event != null) { // only happens the first time
			measures = getMeasurements();
			if (measures == null) {
				return;
			}
		}

		Spectrum spectrum = Util.importSpectrum();
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
		
		double deltaRV = ((XSeries[1] - XSeries[0]) * c) / (XSeries[0] * 3);
		
		double[] newXSeries = new double[XSeries.length * 3 - 2];
		newXSeries[0] = XSeries[0];
		for (int i = 1; i < newXSeries.length; i++) {
			newXSeries[i] = newXSeries[i - 1] * (1 + deltaRV / c);
		}
		
		double[] newYSeries = Util.intep(XSeries, YSeries, newXSeries);
		
		ReSpefo.setSpectrum(new Spectrum(newXSeries, newYSeries, spectrum.name()));
		
		results = new ArrayList<>();
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
				MeasurementInputDialog dialog = new MeasurementInputDialog(ReSpefo.getShell());
				Integer result = dialog.open();
				if (result != null) {
					Measurement m = measures.get(index);

					double l0 = m.l0;
					double rV = (c * diff) / l0;
					double radius = m.radius;
					int category = result;
					String name = m.name;
					String comment = dialog.getComment();

					results.add(new Result(rV, radius, category, l0, name, comment));
					index++;
					measureNext();
				}
				break;
			}
		}
	}

	private class MeasurementInputDialog extends Dialog {
		private Integer value;
		private String comment = "";

		public MeasurementInputDialog(Shell parent) {
			super(parent, 0);
		}

		public String getComment() {
			return comment;
		}

		public Integer open() {
			Shell parent = getParent();
			Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE);
			shell.setText("Select category");

			GridLayout l = new GridLayout(2, false);
			shell.setLayout(l);

			Label label = new Label(shell, SWT.NULL);
			label.setText("Category:");

			final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
			text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			text.setText("");

			Label label2 = new Label(shell, SWT.NULL);
			label2.setText("Comment:");

			final Text text2 = new Text(shell, SWT.SINGLE | SWT.BORDER);
			text2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			text2.setText("");

			final Button buttonConfirm = new Button(shell, SWT.PUSH | SWT.CENTER);
			buttonConfirm.setText("Confirm");
			buttonConfirm.setEnabled(false);

			Button buttonCancel = new Button(shell, SWT.PUSH | SWT.CENTER);
			buttonCancel.setText("Cancel");

			shell.setDefaultButton(buttonConfirm);

			text.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					try {
						Integer.parseInt(text.getText());
						buttonConfirm.setEnabled(true);
					} catch (Exception e) {
						buttonConfirm.setEnabled(false);
					}
				}
			});

			buttonConfirm.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					value = Integer.parseInt(text.getText());
					comment = text2.getText();
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
	
	private class FilesInputDialog extends Dialog {
		public FilesInputDialog(Shell parent) {
			super(parent, 0);
		}
		
		public void open() {
			Shell parent = getParent();
			Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE);
			shell.setText("Select files");
			Display display = parent.getDisplay();
			
			GridLayout layout = new GridLayout(1, false);
			shell.setLayout(layout);
			
			Composite group1 = new Composite(shell, SWT.BORDER);
			GridData gridData = new GridData(SWT.FILL,SWT.FILL, true, false);
			gridData.horizontalSpan = 2;
			group1.setLayoutData(gridData);
			group1.setLayout(new GridLayout(2, true));
			
			Text text = new Text(group1, SWT.SINGLE | SWT.BORDER);
			text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			text.setText("file dialog");
			
			Button button = new Button(group1, SWT.PUSH | SWT.CENTER);
			//button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			button.setText("...");
			
			shell.pack();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		}
	
	}
}
