package com.sample;

import javax.swing.SwingUtilities;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class ExpertSystemRunner {
    public static final void main(String[] args) {
        try {
        	SwingUtilities.invokeLater(() -> {
                GuiInterface gui = new GuiInterface();
                gui.setVisible(true);
            });
        } catch (Throwable t) {
        	System.err.println("Err:" + t.getMessage());
            t.printStackTrace();
        }
    }
}