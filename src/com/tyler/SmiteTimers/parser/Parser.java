package com.tyler.SmiteTimers.parser;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sun.audio.AudioDataStream;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

import com.tyler.SmiteTimers.core.Timer;
import com.tyler.SmiteTimers.network.Network;
import com.tyler.SmiteTimers.panels.TimerPanel;
import com.tyler.SmiteTimers.windows.TimerWindow;

public class Parser {

    // Timer Window
    private static final String WINDOW_COLUMNS = "columns";
    private static final String WINDOW_PANEL_WIDTH = "panel_width";
    private static final String WINDOW_FORMAT = "time";
    private static final String WINDOW_FORMAT_DEFAULT = "seconds";
    private static final String WINDOW_TIMERS = "timers";
    private static final String WINDOW_HIDEMODE_HOTKEY = "hidemode_hotkey";
    private static final String WINDOW_CLOSE_HOTKEY = "close_hotkey";
    private static final String NETWORK_MODE = "network_mode";
    private static final String NETWORK_MODE_HOST = "host";
    private static final String NETWORK_MODE_CLIENT = "client";
    private static final String NETWORK_IP = "network_ip";
    private static final String NETWORK_PORT = "network_port";
    
    // Timer Panels
    private static final String PANEL_TYPE = "type";
    private static final String PANEL_TIMER_ID = "id";
    private static final String PANEL_TYPE_DEFAULT = "timer";
    private static final String PANEL_TITLE = "title";
    private static final String PANEL_DURATION = "duration";
    private static final String PANEL_HOTKEY = "hotkey";
    private static final String PANEL_ICON = "icon";
    private static final String PANEL_ALERTS = "alerts";

    // Alerts
    private static final String ALERT_TYPE = "type";
    private static final String ALERT_TIME= "time";
    private static final String ALERT_COLOR_TYPE = "color";
    private static final String ALERT_COLOR_COLOR = "color";
    private static final String ALERT_SOUND_TYPE = "sound";
    private static final String ALERT_SOUND_SRC = "src";

    // Keycode mapper
    private static Map<String, Integer> keyToKeyCodeMap;

    // IDs stuff
    private static HashSet<Integer> ids = new HashSet<Integer>();

    public static TimerWindow parseJsonFile(String filename) {
        ids.clear();
        setUpKeyMapper();
        try {
            StringBuilder builder = new StringBuilder();
            Scanner cin = new Scanner(new File(filename));
            while(cin.hasNext()) {
                builder.append(cin.nextLine());
            }
            cin.close();
            return parseJson(builder.toString());
        } catch(Exception e) {
            e.printStackTrace();
            displayAlert(e);
            return null;
        }
    }

