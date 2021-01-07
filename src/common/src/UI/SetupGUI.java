package common.src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetupGUI extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JTextField LocalPort;
    private JTextField HostIP;
    private JTextField HostPort;
    private JButton connectButton;
    private JCheckBox hostCheckBox;

    public SetupGUI (){

        this.setContentPane(mainPanel);
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
            //TODO Start backend with info
            
            //TODO Launch GUI with info from backend

        } else if (e.getSource() == hostCheckBox){
            if(hostCheckBox.isSelected())
                connectButton.setText("Host");
            else
                connectButton.setText("Connect");
        }
    }
}
