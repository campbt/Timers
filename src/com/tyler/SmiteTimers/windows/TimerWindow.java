package com.tyler.SmiteTimers.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import com.tyler.SmiteTimers.core.Timer;
import com.tyler.SmiteTimers.panels.TimerPanel;

public class TimerWindow extends JFrame implements NativeKeyListener, WindowListener  {

    private static final float TRANSPARENCY = 0.5f;

    private List<Timer> timers = new LinkedList<Timer>();
    private Map<Integer, Timer> keysMapper = new HashMap<Integer, Timer>();

    // Mouse coordinates, used for dragging
    private int posX;
    private int posY;

    public TimerWindow() {
        List<TimerPanel> timerPanels = loadData();

        // Initial Parameters
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(this);
        this.getContentPane().setLayout(new GridLayout(4,2));

        // For Transparency
        this.setUndecorated(true);
        this.setBackground(new Color(0,0,0,TRANSPARENCY));

        // Always on top
        this.setAlwaysOnTop( true );
        this.setLocationByPlatform( true );

        // Set up dragging
        this.addMouseListener(new MouseAdapter()
        {
           public void mousePressed(MouseEvent e)
           {
              TimerWindow.this.posX=e.getX();
              TimerWindow.this.posY=e.getY();
           }
        });
        this.addMouseMotionListener(new MouseAdapter()
        {
             public void mouseDragged(MouseEvent evt)
             {
                //sets frame position when mouse dragged			
                setLocation (evt.getXOnScreen()-posX,evt.getYOnScreen()-posY);
             }
        });

        int height = 0;
        int width = 0;
        for(TimerPanel panel: timerPanels) {
            this.add(panel);
            height += panel.getFrameHeight();
            width = Math.max(width, panel.getFrameWidth());
        }
        width = width * 2 + 5;
        height = height / 2;

        this.setBounds(0,0,width,height);
        this.setShape(new RoundRectangle2D.Double(0, 0, width, height, 50, 50));
        this.setPreferredSize(new Dimension(width,height));
        this.setMinimumSize(new Dimension(width,height));
        this.setMaximumSize(new Dimension(width,height));
        this.pack();
        this.setVisible(true);
    }

    public List<TimerPanel> loadData() {
        // Test Data
        this.timers.add(new Timer(100000));
        this.timers.add(new Timer(110000));
        this.timers.add(new Timer(120000));

        this.keysMapper.put(NativeKeyEvent.VK_I, this.timers.get(0));
        this.keysMapper.put(NativeKeyEvent.VK_O, this.timers.get(1));
        this.keysMapper.put(NativeKeyEvent.VK_P, this.timers.get(2));

        List<TimerPanel> timerPanels = new LinkedList<TimerPanel>();
        timerPanels.add(constructTimerPanel(300000, "Gold Fury", "/com/tyler/SmiteTimers/images/icon_goldfury.png", NativeKeyEvent.VK_7));
        timerPanels.add(constructTimerPanel(300000, "Fire Giant", "/com/tyler/SmiteTimers/images/icon_firegiant.png", NativeKeyEvent.VK_8));
        timerPanels.add(constructTimerPanel(120000, "Left Furies", "/com/tyler/SmiteTimers/images/icon_furies.png", NativeKeyEvent.VK_U));
        timerPanels.add(constructTimerPanel(120000, "Right Furies", "/com/tyler/SmiteTimers/images/icon_furies.png", NativeKeyEvent.VK_I));
        timerPanels.add(constructTimerPanel(240000, "Left Blue", "/com/tyler/SmiteTimers/images/icon_buff_blue.png", NativeKeyEvent.VK_J));
        timerPanels.add(constructTimerPanel(240000, "Right Blue", "/com/tyler/SmiteTimers/images/icon_buff.png", NativeKeyEvent.VK_K));
        timerPanels.add(constructTimerPanel(240000, "Speed Buff", "/com/tyler/SmiteTimers/images/icon_buff_orange.png", NativeKeyEvent.VK_M));
        timerPanels.add(constructTimerPanel(240000, "Damage Buff", "/com/tyler/SmiteTimers/images/icon_buff_red.png", NativeKeyEvent.VK_COMMA));

        return timerPanels;
    }

    private TimerPanel constructTimerPanel(long time, String title, String imagePath, int key) {
        if(time > 0) {
            Timer timer = new Timer(time);
            this.timers.add(timer);
            this.keysMapper.put(key, timer);
            TimerPanel panel = new TimerPanel(timer, title, imagePath);
            panel.addColorAlert(time, Color.BLUE);
            panel.addColorAlert(time - 1000, Color.BLACK);
            return panel;
        } else {
            return new TimerPanel(null, "Test", null);
        }
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
    }

    public void nativeKeyPressed(NativeKeyEvent e) { /* Unimplemented */ }
    public void nativeKeyTyped(NativeKeyEvent e) { /* Unimplemented */ }

}
