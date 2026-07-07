import ui.LoginWindow;
import util.AppTheme;

import javax.swing.*;

/**
 * Entry point for the Hospital Management System GUI application.
 * Demonstrates: Event-driven programming, GUI component handling, OOP principles.
 */
public class MainApp {
    public static void main(String[] args){
        // Apply Nimbus Look-and-Feel on the Event Dispatch Thread
        SwingUtilities.invokeLater(()->{
            try { AppTheme.apply(); } catch(Exception e){ /* fall back to default L&F */ }
            new LoginWindow();
        });
    }
}
