package com.murzik.keyboardrace.server;

import java.io.*;
import java.net.Socket;
import java.util.*;

class ClientServerCommunication extends Thread {
    private static int THREAD_ID = 0;
    private final HashMap<Socket, ClientInfo> info = new HashMap<>();
    private int numClients = 0;
    private final HashSet<Socket> sockets = new HashSet<>();
    private boolean isGameRunning = false; //TODO: maybe atomic, but for what???
    private final long startingTime = System.currentTimeMillis();
    private String text = " ";
    private final Random random = new Random();

    private final Comparator<ClientInfo> comparator = (c1, c2) -> {
        if (c1.correctCharactersNum != c2.correctCharactersNum)
            return c2.correctCharactersNum - c1.correctCharactersNum;
        return c1.mistakesNum - c2.mistakesNum;
    };


    int getNumClients() {
        return numClients;
    }

    boolean getIsGameRunning() {
        return isGameRunning;
    }

    ClientServerCommunication(Socket socket) throws IOException {
        super("ClientServerThread: " + (++THREAD_ID));
        System.out.println(Thread.currentThread().getName() + ": " + getClass().getSimpleName() + ".ClientServerCommunication(" + socket + ") invoked.");
        addClient(socket);
    }

    void addClient(Socket socket) throws IOException { //TODO: synchronized
        this.sockets.add(socket);
        ++numClients;

        BufferedWriter printStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()), 20240);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()), 20240);
        String name = bufferedReader.readLine();

        printStream.write(startingTime + "\n");
        printStream.flush();

        info.put(socket, new ClientInfo(name, printStream, bufferedReader));
        System.out.println("Client " + name + " connect to thread " + THREAD_ID);
    }


    public void run() {
        System.out.println(Thread.currentThread().getName() + ": " + getClass().getSimpleName() + ".run() invoked.");

        while (!isInterrupted()) {

            int numDisconnect = 0;
            try {
                HashSet<Socket> nowSockets = new HashSet<>(sockets);

                for (var client : nowSockets) {
                    ClientInfo clientInfo = info.get(client);
                    if (clientInfo.gamerIsOut) {
                        ++numDisconnect;
                        continue;
                    }

                    String request = clientInfo.bufferedReader.readLine();

                    while (request.equals(" ")) request = clientInfo.bufferedReader.readLine();

                    if (request.equals("sendText")) {
                        inputProcessing(clientInfo);

                    }

                    if (request.equals("needText")) {
                        sendText(client);
                        request = clientInfo.bufferedReader.readLine();
                        isGameRunning = true;
                    }

                    List<ClientInfo> sortsClients = new ArrayList<>();
                    for (var client2 : nowSockets) {
                        sortsClients.add(info.get(client2));
                    }

                    if (request.equals("end")) {
                        StringBuilder infoBar = new StringBuilder();
                        int ind = 1;

                        var winner = sortsClients.get(0);

                        for (var clientInfo2 : sortsClients) {
                            infoBar.append(getClientInfo(ind, clientInfo2));

                            if (clientInfo2.name.equals(clientInfo.name)) {
                                infoBar.append("(это Вы)");
                            }
                            if (winner.mistakesNum == clientInfo2.mistakesNum && winner.correctCharactersNum == clientInfo2.correctCharactersNum) {
                                infoBar.append(" Победитель заезда!!!! ");
                            }

                            infoBar.append(";");
                            ++ind;
                        }
                        info.get(client).bufferedWriter.write(infoBar + "\n");
                        //System.out.println(clientInfo.name + " writes " + infoBar.append("\n"));
                        info.get(client).bufferedWriter.flush();
                    }


                    while (request.equals(" ")) request = clientInfo.bufferedReader.readLine();


                    if (request.equals("ok")) {
                        StringBuilder infoBar = new StringBuilder();
                        int ind = 1;

                        sortsClients.sort(comparator);

                        for (var clientInfo2 : sortsClients) {
                            infoBar.append(getClientInfo(ind, clientInfo2));

                            if (clientInfo2.name.equals(clientInfo.name)) {
                                infoBar.append("(это Вы)");
                            }

                            infoBar.append(";");
                            ++ind;
                        }


                        info.get(client).bufferedWriter.write(System.currentTimeMillis() + "\n" + infoBar.append("\n"));
                        //System.out.println(clientInfo.name + " writes " + infoBar.append("\n"));
                        info.get(client).bufferedWriter.flush();
                    }

                    if (request.equals("exit")) {
                        clientInfo.disconnectClient();
                        client.close();
                        System.out.println("Client " + clientInfo + " is out");
                    }
                }

            } catch (IOException e) {
                for (var s : sockets) {
                    info.get(s).disconnectClient();
                    try {
                        s.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            if (numDisconnect == sockets.size()) break;
        }

        try {
            for (var client : sockets) {
                info.get(client).disconnectClient();

                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println("FINISH");
    }

    private StringBuilder getClientInfo(int ind, ClientInfo clientInfo) {
        StringBuilder infoLine = new StringBuilder();
        infoLine.append(ind).append(". ").append(clientInfo.name).append(" ").append(clientInfo.correctCharactersNum * 100 / text.length()).append("%, ").append(clientInfo.mistakesNum).append(" ошибки, ").append(clientInfo.charactersNum * 60000L / (System.currentTimeMillis() - startingTime)).append(" сим/мин ");
        if (clientInfo.gamerIsOut) {
            infoLine.append("(игрок отсоединился)");
        }
        return infoLine;
    }

    void sendText(Socket socket) throws IOException {
        if (text.equals(" ")) {
            String textName = "Text" + (random.nextInt(5) + 1) + ".txt";
            System.out.println(textName);
//            try (InputStream is = getClass().getClassLoader().getResourceAsStream("com/murzik/keyboardrace/Texts/" + textName); BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
//                text = br.readLine() + "\n";
//            } catch (IOException ex) {
//                System.out.println(ex.getMessage());
//            }
        }
        text = "О прошлом Янусов мы достоверно знали только тот факт, что родился Я. П. Невструев седьмого марта тысяча восемьсот сорок первого года. Каким образом и когда Я. П. Невструев стал директором института, нам было совершенно неизвестно. Мы не знали даже, кто первый догадался и проговорился о том, что У-Янус и А-Янус -- один человек в двух лицах. Я узнал об этом у Ойры-Ойры и поверил, потому что понять не мог. Ойра-Ойра узнал от Жиакомо и тоже поверил, потому что был молод и восхищен.\n";
        info.get(socket).errorsUsed = new char[text.length()];
        info.get(socket).bufferedWriter.write(text);
        System.out.println(text);
        info.get(socket).bufferedWriter.flush();

    }

    void inputProcessing(ClientInfo clientInfo) throws IOException {
        String clientText = clientInfo.bufferedReader.readLine();

        int n = Math.min(clientText.length(), text.length());
        int numMist = 0;
        int numCorr = 0;
        for (int i = 0; i < n; ++i) {
            if (clientText.charAt(i) != text.charAt(i)) {
                ++numMist;
                if (clientInfo.errorsUsed[i] != '1') {
                    ++clientInfo.mistakesNum;
                    clientInfo.errorsUsed[i] = '1';
                }
            } else ++numCorr;
        }
        clientInfo.charactersNum = clientText.length();

        clientInfo.correctCharactersNum = numCorr;

        if (numMist != 0) clientInfo.bufferedWriter.write("incorrect\n");
        else clientInfo.bufferedWriter.write("correct\n");
        clientInfo.bufferedWriter.flush();
    }

    private static class ClientInfo {
        String name;
        int mistakesNum;
        int charactersNum;
        int correctCharactersNum;
        char[] errorsUsed = null;
        BufferedWriter bufferedWriter;
        BufferedReader bufferedReader;
        boolean gamerIsOut = false;//TODO

        ClientInfo(String name, BufferedWriter printStream, BufferedReader bufferedReader) {
            this.name = name;
            this.bufferedWriter = printStream;
            this.bufferedReader = bufferedReader;

            mistakesNum = 0;
            charactersNum = 0;
            correctCharactersNum = 0;
        }

        void disconnectClient() {
            try {
                gamerIsOut = true;
                bufferedWriter.close();
                bufferedReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(name + " disconnect");
        }
    }
}


