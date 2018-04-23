package cz.cuni.mff.respefo.Listeners;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	private class Measurement {
		double l0;
		double radius;
		String name;

		public Measurement(double l0, double radius, String name) {
			this.l0 = l0;
			this.radius = radius;
			this.name = name;
		}
	}

	private class Result {
		double RV;
		int category;
		String comment;

		public Result(double RV, int category, String comment) {
			this.RV = RV;
			this.category = category;
			this.comment = comment;
		}
	}

	private class EmptyResult extends Result {
		public EmptyResult(double RV, int category, String comment) {
			super(RV, category, comment);
		}
	}

	private static final double c = 299792.458; // speed of light
	private static ArrayList<Measurement> measures;
	private static ArrayList<Result> results;
	private static int index;

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
			double deltaRV = spectrum.getX(1) - spectrum.getX(0);

			double[] temp = spectrum.getTrimmedXSeries(m.l0 - m.radius * 2, m.l0 + m.radius * 2);
			double[] temp2 = Util.fillArray(temp.length * 3 - 2, temp[0], (temp[1] - temp[0]) / 3);

			double[] XSeries = new double[temp2.length];
			XSeries[0] = temp2[0];
			for (int i = 1; i < XSeries.length; i++) {
				XSeries[i] = XSeries[i - 1] * (1 + deltaRV / c);
			}

			double[] YSeries = Util.intep(temp, spectrum.getTrimmedYSeries(m.l0 - m.radius * 2, m.l0 + m.radius * 2),
					XSeries);

			// double[] mirroredXSeries = new double[XSeries.length];
			/*
			 * for (int i = 0; i < mirroredXSeries.length; i++) { mirroredXSeries[i] =
			 * XSeries[XSeries.length / 2] - (XSeries[XSeries.length - i - 1] -
			 * XSeries[XSeries.length / 2]); }
			 */

			double[] mirroredYSeries = YSeries.clone();
			Util.mirrorArray(mirroredYSeries);

			Chart chart = ReSpefo.getChart();

			if (chart != null) {
				chart.dispose();
			}
			chart = new ChartBuilder(ReSpefo.getShell()).setTitle(m.name).setXAxisLabel("RV (km/s)")
					.setYAxisLabel("relative flux I(λ)")
					.addSeries(LineStyle.SOLID, "original", ChartBuilder.green, XSeries, YSeries)
					.addSeries(LineStyle.SOLID, "mirrored", ChartBuilder.blue, XSeries, mirroredYSeries).adjustRange()
					.build();

			ReSpefo.setChart(chart);

			index++;
		} else {

			try (PrintWriter writer = new PrintWriter(
					ReSpefo.getFilterPath() + File.separator + ReSpefo.getSpectrum().name() + ".rv")) {
				for (Result r : results) {
					if (!(r instanceof EmptyResult)) {
						writer.println(r.RV + "\t" + r.category + "\t" + r.comment);
					}
				}
			} catch (FileNotFoundException e) {
				MessageBox warning = new MessageBox(ReSpefo.getShell(), SWT.ICON_ERROR | SWT.OK);
				warning.setText("Error occured while printing results.");
				warning.open();
			}

			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			mb.setMessage("Measure more?");
			if (mb.open() == SWT.YES) {
				this.widgetSelected(null);

			} else {
				Chart chart = ReSpefo.getChart();

				if (chart != null) {
					chart.dispose();
				}

				ReSpefo.setChart(null);
				ReSpefo.setSpectrum(null);

				for (Listener l : ReSpefo.getShell().getListeners(SWT.KeyDown)) {
					ReSpefo.getShell().removeListener(SWT.KeyDown, l);
				}
			}
		}
	}

	@Override
	public void widgetSelected(SelectionEvent event) {

		for (Listener l : ReSpefo.getShell().getListeners(SWT.KeyDown)) {
			ReSpefo.getShell().removeListener(SWT.KeyDown, l);
		}

		if (measures == null) { // only happens the first time
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

		ReSpefo.setSpectrum(spectrum);

		results = new ArrayList<>();
		index = 0;

		ReSpefo.getShell().addKeyListener(new MeasureRVKeyAdapter());

		measureNext();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}

	static protected int prevTime = 0; // to filter multiple events fired at the same time

	private class MeasureRVKeyAdapter extends KeyAdapter {
		double diff = 0;

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

			case 'q':
				results.add(new EmptyResult(0, 0, null));
				measureNext();
				return;

			case SWT.BS:
				if (results.size() > 0) {
					results.remove(results.size() - 1);
					index -= 2;
					measureNext();
					
				}
				return;

			case SWT.CR:
				if (e.time != prevTime) {
					prevTime = e.time;
					InputDialog dialog = new InputDialog(ReSpefo.getShell());
					Integer result = dialog.open();
					if (result != null) {
						results.add(new Result(diff, result, dialog.getComment()));
						measureNext();
					}
				}
				return;

			}
			chart.redraw();
		}
	}

	private class InputDialog extends Dialog {
		private Integer value;
		private String comment = "";

		public InputDialog(Shell parent) {
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
}
