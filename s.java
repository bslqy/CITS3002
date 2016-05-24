import javax.net.ssl.*;
import javax.security.cert.Certificate;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class s implements Runnable {


    List<Socket> socketList= new ArrayList<Socket>();
    List<myFile> FileList= new ArrayList<myFile>();
    List<Certificate> CertificateList = new ArrayList<Certificate>();

    public static void main(String[] args) {
        s manager = new s();
        new Thread(manager).start();
        Scanner scanner = new Scanner(System.in);

        while(true){
            System.out.printf("Send> ");
            String message = scanner.nextLine();
            if(message.equals("") || message.equals("\n")){
                continue;
            }else{
                manager.send(message);
            }
        }
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
            String key="G:\\mySrvKeystore";

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
            BufferedReader  input;
            PrintWriter output;
            try {
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output = new PrintWriter(new BufferedOutputStream(client.getOutputStream()),true);

                while(true){
                    String listMsg = input.readLine();
                    String type = listMsg.split("sprt")[0];

                    if(type.equals("EXIT"))
                    {
                        output.println("server close");
                        socketList.remove(client);
                        System.out.println("connection close");
                    }
                    if(type.equals("FILE"))
                    {
                        try {
                            String fileName = listMsg.split("sprt")[1];
                            String info = listMsg.split("sprt")[2];
                            File f = createFile(fileName,info);
                            //System.out.println(info);
                            output.println("Receive " + fileName + "successfully");
                            myFile mf = new myFile(f);
                            fileList.add(mf);
                            for(myFile file: fileList)
                            {
                                System.out.println(file.getFileName());
                            }
                        }catch (Exception e){e.printStackTrace();}
                    }
                    if(type.equals("NORMAL"))
                    {
                        String info = listMsg.split("sprt")[1];
                        System.out.println("Receive Normal Message from" + client.getInetAddress()+":\n" + info);
                    }
                    if(type.equals("DOWNLOAD"))
                    {
                        String fileName = "H:\\"+listMsg.split("sprt")[1]; //DOWNLOAD%fileName%-c% number'
                        System.out.println("Receive DOWNLOAD "+fileName + " command from "+ client.getInetAddress()+ ":\n");

                        if(fileList.size() == 0)
                        {
                            output.println("NO FILE EXISTS !");
                        }
                        else {
                            for (myFile mf : fileList) {
                                //Looking at the directory of a particular file
                                if (mf.getFileName().equals(fileName)) {
                                    BufferedReader br = new BufferedReader(new FileReader(mf.getFile()));
                                    while(br.ready()){
                                        output.println(br.readLine());
                                    }
                                    break;
                                } else {
                                    output.println("No such file or directory");
                                }
                            }
                        }
                    }

                    if(type.equals("LIST"))
                    {
                        System.out.println("Receive List Command from "+ client.getInetAddress()+":\n");
                        if(fileList.size() == 0)
                        {
                            output.println("No file exist");
                        }
                        else
                        {
                            for(myFile f : fileList)
                            {
                                output.println(f.getFileName());
                            }
                        }
                    }

                    if(type.equals("U&P"))
                    {
                        System.out.println("Receive Password from " + client.getInetAddress() + ":\n");
                        String username = listMsg.split("sprt")[1];
                        String password = listMsg.split("sprt")[2];
                        if(username.equals("aaa")&& password.equals("123"))
                        {
                            output.printf("YES");
                        }
                        else{
                            output.printf("NO");
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public File createFile(String fileName,String info)
        {
            File f = new File("H:/" + fileName);
            try {
                //What path name? Can be replace by explicit directory, not necessary G:/
                FileWriter fw = new FileWriter(f);
                PrintWriter pw = new PrintWriter(fw);
                pw.println(info);
                pw.close();
                System.out.println("Receive " + fileName + " from" + client.getInetAddress());
            }catch (Exception e){e.printStackTrace();}
            return  f;
        }
    }
}

 class myFile {
    ArrayList<String> vouch = new ArrayList<>();
    File FileName;
    int HowManyPeopleHaveVouched = 0;
    int circleSize = 0;

    public ArrayList<String> getVouch() {
        return vouch;
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

     public void setVouch(String person) {
         vouch.add(person);
     }

     public void setHowManyPeopleHaveVouched() {
         HowManyPeopleHaveVouched ++;
     }

     public void setCircleSize(int circleSize) {
         this.circleSize = circleSize;
     }

     public myFile(File fileName) {
        FileName = fileName;
    }

}
