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
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;

import javax.swing.ImageIcon;
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
import org.zaproxy.zap.view.table.AbstractCustomColumnHistoryReferencesTableModel;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableEntry;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableModel;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel.Column;
import java.io.Serializable;
import java.net.Socket;

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
                        // This is where you do what you want to do.
                        // In this case we'll just show a popup message.
            	    	JSONArray array = new JSONArray();
       	    		 for ( int i = 0 ; i < 10000 ; i++) {

            	    	 try {
            	    		 	String site = "www.exploit-db.com";
                 	    	    JSONObject item = new JSONObject();
            	    		// 	System.out.println(i);
								HistoryReference history = new HistoryReference(i);
								HttpMessage http_mess = new HttpMessage(history.getHttpMessage());
							 //   System.out.println(http_mess.getRequestHeader().toString());
							 //   System.out.println(http_mess.getResponseHeader().toString());
							    
							    DefaultHistoryReferencesTableEntry table = new DefaultHistoryReferencesTableEntry(history, HistoryReferencesTableModel.Column.values());
						//	    System.out.println("history: "+table.getHistoryId());
						//	    System.out.println(table.getUri());
							//    System.out.println("hostname: " +table.getHostName());
							    if(table.getHostName().equals(site)) {
								    if (!table.getUri().contains(".jsp") && !table.getUri().contains(".css") && !table.getUri().contains(".js") && !table.getUri().contains(".html") && !table.getUri().contains(".ico")  && !table.getUri().contains(".png")) {
								    	//System.out.println(table.getUri());
								    	  item.put("request",http_mess.getRequestHeader().toString());
										    item.put("response",http_mess.getResponseHeader().toString() );
					            	    	array.add(item);
								    }
								  

							    }
							    //www.exploit-db.com

            	    		 }
					 


						catch (HttpMalformedHeaderException | DatabaseException | JSONException e1) {
							// TODO Auto-generated catch block
						//	e1.printStackTrace();
							System.out.println("error");
						}
             	    	JSONObject json = new JSONObject();
             	    	try {
							json.put("session",array);
							ExtensionCherryBomb.message = json.toString();
							System.out.println(message);
							/*
							  Socket socket = new Socket("localhost", 7777);
						//      ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
							  Socket s = new Socket("192.168.0.100", 7777);
							  try (OutputStreamWriter out = new OutputStreamWriter(
							          s.getOutputStream(), StandardCharsets.UTF_8)) {
							      out.write(json.toString());
							      						      System.out.println("Connected!");

							      
							  }
							  */
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

            	    	 
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
