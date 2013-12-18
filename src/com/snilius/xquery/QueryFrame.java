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

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.xml.sax.InputSource;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.*;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * <p>
 *   This is the primary class that implements most of the logic in 
 *   XQuisitor. It represents an XQuisitor window and its menu bar.
 * </p>
 * 
 * @author Elliotte Rusty Harold
 * @version 1.0a5
 *
 */
final class QueryFrame extends JFrame {

    // cut and paste menu items should be disabled when queryPane gets the focus????
    // add to CVS????
    // Write a French translation????
    // add and check accessibility????
    // is the GPL binding to Saxon a license problem?


    //constants
    private final int bottomOffset = 64;
    private final String PRF_CONTEXT ="context";
    private final String PRF_BASEURI ="baseUri";

    Preferences prefs = Preferences.userNodeForPackage(com.snilius.xquery.QueryFrame.class);

    private static int  preferredMetaKey;
    private static List openWindows = new LinkedList();
    
    //private JTextArea queryPane = new JTextArea();

    private RSyntaxTextArea queryArea = new RSyntaxTextArea();
    private RSyntaxTextArea outputArea = new RSyntaxTextArea();

    //private JEditorPane outputPane = new JEditorPane();
    private JTextField contextField = new JTextField(24);
    private JTextField baseField = new JTextField(24);
    private JCheckBox doWrapping = new JCheckBox(Messages.getString("wrap")); 
    private JCheckBox doIndenting = new JCheckBox(Messages.getString("prettyPrint")); 
    private static Font display = new Font("Monospaced", Font.BOLD, 16); 
    private static Insets margin = new Insets(4, 3, 2, 4);
    private UndoManager manager = new UndoManager();
    private JCheckBoxMenuItem wrapItem 
      = new JCheckBoxMenuItem(Messages.getString("wrap")); 
    private JCheckBoxMenuItem indentItem 
      = new JCheckBoxMenuItem(Messages.getString("prettyPrint")); 
        
    private File queryFile = null;
    private File contextFile;
    private SequenceIterator queryResult;
    private StaticQueryContext queryContext;
    
    private final static Properties prettyPrint = new Properties();
    private final static Properties uglyPrint = new Properties();
            final static boolean thisIsAMac = System.getProperty("mrj.version") != null;
    
    static {
        prettyPrint.setProperty(OutputKeys.INDENT, "yes"); 
        preferredMetaKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }
    
    public QueryFrame() {
        
        super(Messages.getString("XQuisitor_5")); 
        if (thisIsAMac) new MacOSHandler(this);
        this.setJMenuBar(makeMenuBar());
        
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                  makeQueryPanel(),
                  makeOutputPanel());
        this.getContentPane().add(splitter);

        loadPreferences();
       
        Toolkit toolkit = this.getToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        this.setSize(screenSize.width, screenSize.height-bottomOffset);
        this.setLocation(0, 0);        
        
