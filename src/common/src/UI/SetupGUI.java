package common.src.UI;

import common.src.main.App;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetupGUI extends JFrame implements ActionListener {

    private JPanel setupPanel;
    private JTextField LocalPort;
    private JTextField HostIP;
    private JTextField HostPort;
    private JButton connectButton;
    private JCheckBox hostCheckBox;

    public SetupGUI (){

        this.setContentPane(setupPanel);
        this.pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Launch settings");
        setBounds(300, 300, 300, 250);
        setResizable(false);

        connectButton.addActionListener(this);
        hostCheckBox.addActionListener(this);
    }

    public static void main(String[] args) {

        JFrame frame = new SetupGUI();
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
        if(e.getSource() == connectButton){

            //Start backend
            //new Thread(new App(hostCheckBox.isSelected(),HostIP.getText(),HostPort.getText(),LocalPort.getText())).start();

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
}
