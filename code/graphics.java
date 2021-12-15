package CN;

import javax.swing.*;
import java.awt.event.*;
public class graphics extends JFrame implements ActionListener{
    public static AbstractButton b;
    static boolean checkboxCompleted;
    JLabel l;
    static boolean isEchoSelected;
    static boolean isImageSelected;
    static boolean isErrImageSelected;
    static boolean isGpsSelected;
    static boolean isAckNackSelected;
    JCheckBox echo, image, errImage, gps, ackNack;
    graphics(){
        l = new JLabel("What actions do you want to do?");
        l.setBounds(50,50,300,20);
        echo = new JCheckBox("Echo Request");
        echo.setBounds(100,100,150,20);
        image = new JCheckBox("Image without error Request");
        image.setBounds(100,150,200,20);
        errImage = new JCheckBox("Image with error Request");
        errImage.setBounds(100,200,200,20);
        gps = new JCheckBox("GPS Pins Request");
        gps.setBounds(100,250,200,20);
        ackNack = new JCheckBox("ARQ Request");
        ackNack.setBounds(100,300,150,20);
        b = new JButton("Proceed");
        b.setBounds(100,450,150,30);
        b.addActionListener(this);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {dispose();}
        });
        add(l);add(echo);add(image);add(errImage);add(gps);add(ackNack);add(b);
        setSize(600,600);

        setLocationRelativeTo(null);
        setLayout(null);
        setVisible(true);
    }
    public void actionPerformed(ActionEvent e){
        if(echo.isSelected())
            isEchoSelected = true;
        if(image.isSelected())
            isImageSelected = true;
        if(errImage.isSelected())
            isErrImageSelected = true;
        if(gps.isSelected())
            isGpsSelected = true;
        if(ackNack.isSelected())
            isAckNackSelected = true;

        checkboxCompleted = true;
    }
}