        initQueryProcessor();
        
    }

    private void loadPreferences() {
        System.out.println("load");
        contextField.setText(prefs.get(PRF_CONTEXT,""));
        System.out.println(prefs.get(PRF_CONTEXT,""));
        baseField.setText(prefs.get(PRF_BASEURI,getUserDir()));
        System.out.println("load");
    }


    private JPanel makeQueryPanel() {
        
        JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new BorderLayout());
        queryArea.setTabSize(2);
        queryArea.setCodeFoldingEnabled(true);
        queryArea.getDocument().addUndoableEditListener(manager);
        RTextScrollPane rTextScrollPane = new RTextScrollPane(queryArea);

        //queryPane.setMargin(margin);
        //queryPane.setFont(display);
        //queryPane.getDocument().addUndoableEditListener(manager);
        //JScrollPane queryScroll = new JScrollPane(queryPane);
        queryPanel.add(new JLabel(Messages.getString("Query_6")), BorderLayout.NORTH); 
        queryPanel.add(rTextScrollPane, BorderLayout.CENTER);
        queryPanel.add(makeOptionsPanel(), BorderLayout.EAST);
        return queryPanel;
        
    }

    
    private JPanel makeOutputPanel() {
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        outputPanel.add(new JLabel(Messages.getString("Query_Result_7")), BorderLayout.NORTH);

        outputArea.setCodeFoldingEnabled(true);
        outputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        outputArea.setTabSize(2);

        //hack for no line cursor
        outputArea.setCurrentLineHighlightColor(Color.WHITE);
        outputArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                outputArea.setCurrentLineHighlightColor(Color.WHITE);
            }
        });

        RTextScrollPane rTextScrollPane = new RTextScrollPane(outputArea);

        outputPanel.add(rTextScrollPane, BorderLayout.CENTER);
        return outputPanel;
    }

    private JPanel makeOptionsPanel() {
        
        JPanel options = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        options.setLayout(gbl);
        
        // It looks like each text field needs its own
        // UndoManager which is active whenever that field has the focus.
        // Otherwise, the undo crosses field boundaries????
        // baseField.getDocument().addUndoableEditListener(manager);

        JLabel baseLabel = new JLabel(
          Messages.getString("Base_URI___9"), JLabel.LEFT); 
        baseLabel.setDisplayedMnemonic(KeyEvent.VK_B);
        baseLabel.setLabelFor(baseField);
        
        JLabel chooserLabel = new JLabel(
          Messages.getString("Context___10"), JLabel.LEFT); 
        chooserLabel.setDisplayedMnemonic(KeyEvent.VK_C);
        chooserLabel.setLabelFor(contextField);
        
        // baseLabel
        GridBagConstraints baseLabelConstraints = new GridBagConstraints();
        baseLabelConstraints.gridx=0;
        baseLabelConstraints.gridy=0;
        baseLabelConstraints.gridwidth=3;
        baseLabelConstraints.gridheight=1;
        baseLabelConstraints.anchor = GridBagConstraints.NORTHWEST;
        baseLabelConstraints.insets = new Insets(0, 0, 0, 12);
        gbl.setConstraints(baseLabel, baseLabelConstraints);
        options.add(baseLabel);
        
        // baseField
        GridBagConstraints baseFieldConstraints = new GridBagConstraints();
        baseFieldConstraints.anchor = GridBagConstraints.NORTHWEST;
        baseFieldConstraints.gridx=3;
        baseFieldConstraints.gridy=0;
        baseFieldConstraints.gridwidth=4;
        baseFieldConstraints.gridheight=1;
        baseFieldConstraints.insets = new Insets(0, 0, 0, 6);
        gbl.setConstraints(baseField, baseFieldConstraints);
        options.add(baseField);
        
        // choose base
        JButton chooseBase = new JButton(Messages.getString("..._11")); 
        GridBagConstraints chooseBaseConstraints = new GridBagConstraints();
        chooseBaseConstraints.anchor = GridBagConstraints.NORTHWEST;
        chooseBaseConstraints.gridx=7;
        chooseBaseConstraints.gridy=0;
        chooseBaseConstraints.gridwidth=1;
        chooseBaseConstraints.gridheight=1;
        chooseBaseConstraints.insets = new Insets(0, 2, 2, 0);
        gbl.setConstraints(chooseBase, chooseBaseConstraints);
        options.add(chooseBase);
        chooseBase.addActionListener(new BaseURIChooser());
        
        // chooserLabel
        GridBagConstraints chooserLabelConstraints = new GridBagConstraints();
        chooserLabelConstraints.gridx=0;
        chooserLabelConstraints.gridy=1;
        chooserLabelConstraints.gridwidth=3;
        chooserLabelConstraints.gridheight=1;
        chooserLabelConstraints.anchor = GridBagConstraints.NORTHWEST;
        chooserLabelConstraints.insets = new Insets(0, 0, 0, 12);
        gbl.setConstraints(chooserLabel, chooserLabelConstraints);
        options.add(chooserLabel);
        
        
        // contextField
        // contextField.getDocument().addUndoableEditListener(manager);
        GridBagConstraints contextFieldConstraints = new GridBagConstraints();
        contextFieldConstraints.anchor = GridBagConstraints.NORTHWEST;
        contextFieldConstraints.gridx=3;
        contextFieldConstraints.gridy=1;
        contextFieldConstraints.gridwidth=4;
        contextFieldConstraints.gridheight=1;
        contextFieldConstraints.insets = new Insets(0, 0, 0, 6);
        gbl.setConstraints(contextField, contextFieldConstraints);
        options.add(contextField);
        
        // chooseFile button
        JButton chooseFile = new JButton(Messages.getString("..._12")); 
        GridBagConstraints chooseFileConstraints = new GridBagConstraints();
        chooseFileConstraints.anchor = GridBagConstraints.NORTHWEST;
        chooseFileConstraints.gridx=7;
        chooseFileConstraints.gridy=1;
        chooseFileConstraints.gridwidth=1;
        chooseFileConstraints.gridheight=1;
        chooseFileConstraints.insets = new Insets(0, 2, 2, 0);
        gbl.setConstraints(chooseFile, chooseFileConstraints);
        options.add(chooseFile);
        chooseFile.addActionListener(new ContextChooser());
        
        ItemListener redrawer = new NeedsSerialization();
        doWrapping.addItemListener(redrawer);
        doWrapping.setMnemonic(KeyEvent.VK_W);
        doIndenting.addItemListener(redrawer);
        doIndenting.setMnemonic(KeyEvent.VK_P);
        
        // doWrapping checkbox
        GridBagConstraints doWrappingConstraints = new GridBagConstraints();
        doWrappingConstraints.gridx=3;
        doWrappingConstraints.gridy=2;
        doWrappingConstraints.gridwidth=3;
        doWrappingConstraints.gridheight=1;
        doWrappingConstraints.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(doWrapping, doWrappingConstraints);
        options.add(doWrapping);

        // doIndenting checkbox
        GridBagConstraints doIndentingConstraints = new GridBagConstraints();
        doIndentingConstraints.gridx=3;
        doIndentingConstraints.gridy=3;
        doIndentingConstraints.gridwidth=3;
        doIndentingConstraints.gridheight=1;
        doWrappingConstraints.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(doIndenting, doIndentingConstraints); 
        options.add(doIndenting);
        
        // executeButton
        GridBagConstraints executeButtonConstraints = new GridBagConstraints();
        JButton executeButton = new JButton(Messages.getString("Run_Query_13")); 
        executeButton.addActionListener(new RunQuery());
        executeButtonConstraints.gridx=5;
        executeButtonConstraints.gridy=4;
        executeButtonConstraints.gridwidth=2;
        executeButtonConstraints.gridheight=1;
        executeButtonConstraints.anchor = GridBagConstraints.SOUTHEAST;
        executeButtonConstraints.insets = new Insets(17, 0, 0, 0);
        gbl.setConstraints(executeButton, executeButtonConstraints);
        options.add(executeButton);

        FieldFocusListener fieldFocusListenerContext = new FieldFocusListener("context");
        FieldFocusListener fieldFocusListenerBase = new FieldFocusListener("base");
        //register eventhandler for fields
        contextField.addFocusListener(fieldFocusListenerContext);
        baseField.addFocusListener(fieldFocusListenerBase);
        
        // baseField.set(KeyEvent.VK_B);
        
        JPanel master = new JPanel();
        master.add(options);
        return master;
    }

    private String getUserDir(){
        String userdir = System.getProperty("user.dir");
        String dir="";
        if (userdir != null) {
            File baseDir = new File(userdir);
            URI baseURI = baseDir.toURI();
            dir=baseURI.toASCIIString();
        }
        return dir;
    }

    
    private void initQueryProcessor() {
        Configuration config = new Configuration();
        config.setHostLanguage(Configuration.XQUERY);
         queryContext = new StaticQueryContext(config);
        
    }
    
    
    private JMenuBar makeMenuBar() {
        JMenuBar jmb = new JMenuBar();
        jmb.add(makeFileMenu());
        jmb.add(makeEditMenu());
        jmb.add(makeQueryMenu());
        if (!thisIsAMac) jmb.add(makeHelpMenu());
        return jmb;
    }

    
    private JMenu makeFileMenu() {
        
        JMenu menu = new JMenu("File"); 
        menu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem newItem = new JMenuItem(Messages.getString("New_16")); 
        newItem.setMnemonic(KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, preferredMetaKey));
        newItem.addActionListener(new NewListener());
        menu.add(newItem);
        
        JMenuItem openItem = new JMenuItem(Messages.getString("Open..._17")); 
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.addActionListener(new OpenListener());
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, preferredMetaKey));
        menu.add(openItem);     
        
        menu.addSeparator();

        JMenuItem closeItem = new JMenuItem(Messages.getString("Close_18")); 
        closeItem.setMnemonic(KeyEvent.VK_C);
        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, preferredMetaKey));
        closeItem.addActionListener(new CloseListener());
        menu.add(closeItem);     

        menu.addSeparator();

        JMenuItem saveItem = new JMenuItem(Messages.getString("Save_19")); 
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, preferredMetaKey));
        saveItem.addActionListener(new SaveListener(queryArea, false));
        menu.add(saveItem);     
        
        JMenuItem saveAsItem = new JMenuItem(Messages.getString("Save_As..._20")); 
        saveAsItem.setMnemonic(KeyEvent.VK_A);
        saveAsItem.addActionListener(new SaveListener(queryArea, true));
        menu.add(saveAsItem);     
        
        JMenuItem saveOutputItem = new JMenuItem(Messages.getString("Save_Query_Result..._21")); 
        saveOutputItem.setMnemonic(KeyEvent.VK_V); 
        saveOutputItem.addActionListener(new SaveListener(this.outputArea, true));
        menu.add(saveOutputItem);     

        menu.addSeparator();

        JMenuItem pageSetupItem = new JMenuItem(Messages.getString("Page_Setup..._22")); 
        pageSetupItem.setMnemonic(KeyEvent.VK_U);
        pageSetupItem.addActionListener(new PageSetupListener());
        menu.add(pageSetupItem);     

        JMenuItem printItem = new JMenuItem(Messages.getString("Print..._23")); 
        printItem.setMnemonic(KeyEvent.VK_P);
        printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, preferredMetaKey));
        printItem.addActionListener(new PrintListener(outputArea));
        menu.add(printItem);     

        JMenuItem printQueryItem = new JMenuItem(Messages.getString("Print_Query..._24")); 
        printQueryItem.setMnemonic(KeyEvent.VK_Q);
        printQueryItem.addActionListener(new PrintListener(queryArea));
        menu.add(printQueryItem);     

        if (!thisIsAMac) {
            // Mac OS X adds its own quit item
            menu.addSeparator();
    
            JMenuItem exitItem = new JMenuItem(Messages.getString("Exit_25")); 
            exitItem.setMnemonic(KeyEvent.VK_X);
            exitItem.addActionListener(new ExitListener());
            menu.add(exitItem);  
        }   
        
        return menu;
        
    }

    
    private JMenu makeEditMenu() {
        
        JMenu menu = new JMenu(Messages.getString("Edit_26")); 
        menu.setMnemonic(KeyEvent.VK_E);
        
        JMenuItem undoItem = new JMenuItem(Messages.getString("Undo_27")); 
        undoItem.setMnemonic(KeyEvent.VK_U);
        undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', preferredMetaKey));
        undoItem.addActionListener(new UndoAction());
        menu.add(undoItem); 
           
        JMenuItem redoItem = new JMenuItem(Messages.getString("Redo_28")); 
        redoItem.setMnemonic(KeyEvent.VK_R);
        redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', preferredMetaKey));
        redoItem.addActionListener(new RedoAction());
        menu.add(redoItem);  
           
        menu.addSeparator();

        JMenuItem cutItem = new JMenuItem(Messages.getString("Cut_29")); 
        cutItem.setMnemonic(KeyEvent.VK_C);
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, preferredMetaKey));
        cutItem.addActionListener(queryArea.getActionMap().get(DefaultEditorKit.cutAction));
        menu.add(cutItem);     
        
        JMenuItem copyItem = new JMenuItem(Messages.getString("Copy_30")); 
        copyItem.setMnemonic(KeyEvent.VK_O);
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, preferredMetaKey));
        copyItem.addActionListener(queryArea.getActionMap().get(DefaultEditorKit.copyAction));
        menu.add(copyItem);     
        
        JMenuItem pasteItem = new JMenuItem(Messages.getString("Paste_31")); 
        pasteItem.setMnemonic(KeyEvent.VK_P);
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, preferredMetaKey));
        pasteItem.addActionListener(queryArea.getActionMap().get(DefaultEditorKit.pasteAction));
        menu.add(pasteItem);     
        
        if (thisIsAMac) {
            JMenuItem clearItem = new JMenuItem(Messages.getString("Clear_32")); 
            clearItem.setMnemonic(KeyEvent.VK_E);
            // clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ????));
            clearItem.setEnabled(false); // ????
            menu.add(clearItem);     
        }

        menu.addSeparator();

        JMenuItem selectAllItem = new JMenuItem(Messages.getString("Select_All_33")); 
        selectAllItem.setMnemonic(KeyEvent.VK_A);
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, preferredMetaKey));
        ActionListener selectAllAction = queryArea.getActionMap().get("select-all");
        selectAllItem.addActionListener(selectAllAction);
        menu.add(selectAllItem);     
        
        return menu;
    }

    
    private JMenu makeQueryMenu() {
        
        JMenu menu = new JMenu(Messages.getString("Query_6")); 
        menu.setMnemonic(KeyEvent.VK_Q);

        JMenuItem queryItem = new JMenuItem(Messages.getString("Run_Query_13")); 
        queryItem.setMnemonic(KeyEvent.VK_R); 
        queryItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, preferredMetaKey));
        queryItem.addActionListener(new RunQuery());
        menu.add(queryItem);
        
        menu.addSeparator();

        JMenuItem baseItem = new JMenuItem(Messages.getString("setBaseURI")); 
        baseItem.setMnemonic(KeyEvent.VK_B); 
        baseItem.addActionListener(new BaseURIChooser()); 
        menu.add(baseItem);

        JMenuItem contextItem = new JMenuItem(Messages.getString("setContextNode")); 
        contextItem.setMnemonic(KeyEvent.VK_C); 
        contextItem.addActionListener(new ContextChooser()); 
        menu.add(contextItem);
 
        menu.addSeparator();

        ActionListener changer = new NeedsSerialization();
        wrapItem.setMnemonic(KeyEvent.VK_W); 
        wrapItem.addActionListener(changer); 
        menu.add(wrapItem);

        indentItem.setMnemonic(KeyEvent.VK_P); 
        indentItem.addActionListener(changer); 
        menu.add(indentItem);
        
        return menu;
    }
    
    
    private JMenu makeHelpMenu() {
        
        JMenu menu = new JMenu(Messages.getString("Help_35")); 
        menu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem(Messages.getString("About_XQuisitor..._36")); 
        aboutItem.setMnemonic(KeyEvent.VK_A); 
        aboutItem.addActionListener(new AboutListener());
        menu.add(aboutItem);
        // ????
        return menu;
    }
    
    
    public void show() {
        openWindows.add(this);
        super.show();   
    }

    
    public void hide() {
        super.hide();
        openWindows.remove(this);
        if (openWindows.isEmpty()) System.exit(0);  
    }
    
    
    private class ContextChooser implements ActionListener {
        
        public void actionPerformed(ActionEvent evt) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(Messages.getString("Please_choose_context_document_or_directory__37")); 
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (contextFile != null) {
                chooser.setCurrentDirectory(contextFile);
            }
            if (chooser.showOpenDialog(QueryFrame.this) == JFileChooser.APPROVE_OPTION) {
                contextFile = new File(
                  chooser.getCurrentDirectory(), 
                  chooser.getSelectedFile().getName()
                );
                contextField.setText(contextFile.toURI().toString());
                saveContextField();
            }
            
        }

    }
    
 
    private class CancelQuery implements ActionListener {
        
        private QueryThread t;
        
        CancelQuery(QueryThread t) {
            this.t = t;   
        }

        public void actionPerformed(ActionEvent evt) {
            t.userCancelled();
        }   
        
    }

    private class BaseURIChooser implements ActionListener {
        
        private File uriFile;

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(Messages.getString("Please_choose_directory_that_sets_base_URI__38")); 
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (uriFile != null) chooser.setSelectedFile(uriFile);
            else if (contextFile != null) chooser.setSelectedFile(contextFile);
            if (chooser.showOpenDialog(QueryFrame.this) == JFileChooser.APPROVE_OPTION) {
                uriFile = new File(
                  chooser.getCurrentDirectory(), 
                  chooser.getSelectedFile().getName()
                );
                baseField.setText(uriFile.toURI().toString());
            }
            
        }

    }
 
    private static String lineSeparator = System.getProperty("line.separator"); 
 
    private class RunQuery implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            Thread t = new QueryThread(queryArea.getText(), baseField.getText().trim());
            t.start();
        }

    }


    private void performQuery(String query, String baseURI)
      throws XPathException, TransformerException, IOException {
          
        XQueryExpression exp = queryContext.compileQuery(query);
        
        if (null != baseURI && !"".equals(baseURI)) { 
            queryContext.setBaseURI(baseURI);   
        }
        
        DynamicQueryContext result = new DynamicQueryContext(queryContext.getConfiguration());
        // set context
        String uri = contextField.getText().trim();
        if (null != uri && !("".equals(uri))) { 
            InputSource eis = new InputSource(uri);
            Source sourceInput = new SAXSource(eis);
            DocumentInfo doc = queryContext.buildDocument(sourceInput);
            result.setContextNode(doc);
        }

        queryResult = exp.iterator(result);
    }

    private String parseLineComment(String query){

        return "";
    }
    
    private class QueryThread extends Thread {
        
        private String query;
        private String baseURI;
        
        QueryThread(String query, String baseURI) {
            this.query = query;     
            this.baseURI = baseURI;     
        }
        
        synchronized void userCancelled() {
            stop();
            // QueryProcessor may have been left in an inconsistent
            // state, so reinitialize it
            initQueryProcessor(); 
            hideProgressBar();
        }

        public void run() {
            // should wait a second or two before popping up progress
            // dialog in case it's a quick query???? use a Timer
            showProgressBar(Messages.getString("Querying..._42")); 
            try {
                performQuery(query, baseURI);                
                final String result = serialize();
                hideProgressBar();
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        outputArea.setText(result);
                    }
                });
             }
             catch (IOException ex) {
                hideProgressBar();
                JOptionPane.showMessageDialog(QueryFrame.this, 
                    ex.getMessage(), Messages.getString("Error_while_executing_query_43"), 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();   
             } 
             catch (TransformerException ex) {
                hideProgressBar();
                JOptionPane.showMessageDialog(QueryFrame.this, 
                    ex.getMessage(), Messages.getString("Error_while_executing_query_43"), 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();   
             } 

        }

        private JDialog progress;

        private synchronized void showProgressBar(String message) {
            
            // arbitrary length
            JProgressBar progressBar = new JProgressBar(0, 500);
            progressBar.setValue(0);
            progressBar.setIndeterminate(true);
            progressBar.setString(Messages.getString("Querying..._45")); 
    
            progress = new JDialog(QueryFrame.this, message, true);
            progress.setResizable(false);
            progress.getContentPane().setLayout(new GridLayout(3, 1));
            progress.getContentPane().add(BorderLayout.NORTH, progressBar);
            JButton cancel = new JButton(Messages.getString("Cancel_46")); 
            cancel.addActionListener(new CancelQuery(this));
            
            JPanel buttonPanel = new JPanel(); 
            buttonPanel.add(cancel);
            progress.getContentPane().add(buttonPanel);
            
            progress.pack();
            progress.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            progress.setLocationRelativeTo(null);
            
            // Need a modal dialog but don't want to block this thread
            // when it's shown.
            Thread dontBlockMe = new Thread(new Runnable() {
                public void run() {
                    progress.show();                    
                }
            });
            dontBlockMe.start();
            // need to make sure dontBlockMe shows the dialog
            // before we continue. The dontBlockMe thread won't exit
            // so check that the dialog is on the screen:
            while (!(progress.isVisible())) {
                try {
                    wait(50);   
                }
                catch (InterruptedException ex) {
                    // try again
                }
            }
            
        }
    
        
        private synchronized void hideProgressBar() {
            progress.setVisible(false);   
            progress.dispose();
        }   
        
    }
    
    /* private class LaunchProgressBar extends TimerTask {

        private QueryThread thread;
        
        public LaunchProgressBar(QueryThread thread) {
            this.thread = thread;   
        } 

        public void run() {
            if (thread.isAlive()) {
                showProgressBar("Querying...", thread);   
            }
        }
        
    } */
    
    private void turnOnWaitCursor() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    private void turnOffWaitCursor() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private class NeedsSerialization 
      implements ActionListener, ItemListener, Runnable {

        public void itemStateChanged(ItemEvent evt) {
             wrapItem.setState(doWrapping.isSelected());
             indentItem.setState(doIndenting.isSelected());
             startSerialize();
        }

        private void startSerialize() {
            if (queryResult != null) {
                Thread t = new Thread(this);
                t.start();
            }
        }
        
        public void run() {
            try {
                turnOnWaitCursor();
                final String result = serialize();
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        outputArea.setText(result);
                    }
                });
                turnOffWaitCursor();
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(QueryFrame.this, 
                    ex.getMessage(), Messages.getString("Error_while_executing_query_43"), 
                    JOptionPane.ERROR_MESSAGE);
            }            
        }

        public void actionPerformed(ActionEvent evt) {
             doWrapping.setSelected(wrapItem.getState());
             doIndenting.setSelected(indentItem.getState());
             startSerialize();
        }
        
    }
    

    private String serialize()
      throws TransformerException, XPathException, IOException {
        StringWriter out = new StringWriter();                
        
        Properties indent;
        if (doIndenting.isSelected()) {
            indent = prettyPrint;
        }
        else {
            indent = uglyPrint;  
        }
        
        // reset a copy of the results
        SequenceIterator queryResult = this.queryResult.getAnother();
        
        if (doWrapping.isSelected()) {
            DocumentInfo resultDoc = QueryResult.wrap(queryResult, queryContext.getConfiguration());
            QueryResult.serialize(resultDoc, new StreamResult(out), indent, queryContext.getConfiguration());
        }
        else {
            while (true) {
                Item next = queryResult.next(); 
                if (next == null) break;
                // Check if item is a NodeInfo or a something else
                // if (Type.isNodeType(next.getItemType())) {
                   QueryResult.serialize((NodeInfo) next, new StreamResult(out), indent, queryContext.getConfiguration()); 
                   // This only works for document and element nodes????
                   // Will this be improved in future Saxon's? if so
                   // this is all I need, and don't need to write my own. 
                /*}
                else {
                    out.write(next.toString()); */
                    // what about things where toString() is not
                    // a string representation; e.g. hexBinary or Date????
                    // it appears the date time values are a special problem
                    /*net.sf.saxon.value.DateValue@c9290c78
                      net.sf.saxon.value.TimeValue@6c76f794
                      net.sf.saxon.value.GYearMonthValue@30283c79
                      net.sf.saxon.value.GYearValue@781c3472*/
                    // can I get the string value of any of these????
                    
                // }
                out.write(lineSeparator);
            }   
        }
        out.flush();
        out.close(); 
        return out.toString();
    }
    
    private class AboutListener implements ActionListener {
        
        public void actionPerformed(ActionEvent evt) {
            JDialog dialog = new AboutDialog(QueryFrame.this);
            dialog.show();
        }

    }
    
    private class OpenListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(Messages.getString("Please_choose_query_file__50")); 
            if (chooser.showOpenDialog(QueryFrame.this) == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                try {
                    // stuff file in query box
                    Reader reader = new FileReader(selected);
                    reader = new BufferedReader(reader);
                    StringBuffer sb = new StringBuffer();
                    for (int c = reader.read(); c != -1; c = reader.read()) {
                       sb.append((char) c); 
                    }
                    outputArea.setText(sb.toString());
                    queryFile = selected;
                }
                catch (IOException ex) {
                    JOptionPane.showMessageDialog(QueryFrame.this, 
                      ex.getMessage(),
                      Messages.getString("I/O_Error_while_reading_query_51"), 
                      JOptionPane.ERROR_MESSAGE);
                }
            }
            
        }

    }

    private class NewListener implements ActionListener {
        
        public void actionPerformed(ActionEvent evt) {
            JFrame frame = new QueryFrame();
            // Is this really necessary since we're in the
            // event queue/AWT thread already????
            Runnable runner = new FrameDisplayer(frame);
            EventQueue.invokeLater(runner);
        }

    }
    
    private class ExitListener implements ActionListener {
        
        public void actionPerformed(ActionEvent evt) {
            quit();
        }

    }
    
    void quit() {
        // need to adjust this on the Mac so you don't exit
        // after last window closes????
        while (!(openWindows.isEmpty())) {
            QueryFrame frame = (QueryFrame) openWindows.get(0);
            frame.hide();   
        }
    }

    private void saveContextField(){
        prefs.put(PRF_CONTEXT, contextField.getText().trim());
        System.out.println("'" + contextField.getText().trim() + "'");
        System.out.println("context save");
    }
    
    private class CloseListener implements ActionListener {
        
        public void actionPerformed(ActionEvent evt) {
            QueryFrame.this.hide();
        }

    }
    
    private class SaveListener implements ActionListener {
        
        private JTextComponent text;
        private boolean saveAs;
        
        SaveListener(JTextComponent text, boolean saveAs) {  
            this.saveAs = saveAs;
            this.text = text; 
        }
        
        public void actionPerformed(ActionEvent evt) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(Messages.getString("Save_query_to___52")); 
            if (contextFile != null) chooser.setSelectedFile(contextFile); // ????

            saveFile(chooser);
        }

        private void saveFile(JFileChooser chooser) {
            
            File outputFile = queryFile;
            if ((outputFile == null || saveAs) &&
              chooser.showSaveDialog(text) == JFileChooser.APPROVE_OPTION) {
                outputFile = new File(
                  chooser.getCurrentDirectory(), 
                  chooser.getSelectedFile().getName()
                );
                if (outputFile.exists()) {
                   int choice = JOptionPane.showConfirmDialog(text,
                     Messages.getString("Overwrite_file__53")  
                       + outputFile.getName() 
                       + Messages.getString("QuestionMark__54"));  
                   if (choice == JOptionPane.CANCEL_OPTION) {
                        chooser.setVisible(false);
                        return;   
                   }
                   else if (choice == JOptionPane.NO_OPTION) {
                       saveFile(chooser);
                   }
                }
            }
            if (outputFile != null) {
                try {
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
                    Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8")); 
                    String data = text.getText();
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    queryFile = outputFile;
                }
                catch (IOException ex) {
                    JOptionPane.showMessageDialog(text, 
                      ex.getMessage(),
                      Messages.getString("I/O_Error_while_saving_data_56"), 
                      JOptionPane.ERROR_MESSAGE);  
                }
            }
        }

    }
    
    private class UndoAction extends AbstractAction {
        
        public void actionPerformed(ActionEvent evt) {
            try {
                manager.undo();
            }
            catch (CannotUndoException ex) {
                getToolkit().beep();   
            }
        }
        
        
    }
    
    private class RedoAction extends AbstractAction {
        
        public void actionPerformed(ActionEvent evt) {
            try {
                manager.redo();
            }
            catch (CannotRedoException ex) {
                getToolkit().beep();   
            }
        }   
        
    }
    
    
    private PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet(); 
    
    private class PrintListener implements ActionListener {
        
        private JTextComponent source;
        
        PrintListener(JTextComponent source) {
            this.source = source;   
        }

        public void actionPerformed(ActionEvent evt) {    
            
            String data = source.getText();

            DocFlavor myFormat = DocFlavor.STRING.TEXT_PLAIN;
            Doc myDoc = new SimpleDoc(data, myFormat, null); 
            PrinterJob dialog = PrinterJob.getPrinterJob(); 
            if (!dialog.printDialog(attributes)) return;
            
            // landscape printing is flaky????
            PrintService[] services = PrintServiceLookup.lookupPrintServices(myFormat, attributes);
            if (services.length > 0) { 
                DocPrintJob job = services[0].createPrintJob(); 
                try { 
                    job.print(myDoc, attributes); 
                } 
                catch (PrintException ex) {
                    JOptionPane.showMessageDialog(QueryFrame.this, 
                      ex.getMessage(),
                      Messages.getString("Printing_error_57"), 
                      JOptionPane.ERROR_MESSAGE);
                } 
            }
            else {
                JOptionPane.showMessageDialog(QueryFrame.this, 
                  Messages.getString("Could_not_locate_a_printer._58"), 
                  Messages.getString("Printing_error_57"), 
                  JOptionPane.ERROR_MESSAGE);
            }
             
        }   
        
    }

    private class PageSetupListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {    
            PrinterJob job = PrinterJob.getPrinterJob(); 
            PageFormat result = job.pageDialog(attributes); 
            if (result == null) { // user canceled
                return;
            }
                        
        }   
        
    }

    private class FieldFocusListener extends FocusAdapter {
        private String who;

        public FieldFocusListener(String who) {
            this.who = who;
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (who.equals("base")) {
                prefs.get(PRF_BASEURI,baseField.getText());
            }else if (who.equals("context")){
                saveContextField();
            }
            System.out.println("Save stuff for next time");
        }
    }
}


