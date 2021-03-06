package common.src.UI;

import common.src.util.PropManager;
import common.src.util.SpaceManager;

import common.src.main.Data.PlayerState;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.*;
import java.io.IOException;
import java.util.Vector;

public class GameGUI extends JFrame implements ListSelectionListener {

    private static GameGUI _INSTANCE;

    public static GameGUI getInstance() {
        return  _INSTANCE;
    }

    private JPanel gamePanel;
    private JList<String> list1;
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
    private JTextArea chatArea;
    private JLabel totalwood;
    private JLabel totalstone;
    private JLabel totalmeat;
    private JLabel totalwheat;
    private JLabel totalhouses;
    private JLabel CurrentLvl;
    private JTextPane jobInfo;
    private JList<String> playersAtTask;

    private PlayerState player;
    private Space hostChat, GUIjob;

    public class setProgress implements Runnable {
        private final int wood;
        private final int stone;
        private final int animal;
        private final int wheat;
        private final int house;
        private final String task;

        public setProgress(int wood, int stone, int animal, int wheat, int house, String task) {
            this.wood = wood;
            this.stone = stone;
            this.animal = animal;
            this.wheat = wheat;
            this.house = house;
            this.task = task;
        }

        @Override
        public void run() {

        	JProgressBar pBar;
            pBar = progressBar1;
            switch (task){
                case "setHP":
                    pBar = progressBar2;
                case"setXP":
                    switch (list1.getSelectedValue().toString()) {
                        case "Woodcutting":
                            pBar.setValue(wood);
                            break;
                        case "Mining":
                            pBar.setValue(stone);
                            break;
                        case "Hunting":
                            pBar.setValue(animal);
                            break;
                        case "Farming":
                            pBar.setValue(wheat);
                            break;
                        case "Construction":
                            pBar.setValue(house);
                            break;
                    }
                    break;
                case "setRes":
                    totalwood.setText("Wood: " + wood);
                    totalstone.setText("Stone: " + stone);
                    totalmeat.setText("Meat: " + animal);
                    totalwheat.setText("Wheat: " + wheat);
                    totalhouses.setText("Houses: " + house);
                    break;
                case "setLvl":
                    switch (list1.getSelectedValue().toString()) {
                        case "Woodcutting":
                            CurrentLvl.setText("Current level: " + wood);
                            break;
                        case "Mining":
                            CurrentLvl.setText("Current level: " + stone);
                            break;
                        case "Hunting":
                            CurrentLvl.setText("Current level: " + animal);
                            break;
                        case "Farming":
                            CurrentLvl.setText("Current level: " + wheat);
                            break;
                        case "Construction":
                            CurrentLvl.setText("Current level: " + house);
                            break;
                    }
                break;
            }
        }
    }

    private Vector<String>[] workerList = new Vector[]{new Vector(), new Vector(), new Vector(), new Vector(), new Vector()};
    public class setWorkers implements Runnable {
        private List<String>[] workers;

        public setWorkers(List<String>[] workers) {
            this.workers = workers;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                workerList[i].clear();
                workerList[i].addAll(workers[i]);
            }
            switch (list1.getSelectedValue()) {
                case "Woodcutting":
                    playersAtTask.setListData(workerList[0]);
                    break;
                case "Mining":
                    playersAtTask.setListData(workerList[1]);
                    break;
                case "Hunting":
                    playersAtTask.setListData(workerList[2]);
                    break;
                case "Farming":
                    playersAtTask.setListData(workerList[3]);
                    break;
                case "Construction":
                    playersAtTask.setListData(workerList[4]);
                    break;
            }
        }
    }

    GameGUI(){
        GameGUI._INSTANCE = this;
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
            	    String host = PropManager.getProperty("host");
            	    if (host == null)
            	        host = "false";
            	    if (host.equals("true")) {
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
        setBounds(0, 0  , 1000, 800);
        setResizable(false);

        playersAtTask.setLayoutOrientation(JList.VERTICAL);
        playersAtTask.setSelectionModel(new NoSelectionModel());
        DefaultListModel<String> skillList = new DefaultListModel<>();
        skillList.addElement("Woodcutting");
        skillList.addElement("Mining");
        skillList.addElement("Hunting");
        skillList.addElement("Farming");
        skillList.addElement("Construction");

        list1.setModel(skillList);
        list1.setLayoutOrientation(JList.VERTICAL);
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.setSelectedIndex(0);
        CurrentSkill.setText(CurrentSkill.getText()+ " " + list1.getSelectedValue().toString());
        list1.addListSelectionListener(this);
        Border margin = new EmptyBorder(10,10,10,10);
        totalwood.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.BLACK,2),margin));
        totalstone.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.BLACK,2),margin));
        totalmeat.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.BLACK,2),margin));
        totalwheat.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.BLACK,2),margin));
        totalhouses.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.BLACK,2),margin));
        chatArea.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.BLACK,2),margin));

        sendMsg.addActionListener((ActionEvent e) -> {
        	if (hostChat == null) {
                try {
                    hostChat = SpaceManager.getHostSpace("chat");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            try {
                if (!chatMsg.getText().isBlank()) {
                    hostChat.put("msg", chatMsg.getText(), PropManager.getProperty("username"));
                    chatMsg.setText("");
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        startWorkButton.addActionListener((ActionEvent e)->{

            if(GUIjob == null){
                try {
                    GUIjob = SpaceManager.getLocalSpace("GUIjob");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            String job = list1.getSelectedValue().toString();
            try {
                GUIjob.put(job);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(list1.getSelectedValue() == "Woodcutting"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());
            jobInfo.setText("Collect wood in the forrest. Bringing a friend will make this task faster!");
            playersAtTask.setListData(workerList[0]);
        } else if (list1.getSelectedValue() == "Mining"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());
            jobInfo.setText("Collect stone in the mine. Bringing a friend will make this task faster, and bringing more is even better!");
            playersAtTask.setListData(workerList[1]);
        }else if (list1.getSelectedValue() == "Hunting"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());
            jobInfo.setText("Go hunting in the woods. You need a hunting partner to do this task!");
            playersAtTask.setListData(workerList[2]);
        }else if (list1.getSelectedValue() == "Farming"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());
            jobInfo.setText("Farming the lush fields. Farming is slow but gives a lot of food. This task is faster and gives more, the more players that are working on it!");
            playersAtTask.setListData(workerList[3]);
        }else if (list1.getSelectedValue() == "Construction"){
            CurrentSkill.setText("Current Skill: " + list1.getSelectedValue().toString());
            jobInfo.setText("You need wood, stone and some form of food to complete this task!");
            playersAtTask.setListData(workerList[4]);
        }
    }

    public class addChatMessage implements Runnable {
        String msg;
        public addChatMessage(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            chatArea.append(msg + "\n");
        }
    }

    /*This solution below for disabling a Jlist is not made by us but, but was taken from stackoverflow:
    https://stackoverflow.com/questions/31669350/disable-jlist-cell-selection-property (07-28-2015)
    This solution was created by user morpheus05 (07-28-2015)
     */
    private static class NoSelectionModel extends DefaultListSelectionModel {

        @Override
        public void setAnchorSelectionIndex(final int anchorIndex) {}

        @Override
        public void setLeadAnchorNotificationEnabled(final boolean flag) {}

        @Override
        public void setLeadSelectionIndex(final int leadIndex) {}

        @Override
        public void setSelectionInterval(final int index0, final int index1) {}
    }
}
