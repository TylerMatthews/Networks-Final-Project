//****************************************************************************
// 
// Tyler Matthews U#09879383, Navin Ramkishun U#
// 11/21/2017
// Simple Class used to print the current statistics for the file transfer
//
//***************************************************************************
class Statistics
{

    public Statistics() 
    {
    }

    //
    public static void printStats(int currSize, int prevSize, StartTime timer) 
    {
        blankLine();
        blankLine();
        dividerLine();
        System.out.println("Current Stats");

        double throughput = ((currSize / 1000) / timer.getElapsedTime());

        System.out.println("Total transfer time : " + timer.getElapsedTime() / 1000 + " Seconds");
        System.out.println("Average throughput is:" + throughput + "Mbps");
        System.out.println("Total KB Received" + currSize / 1000);

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