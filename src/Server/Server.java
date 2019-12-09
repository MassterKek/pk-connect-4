package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Client.ChatHandler;

public class Server implements Runnable {
	private int portNumber;
	private ServerSocket socket;
	private ArrayList<Socket> clients;
	private ArrayList<ClientThread> clientThreads;
	public ObservableList<String> serverLog;
	public ObservableList<String> clientNames;
        private int gameInd = 0;
        
        Socket socke = null;
        
        /* String array containing message formatted for game protocol */
        public String[] opcode = new String[3];
        
	public Server(int portNumber) throws IOException {
		this.portNumber = portNumber;
		serverLog = FXCollections.observableArrayList();
		clientNames = FXCollections.observableArrayList();
		clients = new ArrayList<Socket>();
		clientThreads = new ArrayList<ClientThread>();
		socket = new ServerSocket(portNumber);
		
	}

	public void startServer() {

		try {
			socket = new ServerSocket(this.portNumber); /*
														 * Instantiates the
														 * server socket so
														 * clients can connect
														 */
			serverLog = FXCollections.observableArrayList();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {

		try {
			/* Infinite loop to accept any incoming connection requests */
			while (true) {
				/* Add to log that the server's listening */

				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						serverLog.add("Listening for client");

					}
				});

				final Socket clientSocket = socket.accept();
				

				/* Add the incoming socket connection to the list of clients */
				clients.add(clientSocket);
				/* Add to log that a client connected */
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						serverLog.add("Client "
								+ clientSocket.getRemoteSocketAddress()
								+ " connected");
					}
				});
				ClientThread clientThreadHolderClass = new ClientThread(
						clientSocket, this);
				Thread clientThread = new Thread(clientThreadHolderClass);
				clientThreads.add(clientThreadHolderClass);
				clientThread.setDaemon(true);
				clientThread.start();
				
				ClientThread clientVThreadHolderClass = new ClientThread(
						clientSocket, this);
				Thread clientVThread = new Thread(clientVThreadHolderClass);
				ChatHandler handler = new ChatHandler(socket.accept());
				clientVThread.setDaemon(true);
				clientVThread.start();
                handler.start();
                
                
                ServerApplication.threads.add(clientVThread);
				ServerApplication.threads.add(clientThread);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void clientDisconnected(ClientThread client) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				serverLog.add("Client "
						+ client.getClientSocket().getRemoteSocketAddress()
						+ " disconnected");
				clients.remove(clientThreads.indexOf(client));
				clientNames.remove(clientThreads.indexOf(client));
				clientThreads.remove(clientThreads.indexOf(client));
			}
		});
		
		
	}
        
      
	public void writeToAllSockets(String input) {
                
            for (ClientThread clientThread : clientThreads) {
		clientThread.writeToServer(input);
            }
	}

}