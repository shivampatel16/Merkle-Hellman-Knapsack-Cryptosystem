/**
 * @Author: Shivam Patel
 * @Andrew_ID: shpatel
 * @Course: 95-771 Data Structures and Algorithms for Information Processing
 * @Assignment_Number: Project 1 - Part 2
 */

import java.math.BigInteger;
import java.util.Scanner;
import edu.cmu.andrew.shpatel.SinglyLinkedList;
import edu.colorado.nodes.ObjectNode;

public class Main {

    // Source to convert characters to binary values
    // https://stackoverflow.com/questions/40539115/how-to-convert-a-character-into-a-8-bit-binary-number-and-returns-the-result-as
    // Function to convert characters to binary values
    public static String valueToBinary(char character)
    {
        char []ret = {'0','0','0','0','0','0','0','0'};
        int v = (int)character & 0xFFFF;
        for (int idx = 0; v > 0; v >>= 1, idx++)
            if ((v & 1) == 1)
                ret[7-idx] = '1';
        return new String(ret);
    }

    public static void main(String[] argv) throws Exception {
        BigInteger W_i; // For storing BigInteger values for SinglyLinkedList W
        BigInteger seven = BigInteger.valueOf(7);

        BigInteger big_sum = BigInteger.valueOf(0);
        // big_sum stores the sum of all BigIntegers in W

        SinglyLinkedList W = new SinglyLinkedList();
        // W stores a random (not so random!) super increasing sequence of 640 values

        SinglyLinkedList B = new SinglyLinkedList();
        // B is our public key here

        // Creation of W - our super increasing sequence of 640 values
        for (int i = 0; i < 640; i++) {
            W_i = new BigInteger(seven.pow(i).toByteArray());
            big_sum = big_sum.add(W_i);
            W.addAtEndNode(W_i);
        }

        // Choosing a random BigInteger q such that it is greater than big_sum
        BigInteger q = big_sum.add(BigInteger.valueOf(50));

        boolean coPrimeFound = false;
        BigInteger coPrimeFinder = BigInteger.valueOf(2);
        BigInteger r = null; // r is the co-prime of q

        // Finding r, the co-prime of q
        while (!coPrimeFound) {
            if (q.gcd(coPrimeFinder).equals(BigInteger.valueOf(1))) {
                r = coPrimeFinder;
                coPrimeFound = true;
            }
            coPrimeFinder = coPrimeFinder.add(BigInteger.valueOf(1));
        }

        // Creating our public key B using r, W_i and q
        // (W, q, r) forms our private key
        W.reset();
        BigInteger B_i;
        while (W.hasNext())
        {
            W_i = (BigInteger) W.next();

            B_i = new BigInteger(W_i.multiply(r).mod(q).toByteArray());

            B.addAtEndNode(B_i);
        }

        Scanner s = new Scanner(System.in);
        System.out.print("Enter a string and I will encrypt it as single large integer: ");

        // Get a string from the user to encrypt
        String user_input = s.nextLine();
        System.out.println("Clear text: " + user_input);

        // Calculate the length of the user's string
        int user_input_length = user_input.length();
        System.out.println("Number of clear text bytes = " + user_input_length);

        // Request the user to enter the string again if it is greater than 80 characters in length
        while (user_input_length > 80) {
            System.out.print("The string entered is too long. Please enter a shorter string: ");

            user_input = s.nextLine();
            System.out.println("Clear text: " + user_input);

            user_input_length = user_input.length();
            System.out.println("Number of clear text bytes = " + user_input_length);
        }

        BigInteger encrypted_message = BigInteger.valueOf(0);
        String user_message_in_bits = "";

        // Convert user's message into binary where each character in the message is represented using
        // 8 bits of 1s or 0s
        for (int i = 0; i < user_input_length; i++) {
            char user_input_char = user_input.charAt(i);
            user_message_in_bits = user_message_in_bits + (valueToBinary(user_input_char));
        }

        B.reset();
        int user_bits_i = 0;
        int user_message_in_bits_length = user_message_in_bits.length();

        // Find the encrypted message
        // Let m (user_message_in_bits) be an n-bit message consisting of bits
        // m1, m2, m3....mn with m1 the highest order bit.
        // Select each b_i from B for which m_i is nonzero, and add them together.
        // Adding them together forms our encrypted message (encrypted_message here, and c on Wikipedia)
        while (B.hasNext() && user_bits_i < user_message_in_bits_length)
        {
            if (user_message_in_bits.charAt(user_bits_i) == '1') {
                encrypted_message = encrypted_message.add((BigInteger) B.next());
            }
            else {
                B.next();
            }
            user_bits_i++;
        }

        System.out.println(user_input + " is encrypted as \n" + encrypted_message);

        // r_dash = r^-1 (mod q)
        BigInteger r_dash = r.modInverse(q);

        // c_dash = (c * r_dash) mod q
        BigInteger c_dash = encrypted_message.multiply(r_dash).mod(q);

        // m_str will store the decrypted message in binary form, which will be later converted to alphanumeric
        String m_str = "";

        // Creating a copy of W as W_copy and reversing it to travel W in the reverse order
        SinglyLinkedList W_copy = W;
        ObjectNode W_copy_reverse_head = W_copy.reverse(W_copy.getHead());

        // Creating c_dash_remaining to initially store c_dash (to later perform the subset sum problem using the greedy approach)
        BigInteger c_dash_remaining = c_dash;

        // Decrypting the message into binary form.
        // Use a greedy algorithm for subset sum problem to find the subset of a
        // superincreasing sequence W which sums to c_dash, in polynomial time.
        while(W_copy_reverse_head != null) {
            BigInteger prospective_W_i = (BigInteger) W_copy_reverse_head.getData();

            if (prospective_W_i.equals(prospective_W_i.min(c_dash_remaining))) {
                m_str = "1" + m_str;
                c_dash_remaining = c_dash_remaining.subtract(prospective_W_i);
            }
            else {
                m_str = "0" + m_str;
            }

            W_copy_reverse_head = W_copy_reverse_head.getLink();
        }

        // decrypted_user_message will store user's message in its actual form, that is in characters
        String decrypted_user_message = "";

        // Source for converting 8 bits binary to characters
        // https://stackoverflow.com/questions/8634527/converting-binary-data-to-characters-in-java

        // Convert the binary m_str into decrypted_user_message, by combining chunk of 8-bits binary
        // and converting them to characters.
        for (int i = 0; i < m_str.length()/8; i++) {
            String substring = m_str.substring(8 * i, (i + 1) * 8);
            if (!substring.equals("00000000")) {
                int a = Integer.parseInt(substring, 2);
                decrypted_user_message += (char) (a);
            }
        }

        // Printing the decrypted message
        System.out.println("Result of decryption: " + decrypted_user_message);
    }
}