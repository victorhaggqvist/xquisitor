//  Copyright 2003-2005 Elliotte Rusty Harold
//
//  This file is part of XQuisitor.
//
//  XQuisitor is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published 
//  by the Free Software Foundation; either version 2 of the License, 
//  or (at your option) any later version.
//
//  XQuisitor is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with XQuisitor; if not, write to the 

//  Free Software Foundation, Inc. 
//  59 Temple Place, Suite 330
//  Boston, MA  02111-1307  
//  USA
//
// In addition, as a special exception, Elliotte Rusty Harold gives
// permission to link the code of this program with the Saxon-B library (or
// with modified versions of Saxon-B that use the same license as Saxon-B), 
// and distribute linked combinations including the two. You must obey the 
// GNU General Public License in all respects for all of the code used other
// than Saxon-B. If you modify this file, you may extend this exception to 
// your version of the file, but you are not obligated to do so. If you do 
// not wish to do so, delete this exception statement from your version.

package com.snilius.xquery;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

/**
 * @author Elliotte Rusty Harold
 * @version 1.0a5
 */
class AboutDialog extends JDialog {

    AboutDialog(JFrame parent) {
        super(parent, Messages.getString("About_XQuisitor_49"));
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(BorderLayout.CENTER, makeMainPane());
        this.getContentPane().add(BorderLayout.WEST, new JPanel());
        this.getContentPane().add(BorderLayout.EAST, new JPanel());

        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
    }
    
    private static Font font = new Font("Dialog", Font.PLAIN, 12);
    
    
    private JPanel makeMainPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(
          BorderLayout.NORTH, 
          makeTopPane()
        );
        JTextComponent information = new JTextArea();
        information.setText(Messages.getString("gpl"));
        information.setEditable(false);
        information.setBackground(this.getBackground());
        panel.add(BorderLayout.CENTER, information);        
        JPanel okPanel = new JPanel();
        okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton(Messages.getString("OK"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                hide();
                dispose();
            }
        });
        okPanel.add(ok);
        getRootPane().setDefaultButton(ok);
        panel.add(BorderLayout.SOUTH, okPanel);
        
        return panel;
    }
    
    private JPanel makeTopPane() {
        JPanel panel = new JPanel();
        LayoutManager layout = new GridLayout(8, 1);
        panel.setLayout(layout);
        panel.add(new JLabel());
        JLabel title = new JLabel("XQuisitor");
        panel.add(title);
        
        JLabel copyright = new JLabel("Copyright 2003-2005 Elliotte Rusty Harold");
        copyright.setFont(font);
        panel.add(copyright);
        
        JLabel version = new JLabel("Version: 1.0a5");
        version.setFont(font);
        panel.add(version);
        
        panel.add(new JLabel());
        
        JLabel credits = new JLabel("Contains the Saxon-B XSLT Processor from Michael Kay");
        credits.setFont(font);
        panel.add(credits);
        
        JLabel credits2 = new JLabel("http://saxon.sourceforge.net/");
        credits2.setFont(font);
        panel.add(credits2);
        
        return panel;  
    }

}
