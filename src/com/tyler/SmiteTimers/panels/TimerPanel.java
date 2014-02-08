package com.tyler.SmiteTimers.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
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

    private static final int ICON_SIZE = 30;

    JLabel timerText;
    JLabel title;

    public TimerPanel(Timer timer, String titleText, String imagePath) {
        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);

        // Create the Title layout
        this.title = new JLabel(titleText);
        this.title.setForeground(Color.BLACK);
        this.add(this.title);
        layout.putConstraint(SpringLayout.WEST, this.title, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, this.title, 5, SpringLayout.NORTH, this);

        if(timer != null) {
            // Create timer layout
            timer.addTimeUpdatedListener(this);
            this.timerText = new JLabel("00:00");
            this.timerText.setForeground(Color.WHITE);
            this.add(this.timerText);
            layout.putConstraint(SpringLayout.NORTH, this.timerText, 5, SpringLayout.SOUTH, title); // Above timer
            layout.putConstraint(SpringLayout.WEST, this.timerText, 5, SpringLayout.WEST, this);        

            timeUpdated(timer.getTime());
        }

        // Add in an image icon
        if(imagePath != null) {
            try {
                System.out.println("Trying to read: " + imagePath);
                if(getClass().getResource(imagePath) != null) {
                    BufferedImage icon = ImageIO.read(getClass().getResource(imagePath));
                    Image scaled = icon.getScaledInstance(ICON_SIZE, ICON_SIZE, java.awt.Image.SCALE_SMOOTH);
                    JLabel picLabel = new JLabel(new ImageIcon(scaled));
                    this.add(picLabel);
                    this.setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));
                    //layout.putConstraint(SpringLayout.WEST, picLabel, 5, SpringLayout.EAST, title); // Above timer
                    layout.putConstraint(SpringLayout.NORTH, picLabel, 5, SpringLayout.NORTH, this);        
                    layout.putConstraint(SpringLayout.EAST, picLabel, -5, SpringLayout.EAST, this);        
                } else {
                    System.out.println("WARNING: Image Not Found: " + imagePath);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
