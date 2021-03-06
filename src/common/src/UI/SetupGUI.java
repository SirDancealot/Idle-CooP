package common.src.UI;

import common.src.main.App;
import common.src.util.FileManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class SetupGUI extends JFrame implements ActionListener {

    private JPanel setupPanel;
    private JTextField LocalPort;
    private JTextField HostIP;
    private JTextField HostPort;
    private JButton connectButton;
    private JCheckBox hostCheckBox;
    private JTextField username;

    public static void main(String[] args) {
        JFrame frame = new SetupGUI();
        frame.setVisible(true);
    }

    SetupGUI (){
        this.setContentPane(setupPanel);
        this.pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Launch settings");
        setBounds(300, 300, 300, 250);
        setResizable(false);

        connectButton.addActionListener(this);
        hostCheckBox.addActionListener(this);

        File directory = new File("./data");
        if(!directory.exists())
            directory.mkdir();

        LastOptions lastOptions = FileManager.loadObject("./data/LastOptions.ser");

        if (lastOptions != null) {
            hostCheckBox.setSelected(lastOptions.isHost());
            if (hostCheckBox.isSelected()) {
                connectButton.setText("Host");
                HostIP.setEditable(false);
            }
            HostIP.setText(lastOptions.getHostIp());
            HostPort.setText(lastOptions.getHostPort());
            LocalPort.setText(lastOptions.getLocalPort());
            username.setText(lastOptions.getUsername());
        }
    }

    public void actionPerformed(ActionEvent e){
        if(e.getSource() == connectButton){

            new Thread(new App(hostCheckBox.isSelected(),HostIP.getText().trim(),HostPort.getText().trim(),LocalPort.getText().trim(),username.getText().trim())).start();

            LastOptions thisOptions = new LastOptions();
            thisOptions.setHost(hostCheckBox.isSelected());
            thisOptions.setHostIp(HostIP.getText());
            thisOptions.setHostPort(HostPort.getText());
            thisOptions.setLocalPort(LocalPort.getText());
            thisOptions.setUsername(username.getText());
            FileManager.saveObject("./data/LastOptions.ser", thisOptions);

            //Launch gameGUI
            JFrame gameGUI = new GameGUI();
            gameGUI.setVisible(true);

            dispose();

        } else if (e.getSource() == hostCheckBox){
            if(hostCheckBox.isSelected()){
                connectButton.setText("Host");
                HostIP.setEditable(false);
            } else {
                connectButton.setText("Connect");
                HostIP.setEditable(true);
            }
        }
    }

    private static class LastOptions implements Serializable {
        private boolean isHost;
        private String hostIp, hostPort, localPort, username;

        public boolean isHost() {
            return isHost;
        }

        public void setHost(boolean host) {
            isHost = host;
        }

        public String getHostIp() {
            return hostIp;
        }

        public void setHostIp(String hostIp) {
            this.hostIp = hostIp;
        }

        public String getHostPort() {
            return hostPort;
        }

        public void setHostPort(String hostPort) {
            this.hostPort = hostPort;
        }

        public String getLocalPort() {
            return localPort;
        }

        public void setLocalPort(String localPort) {
            this.localPort = localPort;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
