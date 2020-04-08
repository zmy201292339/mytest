package java8;

import java.util.ArrayList;
import java.util.List;

public class LambdaTest {
    public static void main(String[] args) {

        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");
        list.add("hello world");

        list.stream().forEach(s -> {
            System.out.println(s);
        });
    }
}
