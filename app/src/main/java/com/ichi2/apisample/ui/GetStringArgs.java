package com.ichi2.apisample.ui;

public class GetStringArgs {
    private final int resId;
    private final Object[] formatArgs;

    public GetStringArgs(int resId, Object... formatArgs) {
        this.resId = resId;
        this.formatArgs = formatArgs;
    }

    public int getResId() {
        return resId;
    }

    public Object[] getFormatArgs() {
        return formatArgs;
    }
}
