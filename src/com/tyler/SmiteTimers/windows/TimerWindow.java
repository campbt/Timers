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
import com.tyler.SmiteTimers.parser.Parser;

public class TimerWindow extends JFrame implements NativeKeyListener, WindowListener  {

    private static final float TRANSPARENCY = 0.5f;

    private List<Timer> timers = new LinkedList<Timer>();
    private List<TimerPanel> panels = new LinkedList<TimerPanel>();
    private Map<Integer, Timer> keysMapper = new HashMap<Integer, Timer>(); // uses Parser.convertToKeyCode for the key, maps to the timer for that hotkey

    private int numCols;
    private int panelWidth = 0;

    // Mouse coordinates, used for dragging
    private int posX;
    private int posY;

    public TimerWindow() {
        // Initial Parameters
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(this);

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

    }

    public void display() {
        int numRows = this.panels.size() / numCols + this.panels.size() % this.numCols;
        this.getContentPane().setLayout(new GridLayout(numRows,numCols));
        int height = 0;
        int width = this.panelWidth;
        for(TimerPanel panel: this.panels) {
            this.add(panel);
            height = Math.max(height, panel.getFrameHeight());
            if(this.panelWidth == 0) {
                width = Math.max(width, panel.getFrameWidth());
            }
        }
        width = width * numCols + 5;
        height = height * numRows + 5;

        this.setBounds(0,0,width,height);
        this.setShape(new RoundRectangle2D.Double(0, 0, width, height, 50, 50));
        this.setPreferredSize(new Dimension(width,height));
        this.setMinimumSize(new Dimension(width,height));
        this.setMaximumSize(new Dimension(width,height));
        this.pack();
        this.setVisible(true);
    }

    public void addTimerPanel(TimerPanel panel) {
        this.panels.add(panel);
        this.keysMapper.put(panel.getHotkey(), panel.getTimer());
    }

    public void windowOpened(WindowEvent e) {
            //Initialize native hook.
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
        //System.out.println("You pressed modifies: " + e.getModifiers());
        //System.out.println("You pressed key: " + e.getKeyCode());
        int convertedKey = Parser.convertNativeKey(e);
        if(keysMapper.containsKey(convertedKey)) {
            keysMapper.get(convertedKey).toggle();
        }
    }

    public void nativeKeyPressed(NativeKeyEvent e) { /* Unimplemented */ }
    public void nativeKeyTyped(NativeKeyEvent e) { /* Unimplemented */ }

    public void setPanelWidth(int panelWidth) {
        this.panelWidth =  panelWidth;
    }
    public void setNumCols(int numCols) {
        this.numCols =  numCols;
    }
}
