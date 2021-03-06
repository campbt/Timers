package com.tyler.SmiteTimers.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import sun.audio.AudioDataStream;
import sun.audio.AudioPlayer;
import sun.audio.ContinuousAudioDataStream;

import com.tyler.SmiteTimers.core.Alert;
import com.tyler.SmiteTimers.core.Alert.AlertTriggeredListener;
import com.tyler.SmiteTimers.core.Timer;

public class TimerPanel extends JPanel implements Timer.TimeUpdatedListener {

    // Constants
    private static final int PADDING = 10;

    // Icon
    private static final int ICON_SIZE = 30;

    // Title
    private static final String TITLE_FONT_NAME = Font.SERIF;
    private static final int TITLE_FONT_STYLE = Font.PLAIN;
    private static final int TITLE_FONT_SIZE = 14;
    private static final Color TITLE_COLOR = Color.WHITE;

    // Timer
    private static final String TIMER_FONT_NAME = Font.SANS_SERIF;
    private static final int TIMER_FONT_STYLE = Font.BOLD;
    private static final int TIMER_FONT_SIZE = 18;
    private static final Color TIMER_COLOR = Color.WHITE;
    private static final int TIMER_PADDING_LEFT = 8;
    private static final int TIMER_PADDING_TOP = 4;

    Timer timer;
    JLabel timerText;
    JLabel title;
    private int hotkey;

    public TimerPanel(Timer timer, String titleText, String imagePath) {
        this.timer = timer;

        // Set parameters
        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);
        this.setBackground(new Color(0,0,0,0f));
        this.setOpaque(false);

        // Add in an image icon
        boolean hasImage = false;
        JLabel picLabel = null;
        if(imagePath != null) {
            try {
                picLabel = new JLabel(new ImageIcon(convertIconString(imagePath)));
                this.add(picLabel);
                this.setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));
                //layout.putConstraint(SpringLayout.WEST, picLabel, PADDING, SpringLayout.EAST, title); // Above timer
                layout.putConstraint(SpringLayout.NORTH, picLabel, PADDING, SpringLayout.NORTH, this);        
                layout.putConstraint(SpringLayout.WEST, picLabel, PADDING, SpringLayout.WEST, this);        
                hasImage = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create the Title layout
        this.title = new JLabel(titleText);
        this.title.setForeground(TITLE_COLOR);
        this.title.setFont(new Font(TITLE_FONT_NAME, TITLE_FONT_STYLE, TITLE_FONT_SIZE));
        this.add(this.title);
        if(hasImage) {
            layout.putConstraint(SpringLayout.WEST, this.title, PADDING, SpringLayout.EAST, picLabel);
        } else {
            layout.putConstraint(SpringLayout.WEST, this.title, PADDING, SpringLayout.WEST, this);
        }
        layout.putConstraint(SpringLayout.NORTH, this.title, PADDING, SpringLayout.NORTH, this);

        if(this.timer != null) {
            // Create timer layout
            this.timer.addTimeUpdatedListener(this);
            this.timerText = new JLabel("00:00");
            this.timerText.setForeground(TIMER_COLOR);
            this.timerText.setFont(new Font(TIMER_FONT_NAME, TIMER_FONT_STYLE, TIMER_FONT_SIZE));
            this.add(this.timerText);
            layout.putConstraint(SpringLayout.NORTH, this.timerText, TIMER_PADDING_TOP, SpringLayout.SOUTH, title); // Above timer
            layout.putConstraint(SpringLayout.SOUTH, this.timerText, -1 * TIMER_PADDING_TOP, SpringLayout.SOUTH, this); // Above timer
            if(hasImage) {
                layout.putConstraint(SpringLayout.WEST, this.timerText, TIMER_PADDING_LEFT, SpringLayout.EAST, picLabel);        
            } else {
                layout.putConstraint(SpringLayout.WEST, this.timerText, TIMER_PADDING_LEFT, SpringLayout.WEST, this);        
            }

            timeUpdated(timer.getTime());
        }

    }

    public void addColorAlert(long timeToTrigger, final Color color) {
        AlertTriggeredListener listener = new AlertTriggeredListener() {
            @Override
            public void alertTriggered(Alert alert) {
                TimerPanel.this.timerText.setForeground(color);
            }
        };
        this.timer.addAlert(new Alert(timeToTrigger, listener));
    }

    public void addSoundAlert(long timeToTrigger, final AudioDataStream cas) {
        AlertTriggeredListener listener = new AlertTriggeredListener() {
            @Override
            public void alertTriggered(Alert alert) {
                AudioPlayer.player.start(cas);
            }
        };
        this.timer.addAlert(new Alert(timeToTrigger, listener));
    }

    @Override
    public void timeUpdated(long timeInMilli) {
        long seconds = timeInMilli / 1000 % 60;
        long minutes = timeInMilli /  1000 / 60;
        this.timerText.setText(String.format("%2d:%02d", minutes, seconds));
    }

    public Image convertIconString(String icon) {
        String imagePath = null;
        if(icon == null) {
            return null;
        } else if(icon.equals("goldfury")) {
            imagePath = "/com/tyler/SmiteTimers/images/icon_goldfury.png";
        } else if(icon.equals("firegiant")) {
            imagePath = "/com/tyler/SmiteTimers/images/icon_firegiant.png";
        } else if(icon.equals("furies")) {
            imagePath = "/com/tyler/SmiteTimers/images/icon_furies.png";
        } else if(icon.equals("buff")) {
            imagePath = "/com/tyler/SmiteTimers/images/icon_buff.png";
        } else if(icon.equals("buff_red")) {
            imagePath = "/com/tyler/SmiteTimers/images/icon_buff_red.png";
        } else if(icon.equals("buff_blue")) {
            imagePath = "/com/tyler/SmiteTimers/images/icon_buff_blue.png";
        } else if(icon.equals("buff_orange")) {
            imagePath = "/com/tyler/SmiteTimers/images/icon_buff_orange.png";
        }
        if(imagePath != null) {
            try {
                BufferedImage buffered = ImageIO.read(getClass().getResource(imagePath));
                return buffered.getScaledInstance(TimerPanel.ICON_SIZE, TimerPanel.ICON_SIZE, java.awt.Image.SCALE_SMOOTH);
            } catch (Exception e) {
                System.out.println("Couldn't load resource: " + imagePath);
                e.printStackTrace();
            }
        } else {
            // String not known, try to look it up
            // TODO: Look up resource on their comp
            System.out.println("Unknown Image: " + icon);
        }
        return null;
            
    }

    public Timer getTimer() {
        return this.timer;
    }

    public int getFrameWidth() {
        return 120;
    }

    public int getFrameHeight() {
        return 60;
    }

    public void setHotkey(int hotkey) {
        this.hotkey = hotkey;
    }

    public int getHotkey() {
        return this.hotkey;
    }

}
