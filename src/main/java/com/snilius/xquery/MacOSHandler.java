//  Copyright 2004 Elliotte Rusty Harold
//  Copyright 2013-2014 Victor Häggqvist
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
//
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

import java.awt.Dialog;
import java.awt.EventQueue;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.Application;

import javax.swing.*;

/**
 * @author Elliotte Rusty Harold
 * @version 1.0a5
 *
 * @author Victor Häggqvist
 * @since 2013-12-18
 * @version 2.0
 */
public class MacOSHandler extends Application {

    private QueryFrame frame;
    private JFrame about;
    
    // can I add a hiddenFrame of some kind to keep the 
    // menu bar onscreen????
    
    public MacOSHandler(QueryFrame frame) {
        this.frame = frame;
        about = new AboutDialog();
        addApplicationListener(new AboutBoxHandler());
    }

    class AboutBoxHandler extends ApplicationAdapter {
        // what else can I handle here????
        
        public void handleAbout(ApplicationEvent event) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    about.show();
                }
            });
            event.setHandled(true);
        }
        
        public void handleQuit(ApplicationEvent event) {
            frame.quit();
        }
        
    }
    
}