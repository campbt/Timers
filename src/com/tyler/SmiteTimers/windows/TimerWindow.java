package com.tyler.SmiteTimers.windows;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import com.tyler.SmiteTimers.panels.TimerPanel;

public class TimerWindow extends JFrame implements NativeKeyListener, WindowListener  {

    private List<TimerPanel> timerPanels = new LinkedList<TimerPanel>();

    public TimerWindow() {
        // Test Data
        this.timerPanels.add(new TimerPanel(100000,"Gold Fury",null));
        this.timerPanels.add(new TimerPanel(100000,"Fire Giant",null));
        this.timerPanels.add(new TimerPanel(100000,"Blue Buff",null));

        // Initial Parameters
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Smite Timers");
        this.addWindowListener(this);
        this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        int height = 0;
        int width = 0;
        for(TimerPanel panel: this.timerPanels) {
            this.add(panel);
            height += panel.getFrameHeight();
            width = Math.max(width, panel.getFrameWidth());
        }

        this.setBounds(10,10,300,300);
        this.setPreferredSize(new Dimension(width,height));
        this.setMinimumSize(new Dimension(width,height));
        this.setMaximumSize(new Dimension(width,height));
        this.pack();
        this.setVisible(true);
    }

    public void windowOpened(WindowEvent e) {
            //Initialze native hook.
            try {
                    GlobalScreen.registerNativeHook();
            }
            catch (NativeHookException ex) {
                    System.err.println("There was a problem registering the native hook.");
                    System.err.println(ex.getMessage());
                    ex.printStackTrace();

                    System.exit(1);
            }

            GlobalScreen.getInstance().addNativeKeyListener(this);
    }

    public void windowClosed(WindowEvent e) {
        //Clean up the native hook.
        GlobalScreen.unregisterNativeHook();
        System.runFinalization();
        System.exit(0);
    }

    public void windowClosing(WindowEvent e) { /* Unimplemented */ }
    public void windowIconified(WindowEvent e) { /* Unimplemented */ }
    public void windowDeiconified(WindowEvent e) { /* Unimplemented */ }
    public void windowActivated(WindowEvent e) { /* Unimplemented */ }
    public void windowDeactivated(WindowEvent e) { /* Unimplemented */ }

    public void nativeKeyReleased(NativeKeyEvent e) {
        System.out.println("You pressed: " + e);
        if (e.getKeyCode() == NativeKeyEvent.VK_SPACE) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(null, "This will run on Swing's Event Dispatch Thread.");
                }
            });
        }
    }

    public void nativeKeyPressed(NativeKeyEvent e) { /* Unimplemented */ }
    public void nativeKeyTyped(NativeKeyEvent e) { /* Unimplemented */ }

}
