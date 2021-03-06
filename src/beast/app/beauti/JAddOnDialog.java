package beast.app.beauti;

import beast.util.AddOnManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * dialog for managing Add-ons.
 * List, install and uninstall add-ons
 * *
 */
public class JAddOnDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    JPanel panel;
    DefaultListModel model = new DefaultListModel();
    JList list;

    public JAddOnDialog(JFrame frame) {
        super(frame);
        frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        setModal(true);

        panel = new JPanel();
        getContentPane().add(BorderLayout.CENTER, panel);
        setTitle("BEAST 2 Add-On Manager");


        Component pluginListBox = createList();
        panel.add(pluginListBox);
        Box buttonBox = createButtonBox();
        getContentPane().add(buttonBox, BorderLayout.SOUTH);

        Dimension dim = panel.getPreferredSize();
        Dimension dim2 = buttonBox.getPreferredSize();
        setSize(dim.width + 10, dim.height + dim2.height + 30);
        Point frameLocation = frame.getLocation();
        Dimension frameSize = frame.getSize();
        setLocation(frameLocation.x + frameSize.width / 2 - dim.width / 2, frameLocation.y + frameSize.height / 2 - dim.height / 2);
        frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    class AddOn {
        String sAddOnURL;
        String sAddOnDescription;
        boolean bIsInstalled;

        AddOn(List<String> list) {
            sAddOnDescription = list.get(0);
            sAddOnDescription = AddOnManager.formatAddOnInfo(list);
            sAddOnURL = list.get(1);
            bIsInstalled = false;
            List<String> sBeastDirs = AddOnManager.getBeastDirectories();
            String sAddOnName = AddOnManager.URL2AddOnName(sAddOnURL);
            for (String sDir : sBeastDirs) {
                File f = new File(sDir + "/" + sAddOnName);
                if (f.exists()) {
                    bIsInstalled = true;
                }
            }
        }

        public String toString() {
            return sAddOnDescription;
        }
    }

    private Component createList() {
        Box box = Box.createVerticalBox();
        box.add(new JLabel("List of available Add-ons"));
        list = new JList(model);
        list.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resetList();

        JScrollPane pane = new JScrollPane(list);
        box.add(pane);
        return box;
    }

    private void resetList() {
        model.clear();
        try {
            List<List<String>> addOns = AddOnManager.getAddOns();
            for (List<String> addOn : addOns) {
                AddOn addOnObject = new AddOn(addOn);
                model.addElement(addOnObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        list.setSelectedIndex(0);
    }

    private Box createButtonBox() {
        Box box = Box.createHorizontalBox();
        box.add(Box.createGlue());
        JButton installButton = new JButton("Install/Upgrade");
        installButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] addOns = list.getSelectedValues();
                for (Object addOnObject : addOns) {
                    if (addOnObject != null) {
                        AddOn addOn = (AddOn) addOnObject;
                        try {
                            if (addOn.bIsInstalled) {
                                //TODO upgrade version
                            } else {
                                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                AddOnManager.installAddOn(addOn.sAddOnURL, false, null);
                                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            }
                            resetList();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Install failed because: " + ex.getMessage());
                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }
            }
        });
        box.add(installButton);

        JButton uninstallButton = new JButton("Uninstall");
        uninstallButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] addOns = list.getSelectedValues();

                boolean toDeleteFileExists = false;
                for (Object addOnObject : addOns) {
                    if (addOnObject != null) {
                        AddOn addOn = (AddOn) addOnObject;
                        try {
                            if (addOn.bIsInstalled) {
//                            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to uninstall " + AddOnManager.URL2AddOnName(addOn.sAddOnURL) + "?", "Uninstall Add On", JOptionPane.YES_NO_OPTION) ==
//                                    JOptionPane.YES_OPTION) {
                                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                AddOnManager.uninstallAddOn(addOn.sAddOnURL, false, null);
                                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                                File toDeleteFile = AddOnManager.getToDeleteListFile();
                                if (toDeleteFile.exists()) {
                                    toDeleteFileExists = true;
                                }
//                            }
                            } else {
                                //TODO ?
                            }
                            resetList();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Uninstall failed because: " + ex.getMessage());
                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }

                if (toDeleteFileExists) {
                    JOptionPane.showMessageDialog(null, "<html>To complete uninstalling the addon, BEAUti need to be restarted<br><br>Exiting now.</html>");
                    System.exit(0);
                }

            }
        });
        box.add(uninstallButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        box.add(Box.createGlue());
        box.add(closeButton);
        box.add(Box.createGlue());

        JButton button = new JButton("?");
        button.setToolTipText(AddOnManager.getAddOnUserDir() + " " + AddOnManager.getAddOnAppDir());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(panel, "<html>Add-on are installed in <br><br><em>" + AddOnManager.getAddOnUserDir() +
                        "</em><br><br> by you, and are available to you,<br>the user, only.<br>" +
                        "System wide add-ons are installed in <br><br><em>" + AddOnManager.getAddOnAppDir() +
                        "</em><br><br>and are available to all users." +
                        "<br>(just move the add-on there manually" +
                        "<br>to make it system wide available).</html>");
            }
        });
        box.add(button);
        return box;
    }


}
