import java.io.*;
import java.net.*;
import java.util.Random;

//********************************************************************************************************************
// Authors: Tyler Matthews U#09879383, Navin Ramkishun U#58568482
// U
// JAVA SERVER MUST BE RUNNING FIRST!!! Please look at Server.java and follow the instructions to start the server.
// To run, do "java Client (send rate) (domain) (port #) (file name to transfer) (file name to create on server)"
// send rate is the reverse packet loss rate. So 98 would be 2% packet loss, 100 would be 0%
// i.e. "java Client 100 127.0.0.1 80 testSend.dat testRecv.dat"
// 
// Programs we looked at https://goo.gl/nE6jW2, https://goo.gl/ufgcBy
// ******************************************************************************************************************
class Client 
{
    private static StopWatch timer = null;
    private static int port;
    private static String fileName;
    private static String destFile;
    private static int retransmitted = 0;
    private static int totalTransferred = 0;
    private static final int prevSize = 0;
    private static int sendRate = 0;
    private static String host;

    public static void main(String args[]) throws Exception 
    {

        sendRate = Integer.parseInt(args[0]);
        setLossRate(sendRate);
        host = args[1];
        sethost(host);
        port = Integer.parseInt(args[2]);
        setPort(port);
        fileName = args[3];
        setFile(fileName);
        destFile = args[4];
        setDestFile(destFile);
        startUp();
    }

    public static void startUp() throws IOException 
    {

        System.out.println("Sending the file");
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(gethost());

        String saveFileAs = getDestFile();
        byte[] saveFileAsData = saveFileAs.getBytes();

        //create datagram packet
        DatagramPacket fileStatPacket = new DatagramPacket(saveFileAsData, saveFileAsData.length, address, getPort());
        socket.send(fileStatPacket);

        File file = new File(getFile());
        // Create a byte array to store file
        byte[] transferArray = new byte[(int) file.length()];

        StopWatch();
        startFileTrans(socket, transferArray, address);
        String finalStatString = getFinalStatistics(transferArray, retransmitted);
        finalServerStats(socket, address, finalStatString);
        closeSocket(socket);
    }

    private static void closeSocket(DatagramSocket socket) 
    {
        socket.close();
    }

