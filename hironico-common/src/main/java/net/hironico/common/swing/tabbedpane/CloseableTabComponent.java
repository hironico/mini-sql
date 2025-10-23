package net.hironico.common.swing.tabbedpane;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.BorderFactory;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CloseableTabComponent extends JPanel {
    private final JTabbedPane tab;

    private JLabel lbl;

    private JPopupMenu menu;
    private JMenuItem itemClose;
    private JMenuItem itemCloseAll;
    private JMenuItem itemCloseAllButThis;
    private JMenuItem itemCloseLeft;
    private JMenuItem itemCloseRight;

    private boolean hideWhenLastClosed = false;

    public CloseableTabComponent(JTabbedPane tab, String title) {
        this.tab = tab;

        initialize(title);
    }

    private void initialize(String title) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setOpaque(false);

        lbl = new JLabel(title);
        lbl.setBorder(BorderFactory.createEmptyBorder());
        add(lbl);

        JButton btn = new JButton("x");
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 3, 2, 0));
        btn.setContentAreaFilled(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                CloseableTabComponent.this.closeThisTab();
            }
        });
        add(btn);

        tab.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                showMenu(evt);
            }

            public void mouseReleased(MouseEvent evt) {
                showMenu(evt);
            }

            protected void showMenu(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    getMenu().show(tab, evt.getX(), evt.getY());
                }
            }
        });
    }

    protected int getTabIndex() {
        for(int index = 0; index < tab.getTabCount(); index++) {
            Component comp = tab.getTabComponentAt(index);
            if (comp instanceof CloseableTabComponent) {
                CloseableTabComponent ctab = (CloseableTabComponent)comp;
                if (ctab.equals(this)) {
                    return index;
                }
            }
        }

        return -1;
    }

    protected JPopupMenu getMenu() {
        if (menu == null) {
            menu = new JPopupMenu();

            menu.add(getItemClose());
            menu.add(getItemCloseAll());
            menu.add(getItemCloseAllButThis());
            menu.add(getItemCloseLeft());
            menu.add(getItemCloseRight());
        }

        return menu;
    }

    protected JMenuItem getItemClose() {
        if (itemClose == null) {
            itemClose = new JMenuItem();
            itemClose.setText("Close");

            itemClose.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    CloseableTabComponent.this.closeThisTab();
                }
            });
        }

        return itemClose;
    }

    protected JMenuItem getItemCloseAll() {
        if (itemCloseAll == null) {
            itemCloseAll = new JMenuItem();
            itemCloseAll.setText("Close all");

            itemCloseAll.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    CloseableTabComponent.this.closeAllTabs();
                }
            });
        }

        return itemCloseAll;
    }

    protected JMenuItem getItemCloseAllButThis() {
        if (itemCloseAllButThis == null) {
            itemCloseAllButThis = new JMenuItem();
            itemCloseAllButThis.setText("Close all but this");

            itemCloseAllButThis.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    CloseableTabComponent.this.closeAllButThis();
                }
            });
        }

        return itemCloseAllButThis;
    }

    protected JMenuItem getItemCloseLeft() {
        if (itemCloseLeft == null) {
            itemCloseLeft = new JMenuItem();
            itemCloseLeft.setText("Close left");

            itemCloseLeft.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    CloseableTabComponent.this.closeLeft();
                }
            });
        }

        return itemCloseLeft;
    }

    protected JMenuItem getItemCloseRight() {
        if (itemCloseRight == null) {
            itemCloseRight = new JMenuItem();
            itemCloseRight.setText("Close right");

            itemCloseRight.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    CloseableTabComponent.this.closeRight();
                }
            });
        }

        return itemCloseRight;
    }

    protected void closeRight() {
        while (tab.getTabCount() - 1 > tab.getSelectedIndex()) {
            tab.removeTabAt(tab.getTabCount() - 1);
        }
        checkLastTab();
    }

    protected void closeLeft() {
        while (tab.getSelectedIndex() > 0) {
            tab.removeTabAt(0);
        }
        checkLastTab();
    }

    protected void closeAllButThis() {
        closeLeft();

        closeRight();
    }

    protected void closeThisTab() {
        int thisIndex = this.getTabIndex();
        if (thisIndex < 0) {
            return;
        }

        tab.removeTabAt(thisIndex);
        checkLastTab();
    }

    protected void closeAllTabs() {
        while (this.tab.getTabCount() > 0) {
            tab.removeTabAt(0);
        }
        checkLastTab();
    }

    protected void checkLastTab() {
        if ((this.tab.getTabCount() == 0) && hideWhenLastClosed) {
            this.tab.setVisible(false);
        }
    }

    public void setTitle(String title) {
        this.lbl.setText(title);
    }

    public String getTitle() {
        return this.lbl.getText();
    }

    public void setHideWhenLastClosed(boolean hideWhenLastClosed) {
        this.hideWhenLastClosed = hideWhenLastClosed;
    }

    public boolean isHideWhenLastClosed() {
        return this.hideWhenLastClosed;
    }

}