    /**
     * Given a JSON file, it reads it in and returns a TimerWindow complete with TimerPanels
     */
    public static TimerWindow parseJson(String JSON) throws JSONException {
        TimerWindow timerWindow = new TimerWindow();

        JSONObject object = new JSONObject(JSON);
        
        // Configure timerWindows
        int columns = Math.max(1,object.optInt(WINDOW_COLUMNS, 1));
        int panelWidth = object.optInt(WINDOW_PANEL_WIDTH, 0);
        String timeFormat = object.optString(WINDOW_FORMAT, WINDOW_FORMAT_DEFAULT);
        boolean useSeconds = false;
        if(timeFormat.equals("seconds")) {
            useSeconds = true;
        }

        // Close hotkey stuff
        if(object.has(WINDOW_CLOSE_HOTKEY)) {
            String closeHotkey = object.getString(WINDOW_CLOSE_HOTKEY);
            timerWindow.setCloseHotkey(convertStringToNativeKey(closeHotkey));
        }

        // Hidemode hotkey stuff
        if(object.has(WINDOW_HIDEMODE_HOTKEY)) {
            String hideModeHotKey = object.getString(WINDOW_HIDEMODE_HOTKEY);
            timerWindow.setHideModeToggleKey(convertStringToNativeKey(hideModeHotKey));
        }

        // Set
        timerWindow.setNumCols(columns);
        timerWindow.setPanelWidth(panelWidth);

        JSONArray timersJSON = object.getJSONArray(WINDOW_TIMERS);
        String networkMode = object.getString(NETWORK_MODE);
        boolean networkModeEnabled = networkMode.equals(NETWORK_MODE_HOST) || networkMode.equals(NETWORK_MODE_CLIENT);
        for(int i = 0; i < timersJSON.length(); i++) {
            JSONObject panel = timersJSON.getJSONObject(i);
            timerWindow.addTimerPanel(parsePanel(panel, useSeconds, networkModeEnabled));
        }

        // Network Stuff
        if(networkModeEnabled) {
            // Get network parameters
            int port = object.getInt(NETWORK_PORT);
            Collection<Timer> timers = timerWindow.getTimers();
            Network network;
            if(networkMode.equals(NETWORK_MODE_HOST)) {
            	network = new Network(port,timers);
            } else { //if(networkMode.equals(NETWORK_MODE_CLIENT)) {
                String ip = object.getString(NETWORK_IP);
                network = new Network(ip,port,timers);
            }
            for(Timer timerInstance : timers){
            	timerInstance.addStateChangedListener(network);
            }
        }

        timerWindow.display();

        return timerWindow;
    }

    public static TimerPanel parsePanel(JSONObject json, boolean useSeconds, boolean forceId) throws JSONException {
        // Assume type is timer for now, until other panels are made
        //String type = json.getString(PANEL_TYPE);
        //if(type == null) {
            //type = PANEL_TYPE_DEFAULT;
        //}
        return parseTimerPanel(json, useSeconds, forceId);
    }

