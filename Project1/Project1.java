import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class Project1 implements Serializable  {
	static String outputFileName;
	static final int white=0,red=1;
	int nodeID;
	int numOfNodes,minPerActive,maxPerActive,minSendDelay,snapshotDelay,maxNumber;
	int totalMessagesSent = 0;
	boolean active=false;
	int[][] adjMatrix;
	int[] vectorClock;
	int[] neighbors;
	int myColor=white;
	int logging=0;
	boolean firstTime = true;
	ArrayList<Node> nodesOnNtwrk = new ArrayList<Node>();
	TreeMap<Integer,Node> store = new TreeMap<>();
	TreeMap<Integer,Socket> openChnlsOnCrntNode = new TreeMap<>();
	TreeMap<Integer,ObjectOutputStream> openStrmsOnCrntNode = new TreeMap<>();
	TreeMap<Integer,ArrayList<ApplicationMessage>> channelStates;
	TreeMap<Integer,Boolean> mrkrMsgRcvd;
	TreeMap<Integer,StateMessage> allStateMessages;	
	boolean[] stateMsgsRcvd;
	StateMessage myState;
	ArrayList<int[]> output = new ArrayList<int[]>();
	public void readConfigFile(String name) throws IOException{
		int count = 0,flag = 0;
		int curNode = 0;
		String curDir = System.getProperty("user.dir");
		String fileName = curDir+"/"+name;
		String line = null;
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				if(line.length() == 0)
					continue;
				if(!line.startsWith("#")){
					if(line.contains("#")){
						String[] input = line.split("#.*$");
						String[] input1 = input[0].split("\\s+");
						if(flag == 0 && input1.length == 6){
							numOfNodes = Integer.parseInt(input1[0]);
							minPerActive = Integer.parseInt(input1[1]);
							maxPerActive = Integer.parseInt(input1[2]);
							minSendDelay = Integer.parseInt(input1[3]);
							snapshotDelay = Integer.parseInt(input1[4]);
							maxNumber = Integer.parseInt(input1[5]);
							flag++;
							adjMatrix = new int[numOfNodes][numOfNodes];
						}
						else if(flag == 1 && count < numOfNodes)
						{							
							nodesOnNtwrk.add(new Node(Integer.parseInt(input1[0]),input1[1],Integer.parseInt(input1[2])));
							count++;
							if(count == numOfNodes){
								flag = 2;
							}
						}
						else if(flag == 2){
							insertIntoMatrix(input1, curNode);
							curNode++;
						}
					}
					else {
						String[] input = line.split("\\s+");
						if(flag == 0 && input.length == 6){
							numOfNodes = Integer.parseInt(input[0]);
							minPerActive = Integer.parseInt(input[1]);
							maxPerActive = Integer.parseInt(input[2]);
							minSendDelay = Integer.parseInt(input[3]);
							snapshotDelay = Integer.parseInt(input[4]);
							maxNumber = Integer.parseInt(input[5]);
							flag++;
							adjMatrix = new int[numOfNodes][numOfNodes];
						}
						else if(flag == 1 && count < numOfNodes)
						{
							nodesOnNtwrk.add(new Node(Integer.parseInt(input[0]),input[1],Integer.parseInt(input[2])));
							count++;
							if(count == numOfNodes){
								flag = 2;
							}
						}
						else if(flag == 2){
							insertIntoMatrix(input,curNode);
							curNode++;
						}
					}
				}
			}
			bufferedReader.close();  
		}
		catch(FileNotFoundException ex) {
			ex.printStackTrace();                
		}
		catch(IOException ex) {
			ex.printStackTrace();                  
		}
		for(int i=0;i<numOfNodes;i++){
			for(int j=0;j<numOfNodes;j++){
				if(adjMatrix[i][j] == 1){
					adjMatrix[j][i] = 1;
				}
			}
		}
		
	}

	 void insertIntoMatrix(String[] input,int curNode) {
		for(String i:input){
			adjMatrix[curNode][Integer.parseInt(i)] = 1;
		}
	}
	void initialize(Project1 mapProtocol){
		mapProtocol.channelStates = new TreeMap<>();
		mapProtocol.mrkrMsgRcvd = new TreeMap<>();
		mapProtocol.allStateMessages = new TreeMap<>();	

		Set<Integer> keys = mapProtocol.openChnlsOnCrntNode.keySet();
		for(Integer element : keys){
			ArrayList<ApplicationMessage> arrList = new ArrayList<ApplicationMessage>();
			mapProtocol.channelStates.put(element, arrList);
		}
		for(Integer e: mapProtocol.neighbors){
			mapProtocol.mrkrMsgRcvd.put(e,false);
		}
		mapProtocol.stateMsgsRcvd = new boolean[mapProtocol.numOfNodes];
		mapProtocol.myState = new StateMessage();
		mapProtocol.myState.vectorClock= new int[mapProtocol.numOfNodes];
	}


	public static void main(String[] args) throws IOException, InterruptedException {
		Project1 mapProtocol= new Project1(); 
		mapProtocol.readConfigFile(args[0]);
		mapProtocol.nodeID = Integer.parseInt(args[1]);
		int curNode = mapProtocol.nodeID;
		String filename=args[0];
		Project1.outputFileName = filename.substring(0, filename.lastIndexOf('.'));
		ConvergeCast.createSpnningTree(mapProtocol.adjMatrix);
		for(int i=0;i<mapProtocol.nodesOnNtwrk.size();i++){
			mapProtocol.store.put(mapProtocol.nodesOnNtwrk.get(i).nodeId, mapProtocol.nodesOnNtwrk.get(i));
		}
		int serverPort = mapProtocol.nodesOnNtwrk.get(mapProtocol.nodeID).port;
		ServerSocket listener = new ServerSocket(serverPort);
		Thread.sleep(10000);
		for(int i=0;i<mapProtocol.numOfNodes;i++){
			if(mapProtocol.adjMatrix[curNode][i] == 1){
				String hostName = mapProtocol.store.get(i).host;
				int port = mapProtocol.store.get(i).port;
				InetAddress address = InetAddress.getByName(hostName);
				Socket client = new Socket(address,port);
				mapProtocol.openChnlsOnCrntNode.put(i, client);
				ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
				mapProtocol.openStrmsOnCrntNode.put(i, oos);		
			}
		}
		Set<Integer> keys = mapProtocol.openChnlsOnCrntNode.keySet();
		mapProtocol.neighbors = new int[keys.size()];
		int index = 0;
		for(Integer element : keys) mapProtocol.neighbors[index++] = element.intValue();
		mapProtocol.vectorClock = new int[mapProtocol.numOfNodes];

		mapProtocol.initialize(mapProtocol);

		if(curNode == 0){
			mapProtocol.active = true;
			new CLThread(mapProtocol).start();		
			new SendMessagesThread(mapProtocol).start();
		}
		try {
			while (true) {
				Socket socket = listener.accept();
				new ClientThread(socket,mapProtocol).start();
			}
		}
		finally {
			listener.close();
		}
	}


	void emitMessages() throws InterruptedException{
		int numMsgs = 1;
		int minSendDelay = 0;
		synchronized(this){
			numMsgs = this.getRandomNumber(this.minPerActive,this.maxPerActive);
			if(numMsgs == 0){
				numMsgs = this.getRandomNumber(this.minPerActive + 1,this.maxPerActive);
			}
			minSendDelay = this.minSendDelay;
		}
		for(int i=0;i<numMsgs;i++){
			synchronized(this){
				int neighborIndex = this.getRandomNumber(0,this.neighbors.length-1);
				int curNeighbor = this.neighbors[neighborIndex];
				if(this.active == true){
					ApplicationMessage m = new ApplicationMessage(); 
					this.vectorClock[this.nodeID]++;
					m.vectorClock = new int[this.vectorClock.length];
					System.arraycopy( this.vectorClock, 0, m.vectorClock, 0, this.vectorClock.length );
					m.nodeId = this.nodeID;
					try {
						ObjectOutputStream oos = this.openStrmsOnCrntNode.get(curNeighbor);
						oos.writeObject(m);	
						oos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}	
					totalMessagesSent++;
				}
			}
			try {
				Thread.sleep(minSendDelay);
			} catch (InterruptedException e) {
				System.out.println("Error in EmitMessages");
			}
		}
		synchronized(this){
			this.active = false;
		}

	}

	int getRandomNumber(int min,int max){
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
}
class ClientThread extends Thread {
	Socket cSocket;
	Project1 mapProtocol;

