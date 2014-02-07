package com.tyler.SmiteTimers.panels;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class TimerPanel extends JPanel {

    JLabel timer;
    JLabel title;

    public TimerPanel(long time, String titleText, String imagePath) {

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);

        // Create the Title layout
        this.title = new JLabel(titleText);
        this.add(this.title);
        layout.putConstraint(SpringLayout.WEST, this.title, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, this.title, 5, SpringLayout.NORTH, this);

        // Create timer layout
        this.timer = new JLabel("00:00");
        this.add(this.timer);
        layout.putConstraint(SpringLayout.NORTH, this.timer, 5, SpringLayout.SOUTH, title); // Above timer
        layout.putConstraint(SpringLayout.WEST, this.timer, 5, SpringLayout.WEST, this);        

        // Add in an image icon
        if(imagePath != null) {
            try {
                BufferedImage myPicture = ImageIO.read(new File("path-to-file"));
                JLabel picLabel = new JLabel(new ImageIcon(myPicture));
                this.add(picLabel);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
