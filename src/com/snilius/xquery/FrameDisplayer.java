//  Copyright 2003 Elliotte Rusty Harold
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
//  Boston, MA  02111-1307  USA
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

import java.awt.Frame;

/**
 * For an explanation of this class see John Zukowski's artile at
 * http://java.sun.com/developer/JDCTechTips/2003/tt1208.html
 */
class FrameDisplayer implements Runnable {
    
    private final Frame frame;
    
    public FrameDisplayer(Frame frame) {
        this.frame = frame;
    }
    
    public void run() {
        frame.show();
    }
    
}
