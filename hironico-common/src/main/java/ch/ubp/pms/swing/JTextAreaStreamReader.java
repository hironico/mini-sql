package ch.ubp.pms.swing;

import java.io.InputStream;
import java.util.function.Consumer;
import java.io.IOException;

import javax.swing.JTextArea;

import ch.ubp.pms.utils.StringUtils;

/**
 * This is a stream reader to put LIVE data read from the stream into a JTextArea.
 * This is a non blocking reader meaning the data is read in a separeated thread every 250 milliseconds.
 * 
 */
public class JTextAreaStreamReader implements Consumer<Void> {

    private JTextArea txtTarget;
    private Thread displayThread;
    private int maxRows = 5000;

    public JTextAreaStreamReader(InputStream srcInputStream, JTextArea txtTarget) {
        this(srcInputStream, txtTarget, 5000);
    }

    public JTextAreaStreamReader(InputStream srcInputStream, JTextArea txtTarget, int maxRows) {
        assert txtTarget != null;
        assert srcInputStream != null;

        this.txtTarget = txtTarget;
        this.maxRows = maxRows;

        displayThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[1024 * 1024];
                    while (true) {
                        int available = srcInputStream.available();
                        while (available > 0) {
                            int read = srcInputStream.read(buffer, 0, buffer.length);

                            if (JTextAreaStreamReader.this.txtTarget != null) {
                                JTextAreaStreamReader.this.txtTarget.append(new String(buffer, 0, read));
                                JTextAreaStreamReader.this.checkMaxRows();
                            }

                            available = srcInputStream.available();
                        }

                        Thread.sleep(50);
                    }
                } catch (Exception ex) {
                    txtTarget.append("Communication BROKEN.");
                }
            }
        });

        displayThread.setName("JTextArea display thread");
        displayThread.start();
    }

    public void die() throws IOException {
        System.out.println("Stream reader dying ...");
        this.displayThread.interrupt();
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    protected void checkMaxRows() {
        try {
            int rows = StringUtils.countOccurences(txtTarget.getText(), '\n');
            while (rows > maxRows) {
                String txt = txtTarget.getText();
                int index = txt.indexOf("\n");
                if (index < 0) {
                    return;
                }

                txtTarget.getDocument().remove(0, index + 1);

                rows = StringUtils.countOccurences(txtTarget.getText(), '\n');
            }
        } catch (Exception ex) {
            // no op
        }
    }

    @Override
    public void accept(Void v) {
        try {
            die();
        } catch (Exception ex) {
            // never mind
        }

    }
}
