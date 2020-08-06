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

import cz.cuni.mff.respefo.function.AddToLstItemListener;
import cz.cuni.mff.respefo.function.ChironToAsciiItemListener;
import cz.cuni.mff.respefo.function.ClearCosmicsItemListener;
import cz.cuni.mff.respefo.function.CompareItemListener;
import cz.cuni.mff.respefo.function.ConvertToListener;
import cz.cuni.mff.respefo.function.EWResultItemListener;
import cz.cuni.mff.respefo.function.ExtractFitsHeaderItemListener;
import cz.cuni.mff.respefo.function.FileExportItemListener;
import cz.cuni.mff.respefo.function.FileImportItemListener;
import cz.cuni.mff.respefo.function.FileQuitItemListener;
import cz.cuni.mff.respefo.function.FitsToLstItemListener;
import cz.cuni.mff.respefo.function.HelCorItemListener;
import cz.cuni.mff.respefo.function.MeasureEWItemListener;
import cz.cuni.mff.respefo.function.MeasureRVItemListener;
import cz.cuni.mff.respefo.function.PrepareDirectoryItemListener;
import cz.cuni.mff.respefo.function.RVResultItemListener;
import cz.cuni.mff.respefo.function.RectifyItemListener;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;

/**
 * Main class responsible for creating a Display and a Shell for the application as well as the main menu
 * 
 * @author Adam Harmanec
 *
 */
