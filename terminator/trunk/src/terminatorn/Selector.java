package terminatorn;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

/**

@author Phil Norman
*/

public class Selector implements MouseListener, MouseMotionListener, Highlighter {
	private JTextBuffer view;
	private Highlight highlight;
	private Location startLocation;
	
	/** Creates a Selector for selecting text in the given view, and adds us as mouse listeners to that view. */
	public Selector(JTextBuffer view) {
		this.view = view;
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		view.addHighlighter(this);
	}
	
	// Mouse (motion) listener methods.

	public void mouseClicked(MouseEvent event) {
		if (event.getButton() == MouseEvent.BUTTON1) {
			Location loc = view.viewToModel(event.getPoint());
			TextBuffer model = view.getModel();
			if (loc.getLineIndex() >= model.getLineCount()) {
				return;
			}
			if (event.getClickCount() == 2) {  // Select word.
				String line = model.getLine(loc.getLineIndex());
				if (loc.getCharOffset() >= line.length()) {
					return;
				}
				selectWord(model, loc);
			} else if (event.getClickCount() == 3) {  // Select line.
				Location start = new Location(loc.getLineIndex(), 0);
				Location end = new Location(loc.getLineIndex() + 1, 0);
				setHighlight(start, end);
			}
		} else if (event.getButton() == MouseEvent.BUTTON2) {
			try {
				Transferable contents = view.getToolkit().getSystemClipboard().getContents(view);
				String string = (String) contents.getTransferData(DataFlavor.stringFlavor);
				view.insertText(string);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void selectWord(TextBuffer model, Location location) {
		Location start = new Location(location.getLineIndex(), location.getCharOffset());
		Location end = new Location(location.getLineIndex(), location.getCharOffset() + 1);
		setHighlight(start, end);
	}
	
	public void mousePressed(MouseEvent event) {
		if (event.getButton() == MouseEvent.BUTTON1) {
			view.removeHighlightsFrom(this, 0);
			startLocation = view.viewToModel(event.getPoint());
		}
	}
	
	public void mouseReleased(MouseEvent event) {
		if (event.getButton() == MouseEvent.BUTTON1) {
			if (highlight != null) {
				setClipboard(view.getText(highlight));
			}
			startLocation = null;
		}
	}
	
	/**
	 * Sets the clipboard (and X11's nasty hacky semi-duplicate).
	 */
	public static void setClipboard(String newContents) {
		StringSelection selection = new StringSelection(newContents);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		toolkit.getSystemClipboard().setContents(selection, selection);
		if (toolkit.getSystemSelection() != null) {
			toolkit.getSystemSelection().setContents(selection, selection);
		}
	}
	
	public void mouseDragged(MouseEvent event) {
		if (startLocation != null) {
			view.removeHighlightsFrom(this, 0);
			Location endLocation = view.viewToModel(event.getPoint());
			if (endLocation.equals(startLocation)) {
				return;
			}
			setHighlight(min(startLocation, endLocation), max(startLocation, endLocation));
			view.scrollRectToVisible(new Rectangle(0, event.getY(), 10, 10));
		}
	}
	
	public void mouseMoved(MouseEvent event) { }
	
	public void mouseEntered(MouseEvent event) { }
	
	public void mouseExited(MouseEvent event) { }
	
	private void setHighlight(Location start, Location end) {
		highlight = new Highlight(this, start, end, new SelectedStyleMutator());
		view.addHighlight(highlight);
	}

	// Highlighter methods.
	
	public String getName() {
		return "Selection Highlighter";
	}
	
	/** Request to add highlights to all lines of the view from the index given onwards. */
	public void addHighlights(JTextBuffer view, int firstLineIndex) { }

	/** Request to do something when the user clicks on a Highlight generated by this Highlighter. */
	public void highlightClicked(JTextBuffer view, Highlight highlight, String highlitText, MouseEvent event) { }

	public static class SelectedStyleMutator implements StyleMutator {
		public Style mutate(Style style) {
			// Not obvious when you first look at it, but this just switches foreground and background colours.
			return new Style(style.getBackground(), style.getForeground(), style.isBold(), style.isUnderlined());
		}
	}
	
	// These two should be in java.lang.Math, but they're not.
	
	public Location min(Location one, Location two) {
		return (one.compareTo(two) < 0) ? one : two;
	}
	
	public Location max(Location one, Location two) {
		return (one.compareTo(two) > 0) ? one : two;
	}
}
