package com.almostreliable.unified;

import java.util.ArrayList;
import java.util.List;

public class ClientTagUpdateEvent {

    private static final List<Invoker> INVOKERS = new ArrayList<>();

    public static void register(Invoker invoker) {
        INVOKERS.add(invoker);
    }

    public static void invoke() {
        for (Invoker invoker : INVOKERS) {
            invoker.invoke();
        }
    }

    public interface Invoker {
        void invoke();
    }
}
