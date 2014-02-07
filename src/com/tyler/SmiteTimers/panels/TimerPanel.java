package com.tyler.SmiteTimers.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class TimerPanel extends JPanel {

    public TimerPanel() {

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);

        JLabel label = new JLabel("Label: ");
        this.add(label);
        JTextField textField = new JTextField("Text field", 15);
        this.add(textField);

layout.putConstraint(SpringLayout.WEST, label,
                     5,
                     SpringLayout.WEST, this);
layout.putConstraint(SpringLayout.NORTH, label,
                     5,
                     SpringLayout.NORTH, this);
layout.putConstraint(SpringLayout.WEST, textField,
                     5,
                     SpringLayout.EAST, label);
layout.putConstraint(SpringLayout.NORTH, textField,
                     5,
                     SpringLayout.NORTH, this);        
    }
}
