package common.src.UI;

import common.src.util.PropManager;
import common.src.util.SpaceManager;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import common.src.main.Data.PlayerState;

import common.src.util.PropManager;
import common.src.util.SpaceManager;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.io.IOException;

public class GameGUI extends JFrame implements ActionListener, ListSelectionListener {

    private JPanel gamePanel;
    private JList list1;
    private JLabel CurrentSkill;
    private JProgressBar progressBar1;
    private JProgressBar progressBar2;
    private JLabel skillMenu;
    private JLabel requirements;
    private JLabel coop;
    private JButton startWorkButton;
    private JScrollPane chat;
    private JTextField chatMsg;
    private JButton sendMsg;
    private JLabel chatWindow;
    private DefaultListModel skillList;

    private PlayerState player;

    GameGUI(){

        this.setContentPane(gamePanel);
        this.pack();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        WindowListener exitListen = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	int confirm = JOptionPane.showOptionDialog(
            	        null, "Want to exit?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null
                );
            	if (confirm == 0) {
            	    if (PropManager.getProperty("host") != null) {
                        SpaceManager.exitHost();
                    }
                    SpaceManager.exitClient();

                    dispose();
            	    System.exit(0);
                }
            }
        };
        addWindowListener(exitListen);


        setTitle("Idle game");
        setBounds(0, 0  , 800, 800);
        setResizable(false);

        list1.setLayoutOrientation(JList.VERTICAL);
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.setSelectedIndex(0);
        list1.addListSelectionListener(this);

        skillList = new DefaultListModel();
        skillList.addElement("Woodcutting");
        skillList.addElement("Mining");
        skillList.addElement("Hunting");
        skillList.addElement("Farming");
        skillList.addElement("Construction");

        list1.setModel(skillList);

        getPlayer();

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == sendMsg){

            //TODO send msg
        }
    }

    private void getPlayer(){

        //TODO get player info

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {

        if(list1.getSelectedValue() == "Woodcutting"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());
            requirements.setText("Requirements: ");

        } else if (list1.getSelectedValue() == "Mining"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());

        }else if (list1.getSelectedValue() == "Hunting"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());

        }else if (list1.getSelectedValue() == "Farming"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());

        }else if (list1.getSelectedValue() == "Construction"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());

        }
    }
}
