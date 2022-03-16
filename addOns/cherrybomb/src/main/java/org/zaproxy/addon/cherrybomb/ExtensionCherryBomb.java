/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.cherrybomb;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.history.ExtensionHistory;
//import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.view.ZapMenuItem;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableEntry;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
//import com.google.gson.*;

/**
 * An example ZAP extension which adds a top level menu item, a pop up menu item and a status panel.
 *
 * <p>{@link ExtensionAdaptor} classes are the main entry point for adding/loading functionalities
 * provided by the add-ons.
 *
 * @see #hook(ExtensionHook)
 */
public class ExtensionCherryBomb extends ExtensionAdaptor {
    public static String  message;
    public static boolean website_exist=false;
    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionCherryBomb";

    // The i18n prefix, by default the package name - defined in one place to make it easier
    // to copy and change this example
    protected static final String PREFIX = "cherryBomb";

    /**
     * Relative path (from add-on package) to load add-on resources.
     *
     * @see Class#getResource(String)
     */
    private static final String RESOURCES = "resources";

    private static final ImageIcon ICON =
            new ImageIcon(ExtensionCherryBomb.class.getResource(RESOURCES + "/hacker.png"));

    private static final String EXAMPLE_FILE = "example/ExampleFile.txt";

    private ZapMenuItem menuExample;
    private RightClickMsgMenu popupMsgMenuExample;
    private AbstractPanel statusPanel;

    private CherryBombAPI api;

    private static final Logger LOGGER = LogManager.getLogger(ExtensionCherryBomb.class);
	private static final String possibilities = null;

