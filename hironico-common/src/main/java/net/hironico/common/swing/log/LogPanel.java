package net.hironico.common.swing.log;

import net.hironico.common.swing.SortedComboBoxModel;
import net.hironico.common.utils.StreamUtils;

import java.awt.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.*;

import static java.util.logging.Level.*;

/**
 * Log panel for displaying global JDK log into a JPanel. Maximum number of rows is set to 5000 by default but can 
 * be specified 
 * @see SwingHandler
 */
public class LogPanel extends JPanel {
    private JPanel pnlTools = null;
    private JComboBox<String> cmbLogName = null;
    private JButton btnRefresh = null;
    private JComboBox<Level> cmbLogLevel = null;
    private JButton btnClear = null;
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
        add(getPnlTools(), BorderLayout.NORTH);
        add(getScrollLog(), BorderLayout.CENTER);
    }

    private JPanel getPnlTools() {
        if (pnlTools == null) {
            pnlTools = new JPanel();
            pnlTools.setLayout(new FlowLayout(FlowLayout.LEADING));
            pnlTools.add(new JLabel("Logger name:"));
            pnlTools.add(getCmbLogName());
            pnlTools.add(getBtnRefresh());
            pnlTools.add(new JLabel("Log level:"));
            pnlTools.add(getCmbLogLevel());
            pnlTools.add(getBtnClear());
        }

        return pnlTools;
    }

    private JComboBox<String> getCmbLogName() {
        if (cmbLogName == null) {
            SortedComboBoxModel<String> model = new SortedComboBoxModel<>();
            cmbLogName = new JComboBox<>(model);
            StreamUtils.stream(LogManager.getLogManager().getLoggerNames()).forEach(model::addElement);

            cmbLogName.addActionListener(actionEvent -> {
                String selectedLogName = (String) getCmbLogName().getSelectedItem();
                if (selectedLogName != null) {
                    Level level = Optional.ofNullable(Logger.getLogger(selectedLogName).getLevel()).orElse(LogManager.getLogManager().getLogger("").getLevel());
                    getCmbLogLevel().setSelectedItem(level);
                }
            });
        }

        return cmbLogName;
    }

    private JButton getBtnRefresh() {
        if (btnRefresh == null) {
            btnRefresh = new JButton("Refresh");
            btnRefresh.addActionListener(actionEvent -> {
                SortedComboBoxModel<String> model = (SortedComboBoxModel<String>)getCmbLogName().getModel();
                model.removeAllElements();
                StreamUtils.stream(LogManager.getLogManager().getLoggerNames()).forEach(model::addElement);
            });
        }

        return btnRefresh;
    }

    private JComboBox<Level> getCmbLogLevel() {
        if (cmbLogLevel == null) {
            cmbLogLevel = new JComboBox<>();
            Level[] standardLevels = {
                    OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
            };

            Arrays.stream(standardLevels).forEach(level -> cmbLogLevel.addItem(level));

            cmbLogLevel.addActionListener(actionEvent -> {
                String selectedLogName = (String) getCmbLogName().getSelectedItem();
                if (selectedLogName != null) {
                    Logger.getLogger(selectedLogName).setLevel((Level) getCmbLogLevel().getSelectedItem());
                }
            });
        }

        return cmbLogLevel;
    }

    private JButton getBtnClear() {
        if (btnClear == null) {
            btnClear = new JButton("Clear");
            btnClear.addActionListener(actionEvent -> {
                getTxtLog().setText("");
            });
        }

        return btnClear;
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