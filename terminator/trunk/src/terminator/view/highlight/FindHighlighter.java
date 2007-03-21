package terminator.view.highlight;

import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;
import java.util.regex.*;
import javax.swing.*;
import org.jdesktop.swingworker.SwingWorker;
import terminator.model.*;
import terminator.view.*;

/**
 * Highlights the results of user-initiated finds.
 */
public class FindHighlighter implements Highlighter {
	private static final ExecutorService executorService = ThreadUtilities.newSingleThreadExecutor("Background Find");
	
	/** The highlighter pen style. */
	private final Style style = new Style(Color.black, Color.yellow, null, null, false);

	private Pattern pattern;
	private String regularExpression;
	
	public String getName() {
		return "Find Highlighter";
	}
	
	/**
	 * Sets the current sought regular expression. Existing highlights will
	 * be removed, matches in the current text will be found, and future
	 * matches will be found as they appear.
	 * 
	 * 'newPattern' can be null to cancel match highlighting.
	 * 
	 * Returns the current number of matches.
	 */
	public void setPattern(final JTextBuffer view, String regularExpression, Pattern newPattern, final JLabel statusLine) {
		forgetPattern(view);
		if (newPattern == null) {
			return;
		}
		
		this.pattern = newPattern;
		this.regularExpression = regularExpression;
		executorService.execute(new SwingWorker<Object, Object>() {
			private int matchCount;
			
			@Override
			protected Object doInBackground() {
				matchCount = addHighlights(view, 0);
				return null;
			}
			
			@Override
			protected void done() {
				statusLine.setText("Matches: " + matchCount);
			}
		});
	}
	
	public void forgetPattern(JTextBuffer view) {
		view.removeHighlightsFrom(this, 0);
		this.pattern = null;
		this.regularExpression = null;
	}
	
	public String getRegularExpression() {
		return regularExpression;
	}
	
	/** Request to add highlights to all lines of the view from the index given onwards. */
	public int addHighlights(JTextBuffer view, int firstLineIndex) {
		view.getBirdView().setValueIsAdjusting(true);
		try {
			TextBuffer model = view.getModel();
			int count = 0;
			for (int i = firstLineIndex; i < model.getLineCount(); i++) {
				String line = model.getTextLine(i).getString();
				count += addHighlightsOnLine(view, i, line);
			}
			return count;
		} finally {
			view.getBirdView().setValueIsAdjusting(false);
		}
	}
	
	private int addHighlightsOnLine(JTextBuffer view, int lineIndex, String text) {
		if (pattern == null) {
			return 0;
		}
		int count = 0;
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			Location start = new Location(lineIndex, matcher.start());
			Location end = new Location(lineIndex, matcher.end());
			Highlight highlight = new Highlight(this, start, end, style);
			view.addHighlight(highlight);
			++count;
		}
		return count;
	}

	/** Request to do something when the user clicks on a Highlight generated by this Highlighter. */
	public void highlightClicked(JTextBuffer view, Highlight highlight, String text, MouseEvent event) {
		return;
	}
}