    public ExtensionCherryBomb() {
        super(NAME);
        setI18nPrefix(PREFIX);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        this.api = new CherryBombAPI();
        extensionHook.addApiImplementor(this.api);

        // As long as we're not running as a daemon
        if (getView() != null) {
            extensionHook.getHookMenu().addToolsMenuItem(getMenuExample());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMsgMenuExample());
            extensionHook.getHookView().addStatusPanel(getStatusPanel());
        }
    }

    @Override
    public boolean canUnload() {
        // The extension can be dynamically unloaded, all resources used/added can be freed/removed
        // from core.
        return true;
    }

    @Override
    public void unload() {
        super.unload();

        // In this example it's not necessary to override the method, as there's nothing to unload
        // manually, the components added through the class ExtensionHook (in hook(ExtensionHook))
        // are automatically removed by the base unload() method.
        // If you use/add other components through other methods you might need to free/remove them
        // here (if the extension declares that can be unloaded, see above method).
    }

    private AbstractPanel getStatusPanel() {
        if (statusPanel == null) {
            statusPanel = new AbstractPanel();
            statusPanel.setLayout(new CardLayout());
            statusPanel.setName(Constant.messages.getString(PREFIX + ".panel.title"));
            statusPanel.setIcon(ICON);
            JTextPane pane = new JTextPane();
            pane.setEditable(false);
            // Obtain (and set) a font with the size defined in the options
            pane.setFont(FontUtils.getFont("Dialog", Font.PLAIN));
            pane.setContentType("text/html");
            pane.setText(Constant.messages.getString(PREFIX + ExtensionCherryBomb.message));//".panel.msg"

            statusPanel.add(pane);
        }
        return statusPanel;
    }

    private ZapMenuItem getMenuExample() {
        if (menuExample == null) {
            menuExample = new ZapMenuItem(PREFIX + ".topmenu.tools.title");

            menuExample.addActionListener(
            	    e -> {
            	    	String[] arr = PopupMessage();//display the menu popup
            	    	JSONObject obj = CreateJson(arr[0]);
              	    	System.out.println(message);
              	    	if (ExtensionCherryBomb.website_exist){
              	    		System.out.println("Ok");
              	    		try {
								byte[] compress= CreatingZIPFile(obj);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
              	    	
              	    		
              	    	}
              	    	else {
              	    		System.out.println("Err");
              	    		JOptionPane.showMessageDialog(popupMsgMenuExample, "Website not Found.","Error", getOrder(), ICON);
              	    	}

                        View.getSingleton()
                                .showMessageDialog(
                                        Constant.messages.getString(PREFIX + ".topmenu.tools.msg"));
                        // And display a file included with the add-on in the Output tab
                        displayFile(EXAMPLE_FILE);
                    });
        }
        return menuExample;
    }
    private String[] PopupMessage() {
    	String[] arr= {"","",""};
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"Map", "Decide", "Regenerate Map"});

    	JTextField field1 = new JTextField("");
        JTextField field2 = new JTextField("");
        TextPrompt placeholder1 = new TextPrompt("www.google.com", field1);
        TextPrompt placeholder2 = new TextPrompt("token", field2);
        placeholder1.changeAlpha(0.75f); placeholder1.changeStyle(Font.ITALIC);
        placeholder2.changeAlpha(0.75f); placeholder2.changeStyle(Font.ITALIC);
        
         

        

        JPanel panel = new JPanel(new GridLayout(4, 3));
        panel.add(new JLabel("Website to attack:"));
        panel.add(field1);
        panel.add(new JLabel("API KEY:"));
        panel.add(field2);
        panel.add(new JLabel("Choose an action:"));
        panel.add(comboBox);
        int result = JOptionPane.showConfirmDialog(null, panel, "CherryBomb Configuration",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,ICON);
            if (result == JOptionPane.OK_OPTION) {
                System.out.println(
                     " " + field1.getText() + field2.getText());
            } else {
                System.out.println("Cancelled");
            }
       
        arr[0]=field1.getText();
        arr[1]=field2.getText();
        arr[2]=(String)comboBox.getSelectedItem();
        return arr;
    }
    private JSONObject CreateJson(String website) {
	     JSONObject json = new JSONObject();
	   	 ExtensionHistory history1 = new ExtensionHistory();
	 	 int lastRef = history1.getLastHistoryId();
	     JSONArray array = new JSONArray();
		 for ( int x = 1;  x <= lastRef ; x++) {
			 try {
			 	//String site = field1.getText();
		    	JSONObject item = new JSONObject();
			// 	System.out.println(i);
				HistoryReference history = new HistoryReference(x);
		    	//    HistoryReference history = history1.getHistoryReference(x);
				HttpMessage http_mess = new HttpMessage(history.getHttpMessage());
			 //   System.out.println(http_mess.getRequestHeader().toString());   System.out.println(http_mess.getResponseHeader().toString());
			    DefaultHistoryReferencesTableEntry table = new DefaultHistoryReferencesTableEntry(history, HistoryReferencesTableModel.Column.values());
		//	    System.out.println("history: "+table.getHistoryId()); System.out.println(table.getUri()); System.out.println("hostname: " +table.getHostName());
			    if(table.getHostName().equals(website)) {
			    	ExtensionCherryBomb.website_exist= true;
				    if (!table.getUri().contains(".jsp") && !table.getUri().contains(".css") && !table.getUri().contains(".js") && !table.getUri().contains(".html") && !table.getUri().contains(".ico")  && !table.getUri().contains(".png")) {
				    	//System.out.println(table.getUri());
				    	if (!http_mess.getResponseHeader().toString().contains("Content-Type: text/html")) {
				    		item.put("request",http_mess.getRequestHeader().toString());
						    item.put("response",http_mess.getResponseHeader().toString() );
	            	    	array.add(item);
				    	}
				    	  
				    }
			    }
			 }
			 
	 
		catch (HttpMalformedHeaderException | DatabaseException | JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//System.out.println("error");
		}
			 
	 	
	 	try {
			json.put("session",array);
			ExtensionCherryBomb.message = json.toString();
			 // Socket socket = new Socket("localhost", 7777);
		     // ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			//  Socket s = new Socket("192.168.0.100", 7777);
			 // try (OutputStreamWriter out = new OutputStreamWriter(
			//  s.getOutputStream(), StandardCharsets.UTF_8)) {
			 // out.write(json.toString());
			  //    						      System.out.println("Connected!");
	
			      
			  }
			 
		 catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		 
		 }
		 
		 return json;
	    	
	    }
    
    
    private byte[] CreatingZIPFile (JSONObject JsonLogs) throws IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
    	ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
    	objectOut.writeObject(JsonLogs);
    	objectOut.close();
    	byte[] bytes = baos.toByteArray();
    	return bytes;

    }
    
    
    
    
    
    
    private void GetlengthofZip() {}

    private void displayFile(String file) {
        if (!View.isInitialised()) {
            // Running in daemon mode, shouldnt have been called
            return;
        }
        try {
            File f = new File(Constant.getZapHome(), file);
            if (!f.exists()) {
                // This is something the user should know, so show a warning dialog
                View.getSingleton()
                        .showWarningDialog(
                                Constant.messages.getString(
                                        ExtensionCherryBomb.PREFIX + ".error.nofile",
                                        f.getAbsolutePath()));
                return;
            }
            // Quick way to read a small text file
            String contents = new String(Files.readAllBytes(f.toPath()));
            // Write to the output panel
            View.getSingleton().getOutputPanel().append(contents);
            // Give focus to the Output tab
            View.getSingleton().getOutputPanel().setTabFocus();
        } catch (Exception e) {
            // Something unexpected went wrong, write the error to the log
            LOGGER.error(e.getMessage(), e);
        }
    }

    private RightClickMsgMenu getPopupMsgMenuExample() {
        if (popupMsgMenuExample == null) {
            popupMsgMenuExample =
                    new RightClickMsgMenu(
                           // this, Constant.messages.getString(PREFIX + ".popup.title"));
                    		this, Constant.messages.getString(PREFIX + ExtensionCherryBomb.message));
        }
        return popupMsgMenuExample;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString(PREFIX + ".desc");
    }
}
