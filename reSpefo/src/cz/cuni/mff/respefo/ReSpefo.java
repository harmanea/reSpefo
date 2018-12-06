package cz.cuni.mff.respefo;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;

import cz.cuni.mff.respefo.Listeners.FileExportItemListener;
import cz.cuni.mff.respefo.Listeners.FileImportItemListener;
import cz.cuni.mff.respefo.Listeners.FileQuitItemListener;
import cz.cuni.mff.respefo.clearCosmics.ClearCosmicsItemListener;
import cz.cuni.mff.respefo.measureRV.MeasureRVItemListener;
import cz.cuni.mff.respefo.rectify.RectifyItemListener;
import cz.cuni.mff.respefo.rvResult.RVResultItemListener;

/**
 * Main class responsible for creating a Display and a Shell for the application as well as the main menu
 * 
 * @author Adam Harmanec
 *
 */
public class ReSpefo {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());

	public static final String VERSION = "0.7.7";

	private static Display display;
	private static Shell shell;
	
	private static Scene scene;

	private static Menu menuBar, fileMenu, toolsMenu;
	private static MenuItem fileMenuHeader, toolsMenuHeader;
	private static MenuItem fileQuitItem, fileExportItem, fileImportItem, rectifyItem, measureRVItem, rVResultsItem, clearCosmicsItem;

	private static Spectrum spectrum;

	private static Chart chart;
	private static String filterPath;
	static {
		try {
			filterPath = System.getProperty("user.dir");
			LOGGER.log(Level.FINEST, "Filter path set to " + filterPath);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Couldn't determine current user directory", e);
			filterPath = "";
		}
	}
	 

	public ReSpefo() {

		display = new Display();
		Display.setAppName("reSpefo");
		shell = new Shell(display, SWT.RESIZE);
		shell.setText("reSpefo (v" + VERSION + ")");
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		shell.setLayout(layout);
		
		scene = new Scene(shell, SWT.NONE);
		scene.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		scene.setLayout(layout);

		menuBar = new Menu(shell, SWT.BAR);

		// File menu
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");

		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);

		fileImportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileImportItem.setText("&Import\tCtrl+I");
		fileImportItem.setAccelerator('I' | SWT.CTRL);
		fileImportItem.addSelectionListener(FileImportItemListener.getInstance());

		fileExportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportItem.setText("&Export\tCtrl+E");
		fileExportItem.setAccelerator('E' | SWT.CTRL);
		fileExportItem.addSelectionListener(FileExportItemListener.getInstance());
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		fileQuitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileQuitItem.setText("&Quit\tCtrl+Q");
		fileQuitItem.setAccelerator('Q' | SWT.CTRL);
		fileQuitItem.addSelectionListener(FileQuitItemListener.getInstance());

		// Tools menu
		toolsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		toolsMenuHeader.setText("&Tools");

		toolsMenu = new Menu(shell, SWT.DROP_DOWN);
		toolsMenuHeader.setMenu(toolsMenu);

		rectifyItem = new MenuItem(toolsMenu, SWT.PUSH);
		rectifyItem.setText("&Rectify\tCtrl+R");
		rectifyItem.setAccelerator('R' | SWT.CTRL);
		rectifyItem.addSelectionListener(RectifyItemListener.getInstance());

		measureRVItem = new MenuItem(toolsMenu, SWT.PUSH);
		measureRVItem.setText("&Measure RV\tCtrl+M");
		measureRVItem.setAccelerator('M' | SWT.CTRL);	
		measureRVItem.addSelectionListener(MeasureRVItemListener.getInstance());
		
		/*
		rVResultsItem = new MenuItem(toolsMenu, SWT.PUSH);
		rVResultsItem.setText("&RV Results\tCtrl+T");
		rVResultsItem.setAccelerator('T' | SWT.CTRL);
		rVResultsItem.addSelectionListener(RVResultItemListener.getInstance());
		*/
		
		clearCosmicsItem = new MenuItem(toolsMenu, SWT.PUSH);
		clearCosmicsItem.setText("&Clear cosmics\tCtrl+C");
		clearCosmicsItem.setAccelerator('C' | SWT.CTRL);
		clearCosmicsItem.addSelectionListener(ClearCosmicsItemListener.getInstance());
		
		shell.setMenuBar(menuBar);
		
		shell.pack();
		shell.setSize(1000, 1000);
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
	
	public static Scene getScene() {
		return scene;
	}
	
	/**
	 * Disposes of all of it's children controls and removes all saved listeners
	 */
	public static void clearScene() {
		for (Control control : scene.getChildren()) {
			control.dispose();
		}
		
		scene.removeSavedListeners();
	}
	
	/**
	 * Clears the scene and nulls all saved values
	 */
	public static void reset() {
		clearScene();

		spectrum = null;
		
		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = null;
	}
	
	public static Spectrum getSpectrum() {
		return spectrum;
	}

	public static void setSpectrum(Spectrum spectrum) {
		ReSpefo.spectrum = spectrum;
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
	
		LOGGER.log(Level.INFO, "This is ReSpefo " + VERSION);
		new ReSpefo();
		LOGGER.log(Level.INFO, "Program terminated");
	}
}
