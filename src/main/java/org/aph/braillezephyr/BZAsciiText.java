package org.aph.braillezephyr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
	
	public BZAsciiText(Shell shell, BZStyledText bzStyledText) {
		styledText = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
		KeyHandler keyHandler = new KeyHandler();
		styledText.addKeyListener(keyHandler);
		styledText.addVerifyKeyListener(keyHandler);
		styledText.addPaintListener(new PaintHandler());

		Font font = new Font(styledText.getDisplay(), "Courier", 15, SWT.NORMAL);
		styledText.setFont(font);

		color = styledText.getDisplay().getSystemColor(SWT.COLOR_BLACK);
	}
	
	private boolean isFirstLineOnPage(int index)
	{
		return index % linesPerPage == 0;
	}
	
	private class KeyHandler implements KeyListener, VerifyKeyListener
	{
		char dotState = 0, dotChar = 0x2800;

		public void keyPressed(KeyEvent event)
		{
			switch(event.character)
			{
			case 'f':

				dotState |= 0x01;
				dotChar |= 0x01;
				break;

			case 'd':

				dotState |= 0x02;
				dotChar |= 0x02;
				break;

			case 's':

				dotState |= 0x04;
				dotChar |= 0x04;
				break;

			case 'j':

				dotState |= 0x08;
				dotChar |= 0x08;
				break;

			case 'k':

				dotState |= 0x10;
				dotChar |= 0x10;
				break;

			case 'l':

				dotState |= 0x20;
				dotChar |= 0x20;
				break;

			}
		}

		public void keyReleased(KeyEvent event)
		{
			switch(event.character)
			{
			case 'f':

				dotState &= ~0x01;
				break;

			case 'd':

				dotState &= ~0x02;
				break;

			case 's':

				dotState &= ~0x04;
				break;

			case 'j':

				dotState &= ~0x08;
				break;

			case 'k':

				dotState &= ~0x10;
				break;

			case 'l':

				dotState &= ~0x20;
				break;

			}

			if(dotState == 0 && (dotChar & 0xff) != 0)
			{
				dotChar = asciiBraille.charAt((dotChar & 0xff));
				styledText.insert(Character.toString(dotChar));
				styledText.setCaretOffset(styledText.getCaretOffset() + 1);
				dotChar = 0x2800;
			}
		}

		private final String asciiBraille = " A1B'K2L@CIF/MSP\"E3H9O6R^DJG>NTQ,*5<-U8V.%[$+X!&;:4\\0Z7(_?W]#Y)=";

		public void verifyKey(VerifyEvent event)
		{
			if(event.keyCode == '\r' || event.keyCode == '\n')
			if((event.stateMask & SWT.SHIFT) != 0)
			{
				event.doit = false;
				int index = styledText.getLineAtOffset(styledText.getCaretOffset());
				String line = styledText.getLine(index);
				if(line.length() > 0)
				if(line.charAt(line.length() - 1) != PARAGRAPH_END)
					styledText.replaceTextRange(styledText.getOffsetAtLine(index), line.length(), line + Character.toString(PARAGRAPH_END));
				else
					styledText.replaceTextRange(styledText.getOffsetAtLine(index), line.length(), line.substring(0, line.length() - 1));
				return;
			}

			if(event.character > ' ' && event.character < 0x7f)
				event.doit = false;
		}
	}

	private class PaintHandler implements PaintListener
	{
		public void paintControl(PaintEvent event)
		{
			event.gc.setForeground(color);
			event.gc.setBackground(color);
			int lineHeight = styledText.getLineHeight();
			int drawHeight = styledText.getClientArea().height;
			int drawWidth = styledText.getClientArea().width;
			int rightMargin = event.gc.stringExtent(" ").x * charsPerLine;

			event.gc.drawLine(rightMargin, 0, rightMargin, drawHeight);

			int at;
			for(int i = styledText.getTopIndex(); i < styledText.getLineCount(); i++)
			{
				at = styledText.getLinePixel(i);
				if(isFirstLineOnPage(i))
					event.gc.drawLine(0, at, drawWidth, at);

				String line = styledText.getLine(i);
				if(line.length() > 0 && line.charAt(line.length() - 1) == PARAGRAPH_END)
				{
					Point point = event.gc.stringExtent(line);
					int span = point.y / 2;
					event.gc.fillOval(point.x + span / 2, at + span / 2, span, span);
				}

				if(at + lineHeight > drawHeight)
					break;
			}
		}
	}

}
