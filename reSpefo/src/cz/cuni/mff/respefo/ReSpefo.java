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
import cz.cuni.mff.respefo.Listeners.FileQuitItemListener;
import cz.cuni.mff.respefo.Listeners.MeasureRVItemListener;
import cz.cuni.mff.respefo.Listeners.RectifyItemListener;

/**
 * Main class responsible for creating a display and a shell for the application
 * 
 * @author Adam Harmanec
 *
 */
public class ReSpefo {

	public static final String version = "0.5.2";

	private static Display display;
	private static Shell shell;

	private static Menu menuBar, fileMenu, toolsMenu;
	private static MenuItem fileMenuHeader, toolsMenuHeader;
	private static MenuItem fileQuitItem, fileExportItem, fileImportItem, rectifyItem, measureRVItem;

	private static Spectrum spectrum;
	private static Chart chart;
	private static String filterPath = System.getProperty("user.dir");

	public ReSpefo() {

		display = new Display();
		Display.setAppName("reSpefo");
		shell = new Shell(display);
		shell.setText("reSpefo (v" + version + ")");
		shell.setSize(1000, 1000);
		shell.setLayout(new FillLayout());

		menuBar = new Menu(shell, SWT.BAR);

		// File menu
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");

		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);

		fileImportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileImportItem.setText("&Import\tCtrl+I");
		fileImportItem.setAccelerator('I' | SWT.CTRL);
		fileImportItem.addSelectionListener(new FileImportItemListener());

		fileExportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportItem.setText("&Export\tCtrl+E");
		fileExportItem.setAccelerator('E' | SWT.CTRL);
		fileExportItem.addSelectionListener(new FileExportItemListener());
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		fileQuitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileQuitItem.setText("&Quit\tCtrl+Q");
		fileQuitItem.setAccelerator('Q' | SWT.CTRL);
		fileQuitItem.addSelectionListener(new FileQuitItemListener());

		// Tools menu
		toolsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		toolsMenuHeader.setText("&Tools");

		toolsMenu = new Menu(shell, SWT.DROP_DOWN);
		toolsMenuHeader.setMenu(toolsMenu);

		rectifyItem = new MenuItem(toolsMenu, SWT.PUSH);
		rectifyItem.setText("&Rectify\tCtrl+R");
		rectifyItem.setAccelerator('R' | SWT.CTRL);
		rectifyItem.addSelectionListener(new RectifyItemListener());

		measureRVItem = new MenuItem(toolsMenu, SWT.PUSH);
		measureRVItem.setText("&Measure RV\tCtrl+M");
		measureRVItem.setAccelerator('M' | SWT.CTRL);	
		measureRVItem.addSelectionListener(new MeasureRVItemListener());

		
		shell.setMenuBar(menuBar);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
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
	
	public static void main(String[] args) {
		new ReSpefo();
	}
}
