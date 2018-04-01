package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.HashMap;

/**
 * @author chupanw
 */
public class HashMapExample {
    @VConditional
    public static boolean A = true;

    private HashMap<String, Integer> map = new HashMap<>();
    private HashMap<Item, Integer> map2 = new HashMap<>();

    void putGetUnderTrue() {
        map.clear();
        map.put("Hello", 1);
        System.out.println(map.get("Hello"));   // True<1>
    }

    void putUnderAGetUnderTrue() {
        map.clear();
        if (A)
            map.put("Hello", 1);
        else
            map.put("World", 2);
        System.out.println(map.get("Hello"));   // <A, 1, null>
    }

    void putUnderAGetUnderTrue2() {
        map.clear();
        if (A)
            map.put("Hello", 1);
        else
            map.put("Hello", 2);
        System.out.println(map.get("Hello"));   // <A, 1, 2>
    }

    void putUnderTrueGetUnderA() {
        map.clear();
        map.put("Hello", 1);
        map.put("World", 2);
        if (A)
            System.out.println(map.get("Hello"));   // A<1>
        else
            System.out.println(map.get("World"));   // !A<2>
    }

    void testNoClash() {
        map2.clear();
        if (A)
            map2.put(new Item(2), 1);
        else
            map2.put(new Item(3), 2);
        System.out.println(map2.get(new Item(1)));  // <A, null, 2>
    }

    void testClash() {
        map2.clear();
        if (A)
            map2.put(new Item(2), 1);
        else
            map2.put(new Item(4), 2);
        System.out.println(map2.get(new Item(6)));  // <A, 1, 2>
    }

    public static void main(String[] args) {
        HashMapExample foo = new HashMapExample();
        foo.putGetUnderTrue();
        foo.putUnderAGetUnderTrue();
        foo.putUnderAGetUnderTrue2();
        foo.putUnderTrueGetUnderA();
        foo.testNoClash();
        foo.testClash();
    }
}

class Item {
    int value;

    Item(int v) {
        value = v;
    }

    @Override
    public boolean equals(Object obj) {
        return (value % 2) == ((Item) obj).value % 2;
    }

    @Override
    public int hashCode() {
        return value % 2;
    }
}
