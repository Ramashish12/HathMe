package code.utils;

import com.squareup.picasso.Target;

public class GlobalData {

    public static String pageFromFavorite = ""; //1=OnClick of Market from BottomMenu,2= ClickOf ViewMore from HomeFragment
    public static String paymentFor = "2"; //1=Repay Credit else normal payment//Currently not in use as repaying amount from wallet. It will be used when repay credit from bank or upi
    public static double buySellCharges = 0.75;

    public static String userId = "";

    public static Target mTarget;
    public static boolean isChatOpen=false;

}
