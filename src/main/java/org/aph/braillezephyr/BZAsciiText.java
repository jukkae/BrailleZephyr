package org.aph.braillezephyr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;

public class BZAsciiText {

	private final static char PARAGRAPH_END = 0xfeff;

	private int linesPerPage = 25;
	private int charsPerLine = 40;
	private String eol = System.getProperty("line.separator");

	private final StyledText styledText;
	private final Color color;

	private BZStyledText bzStyledText;

	public BZAsciiText(Shell shell, BZStyledText bzStyledText) {
		styledText = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP
				| SWT.V_SCROLL);
		styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
		KeyHandler keyHandler = new KeyHandler();
		styledText.addKeyListener(keyHandler);
		styledText.addPaintListener(new PaintHandler());

		this.bzStyledText = bzStyledText;

		StyledTextContent content = bzStyledText.getStyledTextContent();
		styledText.setContent(content);

		Font font = new Font(styledText.getDisplay(), "Courier", 15, SWT.NORMAL);
		styledText.setFont(font);

		color = styledText.getDisplay().getSystemColor(SWT.COLOR_BLACK);
	}

	private boolean isFirstLineOnPage(int index) {
		return index % linesPerPage == 0;
	}

	private class KeyHandler implements KeyListener{

		public void keyPressed(KeyEvent event) {
			styledText.insert(Character.toString(event.character));
		}

		public void keyReleased(KeyEvent arg0) {
			
		}
	}

	private class PaintHandler implements PaintListener {
		public void paintControl(PaintEvent event) {
			event.gc.setForeground(color);
			event.gc.setBackground(color);
			int lineHeight = styledText.getLineHeight();
			int drawHeight = styledText.getClientArea().height;
			int drawWidth = styledText.getClientArea().width;
			int rightMargin = event.gc.stringExtent(" ").x * charsPerLine;

			event.gc.drawLine(rightMargin, 0, rightMargin, drawHeight);

			int at;
			for (int i = styledText.getTopIndex(); i < styledText
					.getLineCount(); i++) {
				at = styledText.getLinePixel(i);
				if (isFirstLineOnPage(i))
					event.gc.drawLine(0, at, drawWidth, at);

				String line = styledText.getLine(i);
				if (line.length() > 0
						&& line.charAt(line.length() - 1) == PARAGRAPH_END) {
					Point point = event.gc.stringExtent(line);
					int span = point.y / 2;
					event.gc.fillOval(point.x + span / 2, at + span / 2, span,
							span);
				}

				if (at + lineHeight > drawHeight)
					break;
			}
		}
	}

}
