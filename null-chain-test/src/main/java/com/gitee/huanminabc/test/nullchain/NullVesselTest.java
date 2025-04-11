package com.gitee.huanminabc.test.nullchain;

import com.gitee.huanminabc.nullchain.vessel.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class NullVesselTest {

    @Test
    public void tes1t() {
        NullList<String> nullArrayList = NullList.newArrayList();
        NullMap<String, Object> nullMap = NullMap.newHashMap();
        NullQuery<String> linkedList = NullQuery.newLinkedBlockingQueue();
        NullDeque<String> deque = NullDeque.newLinkedList();
        NullSet<String> set = NullSet.newHashSet();
    }

    @Test
    public void tes13t() {
        NullList<String> nullArrayList = new NullSuperList<>(new ArrayList<>());
        NullMap<String, Object> nullMap = new NullSuperMap<>(new HashMap<>());
        NullQuery<String> linkedList = new NullSuperQuery<>(new LinkedBlockingQueue<>());
        NullDeque<String> deque = new NullSuperDeque<>(new LinkedList<>());
        NullSet<String> set = new NullSuperSet<>(new HashSet<>());
    }


    @Test
    public void test() {

        NullList<String> nullArrayList = NullList.newArrayList();
        nullArrayList.add("test");
        nullArrayList.add("test2");
        nullArrayList.add("test3");
        nullArrayList.add(null);
        nullArrayList.add("");
        nullArrayList.add(" ");

        System.out.println(nullArrayList.size());

        for (String s : nullArrayList) {
            System.out.println(s);
        }

        nullArrayList.stream().forEach(System.out::println);

        NullList<String> collect = nullArrayList.stream().collect(NullCollectors.toCollection(NullList::newArrayList));
        collect.add("1231");

        NullList<String> collect2 = nullArrayList.stream().collect(NullCollectors.toList());
        collect2.add("1231");
    }
}
