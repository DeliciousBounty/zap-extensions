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
import java.lang.Thread;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
//import CherryBombAPI;

/**
 * An example ZAP extension which adds a top level menu item, a pop up menu item and a status panel.
 *
 * <p>{@link ExtensionAdaptor} classes are the main entry point for adding/loading functionalities
 * provided by the add-ons.
 *
 * @see #hook(ExtensionHook)
 */
public class ExtensionCherryBomb extends ExtensionAdaptor {
  public static String message;
  public static boolean website_exist = false;
  // The name is public so that other extensions can access it
  public static final String NAME = "ExtensionCherryBomb";

  // The i18n prefix, by default the package name - defined in one place to make it easier
  // to copy and change this exampletexאק
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
  private static final String EXAMPLE_TITLE = "example/ExampleFile1.txt";
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
      pane.setText(Constant.messages.getString(PREFIX + ExtensionCherryBomb.message)); //".panel.msg"

      statusPanel.add(pane);
    }
    return statusPanel;
  }

  private ZapMenuItem getMenuExample() { //Start here
    if (menuExample == null) {
      menuExample = new ZapMenuItem(PREFIX + ".topmenu.tools.title");
      menuExample.addActionListener(
        e -> {
          displayFile(EXAMPLE_FILE); // display the first message
          String[] arr = PopupMessage(); //display the menu popup
          System.out.println("HOST choice: " + arr[0]);
          JSONObject obj = api.CreateJsonFromLogs(arr[0]); //create logs 
          //	System.out.println(data_to_send);
          System.out.println(message);
          if (ExtensionCherryBomb.website_exist) {
            System.out.println("Prepare to send..");
            try {
              Thread.sleep(3000);
            } catch (InterruptedException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
            api.SendtoTheServer(arr[0], obj, arr[1]);
            displayFile(EXAMPLE_TITLE);
          } else {
            System.out.println("Err");
            JOptionPane.showMessageDialog(popupMsgMenuExample, "Website not Found.", "Error", getOrder(), ICON);
          }

          View.getSingleton()
          .showMessageDialog(
            Constant.messages.getString(PREFIX + ".topmenu.tools.msg"));
          // And display a file included with the add-on in the Output tab
          //displayFile(EXAMPLE_FILE);
        });
    }
    return menuExample;
  }
  private String[] PopupMessage() {
    String[] arr = {
      "",
      ""
    };
    String[] domain = new String[] {};
    JComboBox < String > comboBox = new JComboBox < > (new String[] {
      "Map",
      "Attack"
    });
    JComboBox < String > host_comboBox = new JComboBox < > ();
    JTextField token_field = new JTextField("");
    TextPrompt placeholder2 = new TextPrompt("token", token_field);
    JPanel panel = new JPanel(new GridLayout(10, 25)); //10,20
    JLabel token_label = new JLabel("API Key: ");
    panel.add(token_label);
    panel.add(token_field);
    JButton button = new JButton("Check");
    button.setPreferredSize(new Dimension(2, 1));
    panel.add(button);

    button.addActionListener(e -> {
      ArrayList < String > domains = selectionButtonPressed(token_field.getText(), token_label);
      System.out.println(token_field.getText());
      token_label.setForeground(new Color(255, 0, 0));
      if (domains.size() == 0) {
        token_label.setText("Subdomain not config");
      }
      if (domains.get(0) == null) {
        token_label.setText("Invalid Token");

      } else {
        token_label.setText("");
        for (String i: domains) {
          System.out.println(i);
          host_comboBox.addItem(i);
        }

      }
    });
    panel.add(new JLabel("Target Url: "));
    panel.add(host_comboBox);
    panel.add(new JLabel("Choose an action: "));
    panel.add(comboBox);

    int result = JOptionPane.showConfirmDialog(null, panel, "CherryBomb Configuration",
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, ICON);
    arr[0] = (String) host_comboBox.getSelectedItem();
    arr[1] = token_field.getText();
    return arr;
  }
 
  public static ArrayList < String > selectionButtonPressed(String token, JLabel lab) { //method to receive subdomains
    String url = "https://saas.blstsecurity.com/zap/get_domains";
    JSONArray array = new JSONArray();
    if (!lab.getText().equals("")) {
      try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).header("x-blst-key", token).build();
        HttpResponse < String > response = client.send(req, HttpResponse.BodyHandlers.ofString());
        array.add(response.body());
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
      ArrayList < String > arrays = new ArrayList < String > ();

      try {
        JSONArray json_arr = array.getJSONObject(0).getJSONArray("sub_domains");
        for (int i = 0; json_arr.size() > i; i++) {
          arrays.add(json_arr.getString(i));
        }
        return arrays;
      } catch (JSONException j) {
        arrays.add(null);
        return arrays;
      }
    } else {
      lab.setText("Please enter your token");

    }
    return null;

  }

  //    private String PrepareTosend(JSONObject logs) {
  //        String result = "["+logs.toString().replaceAll("\\\"", "\\\\\"")+"]";
  //        System.out.println("Logs to send....... "+ result);
  //        try {
  //			Thread.sleep(5000);
  //		} catch (InterruptedException e1) {
  //			e1.printStackTrace();
  //		}
  //    	
  //    	return result;
  //    			
  // }

 
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
      //   LOGGER.error(e.getMessage(), e);
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