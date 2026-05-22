package org.example;

import org.example.client.ClientMain;
import org.example.server.ServerMain;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("1. Start Server");
        System.out.println("2. Start Client");
        System.out.println("Choose");

        Scanner sc = new Scanner(System.in);
        String choice = sc.nextLine();

        if ("1".equals(choice)) {
            ServerMain.main(args);
        }else if ("2".equals(choice)) {
            ClientMain.main(args);
        }

    }
}