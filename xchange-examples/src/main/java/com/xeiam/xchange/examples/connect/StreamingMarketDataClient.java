package com.xeiam.xchange.examples.connect;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.intersango.v0_1.IntersangoExchange;
import com.xeiam.xchange.service.marketdata.streaming.MarketDataEvent;
import com.xeiam.xchange.service.marketdata.streaming.RunnableMarketDataListener;
import com.xeiam.xchange.service.marketdata.streaming.StreamingMarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Streaming market data client to provide the following to XChange:</p>
 * <ul>
 * <li>Demonstration of connection to exchange server using direct socket and displaying events</li>
 * </ul>
 * <h3>How to use it</h3>
 * <p>Simply run this up through main() and click Connect. The default settings will connect to the Intersango exchange</p>
 */
public class StreamingMarketDataClient extends JFrame implements ActionListener {

  private final Logger log = LoggerFactory.getLogger(StreamingMarketDataClient.class);

  private static final long serialVersionUID = -6056260699201258657L;

  private final JButton connect;
  private final JButton close;
  private final JTextArea ta;

  private StreamingMarketDataService streamingMarketDataService = null;
  private ExecutorService executorService = null;


  /**
   * The main entry point to the demonstration
   *
   * @param args CLI arguments (ignored)
   * @throws java.net.MalformedURLException If something goes wrong
   * @throws InterruptedException           If something goes wrong
   */
  public static void main(String[] args) throws MalformedURLException, InterruptedException {

    // Require a client to respond to events
    new StreamingMarketDataClient();

  }

  public StreamingMarketDataClient() {
    super("Intersango Direct Socket Streaming Exchange Client");
    Container c = getContentPane();
    GridLayout layout = new GridLayout();
    layout.setColumns(1);
    layout.setRows(5);
    c.setLayout(layout);

    connect = new JButton("Connect");
    connect.addActionListener(this);
    c.add(connect);

    close = new JButton("Close");
    close.addActionListener(this);
    close.setEnabled(false);
    c.add(close);

    JScrollPane scroll = new JScrollPane();
    ta = new JTextArea();
    scroll.setViewportView(ta);
    c.add(scroll);

    Dimension d = new Dimension(600, 500);
    setPreferredSize(d);
    setSize(d);

    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        if (streamingMarketDataService != null) {
          streamingMarketDataService.stop();
        }
        if (executorService != null) {
          executorService.shutdownNow();
        }
        dispose();
      }
    });

    setLocationRelativeTo(null);
    setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {

    if (e.getSource() == connect) {
      close.setEnabled(true);
      connect.setEnabled(false);

      // Construct an Exchange that we know to use a direct socket to support streaming market data
      Exchange intersango = IntersangoExchange.newInstance();
      streamingMarketDataService = intersango.getStreamingMarketDataService();

      // Create a runnable listener so we can bind it to a thread
      RunnableMarketDataListener listener = new RunnableMarketDataListener() {
        @Override
        public void handleEvent(MarketDataEvent event) {
          // Perform very basic reporting to illustrate different threads
          String data = new String(event.getRawData());
          log.debug("Event data: {}", data);
          ta.append("Received: " + data + "\n");
          ta.setCaretPosition(ta.getDocument().getLength());
        }

      };
      streamingMarketDataService.start(listener);

      // Start a new thread for the listener
      executorService = Executors.newSingleThreadExecutor();
      executorService.submit(listener);
    }

    // Handle application shutdown gracefully
    if (e.getSource() == close) {
      // Stop the streaming market data service
      if (streamingMarketDataService != null) {
        streamingMarketDataService.stop();
      }
      // Stop our listener
      if (executorService != null) {
        executorService.shutdownNow();
      }
    }

  }
}