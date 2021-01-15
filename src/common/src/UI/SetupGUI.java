package common.src.UI;

import common.src.main.App;

import javax.naming.spi.DirectoryManager;
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

    public SetupGUI (){

        this.setContentPane(setupPanel);
        this.pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Launch settings");
        setBounds(300, 300, 300, 250);
        setResizable(false);

        connectButton.addActionListener(this);
        hostCheckBox.addActionListener(this);

        LastOptions lastOptions = null;

        File directory = new File("/data");
        if(!directory.exists())
            directory.mkdir();

        try {
            FileInputStream fis = new FileInputStream("/data/LastOptions.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            lastOptions = (LastOptions) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException ignored) { }

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

    public static void main(String[] args) {

        JFrame frame = new SetupGUI();
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
        if(e.getSource() == connectButton){

            //Start backend - add username
            new Thread(new App(hostCheckBox.isSelected(),HostIP.getText().trim(),HostPort.getText().trim(),LocalPort.getText().trim(),username.getText().trim())).start();

            try {
                FileOutputStream fos = new FileOutputStream("/data/LastOptions.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                LastOptions thisOptions = new LastOptions();
                thisOptions.setHost(hostCheckBox.isSelected());
                thisOptions.setHostIp(HostIP.getText());
                thisOptions.setHostPort(HostPort.getText());
                thisOptions.setLocalPort(LocalPort.getText());
                thisOptions.setUsername(username.getText());
                oos.writeObject(thisOptions);
                oos.close();
                fos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

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
