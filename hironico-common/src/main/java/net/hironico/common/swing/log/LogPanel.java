package net.hironico.common.swing.log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import java.util.logging.LogManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Log panel for displaying global JDK log into a JPanel. Maximum number of rows is set to 5000 by default but can 
 * be specified 
 * @see SwingHandler
 */
public class LogPanel extends JPanel {
    private static final long serialVersionUID = -3157791008840079496L;

    private JScrollPane scrollLog = null;
    private JTextArea txtLog = null;
    private int maxRows = 5000;
    private SwingHandler swingHandler = null;

    public LogPanel() {
        initialize();
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
        getSwingHandler().setMaxRows(maxRows);
    }

    protected void initialize() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(getScrollLog(), BorderLayout.CENTER);
    }

    protected JScrollPane getScrollLog() {
        if (scrollLog == null) {
            scrollLog = new JScrollPane(getTxtLog());
            scrollLog.setBorder(BorderFactory.createEmptyBorder());
        }

        return scrollLog;
    }

    protected JTextArea getTxtLog() {
        if (this.txtLog == null) {
            txtLog = new JTextArea();
            txtLog.setBorder(BorderFactory.createEmptyBorder());
            txtLog.setEditable(false);

            try {
                Font font = new Font("Consolas", Font.PLAIN, 12);
                txtLog.setFont(font);
            } catch (Exception ex) {
                System.out.println("Cannot set font for Swing Logger !");
                ex.printStackTrace();
            }

            try {
                LogManager.getLogManager().getLogger("").addHandler(getSwingHandler());
            } catch (Exception ex) {
                System.out.println("Cannot create LOG handler to swing!");
                ex.printStackTrace();
            }
        }

        return this.txtLog;
    }

    protected SwingHandler getSwingHandler() {
        if (swingHandler == null) {
            try {
                swingHandler = new SwingHandler(txtLog, maxRows);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return swingHandler;
    }
}