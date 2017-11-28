//****************************************************************************
// 
// Authors: Tyler Matthews U#09879383, Navin Ramkishun U#58568482
// Date: 11/22/2017
// Simple Class used to print the current statistics for the file transfer
// Looked over https://goo.gl/yvMDaZ for slight guidance
//***************************************************************************
class Statistics
{

    public Statistics() 
    {
    }

    //
    public static void printStats(long currSize, long prevSize, StopWatch timer) 
    {
        blankLine();
        dividerLine();
        System.out.println("Current Stats");
        double throughput = ((currSize / 1000) / timer.getElapsedTime());

        System.out.println("Total transfer time : " + timer.getElapsedTime() / 1000 + " Seconds");
        System.out.println("Average throughput is:" + throughput + " Mbps");
        System.out.println("Total KB Received: " + currSize / 1000);

        dividerLine();
        blankLine();
        blankLine();
    }

    public static void dividerLine() 
    {
        System.out.println("*********************************");
    }

    public static void blankLine() 
    {
        System.out.println();
    }

}