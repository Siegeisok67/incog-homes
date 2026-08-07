package com.incogdev.homes.util;

public class DangerResult {

    public enum Reason {
        NONE,
        LAVA_OR_FIRE,
        NO_GROUND,
        HOSTILE_MOB_NEARBY,
        RECENT_COMBAT
    }

    private final boolean safe;
    private final Reason reason;

    private DangerResult(boolean safe, Reason reason) {
        this.safe = safe;
        this.reason = reason;
    }

    public static DangerResult safe() {
        return new DangerResult(true, Reason.NONE);
    }

    public static DangerResult unsafe(Reason reason) {
        return new DangerResult(false, reason);
    }

    public boolean isSafe() {
        return safe;
    }

    public Reason getReason() {
        return reason;
    }
}
