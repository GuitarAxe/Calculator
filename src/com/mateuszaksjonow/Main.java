package com.mateuszaksjonow;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws ArrayIndexOutOfBoundsException {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        Map<String, String> map = new HashMap<>();

        while (!exit) {
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                input = "";
            }

            //Menu
            if (input.equals("/exit")) {
                exit = true;
                System.out.println("Bye!");
                break;
            }else if (input.equals("/help")) {
                help();
            }else if (input.equals("")) {
                System.out.println("No input");
            }else if (isMatched("/.*", input)) {
                System.out.println("Unknown command");
            }else if (isMatched("[a-zA-Z]+", input)) {
                if (map.containsKey(input)) {
                    System.out.println(map.get(input));
                } else {
                    System.out.println("Unknown variable");
                }
            }
            else {

                String num = input;
                String[] numbers;

                //Convert String to numbers and signs separated by single space, removing redundant pluses, then splitting to String array
                num = replaceString("\\++", num, " + ");
                num = replaceString("\\-", num, " - ");
                num = replaceString("\\*", num, " * ");
                num = replaceString("/", num, " / ");
                num = replaceString("=", num, " = ");
                num = replaceString("\\(", num, " ( ");
                num = replaceString("\\)", num, " ) ");
                num = replaceString(" +", num," ");
                numbers = num.split(" ");

                //Check for BigIntegers
                boolean isBig = false;
                for (String s : numbers) {
                    if (map.containsKey(s)) {
                        String[] temp = map.get(s).split("");
                        if (temp.length > 9) isBig = true;
                    }
                    String[] temp = s.split("");
                    if (temp.length > 9) isBig = true;
                }

                if (numbers != null) {
                    if (numbers.length == 3) {
                        //Add variable to map
                        if ((isMatched("\\w+", numbers[0])) && (numbers[1].equals("=")) && isMatched("\\w+", numbers[2])) {
                            addVariable(numbers, map);
                        }else {
                            //Preparing to calculate and calculating
                            numbers = removeRedundantOperators(numbers);
                            Deque<String> stringDeque = new ArrayDeque<>();
                            stringDeque = infixToPostFix(numbers);
                            if (isBig) {
                                calculateBigInteger(stringDeque, map);
                            }else {
                                calculate(stringDeque, map);
                            }
                        }
                    } else {
                        numbers = removeRedundantOperators(numbers);
                        Deque<String> stringDeque = new ArrayDeque<>();
                        stringDeque = infixToPostFix(numbers);
                        if (isBig) {
                            calculateBigInteger(stringDeque, map);
                        }else {
                            calculate(stringDeque, map);
                        }
                    }
                }
            }
        }
    }

    //Check for matching Strings
    private static boolean isMatched(String regex, String input) {
        if (input == null) {
//            System.out.println("Null: isMatched");
            return false;
        }else {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            return matcher.matches();
        }
    }

    //Replacing matching Strings
    private static String replaceString(String regex, String input, String newString) {
        String replacedString = "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        replacedString = matcher.replaceAll(newString);
        return replacedString;
    }

    public static String[] removeRedundantOperators(String[] numbers) {
        boolean firstMinus = false;
        boolean invalidExpression = false;
        Deque<String> numbersQueue = new ArrayDeque<>();
        for (int i = 0; i < numbers.length; i++) {
            if (!invalidExpression) {
                if (numbersQueue.size() == 0) {
                    //Check if input is one number and if its negative
                    if (isMatched("\\d+", numbers[i]) || isMatched("[a-zA-Z]+", numbers[i]) || isMatched("[\\(]", numbers[i])) {
                        if (firstMinus) {
                            numbers[i] = "-" + numbers[i];
                            numbersQueue.offerLast(numbers[i]);
                            firstMinus = false;
                        }else {
                            numbersQueue.offerLast(numbers[i]);
                        }
                    }else if (isMatched("\\-", numbers[i])) {
                        firstMinus = !firstMinus;
                    }else if (isMatched("[\\*/=]", numbers[i])) {
                        System.out.println("Invalid expression");
                        invalidExpression = true;
                    }
                }else {
                    //Check for invalid input
                    if (isMatched("\\d+", numbers[i]) || isMatched("[a-zA-Z]+", numbers[i])) {
                        if (isMatched("\\d+", numbers[i - 1]) || isMatched("[a-zA-Z]+", numbers[i - 1])) {
                            System.out.println("Invalid expression");
                            invalidExpression = true;
                        }else {
                            numbersQueue.offerLast(numbers[i]);
                        }
                    }else if (isMatched("[\\+\\-\\*/]", numbers[i])) {
                        if (isMatched("\\+", numbers[i])) {
                            if (isMatched("[\\-\\*/]", numbers[i - 1])) {
                                System.out.println("Invalid expression");
                                invalidExpression = true;
                            }else {
                                numbersQueue.offerLast(numbers[i]);
                            }
                        }//Removing redundant minuses
                        else if (isMatched("\\-", numbers[i])) {
                            if (isMatched("[\\*/]", numbers[i - 1])) {
                                System.out.println("Invalid expression");
                                invalidExpression = true;
                            }else if (isMatched("\\-", numbersQueue.peekLast())) {
                                numbersQueue.pollLast();
                                numbersQueue.offerLast("+");
                            }else if (isMatched("\\+", numbersQueue.peekLast())) {
                                numbersQueue.pollLast();
                                numbersQueue.offerLast("-");
                            } else {
                                numbersQueue.offerLast(numbers[i]);
                            }
                        }else if (isMatched("\\*", numbers[i])) {
                            if (isMatched("[\\+\\-\\*/]", numbers[i - 1])) {
                                System.out.println("Invalid expression");
                                invalidExpression = true;
                            }else {
                                numbersQueue.offerLast(numbers[i]);
                            }
                        }else if (isMatched("/", numbers[i])) {
                            if (isMatched("[\\+\\-\\*/]", numbers[i - 1])) {
                                System.out.println("Invalid expression");
                                invalidExpression = true;
                            }else {
                                numbersQueue.offerLast(numbers[i]);
                            }
                        }
                    }else if (isMatched("[\\(\\)]", numbers [i])) {
                        numbersQueue.offerLast(numbers[i]);
                    }
                }
            }
        }
        //If valid returning valid notation ready to be converted to postifix
        int size = numbersQueue.size();
        String[] finalNumbers = new String[size];
        if (!invalidExpression) {
            for (int i = 0; i < size; i++) {
                finalNumbers[i] = numbersQueue.pollFirst();
            }
            return finalNumbers;
        }else {
            return null;
        }
    }

    public static boolean addVariable(String[] numbers, Map<String, String> map) {
        for (int i = 0; i < numbers.length; i++) {
            try {
                //Check if uses adds valid variable
                if ((isMatched("\\w+", numbers[i])) && (numbers[i + 1].equals("=")) && isMatched("\\w+", numbers[i + 2])) {
                    if (numbers.length == 3) {
                        if (isMatched("[a-zA-Z]+", numbers[i]) && isMatched("[a-zA-Z]+", numbers[i + 2])) {
                            if (map.containsKey(numbers[i + 2])) {
                                map.put(numbers[i], map.get(numbers[i + 2]));
                            }else {
                                System.out.println("Unknown variable");
                                return false;
                            }
                            i = numbers.length;
                        }else if (isMatched("[a-zA-Z]+", numbers[i]) && isMatched("\\d+", numbers[i + 2])) {
                            map.put(numbers[i], numbers[i + 2]);
                            i = numbers.length;
                        }else if (isMatched("^\\d+", numbers[i + 2])) {
                            System.out.println("Invalid identifier");
                            return false;
                        }else {
                            System.out.println("Invalid assignment");
                            return false;
                        }
                    }else {
                        System.out.println("Invalid assignment");
                        return false;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid expression");
                return false;
            }catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println("ArrayIndexOutOfBoundsException addVariable");
            }
        }
        return true;
    }

    private static int calculate(Deque<String> queue, Map<String, String> map) {
        Deque<Integer> stack = new ArrayDeque<>();
        int result = 0;
        try {
            //if there is only one number, print and return
            if (queue.size() == 1) {
                System.out.println(queue.pollLast());
                return 0;
            }
            //Check for variables and changing from name to number
            while (!queue.isEmpty()) {
                int temp = 0;
                if (isMatched("[a-zA-Z]+", queue.peekFirst())) {
                    if (map.containsKey(queue.peekFirst())) {
                        stack.offerLast(Integer.parseInt(map.get(queue.peekFirst())));
                        queue.pollFirst();
                    }
                    //Add number to the stack
                }else if (isMatched("\\d+", queue.peekFirst())) {
                    stack.offerLast(Integer.parseInt(queue.pollFirst()));
                    //Make operation depending on the operator
                }else if (isMatched("[\\+\\-\\*/]", queue.peekFirst())) {
                    int temp2 = stack.pollLast();
                    if (isMatched("\\+", queue.peekFirst())) {
                        temp = stack.pollLast() + temp2;
                    }else if (isMatched("-", queue.peekFirst())) {
                        temp = stack.pollLast() - temp2;
                    }else if (isMatched("\\*", queue.peekFirst())) {
                        temp = stack.pollLast() * temp2;
                    }else if (isMatched("/", queue.peekFirst())) {
                        temp = stack.pollLast() / temp2;
                    }
                    stack.offerLast(temp);
                    queue.pollFirst();
                }
            }
            result = stack.pollLast();
            System.out.println(result);
            return result;
        }catch (NullPointerException e) {
//            System.out.println("NullPointerException: calculate");
        }catch (NumberFormatException a) {
//            System.out.println("NumberFormatException: calculate");
        }
        return 0;
    }

    private static BigInteger calculateBigInteger(Deque<String> queue, Map<String, String> map) {
        Deque<BigInteger> stack = new ArrayDeque<>();
        BigInteger result = BigInteger.ZERO;
        try {
            //if there is only one number, print and return
            if (queue.size() == 1) {
                System.out.println(queue.pollLast());
                return BigInteger.ZERO;
            }
            //Check for variables and changing from name to number
            while (!queue.isEmpty()) {
                BigInteger bigTemp = BigInteger.ZERO;
                if (isMatched("[a-zA-Z]+", queue.peekFirst())) {
                    if (map.containsKey(queue.peekFirst())) {
                        bigTemp = new BigInteger(map.get(queue.peekFirst()));
                        stack.offerLast(bigTemp);
                        queue.pollFirst();
                    }
                    //Add number to the stack
                }else if (isMatched("\\d+", queue.peekFirst())) {
                    bigTemp = new BigInteger(queue.pollFirst());
                    stack.offerLast(bigTemp);
                    //Make operation depending on the operator
                }else if (isMatched("[\\+\\-\\*/]", queue.peekFirst())) {
                    BigInteger bigTemp2 = BigInteger.ZERO;
                    bigTemp2 = bigTemp2.add(stack.pollLast());
                    bigTemp = bigTemp.add(stack.pollLast());
                    if (isMatched("\\+", queue.peekFirst())) {
                        bigTemp = bigTemp.add(bigTemp2);
                    }else if (isMatched("-", queue.peekFirst())) {
                        bigTemp = bigTemp.subtract(bigTemp2);
                    }else if (isMatched("\\*", queue.peekFirst())) {
                        bigTemp = bigTemp.multiply(bigTemp2);
                    }else if (isMatched("/", queue.peekFirst())) {
                        bigTemp = bigTemp.divide(bigTemp2);
                    }
                    stack.offerLast(bigTemp);
                    queue.pollFirst();
                }
            }
            result = result.add(stack.pollLast());
            System.out.println(result);
            return result;
        }catch (NullPointerException e) {
//            System.out.println("NullPointerException: calculate");
        }catch (NumberFormatException a) {
//            System.out.println("NumberFormatException: calculate");
        }
        return BigInteger.ZERO;
    }

    public static void help() {
        System.out.println("Write + to add\n" +
                "Write - to subtract. Two - equals to +\n" +
                "Write /exit to terminate program.\n" +
                "Write * to multiply and / to divide\\n" +
                "You can use parenthesis\\n" +
                "You can add variables (In variable name only letters are permitted)");
    }

    private static int precedence(String str){
        switch (str){
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "^":
                return 3;
        }
        return -1;
    }

    static Deque<String> infixToPostFix(String[] expression){

        try {
            Deque<String> result = new ArrayDeque<>();
            Deque<String> stack = new ArrayDeque<>();
            boolean parenthesis = false;
            for (int i = 0; i < expression.length; i++) {
                String str = expression[i];

                //check if String is operator
                if(precedence(str)>0){
                    while(!stack.isEmpty() && precedence(stack.peekLast())>=precedence(str)){
                        result.offerLast(stack.pollLast());
                    }
                    stack.offerLast(str);
                }else if(str.equals(")")){
                    parenthesis = !parenthesis;
                    String x = stack.pollLast();
                    while(!x.equals("(")){
                        result.offerLast(x);
                        x = stack.pollLast();
                    }
                }else if(str.equals("(")){
                    parenthesis = !parenthesis;
                    stack.offerLast(str);
                }else if (str.equals("=")) {
                    return null;
                }else{
                    //character is neither operator nor (
                    result.offerLast(str);
                }
            }
            for (int i = 0; i <=stack.size() ; i++) {
                result.offerLast(stack.pollLast());
            }
            if (parenthesis) {
                System.out.println("Invalid expression");
            }else {
                return result;
            }
        }catch (NullPointerException e) {
            System.out.println("Invalid expression");
        }
        return null;
    }
}
