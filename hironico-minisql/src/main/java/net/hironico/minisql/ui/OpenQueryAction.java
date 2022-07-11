package net.hironico.minisql.ui;

import java.awt.event.ActionEvent;
import java.awt.Component;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;

import java.io.BufferedReader;
import java.io.FileReader;

public class OpenQueryAction extends AbstractRibbonAction {

    private static final long serialVersionUID = 1L;
    
    public OpenQueryAction()  {
        super("Open...", "icons8_opened_folder_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        QueryPanel queryPanel = null;
        if ((comp == null) || !(comp instanceof QueryPanel)) {
            queryPanel = new QueryPanel();
            MainWindow.getInstance().displayCloseableComponent(queryPanel, "New Query");
        } else {
            queryPanel = (QueryPanel)comp;
        }

        JFileChooser chooser = new JFileChooser(queryPanel.getLastUserDirectory());
        int resp = chooser.showOpenDialog(queryPanel);
        if (resp == JFileChooser.APPROVE_OPTION) {
            queryPanel.setLastUsedDirectory(chooser.getSelectedFile().getAbsolutePath());
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(chooser.getSelectedFile()));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();                            
                while(line != null) {
                    sb.append(line + "\n");
                    line = br.readLine(); 
                }
                queryPanel.setQueryText(sb.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(queryPanel, "Error while reading the file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (br != null) {
                    try { br.close(); } catch (Throwable ignored) { }
                }
            }
        }
    }    
}