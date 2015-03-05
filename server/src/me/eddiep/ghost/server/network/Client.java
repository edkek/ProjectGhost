package me.eddiep.ghost.server.network;

import me.eddiep.ghost.server.TcpUdpServer;
import me.eddiep.ghost.server.game.Player;
import me.eddiep.ghost.server.network.packet.Packet;
import me.eddiep.ghost.server.network.packet.impl.OkPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Client {
    private Player player;
    private InetAddress IpAddress;
    private int port;
    private TcpUdpServer socketServer;
    private boolean connected = true;
    private Thread writerThread;
    private Thread readerThread;
    private Socket socket;
    private int lastReadPacket = 0;

    private OutputStream writer;
    private InputStream reader;

    protected List<byte[]> tcp_packet_queue = Collections.synchronizedList(new LinkedList<byte[]>());
    private int lastWritePacket;

    public Client(Player player, Socket socket, TcpUdpServer server) throws IOException {
        this.player = player;
        this.socket = socket;
        this.IpAddress = socket.getInetAddress();
        this.socketServer = server;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();

        this.player.setClient(this);
    }

    public void listen() {
        if (reader == null)
            return;

        writerThread = new Writer();
        writerThread.start();
        readerThread = new Reader();
        readerThread.start();
    }

    public InetAddress getIpAddress() {
        if (socket != null && IpAddress == null)
            return socket.getInetAddress();
        return IpAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.IpAddress = ipAddress;
    }

    public boolean isLoggedIn() {
        return port != 0 && IpAddress != null;
    }

    public Player getPlayer() {
        return player;
    }

    public void disconnect() throws IOException {
        connected = false;
        if (writerThread != null) {
            writerThread.interrupt();
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (readerThread != null) {
            readerThread.interrupt();
        }

        readerThread = null;
        writerThread = null;

        if (player != null) {
            if (player.isInQueue()) {
                player.getQueue().removeUserFromQueue(player);
            }
        }
        player = null;
        if (socket != null && !socket.isClosed())
            socket.close();
        socket = null;
    }

    public Socket getSocket() {
        return socket;
    }

    public Client sendOk() throws IOException {
        return sendOk(true);
    }

    public Client sendOk(boolean value) throws IOException {
        OkPacket packet = new OkPacket(this);
        packet.writePacket(value);
        return this;
    }

    protected boolean sendTCPNextPacket() throws IOException {
        if (tcp_packet_queue.isEmpty())
            return false;
        byte[] packet = tcp_packet_queue.remove(0);
        if (packet == null)
            return false;
        writer.write(packet);
        return true;
    }

    public void processUdpPacket(DatagramPacket recievePacket) throws IOException {
        byte[] rawData = recievePacket.getData();
        byte opCode = rawData[0];
        byte[] data = new byte[recievePacket.getLength() - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        Packet packet = Packet.get(opCode, this, data);

        packet.handlePacket();
    }

    public int getLastReadPacket() {
        return lastReadPacket;
    }

    public void setLastReadPacket(int number) {
        this.lastReadPacket = number;
    }

    public InputStream getInputStream() {
        return reader;
    }

    public OutputStream getOutputStream() {
        return writer;
    }

    public TcpUdpServer getServer() {
        return socketServer;
    }

    public int getLastWritePacket() {
        return lastWritePacket;
    }

    public void setLastWritePacket(int lastWritePacket) {
        this.lastWritePacket = lastWritePacket;
    }

    private class Writer extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("Client-" + getPlayer().getSession() + "-Writer");
            while (socketServer.isRunning() && connected) {
                try {
                    while (sendTCPNextPacket());

                    Thread.sleep(2);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException ignored) {
                }
            }

            try {
                while (sendTCPNextPacket()); //Be sure all packets get sent
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Reader extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("Client-" + getPlayer().getSession() + "-Reader");
            try {
                while (socketServer.isRunning() && connected) {
                    int readValue = reader.read();

                    if (readValue == -1) {
                        disconnect();
                        return;
                    }

                    byte opCode = (byte) readValue;
                    Packet.get(opCode, Client.this).handlePacket().endTCP();

                }
            } catch (SocketException e) {
                if (!e.getMessage().contains("Connection reset")) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketServer.disconnect(Client.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
