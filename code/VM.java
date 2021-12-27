package CN;

import ithakimodem.Modem;
import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VM {
    static Modem IthakiModem = new Modem(80000);
    String echoCode = "E6269\r";
    String imageCode = "M1158\r";
    String gpsCode = "P2939";
    String gpsFormat = "R=1063999\r";
    String errImageCode = "G2052\r";
    String ackCode = "Q8076\r";
    String nackCode = "R1463\r";
    public static void main(String[] args) {
         VM VirtualModem = new VM();
        IthakiModem.setTimeout(1000);
        IthakiModem.open("ithaki");
        for (; ; ) {
            try {
                int k = IthakiModem.read();
                if (k == -1) break;
            } catch (Exception x) {
                System.out.println("Could not connect to Ithaki! :(");
                break;
            }
        }
        System.out.println("Connection to Ithaki succeeded! :D");
        new graphics();
        VirtualModem.computerNetworksProject(IthakiModem);
        IthakiModem.close();
        System.out.println("The requested work has been completed");
        System.exit(0);
    }

    public void computerNetworksProject(Modem modem) {
        String message = "";
        while(!graphics.checkboxCompleted) {
            //System.out.print("");
            try
            { Thread.sleep(10); }
            catch(InterruptedException ex)
            { Thread.currentThread().interrupt();}
            if (graphics.isEchoSelected) {
                if (echoRequest(echoCode))
                    message += "\"echoResponse.txt\" has been successfully saved in your project files!\n\n";
                else message += "Your echo request has failed due to wrong password!\n\n";
            }
            if (graphics.isImageSelected) {
                if (imageRequest(imageCode, "imageWithoutError.jpeg"))
                    message += "\"imageWithoutError.jpeg\" has been successfully saved in your project files!\n\n";
                else message += "Your image without error request has failed due to wrong password!\n\n";
            }
            if (graphics.isErrImageSelected) {
                if (imageRequest(errImageCode, "imageWithError.jpeg"))
                    message += "\"imageWithError.jpeg\" has been successfully saved in your project files!\n\n";
                else message += "Your image with error request has failed due to wrong password!\n\n";
            }
            if(graphics.isGpsSelected) {
                if (gpsRequest(gpsCode, gpsFormat))
                    message += "\"GPSimage.jpeg\" has been successfully saved in your project files!\n\n";
                else message += "Your GPS image request has failed due to wrong password!\n\n";
            }
            if(graphics.isAckNackSelected){
                if (automaticRepeatRequest(ackCode, nackCode)){
                    message += "\"ackNackResponse.txt\" and \"ackNackTime.txt\" has been successfully saved" +
                            " in your project files!\n\n";
                }
                else message += "Your ARQ request has failed due to wrong password!\n\n";
            }
        }
        JOptionPane.showMessageDialog( null,
                message,"Your files' progress: ", JOptionPane.PLAIN_MESSAGE);
    }

    public boolean echoRequest(String echoCode){
        int maxPacketNum = 10000; // upper limit of number of packets
        int packetsCounter = 0;
        int maxReceivingTime = 900*1000; // upper limit of the time that has passed in milliseconds
        long startingTime = 0; // to count the time response of the packet
        long endingTime = 0; // to count the time response of the packet
        long responseTimePassed = 0;
        long firstPacketTime = 0;
        boolean isPasswordCorrect = true;
        String receivedPacket = "";
        String stopString = "PSTOP";
        String[] receivedPacketParts;
        int receivedCharacterInPacket = 0;

        try{
            PrintWriter writeInFile =  new PrintWriter("echoResponse.txt", StandardCharsets.UTF_8);
            while(endingTime - firstPacketTime <= maxReceivingTime && packetsCounter <= maxPacketNum) {
                IthakiModem.write(echoCode.getBytes());
                packetsCounter++;
                receivedPacket = "";
                startingTime = System.currentTimeMillis();
                while(!receivedPacket.contains(stopString) && receivedCharacterInPacket != -1){
                    receivedCharacterInPacket = IthakiModem.read();
                    receivedPacket = receivedPacket + (char) receivedCharacterInPacket;
                }
                endingTime = System.currentTimeMillis();
                if (receivedCharacterInPacket == -1){
                    isPasswordCorrect = false;
                    break;
                }
                responseTimePassed = endingTime - startingTime;
                receivedPacketParts = receivedPacket.split(" ");
                writeInFile.println("Packet #" + packetsCounter + "\treceived on " + receivedPacketParts[1] + " at " +
                        receivedPacketParts[2] + " with a response time: " + responseTimePassed + "\tmilliseconds");
                if (packetsCounter == 1)
                    firstPacketTime = startingTime;
            }
                writeInFile.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        if(isPasswordCorrect)
            System.out.println("End of echo request.\n");
        else
            System.out.println("Your password: " + echoCode.substring(0,5) + "is wrong." +
                    " Log in to Ithaki to get your password\n");
        return isPasswordCorrect;
    }

    public boolean imageRequest (String imageCode, String imageName){
        boolean isPasswordCorrect = true;
        int lastImageByte = 0;
        int secondToLastImageByte = 0;
        IthakiModem.write(imageCode.getBytes());
        try {
            FileOutputStream imageToSave = new FileOutputStream(imageName);
            while((lastImageByte != 217 || secondToLastImageByte != 255) && lastImageByte != -1 ){
                secondToLastImageByte = lastImageByte;
                lastImageByte = IthakiModem.read();
                imageToSave.write((byte) lastImageByte);
            }
            if (lastImageByte == -1) {
                isPasswordCorrect = false;
            }
            imageToSave.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isPasswordCorrect)
            System.out.println("An image was saved! Go see it!\n");
        else
            System.out.println("Your password: " + imageCode.substring(0,5) + " is wrong." +
                    " Log in to Ithaki to get your password\n");
        if(isPasswordCorrect){
            final JFrame f = new JFrame(); //creates jframe f
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            ImageIcon image = new ImageIcon(imageName); //imports the image
            JLabel lbl = new JLabel(image); //puts the image into a jlabel
            f.getContentPane().add(lbl); //puts label inside the jframe
            f.setSize(image.getIconWidth(), image.getIconHeight()); //gets h and w of image and sets jframe to the size
            int x = (screenSize.width - f.getSize().width)/2; //These two lines are the dimensions
            int y = (screenSize.height - f.getSize().height)/2;//of the center of the screen
            f.setLocation(x, y);
            f.setVisible(true);
            f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
        return isPasswordCorrect;
    }

    public boolean gpsRequest(String gpsCode, String gpsFormat) {
        int lastReceivedGPSDataByte = 0;
        int pointsOnMap = 0;
        int timeOfPreviousReceivedPacket = -8; // Initialized at -8 so as the first point on the map gets accepted
        int timeOfCurrentReceivedPacket = 0;
        String GPSLineData = "";
        String[] receivedPacketInfo;
        String parameterT = "";
        String lastLineParameterT = "";
        String latitude = "";
        String longitude = "";
        boolean isPasswordCorrect = true;
        String tToAdd = "";
        IthakiModem.write((gpsCode + gpsFormat).getBytes());
        for (int receivingLines = 0; receivingLines <= 101; receivingLines++) {
            GPSLineData = "";
            lastReceivedGPSDataByte = 0;
            while (lastReceivedGPSDataByte != -1 && lastReceivedGPSDataByte != 10) {
                lastReceivedGPSDataByte = IthakiModem.read();
                GPSLineData = GPSLineData + (char) lastReceivedGPSDataByte;
            }
            if (Objects.equals(GPSLineData, "START ITHAKI GPS TRACKING\r\n") || pointsOnMap >= 9)
                continue;
            if (Objects.equals(GPSLineData, "STOP ITHAKI GPS TRACKING\r\n") || lastReceivedGPSDataByte == -1)
                break;
            receivedPacketInfo = GPSLineData.split(",");
            timeOfCurrentReceivedPacket = Integer.parseInt(receivedPacketInfo[1].substring(0, 6));
            if (timeOfCurrentReceivedPacket - timeOfPreviousReceivedPacket >= 8) {
                latitude = receivedPacketInfo[2].substring(0, 4) +
                        (int) (Integer.parseInt(receivedPacketInfo[2].substring(5, 9)) * 0.006);
                longitude = receivedPacketInfo[4].substring(1, 5) +
                        (int) (Integer.parseInt(receivedPacketInfo[4].substring(6, 10)) * 0.006);
                tToAdd = "T=" + longitude + latitude;
                if (Objects.equals(tToAdd, lastLineParameterT))
                    continue;
                timeOfPreviousReceivedPacket = timeOfCurrentReceivedPacket;
                parameterT = parameterT + tToAdd;
                lastLineParameterT = tToAdd;
                pointsOnMap++;
            }
        }
            System.out.println("Sending gps image request for " + pointsOnMap + " points");
            isPasswordCorrect = imageRequest(gpsCode + parameterT + "\r", "GPSimage.jpeg");
        System.out.println("End of GPS\n");
        return isPasswordCorrect;
    }

    public boolean automaticRepeatRequest(String ackCode, String nackCode){
        int maxPacketNum = 10000; // upper limit of number of packets
        int maxReceivingTime = 900*1000; // upper limit of the time that has passed in milliseconds
        int correctPacketsCounter = 0;
        long startingTime = 0;
        long endingTime = 0;
        long responseTimePassed = 0;
        long lastPacketTime = 0;
        long firstPacketTime = 0;
        boolean isPasswordCorrect = true;
        int receivedCharacterInPacket = 0;
        String receivedPacket = "";
        String stopString = "PSTOP";
        String[] receivedPacketParts ;
        long totalBits = 0;
        long errorBits = 0;
        boolean isCurrentPacketCorrect;
        String sequenceToCheck = "";
        int erroneousPackets = 0;
        List<Integer> ackNackResponse = new ArrayList(); //the lists were created in order not to lose time writing
        List<Long> ackNackTime = new ArrayList();        // the values in the files each time, but rather
        try{                                             // to write all the values at once
            PrintWriter writeInFileResponse =  new PrintWriter("ackNackResponse.txt", StandardCharsets.UTF_8);
            PrintWriter writeInFileTime =  new PrintWriter("ackNackTime.txt", StandardCharsets.UTF_8);
            firstPacketTime = System.currentTimeMillis();
            while(lastPacketTime - firstPacketTime <= maxReceivingTime && correctPacketsCounter < maxPacketNum){
                isCurrentPacketCorrect = false;
                erroneousPackets = 0;
                sequenceToCheck = "";
                List<String> erroneousSequences = new ArrayList<>();
                IthakiModem.write(ackCode.getBytes());
                startingTime = System.currentTimeMillis();
               while(!isCurrentPacketCorrect) {
                   receivedPacket = "";
                   while (!receivedPacket.contains(stopString) && receivedCharacterInPacket != -1) {
                       receivedCharacterInPacket = IthakiModem.read();
                       receivedPacket = receivedPacket + (char) receivedCharacterInPacket;
                   }
                   if (receivedCharacterInPacket == -1){
                       isPasswordCorrect = false;
                       break;
                   }
                   int XORcheck = 0;
                   receivedPacketParts = receivedPacket.split(" ");
                   for (int i = 1; i < receivedPacketParts[4].length() - 1; i++)
                       XORcheck ^= receivedPacketParts[4].charAt(i);
                   isCurrentPacketCorrect = (XORcheck == Integer.parseInt(receivedPacketParts[5]));
                   if (!isCurrentPacketCorrect) {
                       IthakiModem.write(nackCode.getBytes());
                       sequenceToCheck = receivedPacketParts[4];
                       erroneousSequences.add(sequenceToCheck);
                       erroneousPackets++;
                   }
               }
                endingTime = System.currentTimeMillis();
                if (!isPasswordCorrect)
                    break;
                responseTimePassed = endingTime - startingTime;
                ackNackTime.add((Long)responseTimePassed);
               for (String errSeq : erroneousSequences) {
                   for (int j=0; j<16; j++) {
                       //Count how many bits are different
                       errorBits += Integer.bitCount((sequenceToCheck.charAt(j) ^ errSeq.charAt(j)));
                   }
                   totalBits += errSeq.getBytes().length * 8;   //Count the total number of bits of the wrong sequences
               }
               totalBits += sequenceToCheck.getBytes().length * 8;  //Don't forget the bits of the correct one
               ackNackResponse.add((Integer)erroneousPackets);
               lastPacketTime = System.currentTimeMillis();
               correctPacketsCounter++;
            }
            float errorPercentage = 100 * ((float)errorBits / totalBits);  //Calculate the Bit Error Ratio
            System.out.println("ErrorBits Counted=" + errorBits + " TotalBits counted=" + totalBits +
                    ", the error percentage is: " + errorPercentage);
            for(int i = 0;i < ackNackTime.size(); i++)
                writeInFileTime.println(ackNackTime.get(i));
            for(int i = 0; i < ackNackResponse.size(); i++)
                writeInFileResponse.println(ackNackResponse.get(i));
            writeInFileResponse.close();
            writeInFileTime.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        if(isPasswordCorrect)
            System.out.println("The proccess had examined " + correctPacketsCounter + " correct packages.\n");
        else
            System.out.println("Your password: " + ackCode.substring(0,5) + " or "+  nackCode.substring(0,5) +
                    "is wrong. Log in to Ithaki to get your password\n");
        return isPasswordCorrect;
    }
}
