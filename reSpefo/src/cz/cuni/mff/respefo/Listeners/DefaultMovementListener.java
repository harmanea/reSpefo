package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.swtchart.Chart;
import org.swtchart.IAxis;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;

/**
 * Adds the default movement capabilities
 * 
 * Listens for:
 * 'w' / Arrow Up = Move the view up
 * 'a' / Arrow Left = Move the view left
 * 's' / Arrow Down = Move the view down
 * 'd' / Arrow Right = Move the view right
 * 
 * '+' = Zoom in
 * '-' = Zoom out
 * 
 * '8' / Numpad 8 = Zoom in Y Axis
 * '2' / Numpad 2 = Zoom Out Y Axis
 * '6' / Numpad 6 = Zoom in X Axis
 * '4' / Numpad 4 = Zoom Out X Axis
 * 
 * Space = Adjust the view so that all series are visible
 */
public class DefaultMovementListener implements KeyListener {

	@Override
	public void keyPressed(KeyEvent e) {
		Chart chart = ReSpefo.getChart();

		switch (e.keyCode) {
		case 'w':
		case SWT.ARROW_UP:
			for (IAxis i : chart.getAxisSet().getYAxes()) {
				i.scrollUp();
			}
			break;
		case 'a':
		case SWT.ARROW_LEFT:
			for (IAxis i : chart.getAxisSet().getXAxes()) {
				i.scrollDown();
			}
			break;
		case 's':
		case SWT.ARROW_DOWN:
			for (IAxis i : chart.getAxisSet().getYAxes()) {
				i.scrollDown();
			}
			break;
		case 'd':
		case SWT.ARROW_RIGHT:
			for (IAxis i : chart.getAxisSet().getXAxes()) {
				i.scrollUp();
			}
			break;
		case SWT.SPACE:
			ChartBuilder.adjustRange(chart);
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

	@Override
	public void keyReleased(KeyEvent e) {}

}
