package net.hironico.common.swing;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JTextPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.Arrays;

public class JTextFieldInputStream extends InputStream {
    // private static final Logger LOGGER = Logger.getLogger(JTextFieldInputStream.class.getName());

    int[] contents = new int[20];
    int pointer = -1;

    private Object semaphore = new Object();

    public JTextFieldInputStream(JTextPane text) {

        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (pointer > contents.length - 2) {
                    contents = Arrays.copyOf(contents, contents.length * 2);
                }

                contents[pointer + 1] = e.getKeyChar();
                pointer++;
                super.keyReleased(e);

                synchronized (semaphore) {
                    semaphore.notify();
                }
            }
        });
    }

    public int read(byte[] b, int off, int len) throws IOException {
        try {
            synchronized (semaphore) {
                semaphore.wait();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException(t);
        }

        return super.read(b, off, (len > 1024 ? 1024 : len));
    }

    @Override
    public int read() throws IOException {
        if ((pointer >= contents.length) || (pointer < 0)) {
            return -1;
        }
        pointer--;
        return contents[0];
    }

    @Override
    public int available() throws IOException {
        return this.pointer + 1;
    }

}