package server;


import java.io.BufferedInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ToyFTServer {

    public static void main(String[] args) throws Exception {
        System.setProperty("line.separator", "\r\n");
        ServerSocket serverSock = new ServerSocket(5555);
        int byteSize = 4096;
        
        while (true) 
        {
            //  Create a socket.
            Socket sock = serverSock.accept();
            System.out.println("Connection accepted!");

            OutputStream os = sock.getOutputStream();
            // Initialize byte array
            byte[] tempList = new byte[byteSize];

            File dir = new File(System.getProperty("user.dir"));
            String[] children = dir.list();
            System.out.println("Sending file list... ");

            for (int i = 0; i < children.length; i++) {
                // Get filename of file or directory
                File aFile = new File(children[i]);

                if (aFile.isFile()) {
                    tempList = children[i].getBytes();
                    os.write(tempList, 0, tempList.length);
                    os.flush();
                }
                Thread.sleep(10);
            }            
            os.write("0".getBytes());
            os.flush();
            
            System.out.println("File list sent");
            
            tempList = new byte[byteSize];
            //Inputstream awaits for input from client            
            InputStream is = sock.getInputStream();
            //filename is stored in byte array          
            
            String fileName = "";
            File myFile;
            
            try{
            while (true) {
                int num = is.read(tempList);
                //  A list which will hold the actual number of bytes.
                byte[] list = new byte[num];
                //  list is initialized.
                System.arraycopy(tempList, 0, list, 0, num);
                //  File name is shown.
                fileName = new String(list);
                //  Tell the client it can now exit.
                if (fileName.equals("!")) {

                    os.write("!".getBytes());
                    os.flush();
                    break;
                }

                myFile = new File(fileName);
                //  Requested file which exists will be transfered.
                if (myFile.exists()) {
                    System.out.println("file requested: " + fileName);
                    int fileLength = (int) myFile.length();
                    int numLoops = (int) (fileLength / byteSize);
                    int remainder = (int) (myFile.length() - (numLoops * byteSize));

                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                    //  4096 sized byte packets are sent
                    for (int i = 0; i < numLoops; i++) {
                        bis.read(tempList, 0, byteSize);
                        os.write(tempList, 0, byteSize);
                        os.flush();
                        Thread.sleep(1);
                    }
                    //  The remaining part of the file is sent
                    if (remainder > 0) {
                        bis.read(tempList, 0, remainder);
                        os.write(tempList, 0, remainder);
                        os.flush();
                    }
                    //  Tell the client that the server is finished sending.
                    os.write("0".getBytes());
                    os.flush();
                    
                    System.out.println("File transfer complete: " + fileName);
                } else {
                    //  Tell the client that requested file does not exist.
                    System.out.println("file requested: " + fileName + " does not exist");
                    os.write("DNE".getBytes());
                    os.flush();

                }

            }
        }catch(Exception e){}
        }
    }
}
