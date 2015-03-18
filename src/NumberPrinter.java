/**
 * Created by mx on 30/1/15.
 */

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class NumberPrinter extends TimerTask {
    double n;

    public NumberPrinter(double toPrint){
        n = toPrint;
    }

    public void run(){
        System.out.println(n);
    }

    public static void main(String[] args){
        long startTime = Long.parseLong(args[1]);
        long interval = Long.parseLong(args[2]);

        Timer timer = new Timer();
        timer.schedule(new NumberPrinter(Double.parseDouble(args[0])), startTime*1000, interval*1000);

        Scanner sc = new Scanner(System.in);
        while (true) {
            String quitCommand = sc.nextLine();
            if (quitCommand.equals("q")) {
                timer.cancel();
                System.exit(1);
            }
        }

    }

}