public class ReSpefo {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());

	private static Display display;
	private static Shell shell;
	
	private static Scene scene;

	private static Menu menuBar, fileMenu, projectMenu, toolsMenu, extraMenu, convertSubMenu;
	private static MenuItem fileMenuHeader, projectMenuHeader, toolsMenuHeader, extraMenuHeader;
	private static MenuItem fileQuitItem, fileExportItem, fileImportItem, rectifyItem,
		measureRVItem, rVResultsItem, clearCosmicsItem, chironToAsciiItem, extractFitsHeaderItem,
		prepareDirectoryItem, helCorItem, fitsToLstItem, compareItem, addToLstItem,
		convertItem, convertToFitsItem, convertToAsciiItem, measureEWItem, ewResultsItem;

	private static Spectrum spectrum;

	private static Chart chart;
	 
	public ReSpefo() {
		display = new Display();
		Display.setAppName("reSpefo");
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("reSpefo (" + Version.toFullString() + ")");
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

		/* File menu */
		
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");

		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);

		fileImportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileImportItem.setText("&Open\tCtrl+O");
		fileImportItem.setAccelerator('O' | SWT.CTRL);
		fileImportItem.addSelectionListener(FileImportItemListener.getInstance());

		fileExportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportItem.setText("&Save\tCtrl+S");
		fileExportItem.setAccelerator('S' | SWT.CTRL);
		fileExportItem.addSelectionListener(FileExportItemListener.getInstance());
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		fileQuitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileQuitItem.setText("&Quit\tCtrl+Q");
		fileQuitItem.setAccelerator('Q' | SWT.CTRL);
		fileQuitItem.addSelectionListener(FileQuitItemListener.getInstance());

		
		/* Project Menu */
		
		projectMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		projectMenuHeader.setText("&Project");
		
		projectMenu = new Menu(shell, SWT.DROP_DOWN);
		projectMenuHeader.setMenu(projectMenu);
		
		fitsToLstItem = new MenuItem(projectMenu, SWT.PUSH);
		fitsToLstItem.setText("FITS to Lst");
		fitsToLstItem.addSelectionListener(FitsToLstItemListener.getInstance());
		
		prepareDirectoryItem = new MenuItem(projectMenu, SWT.PUSH);
		prepareDirectoryItem.setText("Prepare Directory");
		prepareDirectoryItem.addSelectionListener(PrepareDirectoryItemListener.getInstance());
		
		helCorItem = new MenuItem(projectMenu, SWT.PUSH);
		helCorItem.setText("Apply HelCorr");
		helCorItem.addSelectionListener(HelCorItemListener.getInstance());
		
		addToLstItem = new MenuItem(projectMenu, SWT.PUSH);
		addToLstItem.setText("Add to Lst");
		addToLstItem.addSelectionListener(AddToLstItemListener.getInstance());
		
		new MenuItem(projectMenu, SWT.SEPARATOR);
		
		rVResultsItem = new MenuItem(projectMenu, SWT.PUSH);
		rVResultsItem.setText("RV Resul&ts\tCtrl+T");
		rVResultsItem.setAccelerator('T' | SWT.CTRL);
		rVResultsItem.addSelectionListener(RVResultItemListener.getInstance());
		
		ewResultsItem = new MenuItem(projectMenu, SWT.PUSH);
		ewResultsItem.setText("EW Resul&ts\tCtrl+Shift+T");
		ewResultsItem.setAccelerator('T' | SWT.CTRL | SWT.SHIFT);
		ewResultsItem.addSelectionListener(EWResultItemListener.getInstance());
		
		new MenuItem(projectMenu, SWT.SEPARATOR);
		
		compareItem = new MenuItem(projectMenu, SWT.PUSH);
		compareItem.setText("Compare");
		compareItem.addSelectionListener(CompareItemListener.getInstance());
		
		
		/* Tools menu */
		
		toolsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		toolsMenuHeader.setText("&Tools");

		toolsMenu = new Menu(shell, SWT.DROP_DOWN);
		toolsMenuHeader.setMenu(toolsMenu);

		rectifyItem = new MenuItem(toolsMenu, SWT.PUSH);
		rectifyItem.setText("&Rectify\tCtrl+R");
		rectifyItem.setAccelerator('R' | SWT.CTRL);
		rectifyItem.addSelectionListener(RectifyItemListener.getInstance());

		clearCosmicsItem = new MenuItem(toolsMenu, SWT.PUSH);
		clearCosmicsItem.setText("&Clear Cosmics\tCtrl+C");
		clearCosmicsItem.setAccelerator('C' | SWT.CTRL);
		clearCosmicsItem.addSelectionListener(ClearCosmicsItemListener.getInstance());
		
		measureRVItem = new MenuItem(toolsMenu, SWT.PUSH);
		measureRVItem.setText("&Measure RV\tCtrl+M");
		measureRVItem.setAccelerator('M' | SWT.CTRL);
		measureRVItem.addSelectionListener(MeasureRVItemListener.getInstance());
		
		measureEWItem = new MenuItem(toolsMenu, SWT.PUSH);
		measureEWItem.setText("&Measure EW\tCtrl+E");
		measureEWItem.setAccelerator('E' | SWT.CTRL);
		measureEWItem.addSelectionListener(MeasureEWItemListener.getInstance());
		
		new MenuItem(toolsMenu, SWT.SEPARATOR);
		
		convertItem = new MenuItem(toolsMenu, SWT.CASCADE);
		convertItem.setText("Convert");
		
		convertSubMenu = new Menu(shell, SWT.DROP_DOWN);
		convertItem.setMenu(convertSubMenu);
		
		convertToAsciiItem = new MenuItem(convertSubMenu, SWT.PUSH);
		convertToAsciiItem.setText("to Ascii");
		convertToAsciiItem.addListener(SWT.Selection, event -> ConvertToListener.getInstance().convertToAscii());
		
		convertToFitsItem = new MenuItem(convertSubMenu, SWT.PUSH);
		convertToFitsItem.setText("to FITS");
		convertToFitsItem.addListener(SWT.Selection, event -> ConvertToListener.getInstance().convertToFits());
		
		// Extra menu
		extraMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		extraMenuHeader.setText("&Extra");

		extraMenu = new Menu(shell, SWT.DROP_DOWN);
		extraMenuHeader.setMenu(extraMenu);

		chironToAsciiItem = new MenuItem(extraMenu, SWT.PUSH);
		chironToAsciiItem.setText("&Chiron to Ascii");
		chironToAsciiItem.addSelectionListener(ChironToAsciiItemListener.getInstance());
		
		extractFitsHeaderItem = new MenuItem(extraMenu, SWT.PUSH);
		extractFitsHeaderItem.setText("Extract &FITS Header");
		extractFitsHeaderItem.addSelectionListener(ExtractFitsHeaderItemListener.getInstance());
		
		
		shell.setMenuBar(menuBar);
		shell.addListener(SWT.Close, event -> event.doit = Message.question("Are you sure you want to quit?"));
		
		shell.pack();
		shell.setSize(shell.computeSize(1000, 1000));
		shell.open();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Exception exception) {
				Message.error("An error occured in one of the components.", exception);
			}
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

	public static void setChart(Chart chart) {
		ReSpefo.chart = chart;
	}
	
	public static void main(String[] args) {
	
		LOGGER.log(Level.INFO, "This is ReSpefo " + Version.toFullString());
		if (!Version.BUILD.equalsIgnoreCase(Version.OS)) {
			LOGGER.warning("Detected OS does not match this build!\nExpected: " + Version.BUILD + "\nDetected: " + Version.OS);
		}
		new ReSpefo();
		LOGGER.log(Level.INFO, "Program terminated");
	}
}
