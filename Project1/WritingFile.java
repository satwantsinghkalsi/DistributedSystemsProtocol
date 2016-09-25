import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class WritingFile {
	Project1 mapProtocol;

	public WritingFile(Project1 mapProtocol) {
		this.mapProtocol = mapProtocol;
	}


	public void writeToFile() {
		String fileName = Project1.outputFileName+"-"+mapProtocol.nodeID+".out";
		synchronized(mapProtocol.output){
			try {
				File file = new File(fileName);
				FileWriter fileWriter;
				if(file.exists()){
					fileWriter = new FileWriter(file,true);
				}
				else
				{
					fileWriter = new FileWriter(file);
				}
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				for(int i=0;i<mapProtocol.output.size();i++){
					for(int j:mapProtocol.output.get(i)){
						bufferedWriter.write(j+" ");
						
					}
					if(i<(mapProtocol.output.size()-1)){
	            bufferedWriter.write("\n");
					}
				}			
				mapProtocol.output.clear();
				bufferedWriter.close();
			}
			catch(IOException ex) {
				System.out.println("Error writing to file '" + fileName + "'");
			}
		}
	}

}
