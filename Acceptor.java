import java.io.File;

import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.FileStoreFactory;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.Headline;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.News;

public class Acceptor extends MessageCracker implements Application {

    public static void main(String[] args) {
        try {
            SessionSettings settings = new SessionSettings("C:/Users/dmelloac/IdeaProjects/untitled/src/acceptorSettings.txt");
            Acceptor acceptor = new Acceptor();
            ScreenLogFactory screenLogFactory = new ScreenLogFactory(settings);
            DefaultMessageFactory messageFactory = new DefaultMessageFactory();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(settings);
            SocketAcceptor socketAcceptor = new SocketAcceptor(acceptor,fileStoreFactory, settings, screenLogFactory,messageFactory);
            socketAcceptor.start();
            System.out.println("Fix Server Started. It can accept orders now..\n Press any key to stop..");
            System.in.read();
            socketAcceptor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("Message received in fromAdmin() " + arg0);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
            UnsupportedMessageType {
        //For Type safety. crack calls onMessage()
        crack(message, sessionID);

    }

    @Override
    public void onCreate(SessionID arg0) {
    }

    @Override
    public void onLogon(SessionID sessionID) {

    }

    @Override
    public void onLogout(SessionID arg0) {

    }

    @Override
    public void toAdmin(Message arg0, SessionID arg1) {
    }

    @Override
    public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
    }

    //@Override
    public void onMessage(NewOrderSingle order, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        // sending some news to the client.
        System.out.println("onMessage on Server.");
        try {
            Session.sendToTarget(new News(new Headline("\nOrder Received on Exchange. Processing....")), sessionID);
        } catch (SessionNotFound e) {
            e.printStackTrace();
        }
        Symbol symbol = new Symbol();
        Side side = new Side();
        OrdType ordType = new OrdType();
        OrderQty orderQty = new OrderQty();
        Price price = new Price();
        ClOrdID clOrdID = new ClOrdID();

        //Printing order details.
        System.err.print("Symbol -"+order.get(symbol)+" || Side -"+order.get(side)+" || Quantity - "+order.get(orderQty)+"" +
                " || Price - "+order.get(price));

        order.get(symbol);
        order.get(side);
        order.get(orderQty);
        order.get(price);
        order.get(clOrdID);
        order.get(ordType);
        //By default we accept order. So no processing is involved.

        ExecutionReport executionReport = new ExecutionReport(
                getOrderIDCounter(), getExecutionIDCounter(),
                new ExecTransType(ExecTransType.NEW), new ExecType(
                ExecType.FILL), new OrdStatus(OrdStatus.FILLED),
                symbol, side, new LeavesQty(0),
                new CumQty(orderQty.getValue()), new AvgPx(price.getValue()));
        executionReport.set(clOrdID);
        executionReport.set(orderQty);
        try {
            //Sending Execution report to client.
            Session.sendToTarget(executionReport, sessionID);
            System.out.println("Order Execution completed.");
        } catch (SessionNotFound ex) {
            ex.printStackTrace();
            System.out.println("Error!!" + ex.getMessage());
        }
    }

    private int orderIDCounter;
    private int executionIDCounter;

    public OrderID getOrderIDCounter() {
        orderIDCounter++;
        return new OrderID(String.valueOf(orderIDCounter));
    }

    public ExecID getExecutionIDCounter() {
        executionIDCounter++;
        return new ExecID(String.valueOf(executionIDCounter));
    }
}
