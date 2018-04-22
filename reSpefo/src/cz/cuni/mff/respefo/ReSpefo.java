package cz.cuni.mff.respefo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;

import cz.cuni.mff.respefo.Listeners.FileExportItemListener;
import cz.cuni.mff.respefo.Listeners.FileImportItemListener;
import cz.cuni.mff.respefo.Listeners.MeasureRVItemListener;
import cz.cuni.mff.respefo.Listeners.RectifyItemListener;

public class ReSpefo {

	public static final String version = "0.4.4";

	private static Display display;

	private static Shell shell;

	private static Menu menuBar, fileMenu, toolsMenu;

	private static MenuItem fileMenuHeader, toolsMenuHeader;

	private static MenuItem fileExportItem, fileImportItem, rectifyItem, measureRVItem;

	private static Spectrum spectrum;

	private static Chart chart;

	private static String filterPath = "";

	public ReSpefo() {

		display = new Display();
		shell = new Shell(display);
		shell.setText("reSpefo (v" + version + ")");
		shell.setSize(1000, 1000);
		shell.setLayout(new FillLayout());

		menuBar = new Menu(shell, SWT.BAR);

		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");

		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);

		fileImportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileImportItem.setText("&Import\tCtrl+I");
		fileImportItem.setAccelerator('I' | SWT.CTRL);

		fileExportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportItem.setText("&Export\tCtrl+E");
		fileExportItem.setAccelerator('E' | SWT.CTRL);

		toolsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		toolsMenuHeader.setText("&Tools");

		toolsMenu = new Menu(shell, SWT.DROP_DOWN);
		toolsMenuHeader.setMenu(toolsMenu);

		rectifyItem = new MenuItem(toolsMenu, SWT.PUSH);
		rectifyItem.setText("&Rectify\tCtrl+R");
		rectifyItem.setAccelerator('R' | SWT.CTRL);

		measureRVItem = new MenuItem(toolsMenu, SWT.PUSH);
		measureRVItem.setText("&Measure RV\tCtrl+M");
		measureRVItem.setAccelerator('M' | SWT.CTRL);

		fileExportItem.addSelectionListener(new FileExportItemListener());
		fileImportItem.addSelectionListener(new FileImportItemListener());
		rectifyItem.addSelectionListener(new RectifyItemListener());
		measureRVItem.addSelectionListener(new MeasureRVItemListener());

		shell.setMenuBar(menuBar);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public static void main(String[] args) {
		new ReSpefo();
	}

	public static Shell getShell() {
		return shell;
	}

	public static Spectrum getSpectrum() {
		return spectrum;
	}

	public static void setSpectrum(Spectrum s) {
		ReSpefo.spectrum = s;
	}

	public static Chart getChart() {
		return chart;
	}

	public static void setChart(Chart c) {
		ReSpefo.chart = c;
	}

	public static String getFilterPath() {
		return filterPath;
	}

	public static void setFilterPath(String path) {
		ReSpefo.filterPath = path;
	}
}
