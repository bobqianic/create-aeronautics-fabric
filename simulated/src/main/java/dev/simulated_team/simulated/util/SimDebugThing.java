package dev.simulated_team.simulated.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Vector3d;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimDebugThing {

    static int steps = 0;
    static int limit = 0;
    static boolean active = false;
    static HashMap<String,List<String>> things = new HashMap<>();
    static NumberFormat nf = NumberFormat.getInstance();
    static ServerLevel level;
    public static void start(final int limit, final ServerLevel level)
    {
        SimDebugThing.limit = limit;
        SimDebugThing.level = level;
        sendMessage("DebugThing started with " + limit + " steps");
        active = true;
        steps=0;
        nf.setMaximumFractionDigits(7);
        if(nf instanceof final DecimalFormat decimalFormat)
            decimalFormat.setNegativePrefix("-");

    }
    public static void step()
    {
        if(active) {
            if (steps >= limit) {
                stop();
                return;
            }
            steps++;
        }
    }
    public static void stop()
    {
        active = false;
        output();
        things.clear();

    }
    public static void abort()
    {
        sendMessage("DebugThing aborted");
        things.clear();
        active = false;
    }
    public static void push(final String label, final Vector3d v)
    {
        push(label,"("+formatNumber(v.x)+","+formatNumber(v.y)+","+formatNumber(v.z)+")");
    }
    public static void push(final String label, final double d)
    {
        push(label,formatNumber(d));
    }
    public static void push(final String label, final int i)
    {
        push(label,Integer.toString(i));
    }
    static void push(final String label, final String s)
    {
        if(active) {
            if (!things.containsKey(label)) {
                things.put(label,new ArrayList<>());
            } else {
                things.get(label).add(s);
            }
        }
    }
    static String formatNumber(final double d)
    {
        return nf.format(d).replace(',','.').replace('?','-');
    }
    static void output()
    {
        if(things.isEmpty())
        {
            sendMessage("DebugThing finished after " + steps + " steps. No result available");
        }else {
            System.out.println("DebugThing output:");
            for (final Map.Entry<String, List<String>> entry : things.entrySet()) {
                final List<String> thing = entry.getValue();
                final StringBuilder s = new StringBuilder(entry.getKey() + "=[");
                for (int j = 0; j < thing.size(); j++) {
                    s.append(thing.get(j));
                    if (j < thing.size() - 1)
                        s.append(",");
                }
                s.append("]");
                System.out.println(s);
            }
            sendMessage("DebugThing finished after " + steps + " steps. Results dumped into system log");
        }
    }
    static void sendMessage(final String s)
    {
        for (final ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal(s));
        }
    }
}
