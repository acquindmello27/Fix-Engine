import java.util.ArrayList;
import java.util.Date;

import quickfix.ApplicationAdapter;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.UnsupportedMessageType;
import quickfix.FieldNotFound;
import quickfix.FileStoreFactory;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.field.ClOrdID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.field.ExecType;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;


public class Initiator extends ApplicationAdapter {

    private SocketInitiator socketInitiator;

    public static void main(String[] args) throws ConfigError {

        Initiator fixInitiator = new Initiator();
        SessionSettings sessionSettings = new SessionSettings("C:/Users/dmelloac/IdeaProjects/untitled/src/initiatorSettings.txt");
        ApplicationAdapter application = new Initiator();
        FileStoreFactory fileStoreFactory = new FileStoreFactory(sessionSettings);
        ScreenLogFactory screenLogFactory = new ScreenLogFactory(sessionSettings);
        DefaultMessageFactory defaultMessageFactory = new DefaultMessageFactory();
        fixInitiator.socketInitiator = new SocketInitiator(application,fileStoreFactory, sessionSettings, screenLogFactory,defaultMessageFactory);
        fixInitiator.socketInitiator.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        ArrayList<SessionID> sessions = fixInitiator.socketInitiator.getSessions();

        NewOrderSingle order = new NewOrderSingle(new ClOrdID("Client1_121231"),
                new HandlInst(HandlInst.MANUAL_ORDER),
                new Symbol("IBM"),
                new Side(Side.BUY),
                new TransactTime(new Date()),
                new OrdType(OrdType.MARKET));

        order.set(new OrderQty(4500));
        order.set(new Price(200.9d));
        SessionID sessionID = sessions.get(0);
        System.out.println("Sending order to Exchange...");
        try {
            Session.sendToTarget(order, sessionID);
        } catch (SessionNotFound e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        System.out.println("Closing Client...");
        fixInitiator.socketInitiator.stop();
    }

    @Override
    public void onLogon(SessionID sessionId) {
        super.onLogon(sessionId);
        System.out.println("onLogon in Client...");
    }

    //outbound admin events like Logon, Logoff messages go through fromAdmin.
    @Override
    public void fromAdmin(quickfix.Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
            RejectLogon {
        super.fromAdmin(message, sessionId);
        System.out.println("fromAdmin in Client");
    }

    @Override
    public void onCreate(SessionID sessionId) {
        super.onCreate(sessionId);
        System.out.println("onCreate in Client");
    }


    //Most of the outGoing messages like Orders, Execution Reports etc go through fromApp.
    @Override
    public void fromApp(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

        if (message instanceof ExecutionReport) {
            ExecutionReport executionReport = (ExecutionReport) message;
            try {
                ExecType executionType = (ExecType) executionReport.getExecType();
                System.out.println("Execution Type: "+executionType);
                System.out.println("Order Executed! Received report from Exchange. \n");
            } catch (FieldNotFound e) {
                e.printStackTrace();
            }
        }

    }
}
