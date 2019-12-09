package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class Client implements Runnable {
	
	
	/* author: ian maloney & akash chinnasamy */
	
	
	/* The Socket of the Client */
    private final int CELL_SIZE = 100;
	private Socket clientSocket;
	private BufferedReader serverToClientReader;
	private PrintWriter clientToServerWriter;
	public String name;
	public ObservableList<String> chatLog;
    public ObservableList<Label> hostList;
    public String[] opcode;
    public boolean waiting = false;
    public RunningGame game;
    

	public Client(String hostName, int portNumber, String name) throws UnknownHostException, IOException {
		
			/* Try to establish a connection to the server */
			clientSocket = new Socket(hostName, portNumber);
			/* Instantiate writers and readers to the socket */
			serverToClientReader = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			clientToServerWriter = new PrintWriter(
					clientSocket.getOutputStream(), true);
			chatLog = FXCollections.observableArrayList();
                        hostList = FXCollections.observableArrayList();
			/* Send name data to the server */
			this.name = name;
			clientToServerWriter.println(name);

		
	}

	public void writeToServer(String oc, String dest, String input) {
		clientToServerWriter.println(oc +";"+ dest +";"+ input);
                System.out.println("We're in client");
	}

	public void run() {
		/* Infinite loop to update the chat log from the server */
		while (true) {
			try {
                            
                                String inputFromServer = serverToClientReader.readLine();
				Platform.runLater(new Runnable() {
					public void run() {
                                            
                                            messageDecoder(inputFromServer);
                                            
                                            //Case if opcode for CHAT
                                            if(opcode[0].equals("c")){
						chatLog.add(opcode[2]);
                                            }
                                            
                                            //Case if opcode for HOST
                                            else if(opcode[0].equals("h")){
                                                hostList.add(eventLabel(opcode[1], opcode[2]));
                                            }
                                            
                                            //Case if opcode is to join
                                            else if (opcode[0].equals("j")){
                                                
                                                //Code to remove hosted game from list when joined
                                                boolean t = true;
                                                int i = 0;
                                                while(t == true){
                                                    if(hostList.get(i).getText().equals("Hosting: " + opcode[1])){
                                                        hostList.remove(i);
                                                        t=false;
                                                    }
                                                    else i++;
                                                }
                                                
                                                //Initiate host
                                                if (opcode[1].equals(name)){
                                                    waiting = false;
                                                    game = new RunningGame(name, name, opcode[2]);
                                                    game.setAlignment(Pos.CENTER);
                                                    Stage gameStage = new Stage();
                                                    gameStage.setScene(new Scene(game, CELL_SIZE*7, CELL_SIZE*7.5 ));
                                                    gameStage.setTitle(opcode[1] +" vs. "+ opcode[2]);
                                                    gameStage.setResizable(false);
                                                    gameStage.show();
                                                }
                                                
                                                //Initiate guest
                                                else if(opcode[2].equals(name)){
                                                    waiting = false;
                                                    game = new RunningGame(name, opcode[1], name);
                                                    game.setAlignment(Pos.CENTER);
                                                    Stage gameStage = new Stage();
                                                    gameStage.setScene(new Scene(game, CELL_SIZE*7, CELL_SIZE*7.5 ));
                                                    gameStage.setTitle(opcode[1] +" vs. "+ opcode[2]);
                                                    gameStage.setResizable(false);
                                                    gameStage.show();
                                                }
                                            }
                                            
                                            
					}
				});

			} catch (SocketException e) {
				Platform.runLater(new Runnable() {
					public void run() {
						chatLog.add("Error in server");
					}

				});
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
        
        //Break down messages based on game protocol
        public void messageDecoder(String msg){
            
            opcode = new String[]{"", "", ""};
            
            char[] code = msg.toCharArray();
            int arrInd = 0;
            boolean[] checks = new boolean[]{true, true};
            
            int ocInd = 0;
            while(checks[0]){
                if(code[arrInd] != ';'){
                    opcode[ocInd] += code[arrInd];
                }
                else{
                    checks[0] = false;
                }
                arrInd++;
            }
            
            ocInd++;
            while(checks[1]){
                if(code[arrInd] != ';'){
                    opcode[ocInd] += code[arrInd];
                }
                else{
                    checks[1] = false;
                }
                arrInd++;
            }
            
            ocInd++;
            for(; arrInd < msg.length(); arrInd++){
                opcode[ocInd] += code[arrInd];
            }
            
            System.out.println("opcode[0] = "+opcode[0]);
            System.out.println("opcode[1] = "+opcode[1]);
            System.out.println("opcode[2] = "+opcode[2]);
        }
        
        //Returns Label for HostGame with event handler
        public Label eventLabel(String n, String gn){
            Label l = new Label("Hosting: " + n);
            
            l.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if(!name.equals(n)){
                            Alert join = new Alert(Alert.AlertType.ERROR, "", 
                            ButtonType.YES, ButtonType.NO);
                            join.setGraphic(null);
                            join.setTitle("Join game?");
                            join.setHeaderText("Join " +n+ "'s game?");
                            join.showAndWait();

                            if(join.getResult() == ButtonType.NO){
                                join.close();
                            }
                            else if(join.getResult() == ButtonType.YES){
                                writeToServer("j", n, name);
                                join.close();
                            }
                        }
                    }
                        
                });
            
            
            
            return l;
        }
}