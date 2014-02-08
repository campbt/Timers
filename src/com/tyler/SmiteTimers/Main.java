package com.tyler.SmiteTimers;

import java.net.URL;

import com.tyler.SmiteTimers.parser.Parser;
import com.tyler.SmiteTimers.windows.TimerWindow;

public class Main {

    // Constants
    private static final String MANFIEST_FILE = "../Manifest.json";

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //new TimerWindow();
                URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
                String filePath = location.getFile().toString() + MANFIEST_FILE;
                System.out.println(filePath);
                Parser.parseJsonFile(filePath);
            }
        });
	}

}
