
import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class s implements Runnable {


    List<Socket> socketList= new ArrayList<Socket>();
    List<myFile> FileList= new ArrayList<myFile>();
    HashMap<String,X509Certificate> CertificateMap = new HashMap<>();

    public static void main(String[] args) {
        s manager = new s();
       
		try{
			File aFile = new File("./A.crt");
			FileInputStream aFis = new FileInputStream(aFile);
			File bFile = new File("./B.crt");
			FileInputStream bFis = new FileInputStream(bFile);
			File cFile = new File("./C.crt");
			FileInputStream cFis = new FileInputStream(cFile);
			File dFile = new File("./D.crt");
			FileInputStream dFis = new FileInputStream(dFile);


         CertificateFactory certificate_factory = CertificateFactory.getInstance("X.509");
         X509Certificate As = (X509Certificate) certificate_factory.generateCertificate(aFis);
         X509Certificate Bs = (X509Certificate) certificate_factory.generateCertificate(bFis);
         X509Certificate Cs = (X509Certificate) certificate_factory.generateCertificate(cFis);
         X509Certificate Ds = (X509Certificate) certificate_factory.generateCertificate(dFis);
		 
		// X509Certificate Es = (X509Certificate) certificate_factory.generateCertificate(aFis);
		 
         aFis.close();
		 bFis.close();
		 cFis.close();
		 dFis.close();
		 
		 File f = new File("cText.txt");
		 myFile testF = new myFile(f);
		 
		 CertListItem A = new CertListItem(As);
		 A.addCert(As);
		 
		 CertListItem B = new CertListItem(Bs);
 		 B.addCert(Cs);
 		 B.addCert(Ds);
		
		 CertListItem C = new CertListItem(Cs);
		 
		 CertListItem D = new CertListItem(Ds);
		 D.addCert(Bs);
		 
		 testF.addVouch(A);
		 testF.addVouch(B);
		 testF.addVouch(C);
		 testF.addVouch(D);
		 
		 // System.out.println("EQUAL? " + As.equals(testF.fileVouchedBy.get(3).getVouchedFor().get(0)));
		 
 		System.out.println("********************");
 		System.out.println("\nSearch for circle containing " + "D" +"...");
 		System.out.print("\n");
 		testF.searchThrough(As);
 		System.out.println("Is there a circle? " + testF.isCircle);
 		System.out.print("\n");
 		System.out.println("Size of circle is: " + testF.circleSize);
 		System.out.print("\n");
 		System.out.print("Circle is: ");
		
		/*X509Certificate cert = ...;

		X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
		RDN cn = x500name.getRDNs(BCStyle.CN)[0];

		return IETFUtils.valueToString(cn.getFirst().getValue());
		 */
 		for(int i = 0; i < testF.actualList.size(); i++){
			X509Certificate temp = testF.actualList.get(i);
			
			if(temp.equals(As)){
 				System.out.print("A-");
			}else if(temp.equals(Bs)){
 				System.out.print("B-");
			}else if(temp.equals(Cs)){
 				System.out.print("C-");
			}else if(temp.equals(Ds)){
 				System.out.print("D-");
			}
 		}
		
		System.out.println("\n\n********************");
     }
     catch (Exception e)
     {
         e.printStackTrace();
     }
		
		//==========
		
		//==========

      /*	new Thread(manager).start();	 
		  Scanner scanner = new Scanner(System.in);  
	  	while(true){
            System.out.printf("Send> ");
            String message = scanner.nextLine();
            if(message.equals("") || message.equals("\n")){
                continue;
            }else{
                manager.send(message);
            }
        }*/
    }

    public void send(String message){

        for(Socket s:socketList){
            PrintWriter output;
            try {
                output = new PrintWriter(new BufferedOutputStream(s.getOutputStream()),true);
                output.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static SSLServerSocket getServerSocket(int thePort)
    {
        SSLServerSocket s=null;
        try
        {
            //String key="G:\\mySrvKeystore";
			String key="./mySrvKeystore";

            char keyStorePass[]="123456".toCharArray();

            char keyPassword[]="123456".toCharArray();
            KeyStore ks= KeyStore.getInstance("JKS");

            ks.load(new FileInputStream(key),keyStorePass);


            KeyManagerFactory kmf= KeyManagerFactory.getInstance("SunX509");

            kmf.init(ks,keyPassword);

            SSLContext sslContext= SSLContext.getInstance("SSLv3");

            sslContext.init(kmf.getKeyManagers(),null,null);


            SSLServerSocketFactory factory=sslContext.getServerSocketFactory();

            s=(SSLServerSocket)factory.createServerSocket(thePort);

        }catch(Exception e)
        {
            System.out.println(e);
        }
        return(s);
    }

    public void run() {
        try {
            SSLServerSocket sslserversocket = getServerSocket(9991);
            while (true) {
                SSLSocket  client = (SSLSocket)sslserversocket.accept();
                socketList.add(client);
                new Thread(new SSocket(client,socketList,FileList)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SSocket implements Runnable {
        SSLSocket client;
        List<Socket> socketList;
        List<myFile> fileList;

        public SSocket(SSLSocket client,List<Socket> socketList,List<myFile> fileList) {
            this.client = client;
            this.socketList = socketList;
            this.fileList = fileList;
        }

        public void run() {
            BufferedReader input;
            PrintWriter output;
            try {
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output = new PrintWriter(new BufferedOutputStream(client.getOutputStream()), true);

                while (true) {
                    String listMsg = input.readLine();
                    String type = listMsg.split("sprt")[0];

                    if (type.equals("EXIT")) {
                        output.println("server close");
                        socketList.remove(client);
                        System.out.println("connection close");
                    }

                    //-a fileName
                    if (type.equals("FILE")) {
                        //FILE % filename % data
                        try {
                            String fileName = listMsg.split("sprt")[1];
                            String info = listMsg.split("sprt")[2];
                            File f = StoreFile(fileName, info);
                            //System.out.println(info);
                            output.println("Receive " + fileName + "successfully");
                            myFile mf = new myFile(f);
                            fileList.add(mf);

                            //Not necessary. Just for testing
                            for (myFile file : fileList) {
                                System.out.println(file.getFileName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (type.equals("NORMAL")) {
                        String info = listMsg.split("sprt")[1];
                        System.out.println("Receive Normal Message from" + client.getInetAddress() + ":\n" + info);
                    }

                    // -f filename
                    if (type.equals("DOWNLOAD")) {
                        //DOWNLOAD % filename
                        //String fileName = "H:\\" + listMsg.split("sprt")[1];
						String fileName = "./" + listMsg.split("sprt")[1];
                        System.out.println("Receive DOWNLOAD " + fileName + " command from " + client.getInetAddress() + ":\n");

                        if (fileList.size() == 0) {
                            output.println("NO FILE EXISTS !");
                        } else {
                            for (myFile mf : fileList) {
                                //Looking at the directory of a particular file
                                if (mf.getFileName().equals(fileName)) {
                                    BufferedReader br = new BufferedReader(new FileReader(mf.getFile()));
                                    while (br.ready()) {
                                        output.println(br.readLine());
                                    }
                                    break;
                                } else {
                                    output.println("No such file or directory");
                                }
                            }
                        }
                    }

                    //-f filename -c number
                    if (type.equals("FC")) {
                        //FC%fileName%number

                    }

                    //-f filename -n name
                    if (type.equals("FN")) {
                        //FN%fileName%name
                    }

                    //-l -c number
                    if (type.equals("LC")) {
                        //LC%fileName%number
                        String requiredFileName = listMsg.split("sprt")[1];
                        int requiredCircleNumber = Integer.valueOf(listMsg.split("sprt")[2]);

                        System.out.println("Receive List+Circle Command from " + client.getInetAddress() + ":\n");
                        if (fileList.size() == 0) {
                            output.println("No file exist");
                        } else {
                            for (myFile f : fileList) {
                                if (f.getFileName().equals(requiredFileName) && f.getCircleSize() == requiredCircleNumber) {
                                    output.println(f.getFileName() + f.getHowManyPeopleHaveVouched() + f.getCircleSize());
                                }
                            }
                            // Give a message that the loop finished. If nothing returns at this point the client will know.
                            output.println("Command execution finished ");
                        }
                    }

                    //-l -n name
                    if (type.equals("LN")) {
                        //LC%fileName%Name
                    }

                    //-u certificateName
                    if (type.equals("CER")) {
                        //CRE%certificateName
                        String certificateName = listMsg.split("sprt")[1];
                        System.out.println("Receive CER " + certificateName + " command from " + client.getInetAddress() + ":\n");

                        // A.cer -> certificate detail 
                        // B.cer -> certificate detail
                        CertificateMap.put(certificateName,storeCertificate(certificateName)); 
						for(String name : CertificateMap.keySet())
						{
							System.out.println(name);
						}
                       
                    }

                    // -v filename certificate
                    if (type.equals("VOUCH")) {

                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public File StoreFile(String fileName, String info)
        {
            File f = new File("H:/" + fileName);
            try {
                FileWriter fw = new FileWriter(f);
                PrintWriter pw = new PrintWriter(fw);
                pw.println(info);
                pw.close();
                System.out.println("Receive " + fileName + " from" + client.getInetAddress());
            }catch (Exception e){e.printStackTrace();}
            return  f;
        }
    }

    public X509Certificate storeCertificate(String CerName)
    {
        try {
           File InFile = new File("G:/"+CerName);
           File OutFile = new File ("H:/"+CerName);

            FileInputStream fis = new FileInputStream(InFile);
            FileOutputStream fos = new FileOutputStream(OutFile);


            CertificateFactory certificate_factory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificate_factory.generateCertificate(fis);

            fis.close();

            //Storing the Certificate locally
            byte[] temp = certificate.getEncoded();
            fos.write(temp);
            fos.close();

            return  certificate;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}

class myFile {
	public ArrayList<CertListItem> fileVouchedBy;
    File FileName;
    int HowManyPeopleHaveVouched = 0;
    int circleSize = 0;
	
    public myFile(File fileName) {
        FileName = fileName;
		fileVouchedBy = new ArrayList<CertListItem>(); //initialize this with...
		HowManyPeopleHaveVouched = fileVouchedBy.size(); ////initialize this with...
    }
	
	public void addVouch(CertListItem item){
		fileVouchedBy.add(item);
		HowManyPeopleHaveVouched++;
	}
	
	public boolean firstTime = true;
	public X509Certificate startingPoint = null;
	public boolean isCircle = false;
	//public int circleSize = 0;
	public int sizeCounter = 0;
	
	public ArrayList<X509Certificate> circleList = new ArrayList<X509Certificate>();
	public ArrayList<X509Certificate> actualList = new ArrayList<X509Certificate>();
	
	public void searchThrough(X509Certificate start){
		
		if(find(start) == -1){
			return;
		}else{
		
			if(firstTime == false){
				if(start.equals(startingPoint)){
					//System.out.println(start);
					//System.out.println("Completed.\n");
					circleSize = sizeCounter;
					isCircle = true;
					circleList.add(start);
					actualList = (ArrayList<X509Certificate>)circleList.clone();
					return;
				}
			}else{
			
				firstTime = false;
				startingPoint = start;
				isCircle = false;
				circleSize = 0;
				sizeCounter = 0;
		}
		
		
			//System.out.println(start);
			CertListItem node = fileVouchedBy.get(find(start));
			
			ArrayList<X509Certificate> temp = node.getVouchedFor();
			
			//System.out.println(node.getSize());
			//System.out.println(temp.size());
			
			for(int i = 0; i < node.getSize(); i++){
				
				if(find(temp.get(i)) != -1){
				CertListItem nextNode = fileVouchedBy.get(find(temp.get(i)));
				//System.out.println("aaaa" + nextNode.getTravelled());
				sizeCounter++;
				circleList.add(start);
				searchThrough(temp.get(i));
				sizeCounter--;
				circleList.remove(start);
			}
			}
					
			
		}
		
	}
	
	public int find(X509Certificate s){
		for(int i = 0; i < getHowManyPeopleHaveVouched(); i++) {
			//System.out.println("SSSS is:" + s);
			//System.out.println("==========");
			//System.out.println("Number   "+ i + "   FILE is:" + fileVouchedBy.get(i).getCert());
			if(fileVouchedBy.get(i).getCert().equals(s)){
				//System.out.println("here");
				//System.out.println(i);
				return i;
			}
		}
		
		return -1;
	}

	
	public boolean getCircle(String name){
		return false;
	}

    public String getFileName() {
        return FileName.toString();
    }

    public File getFile()
    {
        return FileName;
    }

    public int getHowManyPeopleHaveVouched()
    {
        return HowManyPeopleHaveVouched;
    }

    public int getCircleSize()
    {
        return circleSize;
	}



}

class CertListItem {

	private X509Certificate certificate;
	ArrayList<X509Certificate> vouchedFor;

	public CertListItem(X509Certificate cert){
		this.certificate = cert;
		this.vouchedFor = new ArrayList<X509Certificate>();
	}

	// return position of certificate, -1 if not in this list
	public int findCert(X509Certificate cert) {
		//return vouchedFor.indexOf(cert);
		//TODO
		for(int i = 0; i < getSize(); i++){
			if(vouchedFor.get(i).equals(cert)){
				return i;
			}
		}

		return -1;
	}
	
	public void addCert(X509Certificate cert){
		// TODO add if not equal to certificate
		vouchedFor.add(cert);
	}

	public X509Certificate getCert(){
		return certificate;
	}

	public ArrayList<X509Certificate> getVouchedFor(){
		return vouchedFor;
	}

	public int getSize(){
		return vouchedFor.size();
	}
}



