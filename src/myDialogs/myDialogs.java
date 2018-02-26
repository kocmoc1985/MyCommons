/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myDialogs;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 *
 * @author KOCMOC
 */
public class myDialogs {

    public static void main(String[] args) {
        TextFieldCheck_Sql field_A = new TextFieldCheck_Sql(null,null,"\\d{5}",15);
        boolean yesNo = chooseFromJTextFieldWithCheck(field_A, "Please type the code");
        
        String rst = field_A.getText();
        
        if(yesNo == false || rst == null){
            return;
        }
        
    }

    public static boolean chooseFromJTextFieldWithCheck(TextFieldCheck_Sql tf, String msg) {
        requestFocus(tf);
        return JOptionPane.showConfirmDialog(null, tf, msg, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static void requestFocus(final JComponent component) {
        Thread x = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(myDialogs.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //
                component.requestFocus();
            }
        };

        x.start();
    }
}
