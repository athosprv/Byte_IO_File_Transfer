package client;


import java.io.BufferedOutputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ToyFTClient {

    public static void main(String[] args) throws Exception {
        Socket sock = new Socket("localhost", 5555);

        InputStream is = sock.getInputStream();
        int byteSize = 4096;
        byte[] mybytearray = new byte[byteSize];
        byte[] arr;

        int bytesRead = 1;

        System.out.println("File list on the Server: ");
        System.out.println("------------------------");

        //  Client is reading the server list
        while (bytesRead > 0) 
        {
            bytesRead = is.read(mybytearray, 0, byteSize);
            arr = new byte[bytesRead];
            System.arraycopy(mybytearray, 0, arr, 0, bytesRead);
            String fileName = new String(arr);
            if (fileName.equals("0")) {
                break;
            }
            System.out.println(fileName);

        }
        System.out.println("------------------------");

        OutputStream os = sock.getOutputStream();
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
        
        //  Client is requesting a file to be copied
        while (true) {
            System.out.print("Enter a file to copy or ! to quit: ");
            String response = consoleIn.readLine();

            os.write(response.getBytes(), 0, response.getBytes().length);
            os.flush();

            bytesRead = is.read(mybytearray, 0, byteSize);

            arr = new byte[bytesRead];
            System.arraycopy(mybytearray, 0, arr, 0, bytesRead);
            String fileName = new String(arr);
            //  Server tells the server it will now exit.
            if (response.equals("!") || fileName.equals("!")) {
                os.write("!".getBytes(), 0, 1);
                os.flush();
                os.close();
                // Exit the client. 
                System.exit(0);
            //  Server has received a "file does not exist" command.
            } else if (fileName.equals("DNE")) {
                System.out.println("File name " + response + " does not exist on the server.");                
            } else {
                //  File requested exists and is now copied on the client side.
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("copy of " + response));

                while (bytesRead > 0) {
                    arr = new byte[bytesRead];
                    System.arraycopy(mybytearray, 0, arr, 0, bytesRead);

                    String file = new String(arr);
                    if (file.equals("0")) {
                        break;
                    }

                    bos.write(arr, 0, bytesRead);
                    bos.flush();

                    bytesRead = is.read(mybytearray, 0, mybytearray.length);
                }
                System.out.println("...Done");
                bos.close();
            }
        }
    }
}
