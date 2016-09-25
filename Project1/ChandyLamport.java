import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class ChandyLamport {
	static final int white=0,red=1;
    public static void bgnSnpshtProtocol(Project1 mapProtocol) {
		synchronized(mapProtocol){
			mapProtocol.stateMsgsRcvd[mapProtocol.nodeID] = true;
			sendMrkrMsg(mapProtocol,mapProtocol.nodeID);
		}
	}

	public static void sendMrkrMsg(Project1 mapProtocol, int channelNo){
		synchronized(mapProtocol){
			if(mapProtocol.myColor == white){
				mapProtocol.mrkrMsgRcvd.put(channelNo, true);
				mapProtocol.myColor=red;
				mapProtocol.myState.active = mapProtocol.active;
				mapProtocol.myState.vectorClock = mapProtocol.vectorClock;
				mapProtocol.myState.nodeId = mapProtocol.nodeID;
				int[] vectorClockCopy = new int[mapProtocol.myState.vectorClock.length];
				for(int i=0;i<vectorClockCopy.length;i++){
					vectorClockCopy[i] = mapProtocol.myState.vectorClock[i]; 
				}
				mapProtocol.output.add(vectorClockCopy);
				mapProtocol.logging = 1;
				for(int i : mapProtocol.neighbors){
					MarkerMessage m = new MarkerMessage();
					m.nodeId = mapProtocol.nodeID;
					ObjectOutputStream oos = mapProtocol.openStrmsOnCrntNode.get(i);
					try {
						oos.writeObject(m);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if((mapProtocol.neighbors.length == 1) && (mapProtocol.nodeID!=0)){
					int parent = ConvergeCast.getParent(mapProtocol.nodeID);	
					mapProtocol.myState.channelStates = mapProtocol.channelStates;
					mapProtocol.myColor=white;
					mapProtocol.logging = 0;
					ObjectOutputStream oos = mapProtocol.openStrmsOnCrntNode.get(parent);
					try {
						oos.writeObject(mapProtocol.myState);
					} catch (IOException e) {
						e.printStackTrace();
					}
					mapProtocol.initialize(mapProtocol);
				}


			}
			else if(mapProtocol.myColor==red){
				mapProtocol.mrkrMsgRcvd.put(channelNo, true);
				int i=0;
				while(i<mapProtocol.neighbors.length && mapProtocol.mrkrMsgRcvd.get(mapProtocol.neighbors[i]) == true){
					i++;
				}
				if(i == mapProtocol.neighbors.length && mapProtocol.nodeID != 0){
					int parent = ConvergeCast.getParent(mapProtocol.nodeID);				
					mapProtocol.myState.channelStates = mapProtocol.channelStates;
					mapProtocol.myColor=white;
					mapProtocol.logging = 0;
					ObjectOutputStream oos = mapProtocol.openStrmsOnCrntNode.get(parent);
					try {
						oos.writeObject(mapProtocol.myState);
					} catch (IOException e) {
						e.printStackTrace();
					}
					mapProtocol.initialize(mapProtocol);
				}
				if(i == mapProtocol.neighbors.length &&  mapProtocol.nodeID == 0){
					mapProtocol.myState.channelStates = mapProtocol.channelStates;
					mapProtocol.allStateMessages.put(mapProtocol.nodeID, mapProtocol.myState);
					mapProtocol.myColor=white;
					mapProtocol.logging = 0;
				}

			}
		}
	}
	public static boolean processStateMessages(Project1 mapProtocol, StateMessage msg) throws InterruptedException {
		int i=0,j=0,k=0;
		synchronized(mapProtocol){
			while(i<mapProtocol.stateMsgsRcvd.length && mapProtocol.stateMsgsRcvd[i] == true){
				i++;
			}
			if(i == mapProtocol.stateMsgsRcvd.length){
				for(j=0;j<mapProtocol.allStateMessages.size();j++){
					if(mapProtocol.allStateMessages.get(j).active == true){
						return true;
					}
				}
				if(j == mapProtocol.numOfNodes){
					for(k=0;k<mapProtocol.numOfNodes;k++){
						StateMessage value = mapProtocol.allStateMessages.get(k);
						for(ArrayList<ApplicationMessage> g:value.channelStates.values()){
							if(!g.isEmpty()){
								return true;
							}
						}
					}
				}
				if(k == mapProtocol.numOfNodes){
					sendFinishMsg(mapProtocol);
					return false;
				}
			}
		}
		return false;
	}

	public static void logMsg(int channelNo,ApplicationMessage m, Project1 mapProtocol) {
		synchronized(mapProtocol){
			if(!(mapProtocol.channelStates.get(channelNo).isEmpty()) && mapProtocol.mrkrMsgRcvd.get(channelNo) != true){
				mapProtocol.channelStates.get(channelNo).add(m);
			}
			else if((mapProtocol.channelStates.get(channelNo).isEmpty()) && mapProtocol.mrkrMsgRcvd.get(channelNo) != true){
				ArrayList<ApplicationMessage> msgs = mapProtocol.channelStates.get(channelNo);
				msgs.add(m);
				mapProtocol.channelStates.put(channelNo, msgs);
			}
		}
	}
	public static void frwdToParent(Project1 mapProtocol, StateMessage StateMessage) {
		synchronized(mapProtocol){
			int parent = ConvergeCast.getParent(mapProtocol.nodeID);
			ObjectOutputStream oos = mapProtocol.openStrmsOnCrntNode.get(parent);
			try {
				oos.writeObject(StateMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void sendFinishMsg(Project1 mapProtocol) {
		synchronized(mapProtocol){
			new WritingFile(mapProtocol).writeToFile();
			for(int s : mapProtocol.neighbors){
				FinishMessage m = new FinishMessage();
				ObjectOutputStream oos = mapProtocol.openStrmsOnCrntNode.get(s);
				try {
					oos.writeObject(m);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
			System.exit(0);
		}
	}
}
