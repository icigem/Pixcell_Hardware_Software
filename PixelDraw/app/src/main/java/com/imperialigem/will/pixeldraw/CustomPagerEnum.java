package com.imperialigem.will.pixeldraw;

public enum CustomPagerEnum {

    INVADER1(R.string.invader1, R.color.invader1, R.array.invader1, R.drawable.invader01),
    INVADER2(R.string.invader2, R.color.invader2, R.array.invader2, R.drawable.invader02),
    INVADER3(R.string.invader3, R.color.invader3, R.array.invader3, R.drawable.invader03),
    INVADER4(R.string.invader4, R.color.invader4, R.array.invader4, R.drawable.invader04),
    INVADER5(R.string.invader5, R.color.invader5, R.array.invader5, R.drawable.invader05),
    INVADER6(R.string.invader6, R.color.invader6, R.array.invader6, R.drawable.invader06),
    INVADER7(R.string.invader7, R.color.invader7, R.array.invader7, R.drawable.invader07),
    INVADER8(R.string.invader8, R.color.invader8, R.array.invader8, R.drawable.invader08),
    SKULLXB1(R.string.skullxb1, R.color.skullxb1, R.array.skullxb1, R.drawable.skullxb01),
    HELMET1(R.string.helmet1, R.color.helmet1, R.array.helmet1, R.drawable.helmet01),
    WALKER1(R.string.walker1, R.color.walker1, R.array.walker1, R.drawable.walker01),
    JELLY1(R.string.jelly1, R.color.jelly1, R.array.jelly1, R.drawable.jelly01),
    TANK1(R.string.tank1, R.color.tank1, R.array.tank1, R.drawable.tank01),
    BAT1(R.string.bat1, R.color.bat1, R.array.bat1, R.drawable.bat01);

    private int mTitleResId;
    private int mColorResId;
    private int mPatternResId;
    private int mDrawableResId;

    CustomPagerEnum(int titleResId, int colorResId, int patternResId, int drawableResId) {
        mTitleResId = titleResId;
        mDrawableResId = drawableResId;
        mPatternResId = patternResId;
        mColorResId = colorResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getColorResId() {
        return mColorResId;
    }

    public int getPatternResId() {
        return mPatternResId;
    }

    public int getDrawableResId() {
        return mDrawableResId;
    }

}
