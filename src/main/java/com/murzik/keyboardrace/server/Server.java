package com.murzik.keyboardrace.server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    private AtomicBoolean isServerRunning;
    private ServerSocket serverSocket;
    private ServerActivity serverActivity;

    private Server(int serverPort) {
        System.out.println(Thread.currentThread().getName() + ": ConsoleEchoServer(" + serverPort + ") invoked.");
        try {
            serverSocket = new ServerSocket(serverPort);
            isServerRunning = new AtomicBoolean(true);
            serverActivity = new ServerActivity(serverSocket, isServerRunning);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + ": ConsoleEchoServer(" + serverPort + ") performed.");
    }

    private void execute() throws Exception {
        System.out.println(Thread.currentThread().getName() + ": Server.execute() invoked.");
        serverActivity.start();
        // read from console hit...
        Scanner scanner = new Scanner(System.in);
        String s;
        while (true) {
            if ((s = scanner.nextLine()).equalsIgnoreCase("exit")) break;
        }
        shutdownServer();
        System.out.println(Thread.currentThread().getName() + "; Server.execute() performed.");
    }

    private void shutdownServer() throws IOException {
        System.out.println(Thread.currentThread().getName() + ": shutdownServer() invoked.");
        isServerRunning.set(false);
        serverActivity.interrupt(); // question: will it interrupt accept()-method ?
        serverSocket.close();
    }

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + ": Server.main() invoked.");
        int serverPort = 5619;
        try {
            new Server(serverPort).execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Could not create server.");
            System.exit(1);
        }
        System.out.println(Thread.currentThread().getName() + ": Server.main() finished.");
    }
}


class ServerActivity extends Thread {

    private final ServerSocket serverSocket;
    private final AtomicBoolean isServerRunning;

    private final HashSet<ClientServerCommunication> games = new HashSet<>();

    ServerActivity(ServerSocket serverSocket, AtomicBoolean isServerRunning) {
        super("ServerActivity");
        this.isServerRunning = isServerRunning;
        this.serverSocket = serverSocket;

    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + ": " + getClass().getName() + ".run() invoked.");
        while (isServerRunning.get()) {
            try {
                System.out.println(Thread.currentThread().getName() + ": calling accept()...");
                Socket socket = serverSocket.accept();
                System.out.println(Thread.currentThread().getName() + ": accept() performed...");
                boolean connect = false;

                for (var g : games) {
                    if (g.getNumClients() < 3 && !g.getIsGameRunning()) {
                        connect = true;
                        g.addClient(socket);
                        break;
                    }
                }
                if (!connect) {
                    ClientServerCommunication game = new ClientServerCommunication(socket);
                    games.add(game);
                    game.start();
                }
            } catch (IOException ioex) {
                System.out.println("got exception: " + ioex);
            }
        }
        System.out.println(Thread.currentThread().getName() + ": server activity finished...");
    }
}