	public ClientThread(Socket csocket,Project1 mapProtocol) {
		this.cSocket = csocket;
		this.mapProtocol = mapProtocol;
	}

	public void run() {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(cSocket.getInputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while(true){
			try {
				Message msg;
				msg = (Message) ois.readObject();
				synchronized(mapProtocol){

					if(msg instanceof MarkerMessage && ((MarkerMessage)msg).msgType.compareTo("markerMsg")==0){
						int channelNo = ((MarkerMessage) msg).nodeId;
						ChandyLamport.sendMrkrMsg(mapProtocol, channelNo);
					}	
					else if((mapProtocol.active == false) && msg instanceof ApplicationMessage && ((ApplicationMessage)msg).msgType.compareTo("appMsg")==0 &&
							mapProtocol.totalMessagesSent < mapProtocol.maxNumber && mapProtocol.logging == 0){
						mapProtocol.active = true; 
						new SendMessagesThread(mapProtocol).start();
					}
					else if((mapProtocol.active == false) && (msg instanceof ApplicationMessage) && (mapProtocol.logging == 1)&& ((ApplicationMessage)msg).msgType.compareTo("appMsg")==0){
						int channelNo = ((ApplicationMessage) msg).nodeId;
						ChandyLamport.logMsg(channelNo,((ApplicationMessage) msg) ,mapProtocol);
					}
					else if(msg instanceof StateMessage && ((StateMessage)msg).msgType.compareTo("stateMsg")==0){
						if(mapProtocol.nodeID == 0){
							mapProtocol.allStateMessages.put(((StateMessage)msg).nodeId,((StateMessage)msg));
							mapProtocol.stateMsgsRcvd[((StateMessage) msg).nodeId] = true;
							if(mapProtocol.allStateMessages.size() == mapProtocol.numOfNodes){
								boolean restartChandy = ChandyLamport.processStateMessages(mapProtocol,((StateMessage)msg));
								if(restartChandy){
									mapProtocol.initialize(mapProtocol);
									new CLThread(mapProtocol).start();	
								}								
							}
						}
						else{
							ChandyLamport.frwdToParent(mapProtocol,((StateMessage)msg));
						}
					}
					else if(msg instanceof FinishMessage && ((FinishMessage)msg).msgType.compareTo("finishMsg")==0){	
						ChandyLamport.sendFinishMsg(mapProtocol);
					}

					if(msg instanceof ApplicationMessage){
						for(int i=0;i<mapProtocol.numOfNodes;i++){
							mapProtocol.vectorClock[i] = Math.max(mapProtocol.vectorClock[i], ((ApplicationMessage) msg).vectorClock[i]);
						}
						mapProtocol.vectorClock[mapProtocol.nodeID]++;
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
class CLThread extends Thread{

	Project1 mapProtocol;
	public CLThread(Project1 mapProtocol){
		this.mapProtocol = mapProtocol;
	}
	public void run(){
		if(mapProtocol.firstTime){
			mapProtocol.firstTime = false;
		}
		else{
			try {
				Thread.sleep(mapProtocol.snapshotDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ChandyLamport.bgnSnpshtProtocol(mapProtocol);
	}
}
class SendMessagesThread extends Thread{

	Project1 mapProtocol;
	public SendMessagesThread(Project1 mapProtocol){
		this.mapProtocol = mapProtocol;
	}
	public void run(){
		try {
			mapProtocol.emitMessages();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
