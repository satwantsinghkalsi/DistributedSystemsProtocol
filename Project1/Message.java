import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;


@SuppressWarnings("serial")
public class Message implements Serializable {
	Project1 m = new Project1();
	int n = m.numOfNodes;
}
@SuppressWarnings("serial")
class ApplicationMessage extends Message implements Serializable{
	String msg = "hi";
	String msgType="appMsg";
	int nodeId;
	int[] vectorClock;
}
@SuppressWarnings("serial")
class MarkerMessage extends Message implements Serializable{
	String msgType="markerMsg";
	String msg = "markr";
	int nodeId;
}
@SuppressWarnings("serial")
class StateMessage extends Message implements Serializable{
	boolean active;
	String msgType="stateMsg";
	int nodeId;
	TreeMap<Integer,ArrayList<ApplicationMessage>> channelStates;
	int[] vectorClock;
}
@SuppressWarnings("serial")
class FinishMessage extends Message implements Serializable{
	String msg = "stop";
	String msgType="finishMsg";
}