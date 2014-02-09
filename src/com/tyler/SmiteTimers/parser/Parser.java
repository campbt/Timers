package com.tyler.SmiteTimers.parser;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.tyler.SmiteTimers.core.Timer;
import com.tyler.SmiteTimers.panels.TimerPanel;
import com.tyler.SmiteTimers.windows.TimerWindow;

public class Parser {

    // Timer Window
    private static final String WINDOW_COLUMNS = "columns";
    private static final String WINDOW_PANEL_WIDTH = "panel_width";
    private static final String WINDOW_FORMAT = "time";
    private static final String WINDOW_FORMAT_DEFAULT = "seconds";
    private static final String WINDOW_TIMERS = "timers";
    
    // Timer Panels
    private static final String PANEL_TYPE = "type";
    private static final String PANEL_TYPE_DEFAULT = "timer";
    private static final String PANEL_TITLE = "title";
    private static final String PANEL_DURATION = "duration";
    private static final String PANEL_HOTKEY = "hotkey";
    private static final String PANEL_ICON = "icon";
    private static final String PANEL_ALERTS = "alerts";

    // Alerts
    private static final String ALERT_TYPE = "type";
    private static final String ALERT_COLOR_TYPE = "color";
    private static final String ALERT_COLOR_TIME = "time";
    private static final String ALERT_COLOR_COLOR = "color";

    // Keycode mapper
    private static Map<String, Integer> keyToKeyCodeMap;

    public static TimerWindow  parseJsonFile(String filename) {
        setUpKeyMapper();
        try {
            StringBuilder builder = new StringBuilder();
            Scanner cin = new Scanner(new File(filename));
            while(cin.hasNext()) {
                builder.append(cin.nextLine());
            }
            return parseJson(builder.toString());
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Given a JSON file, it reads it in and returns a TimerWindow complete with TimerPanels
     */
    public static TimerWindow parseJson(String JSON) {
        TimerWindow timerWindow = new TimerWindow();
        String alertText = null;

        System.out.println();
        try {
            JSONObject object = new JSONObject(JSON);
            System.out.println("Valid JSON");
            
            // Configure timerWindows
            int columns = Math.max(1,object.optInt(WINDOW_COLUMNS, 1));
            int panelWidth = object.optInt(WINDOW_PANEL_WIDTH, 0);
            String timeFormat = object.optString(WINDOW_FORMAT, WINDOW_FORMAT_DEFAULT);
            boolean useSeconds = false;
            if(timeFormat.equals("seconds")) {
                useSeconds = true;
            }

            // Set
            timerWindow.setNumCols(columns);
            timerWindow.setPanelWidth(panelWidth);

            JSONArray timers = object.getJSONArray(WINDOW_TIMERS);
            for(int i = 0; i < timers.length(); i++) {
                JSONObject panel = timers.getJSONObject(i);
                timerWindow.addTimerPanel(parsePanel(panel, useSeconds));
            }

            timerWindow.display();

        } catch (JSONException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            alertText = sw.toString();
        }

        if(alertText != null) {
            System.out.println("Setting Alert Text");
            displayAlert(alertText);
        }
        return timerWindow;
    }

    public static TimerPanel parsePanel(JSONObject json, boolean useSeconds) throws JSONException {
        // Assume type is timer for now, until other panels are made
        //String type = json.getString(PANEL_TYPE);
        //if(type == null) {
            //type = PANEL_TYPE_DEFAULT;
        //}
        return parseTimerPanel(json, useSeconds);
    }

    public static TimerPanel parseTimerPanel(JSONObject json, boolean useSeconds) {
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

        for(int i = 0; i < alerts.length(); i++) {
            JSONObject alert = alerts.getJSONObject(i);
            String type = alert.getString(ALERT_TYPE);
            if(ALERT_COLOR_TYPE.equals(type)) {
                // Color Type
                int time = alert.getInt(ALERT_COLOR_TIME);
                if(useSeconds) {
                    time *= 1000; // Convert to milliseconds
                }
                String colorString = alert.getString(ALERT_COLOR_COLOR);
                Color color;
                try {
                    Field field = Color.class.getField(colorString);
                    color = (Color)field.get(null);
                } catch (Exception e) {
                    color = Color.WHITE;
                }
                
                panel.addColorAlert(time, color);
            }
        }

        return panel;
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

    public static void displayAlert(String alert) {
        JOptionPane.showMessageDialog(null, alert);
    }
}
