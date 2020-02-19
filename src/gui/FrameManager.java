package gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import actionclasses.Loader;
import actionclasses.MailLoader;
import backrgroundhelpers.ProgressBarInNewFrame;
import data.AccountData;
import data.GlobalDataContainer;
import filewriters.XMLFileManager;
import gui.mainframe.MainFrame;
import gui.newaccountdialog.NewAccountDialog;

public class FrameManager
{
    static
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy/hh-mm-ss");
        System.setProperty("currentDate", dateFormat.format(new Date()));
    }

    public static MainFrame mainFrame;

    public static NewAccountDialog popUP;

    public static boolean debug = false;

    public static final Logger LOGGER = LogManager.getLogger(FrameManager.class);

    private final static Properties LANGUAGE_PROPERTIES = new Properties();

    private final static Properties PROGRAM_SETTINGS = new Properties();

    public static String getLanguageProperty(String propertyName)
    {
        return LANGUAGE_PROPERTIES.getProperty(propertyName);
    }

    public static String getProgramSetting(String propertyName)
    {
        return PROGRAM_SETTINGS.getProperty(propertyName);
    }

    public void showPopUP()
    {
        popUP = new NewAccountDialog();
        popUP.showFrame();
    }

    private static void configureTheame()
    {
        // read theme settings and set selected theme
        String theme = new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings")).getLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(theme);
            LOGGER.info(theme + " look and feel set");
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException
            | UnsupportedLookAndFeelException e1)
        {
            e1.printStackTrace();
            LOGGER.error("while setting " + theme + " look and feel: " + e1.toString());
        }
    }

    public static void loadAccounts()
    {
        if (!debug)
        {
            LOGGER.info("debug modus off");
            ProgressBarInNewFrame progressTermitated = new ProgressBarInNewFrame(new Loader(mainFrame), true);
            Thread t = new Thread(progressTermitated);
            t.start();
            try
            {
                t.join();
            }
            catch (InterruptedException e1)
            {
                e1.printStackTrace();
                LOGGER.error("while joing thread: " + e1.toString());
            }
        }
        else
        {
            LOGGER.info("debug modus on");
            new Loader(mainFrame).action(new JProgressBar(), new JLabel());
        }

    }

    public static void main(String[] args)
    {
        String str = "five";

        try (InputStream input = new FileInputStream("src/en.properties"))
        {
            LANGUAGE_PROPERTIES.load(input);

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        try (InputStream input = new FileInputStream("src/settings.properties"))
        {
            PROGRAM_SETTINGS.load(input);

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        LOGGER.info("programm start");
        configureTheame();
        mainFrame = new MainFrame();
        loadAccounts();
        LOGGER.info("strat displaying main frame");
        // show main frame
        mainFrame.pack();
        mainFrame.setVisible(true);
        // run background threads to check for new mail for all accounts
        for (AccountData data : GlobalDataContainer.getAccounts())
        {
            // new LoggerConfigurator().setUpLoggerForUser(data.getUserName());
            if (data.isRunInBackground())
            {
                if (GlobalDataContainer.getConnectionByAccount(data.getUserName()) != null)
                {
                    LOGGER.info("starting background thread for " + data.getUserName());
                    MailLoader thread = new MailLoader(GlobalDataContainer.getConnectionByAccount(data.getUserName()), data, data.getImapServer() == null ? "pop"
                                                                                                                                                          : "imap");
                    thread.runAsThread();
                    GlobalDataContainer.threads.put(data.getUserName(), thread);
                    LOGGER.info("background thread started");
                }
            }
        }
        // after main frame is closed
        mainFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                LOGGER.info("closing program");
                e.getWindow().dispose();
                LOGGER.info("window disposed");
                if (!GlobalDataContainer.getAccounts().isEmpty())
                {
                    // wait until last mail update circle ends and close threads
                    for (AccountData account : GlobalDataContainer.getAccounts())
                    {
                        // close all connections for account
                        if (account.isRunInBackground() && GlobalDataContainer.threads.get(account.getUserName()) != null)
                        {
                            LOGGER.info("closing thread for " + account.getUserName());
                            GlobalDataContainer.threads.get(account.getUserName()).join();
                            LOGGER.info("thread closed");
                        }

                        // rewrite account data to save changed data
                        if (GlobalDataContainer.getConnectionByAccount(account.getUserName()) != null)
                        {
                            LOGGER.info("closing connections for " + account.getUserName());
                            GlobalDataContainer.getConnectionByAccount(account.getUserName()).closeAllSessions();
                            LOGGER.info("connections closed");
                        }
                        LOGGER.info("rewriting xml");
                        /*
                         * new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings"))
                         * .rewriteAccount(account);
                         */
                        try
                        {
                            account.serialize();
                        }
                        catch (IOException e1)
                        {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        LOGGER.info("xml rewrote");
                    }
                }
                System.exit(0);
            }
        });
    }
}
