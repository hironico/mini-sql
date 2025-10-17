package net.hironico.minisql.ui.history;

import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import net.hironico.minisql.ui.CopyAllAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.common.swing.table.FilterableTable;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class QueryHistoryPanel extends JPanel {
    private static final long serialVersionUID = -1L;

    private static final DateFormat localeDateFormat = DateFormat.getDateTimeInstance();

    private JToolBar searchBar = null;
    private JXTextField txtFilter = null;

    private JScrollPane scrollHistory = null;
    private FilterableTable tableHistory= null;
    private QueryHistoryTableModel queryHistoryTableModel = null;

    private JXPanel pnlInfos = null;
    private JXLabel lblInfos = null;
    private JButton btnCopy = null;

    private RTextScrollPane scrollPreview = null;
    private RSyntaxTextArea txtPreview = null;

    public QueryHistoryPanel() {
        initialize();
    }

    protected void initialize() {
        setOpaque(true);
        setBackground(new Color(236, 243, 250));

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(getSearchBar(), gbc);

        gbc.gridy++;
        gbc.weightx = 1.0d;
        gbc.weighty = 0.75d;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(getScrollHistory(), gbc);

        gbc.gridy++;
        gbc.weighty = 0.0d;
        this.add(getPnlInfos(), gbc);

        gbc.gridy++;
        gbc.weighty = 0.25d;
        this.add(getScrollPreview(), gbc);
    }

    private JToolBar getSearchBar() {
        if (this.searchBar == null) {
            this.searchBar = new JToolBar();
            this.searchBar.setFloatable(false);
            this.searchBar.add(new JLabel("Filter:"));
            this.searchBar.add(getTxtFilter());
        }

        return this.searchBar;
    }

    private JXTextField getTxtFilter() {
        if (this.txtFilter == null) {
            txtFilter = new JXTextField();
            txtFilter.setPrompt("keyword filter");
            txtFilter.addActionListener(evt -> getTableHistory().applyFilter(txtFilter.getText(), 1));
        }

        return txtFilter;
    }

    private QueryHistoryEntry getSelectedQueryHistoryEntry() {
        int row = getTableHistory().getSelectedRow();
        return QueryHistory.getInstance().getQueryHistoryAt(row);
    }

    private JScrollPane getScrollHistory() {
        if (scrollHistory == null) {
            scrollHistory = new JScrollPane(getTableHistory());
            scrollHistory.setBorder(BorderFactory.createEmptyBorder());
            scrollHistory.setOpaque(true);
            scrollHistory.getViewport().setOpaque(true);
            scrollHistory.getViewport().setBackground(new Color(236,243,250));
            scrollHistory.setBackground(new Color(236, 243, 250));
            scrollHistory.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        }

        return scrollHistory;
    }

    private FilterableTable getTableHistory() {
        if (tableHistory == null) {
            tableHistory = new FilterableTable();
            tableHistory.setOpaque(true);
            tableHistory.setBackground(new Color(236, 243, 250));
            tableHistory.addHighlighter(HighlighterFactory.createSimpleStriping());
            tableHistory.setEditable(false);
            tableHistory.setColumnControlVisible(false);
            tableHistory.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            tableHistory.setSortable(false);

            tableHistory.setModel(getQueryHistoryTableModel());

            Font font = getTxtPreview().getFont();
            tableHistory.setFont(font);
            FontMetrics fm = tableHistory.getFontMetrics(font);
            int width = fm.stringWidth(" MM/MM/MMMM 99:99:99 ") + 10;

            TableColumn colDate = tableHistory.getColumn(0);
            colDate.setMinWidth(width);
            colDate.setMaxWidth(width);
            colDate.setResizable(false);

            DefaultTableCellRenderer datetimeRenderer = new DefaultTableCellRenderer() {
                final SimpleDateFormat shortDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                public Component getTableCellRendererComponent(JTable table,
                                                               Object value, boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    if (value instanceof Date) {
                        value = shortDateTimeFormat.format(value);
                    } else if (value instanceof Long) {
                        value = shortDateTimeFormat.format(new Date((Long)value));
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);
                }
            };
            colDate.setCellRenderer(datetimeRenderer);
            tableHistory.setDefaultRenderer(Date.class, datetimeRenderer);

            tableHistory.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() >= 2) {

                        QueryHistoryEntry selectedEntry = getSelectedQueryHistoryEntry();
                        if (selectedEntry == null) {
                            return;
                        }

                        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
                        if (comp instanceof QueryPanel) {
                            QueryPanel queryPanel = (QueryPanel)comp;
                            String newSql = String.format("%s\n\n%s",queryPanel.getQueryText(), selectedEntry.query);
                            queryPanel.setQueryText(newSql);
                        }
                    }
                }
            });

            tableHistory.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    super.mouseMoved(e);
                    int index = getTableHistory().rowAtPoint(e.getPoint());
                    if (index < 0) {
                        return;
                    }
                    index = getTableHistory().getRowSorter().convertRowIndexToModel(index);
                    QueryHistoryEntry entry = QueryHistory.getInstance().getQueryHistoryAt(index);
                    String value = entry.query;
                    int length = Math.min(value.length(), 300);
                    Date time = new Date(entry.timestamp);
                    String queryText = value.substring(0, length).replaceAll("\n", "<br/>");
                    String html = String.format("<html><h4>Executed at %s</h4><code style=\"background-color: white;\">%s</code></html>", time, queryText);
                    getTableHistory().setToolTipText(html);

                }
            });

            tableHistory.getSelectionModel().addListSelectionListener(evt -> {
                if(evt.getValueIsAdjusting()) {
                    return;
                }

                QueryHistoryEntry entry = getSelectedQueryHistoryEntry();
                getTxtPreview().setText(entry == null ? "" : entry.query);
                getLblInfos().setText(entry == null ? "" : localeDateFormat.format(new Date(entry.timestamp)));
            });

        }

        return tableHistory;
    }

    private QueryHistoryTableModel getQueryHistoryTableModel() {
        if (this.queryHistoryTableModel == null) {
            this.queryHistoryTableModel = new QueryHistoryTableModel();
        }

        return this.queryHistoryTableModel;
    }


    private JXPanel getPnlInfos() {
        if (pnlInfos == null) {
            pnlInfos = new JXPanel();
            pnlInfos.setLayout(new FlowLayout(FlowLayout.TRAILING));
            pnlInfos.setOpaque(true);
            pnlInfos.setBackground(Color.WHITE);
            pnlInfos.add(getLblInfos());
            pnlInfos.add(getBtnCopy());
        }


        return pnlInfos;
    }

    private JXLabel getLblInfos() {
        if (lblInfos == null) {
            lblInfos = new JXLabel(" ");
        }

        return lblInfos;
    }

    private JButton getBtnCopy() {
        if (btnCopy == null) {
            CopyAllAction action = new CopyAllAction(getTxtPreview());
            btnCopy = new JButton(action);
            btnCopy.setToolTipText("Copy to clipboard");
            int scaledSize = UIScale.scale(32);
            btnCopy.setPreferredSize(new Dimension(scaledSize,scaledSize));
            btnCopy.setText("");
            btnCopy.setIcon(action.getSmallIcon());
        }

        return btnCopy;
    }

    private RTextScrollPane getScrollPreview() {
        if (scrollPreview == null) {
            scrollPreview = new RTextScrollPane(getTxtPreview());
            scrollPreview.setBorder(BorderFactory.createEmptyBorder());
        }

        return scrollPreview;
    }

    private RTextArea getTxtPreview() {
        if (txtPreview == null) {
            txtPreview = new RSyntaxTextArea();
            txtPreview.setEditable(false);
            txtPreview.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);

            // fix the scaled font size for HiDPI screens depending on the OS
            float fontSize = 11f; // windows
            if (SystemInfo.isLinux) {
                fontSize = SystemInfo.isKDE ? 13f : 15f;
            }
            if (SystemInfo.isMacOS) {
                fontSize = 13f;
            }
            float fontScaledSize = UIScale.scale(fontSize);
            Font scaledFont = txtPreview.getFont().deriveFont(fontScaledSize);
            txtPreview.setFont(scaledFont);
        }

        return txtPreview;
    }
}