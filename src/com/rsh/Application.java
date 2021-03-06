package com.rsh;

import com.alee.extended.tree.WebAsyncTree;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebFrame;
import com.alee.laf.scroll.WebScrollPane;
import com.rsh.miu.ClassIdentity;
import com.rsh.ui.ClassIdentityEditor;
import com.rsh.ui.tree.ASyncNodeProvider;
import com.rsh.ui.tree.PositionNode;
import com.rsh.ui.tree.PositionNodeRenderer;
import com.rsh.util.Store;
import org.objectweb.asm.tree.ClassNode;
import pw.tdekk.deob.usage.UnusedMembers;
import pw.tdekk.util.Archive;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

/**
 * Created by TimD on 1/20/2017.
 */
public class Application {

    private static JFrame frame;
    private static WebPanel panel = new WebPanel();
    private static final String NAME = "revrs";

    public static void main(String[] args) {
        WebLookAndFeel.install();
        WebLookAndFeel.initializeManagers();
        WebLookAndFeel.setDecorateAllWindows(true);

        Store.createFiles();

        WebFrame frame = (WebFrame) getFrame();
        frame.setIconImages(Arrays.asList(getIcon("burn.png").getImage(), getIcon("burn-1.png").getImage(), getIcon("burn-2.png").getImage(), getIcon("burn-3.png").getImage()));
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addMenuItems(frame);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
        if (Store.getCrawler().outdated()) {
            int result = WebOptionPane.showConfirmDialog(frame, "Would you like to update", "Outdated!", WebOptionPane.YES_NO_OPTION);
            if (result == WebOptionPane.YES_OPTION) {
                Store.updateJar();
            }
        } else {
            Store.loadClasses(Store.getHighestRevision().getPath());
        }
    }

    private static void addMenuItems(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        menuBar.add(file);

        JMenuItem view = new JMenu("View");
        menuBar.add(view);

        JMenuItem load = new JMenuItem("Load");
        load.addActionListener(e -> {
            WebFileChooser fd = new WebFileChooser(Store.getHomeDirectory());
            fd.setMultiSelectionEnabled(false);
            fd.setAcceptAllFileFilterUsed(false);
            fd.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".jar");
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });
            if (fd.showOpenDialog(frame) == WebFileChooser.APPROVE_OPTION) {
                String filename = fd.getSelectedFile().getAbsolutePath();
                Store.loadClasses(filename);
            }
        });
        file.add(load);

        JMenuItem update = new JMenuItem("Update");
        update.addActionListener(e -> Store.updateJar());
        file.add(update);

        JMenuItem deob = new JMenuItem("Deob");
        deob.addActionListener(e -> {
            if (Store.getClasses().size() < 1) {
                infoBox("You need to load classes first", "Deobbing Issue");
            } else {
                new UnusedMembers().mutate();
                //allow for custom deobbing here
                Archive.write(new File(Store.getDeobDirectory() + File.separator + Store.getLastVersion() + "_deob.jar"), Store.getClasses());
            }
        });
        file.add(deob);

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(1));
        file.add(exit);

        JMenuItem hierarchy = new JMenuItem("Class Hierarchy");
        hierarchy.addActionListener(e -> {
            // Create data provider
            ASyncNodeProvider dataProvider = new ASyncNodeProvider();

            // Create a tree based on your data provider
            WebAsyncTree<PositionNode> asyncTree = new WebAsyncTree<>(dataProvider);
            asyncTree.setVisibleRowCount(8);
            asyncTree.setEditable(true);
            asyncTree.setCellRenderer(new PositionNodeRenderer());
            WebScrollPane webScrollPane = new WebScrollPane(asyncTree);
            webScrollPane.getVerticalScrollBar().setUnitIncrement(35);
            // Show an example frame
            frame.getContentPane().removeAll();
            frame.getContentPane().add(webScrollPane);
            frame.setVisible(true);
        });
        view.add(hierarchy);

        JMenuItem classIdEditor = new JMenuItem("Class Identity Editor");
        classIdEditor.addActionListener(e -> {
            panel.removeAll();
            panel.add(new ClassIdentityEditor(),BorderLayout.CENTER);
            frame.setVisible(true);
        });
        view.add(classIdEditor);

        frame.setLayout(new BorderLayout());
        frame.setJMenuBar(menuBar);
    }

    public static void infoBox(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(getFrame(), infoMessage, "Message: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }

    public static WebPanel getPanel() {
        return panel;
    }

    public static JFrame getFrame() {
        return frame != null ? frame : (frame = new WebFrame(NAME));
    }

    public static ImageIcon getIcon(String name) {
        return new ImageIcon(Application.class.getResource("ui/icon/" + name));
    }
}
