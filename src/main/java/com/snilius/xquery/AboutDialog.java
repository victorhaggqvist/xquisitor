/**
 * Copyright 2003-2005 Elliotte Rusty Harold
 * Copyright 2013-2014 Victor Häggqvist
 *
 *  This file is part of XQuisitor.
 *
 *  XQuisitor is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published 
 *  by the Free Software Foundation; either version 3 of the License,
 *  or (at your option) any later version.
 *
 *  XQuisitor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XQuisitor; if not, write to the 

 *  Free Software Foundation, Inc. 
 *  59 Temple Place, Suite 330
 *  Boston, MA  02111-1307  
 *  USA
 *
 * In addition, as a special exception, Elliotte Rusty Harold gives
 * permission to link the code of this program with the Saxon-B library (or
 * with modified versions of Saxon-B that use the same license as Saxon-B), 
 * and distribute linked combinations including the two. You must obey the 
 * GNU General Public License in all respects for all of the code used other
 * than Saxon-B. If you modify this file, you may extend this exception to 
 * your version of the file, but you are not obligated to do so. If you do 
 * not wish to do so, delete this exception statement from your version.
 */

package com.snilius.xquery;

import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author Elliotte Rusty Harold
 * @version 1.0a5
 *
 * @author Victor Häggqvist
 * @since 2013-12-18
 * @version 2.0
 */
class AboutDialog extends JFrame {

    public AboutDialog() {
        super(Messages.getString("About_XQuisitor_49"));

        setSize(626, 464);
        getContentPane().add(contentPanel());

        setLocationRelativeTo(null);
    }

    private JPanel contentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel copyrightAndCredits = new JLabel(Messages.getString("copyright_credits"));
        panel.add(copyrightAndCredits,BorderLayout.NORTH);

        JTextArea information = new JTextArea();
        information.setText(Messages.getString("gpl"));
        information.setEditable(false);
        panel.add(information,BorderLayout.CENTER);

        JButton ok = new JButton(Messages.getString("OK"));
        ok.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeBox();
            }
        });
        panel.add(ok,BorderLayout.SOUTH);

        return panel;
    }

    private void closeBox() {
        setVisible(false);
        dispose();
    }
}
