import java.util.Scanner;

/**
 * @author limincai
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String arg = sc.next();
        System.out.println("接收到的参数为：" + arg);
        sc.close();
    }
}
