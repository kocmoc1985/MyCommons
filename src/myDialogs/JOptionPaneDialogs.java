/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myDialogs;

import javax.swing.JOptionPane;

/**
 *
 * @author KOCMOC
 */
public class JOptionPaneDialogs {
    
    public static void main(String[] args) {
        showErrorMessage("AAA");
    }
    
    public static void showErrorMessage(String msg){
        JOptionPane.showMessageDialog(null, msg,"Error",JOptionPane.ERROR_MESSAGE);
    }
}
