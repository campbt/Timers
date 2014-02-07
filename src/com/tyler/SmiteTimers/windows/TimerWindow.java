package com.tyler.SmiteTimers.windows;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import com.tyler.SmiteTimers.core.Timer;
import com.tyler.SmiteTimers.panels.TimerPanel;

public class TimerWindow extends JFrame implements NativeKeyListener, WindowListener  {

    private List<Timer> timers = new LinkedList<Timer>();
    private Map<Integer, Timer> keysMapper = new HashMap<Integer, Timer>();

    public TimerWindow() {
        // Test Data
        this.timers.add(new Timer(100000));
        this.timers.add(new Timer(110000));
        this.timers.add(new Timer(120000));

        this.keysMapper.put(NativeKeyEvent.VK_I, this.timers.get(0));
        this.keysMapper.put(NativeKeyEvent.VK_O, this.timers.get(1));
        this.keysMapper.put(NativeKeyEvent.VK_P, this.timers.get(2));

        List<TimerPanel> timerPanels = new LinkedList<TimerPanel>();
        timerPanels.add(new TimerPanel(this.timers.get(0),"Gold Fury",null));
        timerPanels.add(new TimerPanel(this.timers.get(1),"Fire Giant",null));
        timerPanels.add(new TimerPanel(this.timers.get(2),"Blue Buff",null));

        // Initial Parameters
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Smite Timers");
        this.addWindowListener(this);
        this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        int height = 0;
        int width = 0;
        for(TimerPanel panel: timerPanels) {
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
        if(keysMapper.containsKey(e.getKeyCode())) {
            keysMapper.get(e.getKeyCode()).toggle();
        }
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
