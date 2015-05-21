package me.eddiep.ghost.central;

import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.dataserv.ClientFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TcpServer extends Server {
    private static final int PORT = 2546;

    private ServerSocket tcpServerSocket;
    private Thread tcpThread;

    private List<Client> connectedClients = new ArrayList<>();

    private final List<Runnable> toTick = Collections.synchronizedList(new LinkedList<Runnable>());
    private List<Runnable> tempTick = new LinkedList<>();
    private boolean ticking = false;

    @Override
    public boolean requiresTick() {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        setTickRate(16);
        setTickNanos(666667);

        try {
            tcpServerSocket = new ServerSocket(PORT + 1);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tcpThread = new Thread(TCP_SERVER_RUNNABLE);
        tcpThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        tcpThread.interrupt();
    }

    public void executeNextTick(Runnable runnable) {
        if (!ticking) {
            toTick.add(runnable);
        } else {
            tempTick.add(runnable);
        }
    }

    @Override
    protected void onTick() {
        synchronized (toTick) {
            Iterator<Runnable> runnableIterator = toTick.iterator();

            ticking = true;
            while (runnableIterator.hasNext()) {
                runnableIterator.next().run();
                runnableIterator.remove();
            }
            ticking = false;
        }
        toTick.addAll(tempTick);
        tempTick.clear();
    }

    public void disconnect(Client client) throws IOException {
        System.out.println("[SERVER] " + client.getIpAddress() + " disconnected..");
        connectedClients.remove(client);

        client.disconnect();
    }

    public void sendUdpPacket(DatagramPacket packet) throws IOException {
        throw new IOException("This server cannot send UDP Packets!");
    }

    private void validateTcpSession(Socket connection) throws IOException {
        DataInputStream reader = new DataInputStream(connection.getInputStream());
        byte firstByte = (byte)reader.read();
        if (firstByte != 0x00)
            return;
        byte[] sessionBytes = new byte[36];
        int read = reader.read(sessionBytes, 0, sessionBytes.length);
        if (read == -1)
            return;
        String session = new String(sessionBytes, 0, read, Charset.forName("ASCII"));

        Client client = ClientFactory.attemptLogin(session, connection, this);
        if (client == null)
            return;

        client.listen();
        client.sendOk();
        connectedClients.add(client);
        log("TCP connection made with client " + connection.getInetAddress().toString() + " using session " + session);
    }

    private final Runnable TCP_SERVER_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("TCP Server Listener");
            Socket connection = null;
            while (isRunning()) {
                try {
                    connection = tcpServerSocket.accept();

                    if (connection == null)
                        continue;
                    if (!isRunning())
                        break;

                    connection.setSoTimeout(300000);
                    log("Client connected " + connection.getInetAddress().toString());
                    new AcceptThread(connection).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class AcceptThread extends Thread {
        private Socket connection;
        public AcceptThread(Socket connection) { this.connection = connection; }

        @Override
        public void run() {
            try {
                validateTcpSession(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
