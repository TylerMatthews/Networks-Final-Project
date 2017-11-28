//****************************************************************************
// 
// Authors:Tyler Matthews U#09879383, Navin Ramkishun U#58568482
// Date: 11/23/2017
// Simple StopWatch class used for timing file transfers
// Some programs we looked at at https://goo.gl/hb84go, https://goo.gl/ufgcBy
//***************************************************************************
import java.io.*;
import java.net.*;

class Server 
{

    private static int totalTransferred = 0;
    private static StopWatch timer;
    private static String fileName = "";
    private static String decodedDataUsingUTF82 = null;

    public static void main(String args[]) throws Exception 
    {

        System.out.println("Ready!");
        final int port = Integer.parseInt(args[0]);
        startUp(port);
    }

    //Initial startup of the program
    public static void startUp(int port) throws IOException 
    {

        DatagramSocket socket = new DatagramSocket(port);

        byte[] receiveFileNameChoice = new byte[1024];
        DatagramPacket receiveFileNameChoicePacket = new DatagramPacket(
                receiveFileNameChoice, receiveFileNameChoice.length);
        socket.receive(receiveFileNameChoicePacket);

        try 
        {
            decodedDataUsingUTF82 = new String(receiveFileNameChoice, "UTF-8");
        } 
        catch (UnsupportedEncodingException e) 
        {
            e.printStackTrace();
        }

        String savedFileName = decodedDataUsingUTF82.trim();
        fileName = savedFileName;
        setFile(savedFileName);
        File file = new File(fileName);
        FileOutputStream outToFile = new FileOutputStream(file);

        fileTransAcc(outToFile, socket);

        byte[] finalStatData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(finalStatData,
                finalStatData.length);
        socket.receive(receivePacket);
        printFinalStatistics(finalStatData);
    }

    //Function to print the final stats of the file transfer
    private static void printFinalStatistics(byte[] finalStatData) 
    {
        try 
        {
            String decodedDataUsingUTF8 = new String(finalStatData, "UTF-8");
            Statistics.blankLine();
            Statistics.blankLine();
            System.out.println("Transfer Statistics");
            Statistics.dividerLine();
            System.out.println("File name: " + getFile());
            System.out.println("Transfer statistics");
            System.out.println("" + decodedDataUsingUTF8.trim());
            Statistics.dividerLine();

        } 
        catch (UnsupportedEncodingException e) 
        {
            e.printStackTrace();
        }
    }

    //File Transfer accept function
    private static void fileTransAcc(FileOutputStream outToFile, DatagramSocket socket) throws IOException 
    {

        // last message flag
        boolean flag;
        int sequenceNumber = 0;
        int findLast = 0;

        while (true) 
        {
            byte[] message = new byte[1024];
            byte[] transferArray = new byte[1021];

            // Receive packet and retrieve message
            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
            socket.setSoTimeout(0);
            socket.receive(receivedPacket);

            message = receivedPacket.getData();
            totalTransferred = receivedPacket.getLength() + totalTransferred;
            totalTransferred = ( Math.round((int)totalTransferred));

            // start the timer at the point transfer begins
            if (sequenceNumber == 0) 
            {
                timer = new StopWatch();
            }

            //Periodically print out the stats of the transfer
            if (Math.round((int)totalTransferred / 1000) % 100 == 0) 
            {
                double previousTimeElapsed = 0;
                int prevSize = 0;
                //Statistics.printStats(totalTransferred, prevSize,
                 //       timer);
            }
            // Get port and address for sending acknowledgment
            InetAddress address = receivedPacket.getAddress();
            int port = receivedPacket.getPort();

            //Get the current sequence number.
            sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);

            //get the previous message flag
            flag = (message[2] & 0xff) == 1;
            //as int as the Sequence number is = to previous sequence number +1
            //everything is ok

            //Reset the sequence number/findLast when you hit ~50MB of data,
            //which lets you get around the 65536 byte max of UDP
            //When the Client resets the sequence number to 1, we reset findLast
            //(the previous sequence number) to 0, to ensure program stability
            if (sequenceNumber == 1)
            {
                findLast = 0;
            }
            if (sequenceNumber == (findLast + 1)) 
            {

                //Sets the last sequence number to the one we just received
                findLast = sequenceNumber;

                //get the data from the message
                System.arraycopy(message, 3, transferArray, 0, 1021);

                //print the received file, write message to array
                outToFile.write(transferArray);
                
                //THe below prints out each Ack as it is received.
                //Commented out as it severely slows down program execution
                //System.out.println("Received: Sequence number:"
                //        + findLast);

                //Send the acknowledgement to the client
                sendAck(findLast, socket, address, port);
            } 
            else 
            //if current sequence number does not = previous number +1,
            //something went wrong. Print the error message, resend the Ack
            {
              //Commenting out as it can severly lower performanc
              //  System.out.println("Sequence num should be: "
                  //      + (findLast + 1) + ", received num was:  "
                 //       + sequenceNumber + ". packet discard");
                // Re send the acknowledgement
                sendAck(findLast, socket, address, port);
            }

            // Check for last message
            if (flag) 
            {
                outToFile.close();
                break;
            }
        }
    }

    //Gets the file name
    private static String getFile() 
    {
        return fileName;
    }

    //Sets the file name
    private static void setFile(String theFileName) 
    {
        fileName = theFileName;
    }

    //Function to send Acknowledgement for received packets. Used to help ensure all packets received by the server
    private static void sendAck(int findLast, DatagramSocket socket, InetAddress address, int port) throws IOException 
    {
        //Sends the Acknowledgement for the packet
        byte[] ackPacket = new byte[2];
        ackPacket[0] = (byte) (findLast >> 8);
        ackPacket[1] = (byte) (findLast);
        //The datagram acknowledgement to be sent
        DatagramPacket acknowledgement = new DatagramPacket(ackPacket,
                ackPacket.length, address, port);
        socket.send(acknowledgement);
        //The below prints out a line for each Acknowledgement, so you can see where
        //any errors are, if needed.
        //System.out.println("Acknowledgement Sent for Sequence Number = " + findLast);
    }
}