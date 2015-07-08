import java.util.Scanner;

class Program {
    protected final void out(String out, Object ... args) {
        System.out.println(String.format(out, args));
    }
    protected final Scanner in(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner
    }
    protected final int readint(String prompt) {
        return in(prompt).nextInt();
    }
    protected final boolean readbool(String prompt) {
        return in(prompt).nextBoolean();
    }
    protected final char readchar(String prompt) {
        return in(prompt).next('.').charAt(0);
    }
}