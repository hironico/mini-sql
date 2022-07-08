package ch.ubp.pms.swing.log;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JTextArea;

public class SwingHandler extends Handler {

    private JTextArea txt = null;
    private Formatter formatter = null;
    private int maxRows = -1;

    public SwingHandler(JTextArea txt, int maxRows) throws Exception {
        this(txt, null, maxRows);
    }

    public SwingHandler(JTextArea txt) throws Exception {
        this(txt, null, 5000);
    }

    public SwingHandler(JTextArea txt, Formatter fmt, int maxRows) throws Exception {
        this.txt = txt;
        this.formatter = fmt == null ? new SimpleFormatter() : fmt;
        this.maxRows = maxRows;
    }

    @Override
    public void publish(LogRecord record) {
        try {
            this.txt.append(formatter.format(record));
            if (maxRows > 0) {
                while (this.txt.getDocument().getDefaultRootElement().getElementCount() > maxRows) {
                    int end = this.txt.getLineEndOffset(0);
                    this.txt.replaceRange("", 0, end);
                }
            }
        } catch (Exception ex) {
            System.out.println("Cannot send log to swing stream reader.");
        }
    }
  
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    @Override
    public void close() throws SecurityException {
        // no op
        System.out.println("Close swing handler");
    }

    @Override
    public void flush() {
        // no op
        System.out.println("Flush swing handler");
    }
}