    public static TimerPanel parseTimerPanel(JSONObject json, boolean useSeconds, boolean forceId) {
        int duration = json.getInt(PANEL_DURATION);
        if(useSeconds) {
            duration *= 1000; // Convert to milliseconds
        }
        String title = json.getString(PANEL_TITLE);
        String hotkey = json.getString(PANEL_HOTKEY);
        String icon = json.optString(PANEL_ICON, null);
        JSONArray alerts = json.getJSONArray(PANEL_ALERTS);

        Timer timer = new Timer(duration);
        TimerPanel panel = new TimerPanel(timer, title, icon);
        panel.setHotkey(convertStringToNativeKey(hotkey));

        if(forceId) {
            // Requires this timer to have a unique id
            int id = json.getInt(PANEL_TIMER_ID);
            if(ids.contains(id)) {
                throw new JSONException("Duplicate Timer Id: " + id);
            } else {
                ids.add(id);
                timer.setId(id);
            }
        }

        for(int i = 0; i < alerts.length(); i++) {
            JSONObject alert = alerts.getJSONObject(i);
            String type = alert.getString(ALERT_TYPE);
            int time = alert.getInt(ALERT_TIME);
            if(useSeconds) {
                time *= 1000; // Convert to milliseconds
            }
            if(ALERT_COLOR_TYPE.equals(type)) {
                // Color Type
                String colorString = alert.getString(ALERT_COLOR_COLOR);
                panel.addColorAlert(time, parseColor(colorString));
            } else if(ALERT_SOUND_TYPE.equals(type)) {
                // Sound Type
                String name = alert.getString(ALERT_SOUND_SRC);
                String filename = "/com/tyler/SmiteTimers/sounds/" + name + ".wav";
                InputStream in;
                try {
                    in = panel.getClass().getResourceAsStream(filename);
                } catch (Exception e) {
                    try {
                        filename = System.getProperty("user.dir") + File.separator + name;
                        System.out.println(filename);
                        in = new FileInputStream(new File(filename));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        throw new JSONException("Couldn't find file for sound: " + name);
                    }
                }
                try {
                    final AudioStream as = new AudioStream(in);
                    final AudioDataStream cas = new AudioDataStream(as.getData());
                    as.close();
                    panel.addSoundAlert(time, cas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw new JSONException("Unknown Alert Type: " + type);
            }
        }

        return panel;
    }

    public static Color parseColor(String colorString) {
        Color color;
        try {
            // First see if it is a recognized java color
            Field field = Color.class.getField(colorString);
            color = (Color)field.get(null);
        } catch (Exception e) {
            // Now try to convert its HEX value
            try {
                if(colorString.charAt(0) == '#') {
                    // They're trying to format it in hex
                    return new Color(
                        Integer.valueOf( colorString.substring( 1, 3 ), 16 ),
                        Integer.valueOf( colorString.substring( 3, 5 ), 16 ),
                        Integer.valueOf( colorString.substring( 5, 7 ), 16 ) );
                } else {
                    color = Color.WHITE;
                }
            } catch(Exception e2) {
                // Well, that failed too, so just set it to white
                System.out.println("Failed to get color: " + colorString);
                color = Color.WHITE;
            }
        }
        return color;
    }

    /**
     * Converts a native key event into the form that is stored by the parser
     */
    public static int convertNativeKey(NativeKeyEvent e) {
        return (e.getModifiers() << 16) | e.getKeyCode();
    }

    /**
     *  Converts a string from the JSON to the form that can be read using native key
     *  (convertNativeKey returns the same form as this)
     */
    public static int convertStringToNativeKey(String s) {
        if(Parser.keyToKeyCodeMap == null) {
            setUpKeyMapper();
        }
        try {
            int modifiers = 0;
            String keycodeString = s;
            if(s.contains("-")) {
                // Contains modifies (in form <Modifies>-<Keycode> ex: SA-NUMPAD3
                String[] split = s.split("-");
                String modifiersString = split[0];
                keycodeString = split[1];
                if(modifiersString.contains("S")) {
                    modifiers += NativeKeyEvent.SHIFT_MASK;
                }
                if(modifiersString.contains("C")) {
                    modifiers += NativeKeyEvent.CTRL_MASK;
                }
                if(modifiersString.contains("M")) {
                    modifiers += NativeKeyEvent.META_MASK;
                }
                if(modifiersString.contains("A")) {
                    modifiers += NativeKeyEvent.ALT_MASK;
                }
            }

            int keycode = Parser.keyToKeyCodeMap.get(keycodeString);

            return (modifiers << 16) | keycode;

        } catch(Exception e) {
            System.out.println("Couldn't convert hotkey string: " + s);
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 
     */
    public static void setUpKeyMapper() {
        final String PREFIX = "public static final int org.jnativehook.keyboard.NativeKeyEvent.VK_";
        keyToKeyCodeMap = new HashMap<String, Integer>();
        try {
            Class clazz = Class.forName("org.jnativehook.keyboard.NativeKeyEvent");
            Field[] fields = clazz.getDeclaredFields();
            for(Field f: fields) {
              // for fields that are visible (e.g. private)
              f.setAccessible(true);

              try {
                  if(f.toString().contains(PREFIX)) {
                    // This is a field we should store
                    // note: get(null) for static field
                    keyToKeyCodeMap.put(f.toString().substring(PREFIX.length()), (Integer)f.get(null));
                    //System.out.println("Storing: "  + (f.toString().substring(PREFIX.length()) + " -> "  + f.get(null)));
                  }
              } catch( Exception e) {
                  System.out.println("Couldn't get field: " + f);
              }
            }
        } catch (Exception e) {
            System.out.println("Couldn't set up key mapper");
            e.printStackTrace();
        }

    }

    public static void displayAlert(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        displayAlert(sw.toString());
    }

    public static void displayAlert(String alert) {
        JOptionPane.showMessageDialog(null, alert);
    }
}
