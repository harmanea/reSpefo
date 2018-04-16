package cz.cuni.mff.respefo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;
import cz.cuni.mff.respefo.Listeners.FileExportItemListener;
import cz.cuni.mff.respefo.Listeners.FileImportItemListener;
import cz.cuni.mff.respefo.Listeners.MeasureRVItemListener;
import cz.cuni.mff.respefo.Listeners.RectifyItemListener;

public class ReSpefo {

  static Display display;

  static Shell shell;

  static Menu menuBar, fileMenu, toolsMenu;

  static MenuItem fileMenuHeader, toolsMenuHeader;

  static MenuItem fileExportItem, fileImportItem, rectifyItem, measureRVItem;
  
  static Spectrum spectrum;
  
  static Chart chart;

  public ReSpefo() {

    display = new Display();
    shell = new Shell(display);
    shell.setText("reSpefo");
    shell.setSize(1000, 1000);
    shell.setLayout(new FillLayout());

    menuBar = new Menu(shell, SWT.BAR);
    fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    fileMenuHeader.setText("&File");

    fileMenu = new Menu(shell, SWT.DROP_DOWN);
    fileMenuHeader.setMenu(fileMenu);

    fileImportItem = new MenuItem(fileMenu, SWT.PUSH);
    fileImportItem.setText("&Import");

    fileExportItem = new MenuItem(fileMenu, SWT.PUSH);
    fileExportItem.setText("&Export");

    toolsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    toolsMenuHeader.setText("&Tools");

    toolsMenu = new Menu(shell, SWT.DROP_DOWN);
    toolsMenuHeader.setMenu(toolsMenu);

    rectifyItem = new MenuItem(toolsMenu, SWT.PUSH);
    rectifyItem.setText("&Rectify");
    
    measureRVItem = new MenuItem(toolsMenu, SWT.PUSH);
    measureRVItem.setText("&Measure RV");

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
}
