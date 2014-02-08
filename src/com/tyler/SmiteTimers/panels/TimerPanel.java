package com.tyler.SmiteTimers.panels;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import com.tyler.SmiteTimers.core.Timer;

public class TimerPanel extends JPanel implements Timer.TimeUpdatedListener {

    JLabel timerText;
    JLabel title;

    public TimerPanel(Timer timer, String titleText, String imagePath) {

        timer.addTimeUpdatedListener(this);

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);

        // Create the Title layout
        this.title = new JLabel(titleText);
        this.title.setForeground(Color.BLACK);
        this.add(this.title);
        layout.putConstraint(SpringLayout.WEST, this.title, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, this.title, 5, SpringLayout.NORTH, this);

        // Create timer layout
        this.timerText = new JLabel("00:00");
        this.timerText.setForeground(Color.WHITE);
        this.add(this.timerText);
        layout.putConstraint(SpringLayout.NORTH, this.timerText, 5, SpringLayout.SOUTH, title); // Above timer
        layout.putConstraint(SpringLayout.WEST, this.timerText, 5, SpringLayout.WEST, this);        

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

        timeUpdated(timer.getTime());
    }

    @Override
    public void timeUpdated(long timeInMilli) {
        long seconds = timeInMilli / 1000 % 60;
        long minutes = timeInMilli /  1000 / 60;
        this.timerText.setText(String.format("%02d:%02d", minutes, seconds));
        if(minutes < 1 && seconds < 20) {
            this.timerText.setForeground(Color.RED);
        } else {
            this.timerText.setForeground(Color.BLACK);
        }
    }

    public int getFrameWidth() {
        return 100;
    }

    public int getFrameHeight() {
        return 50;
    }

}
