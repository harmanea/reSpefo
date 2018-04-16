package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.swtchart.Chart;
import org.swtchart.IAxis;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;

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
		}
		chart.redraw();
	}

	@Override
	public void keyReleased(KeyEvent e) {}

}
