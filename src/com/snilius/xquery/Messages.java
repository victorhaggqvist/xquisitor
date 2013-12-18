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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Elliotte Rusty Harold
 * @version 1.0a5
 */
class Messages {

    private static final String BUNDLE_NAME = "com.snilius.xquery.QueryFrame";

    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {}
    
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
