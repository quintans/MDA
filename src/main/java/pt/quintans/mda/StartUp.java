package pt.quintans.mda;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import pt.quintans.mda.core.WorkerStore;

public class StartUp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args == null || args.length == 0)
			throw new RuntimeException("no minimo � necess�rio passar o caminho do workflow");
		
		boolean quiet = false;
		int cnt = 0;
		for(String arg : args){
			if(arg.startsWith("/")){
				if("/Q".equals(arg.toUpperCase())){
					quiet = true;
					cnt++;
				} 
			}else
				break;
		}

		String subnamespace = null;
		String workflowFile = null;
		if(args.length > cnt)
			workflowFile = args[cnt];
		
		if(args.length > cnt + 1)
			subnamespace = args[cnt + 1];

		try {
			WorkerStore.get().doIt(workflowFile, subnamespace, quiet);
			System.out.println("Geração terminada...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(ba));
			//e.printStackTrace();
			System.out.println(ba.toString());
		}
	}

}