    private static void finalServerStats(DatagramSocket socket, InetAddress address, String finalStatString) 
    {

        byte[] bytesData;
        // convert string to bytes so we can send
        bytesData = finalStatString.getBytes();
        DatagramPacket statPacket = new DatagramPacket(bytesData,
                bytesData.length, address, getPort());
        try 
        {
            socket.send(statPacket);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    private static void startFileTrans(DatagramSocket socket, byte[] transferArray, InetAddress address) throws IOException 
    {

        int sequenceNumber = 0;
        boolean flag;
        int ackSequence = 0;

        for (int i = 0; i < transferArray.length; i = i + 1021) 
        {
            //Reset the sequence number when you hit ~50MB of data,
            //which lets you get around the 65536 byte max of UDP
            if (sequenceNumber == 50000)
            {
                sequenceNumber = 0;
            }   
            sequenceNumber += 1;
            // Create message
            byte[] message = new byte[1024];
            message[0] = (byte) (sequenceNumber >> 8);
            message[1] = (byte) (sequenceNumber);

            if ((i + 1021) >= transferArray.length) 
            {
                flag = true;
                message[2] = (byte) (1);
            } 
            else 
            {
                flag = false;
                message[2] = (byte) (0);
            }

            if (!flag) 
            {
                System.arraycopy(transferArray, i, message, 3, 1021);
            } 
            else 
            { // If it is the last message
                System.arraycopy(transferArray, i, message, 3, transferArray.length - i);
            }

            //Sets the random number used in simulated packet loss
            int randomInt = doWeSend();

            DatagramPacket sentPacket = new DatagramPacket(message, message.length, address, getPort());

            //Used to simulate packet loss. If we want a 2% packet loss, we would set loss rate to 98.
            //Then, over a large number of packets, on average 2% the random int rolls would be 99/100
            //and we would not send those packets, simulating a lost packet.
            if (randomInt <= getLossRate()) 
            {
                socket.send(sentPacket);
            }

            totalTransferred = setCurrTransferred(sentPacket);

            //Periodically print out the stats of the transfer
            //if ( Math.round((int)totalTransferred / 1000) % 100 == 0) 
            //{
                //Statistics.printStats(totalTransferred, prevSize, timer);
            //}
            //Used to print out each message as it sends
            //commented out as it severely lowers performance
            //System.out.println("Sent: Sequence number = " + sequenceNumber);

            //Verifies the packet
            boolean ackRec;

            // The acknowledgment is not correct
            while (true) 
            {
                // Creates a new packet, sets a Byte array creates a datagram packet
                byte[] ack = new byte[2];
                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

                try 
                {
                    //used to set timeout value for package acks. If an ack is not received
                    //in time, it will be resent
                    socket.setSoTimeout(80);
                    socket.receive(ackpack);
                    ackSequence = ((ack[0] & 0xff) << 8)
                            + (ack[1] & 0xff);
                    ackRec = true;

                }
                // if an ack is not received
                catch (SocketTimeoutException e) 
                {
                    //System.out.println("Timeout Exceeded, ack not received.");
                    ackRec = false;
                }

                // If there is an ack, we can exit out and send the next packet
                if ((ackSequence == sequenceNumber) && (ackRec)) 
                {
                    //Used to print out which Acks were received for which number.
                    //Commented out as it severely lowers performance
                    //System.out.println("Ack was receied for Sequence Number: "
                     //       + ackSequence);
                    break;
                }

                // Resend the packet that wasnt received
                else 
                {
                    socket.send(sentPacket);
                    //System.out.println("Resending Sequence Number :  "
                    //        + sequenceNumber);
                    // keep track of number of retransmits
                    retransmitted += 1;
                }
            }
        }
    }

    //keeps track of how much of the file has been transferred
    private static int setCurrTransferred(DatagramPacket sentPacket) 
    {
        totalTransferred = sentPacket.getLength() + totalTransferred;
        totalTransferred = ( Math.round((int)totalTransferred));

        return totalTransferred;
    }

    //used to simulate packet loss rate, we get a random number between 1 and 100
    private static int doWeSend() 
    {
        Random randomGenerator = new Random();

        return randomGenerator.nextInt(100);
    }

    //Returns the total statistics for the file transfer
    private static String getFinalStatistics(byte[] transferArray, int retransmitted) 
    {
        double transferTime = timer.getElapsedTime() / 1000;
        double fileSizeKB = (transferArray.length) / 1024;
        double fileSizeMB = fileSizeKB / 1000;
        double throughput = fileSizeMB / transferTime;
        

        System.out.println("The file " + getFile() + " was sent");
        Statistics.blankLine();
        Statistics.blankLine();
        System.out.println("Transfer Statistics");
        Statistics.dividerLine();
        System.out.println("The file " + getFile() + " was succesfully transferred");
        System.out.println("Total File size " + totalTransferred / 1000 + " KB");
        System.out.println("Total Transfer time " + timer.getElapsedTime() / 1000 + " Seconds");
        System.out.printf("Throughput was %.2f MBPS\n", +throughput);
        System.out.println("Total number of retransmissions: " + retransmitted);
        Statistics.dividerLine();

        return "File Size: " + fileSizeMB + "mb\n"
                + "Throughput: " + throughput + " Mbps"
                + "\nTotal transfer time: " + transferTime + " Seconds";
    }

    //The following are some simple functions for setting
    //and getting various variable values. Simple get, set, etc,
    // used to make the code cleaner
    private static void StopWatch() 
    {
        timer = new StopWatch();
    }

    private static int getLossRate() 
    {
        return sendRate;
    }

    private static void setLossRate(int lossRate) 
    {
        sendRate = lossRate;
    }

    private static int getPort() 
    {
        return port;
    }

    private static void setPort(int portNum) 
    {
        port = portNum;
    }

    private static String getFile() 
    {
        return fileName;
    }

    private static void setFile(String theFileName) 
    {
        fileName = theFileName;
    }

    private static void setDestFile(String theDestFile) 
    {
        destFile = theDestFile;
    }

    private static String getDestFile() 
    {
        return destFile;
    }

    private static String gethost() 
    {
        return host;
    }

    private static void sethost(String passed_host_name) 
    {
        host = passed_host_name;
    }
}