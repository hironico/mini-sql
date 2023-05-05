package net.hironico.minisql.ui.renderer;

import net.hironico.minisql.ui.CopyAllAction;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public class ClobTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private static final long serialVersionUID = 1L;
    
    private static final Logger LOGGER = Logger.getLogger(ClobTableCellEditor.class.getName());

    private Object editedValue;

    private JPanel pnl = null;
    private JLabel label = null;
    private JButton button = null;

    private RTextScrollPane scrollClobViewer = null;
    private RSyntaxTextArea txtClobViewer = null;
    private JToolBar toolbarClobViewer = null;
    private JTextField txtSearchClobViewer = null;
    private JPanel pnlClobViewer = null;
    private JDialog dialogClobViewer = null;

    private TableCellEditor editorDelegate;

    private JTable parentTable = null;

    public ClobTableCellEditor(TableCellEditor delegate) {
        this.editorDelegate = delegate;
        initialize();
    }

    protected void initialize() {
        getPanel();
    }

    protected JPanel getPanel() {
        if (pnl == null) {            
            pnl = new JPanel();
            pnl.setBorder(BorderFactory.createEmptyBorder());
            pnl.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnl.add(getLabel());
            pnl.add(getButton());
        }

        return pnl;
    }

    protected JLabel getLabel() {
        if (label == null) {
            label = new JLabel();
            label.setBorder(BorderFactory.createEmptyBorder());
            label.setOpaque(false);
        }

        return label;
    }

    private JScrollPane getScrollClobViewer() {
        if (scrollClobViewer == null) {
            scrollClobViewer = new RTextScrollPane(getTxtClobViewer());
            scrollClobViewer.setBorder(BorderFactory.createEmptyBorder());
        }

        return scrollClobViewer;
    }

    private RSyntaxTextArea getTxtClobViewer() {
        if (txtClobViewer == null) {
            txtClobViewer = new RSyntaxTextArea();
            txtClobViewer.setBorder(BorderFactory.createEmptyBorder());
            txtClobViewer.setEditable(false);
            txtClobViewer.setBackground(new Color(238, 243, 250));
        }

        return txtClobViewer;
    }

    private JToolBar getToolbarClobViewer() {
        if (toolbarClobViewer == null) {
            toolbarClobViewer = new JToolBar();
            toolbarClobViewer.setFloatable(false);

            CopyAllAction copyAllAction = new CopyAllAction(this.getTxtClobViewer());
            copyAllAction.putValue(AbstractAction.LARGE_ICON_KEY, null);
            toolbarClobViewer.add(copyAllAction);
            toolbarClobViewer.addSeparator();
            toolbarClobViewer.add(new JLabel("Hightlight: "));
            toolbarClobViewer.add(getTxtSearchClobViewer());
        }

        return toolbarClobViewer;
    }

    private JTextField getTxtSearchClobViewer() {
        if(txtSearchClobViewer == null) {
            txtSearchClobViewer = new JTextField();
            txtSearchClobViewer.setBorder(BorderFactory.createEmptyBorder());

            txtSearchClobViewer.addActionListener(evt -> {
                try {
                    String word = getTxtSearchClobViewer().getText();
                    final Highlighter h = txtClobViewer.getHighlighter();
                    h.removeAllHighlights();

                    if (word == null || "".equals(word)) {
                        Rectangle rect = getTxtClobViewer().modelToView(0);
                        getScrollClobViewer().scrollRectToVisible(rect);
                        return;
                    }

                    final Document doc = txtClobViewer.getDocument();
                    final String fullText = doc.getText(0, doc.getLength());

                    DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);

                    int pos = 0;
                    int firstOccurencePos = -1;
                    while ((pos = fullText.indexOf(word, pos)) >= 0) {
                        if (firstOccurencePos == -1) {
                            firstOccurencePos = pos;
                        }
                        h.addHighlight(pos, pos + word.length(), painter);
                        pos += word.length();
                    }

                    if (firstOccurencePos >= 0) {
                        getTxtClobViewer().setCaretPosition(firstOccurencePos);
                        Rectangle rect = getTxtClobViewer().modelToView(firstOccurencePos);
                        getScrollClobViewer().scrollRectToVisible(rect);
                    }
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.SEVERE, "Problem while highlighting things in CLOB viewer.", ex);
                }
            });
        }

        return txtSearchClobViewer;
    }

    private JPanel getPnlClobViewer() {
        if (pnlClobViewer == null) {
            pnlClobViewer = new JPanel();
            pnlClobViewer.setLayout(new BorderLayout());
            pnlClobViewer.add(getToolbarClobViewer(), BorderLayout.NORTH);
            pnlClobViewer.add(getScrollClobViewer(), BorderLayout.CENTER);
        }

        return pnlClobViewer;
    }

    private JDialog getDialogClobViewer(Component parent) {
        if (dialogClobViewer == null) {
            dialogClobViewer = new JDialog((Window)null, "CLOB viewer...");
            dialogClobViewer.setModal(true);
            dialogClobViewer.getContentPane().add(this.getPnlClobViewer());
            dialogClobViewer.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialogClobViewer.addWindowListener(new WindowAdapter()  {
                @Override
                public void windowClosed(WindowEvent evt) {
                    ClobTableCellEditor.this.stopCellEditing();
                }

                @Override
                public void windowClosing(WindowEvent evt) {
                    LOGGER.info("Window closing.");
                }
            });

            // int width = Math.min(parent.getWidth() - 40, 240);
            // int height = Math.min(parent.getHeight() - 40, 600);

            
        }

        int width = 240;
        int height = 350;

        int x = (int) parent.getLocationOnScreen().getX();
        int y = (int) parent.getLocationOnScreen().getY();

        dialogClobViewer.setSize(width, height);    
        dialogClobViewer.setLocationRelativeTo(parent);
        dialogClobViewer.setLocation(x + 10, y + 10);

        dialogClobViewer.toFront();
        dialogClobViewer.requestFocus();

        return dialogClobViewer;
    }

    protected JButton getButton() {
        if (button == null) {
            button = new JButton();
            button.setText("...");
            button.setBorderPainted(false);
            button.setMargin(new Insets(0, 0, 0, 0));

            button.addActionListener(evt -> {
                try {
                    String txt = (String)ClobTableCellEditor.this.editedValue;

                    if (txt == null) {
                        LOGGER.fine("CLOB Edited value is null.");
                        return;
                    }

                    // change Record Separator 'RS' chars into '\n'
                    txt = txt.replace((char)0x1E, '\n');

                    // change Unit Separator 'US' chars into '\''
                    txt = txt.replace((char)0x1F, '\'');

                    getTxtClobViewer().setText(txt);
                    getTxtClobViewer().setCaretPosition(0);

                    getTxtSearchClobViewer().setText("");

                    JDialog dlg = getDialogClobViewer(parentTable.getParent().getParent());
                    dlg.setSize(parentTable.getParent().getWidth() / 2, dlg.getHeight());
                    dlg.setVisible(true);
                    dlg.toFront();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Error while getting the CLOB value into readable format.");
                    LOGGER.log(Level.SEVERE, "Cannot display CLOB value.", ex);
                }
            });
        }

        return button;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        this.editedValue = value;
        this.parentTable = table;

        Class<?> clazz = table.getModel().getColumnClass(column);                
        if (clazz.getName().toLowerCase().endsWith("clob")) {
            try {
                String txt = value == null ? "<NULL>" : "<CLOB>";
                getLabel().setText(txt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
            getPanel().setBackground(bg);

            Color fg = isSelected ? table.getSelectionForeground() : table.getForeground();
            getLabel().setForeground(fg);

            return getPanel();
        } else {
            LOGGER.warning("Using delegate editor ...");
            return this.editorDelegate.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }

    @Override
    public Object getCellEditorValue() {
        return this.editedValue;
    }

    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    public void cancelCellEditing() {
        super.cancelCellEditing();
    }
}