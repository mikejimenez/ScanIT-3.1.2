package alpha.com.ScanIT.interfaces;

/**
 * Created by Post on 10/31/2015.
 * Formatting Tracking Numbers
 */

public class FormatString {

    public static String ScanFromFedEXG;
    public static String ScanFromFedEXGManual;
    public static String ScanFromFedEX;
    public static String ScanFromFedEXManual;
    public static String ScanFromUPS;
    public static String ScanFromManualUPS;

    /**
     * Formatting
     */

    public static String ManualFedEXG(String Number) {
        /**
         * 21 Characters
         * Fedex Ground Manual
         * Format: XXXXXXX XXXXXXX XXXXXXX
         */
        ScanFromFedEXGManual = Number.substring(0, 7) + " " + Number.substring(7, 14) + " " + Number.substring(14, 21);
        return ScanFromFedEXGManual;
    }

    public static String FedEXGO(String Number) {
        /**
         * 21 Characters
         * Fedex Ground Old
         * Format: XXXXXXX XXXXXXX XXXXXXX
         */
        ScanFromFedEXG = Number.substring(0, 7) + " " + Number.substring(7, 14) + " " + Number.substring(14, 22);
        return ScanFromFedEXG;
    }

    public static String FedEXE(String Number) {
        /**
         * 34 Characters
         * Fedex Express
         * Format: XXXX XXXX XXXX
         */
        ScanFromFedEX = Number.substring(0, 4) + " " + Number.substring(4, 8) + " " + Number.substring(8, 12);
        return ScanFromFedEX;
    }

    public static String ManualFedEXE(String Number) {
        /**
         * 12 Characters
         * Fedex Express Manual
         * Format: XXXX XXXX XXXX
         */
        ScanFromFedEXManual = Number.substring(0, 4) + "" + Number.substring(4, 9) + "" + Number.substring(9, 14);
        return ScanFromFedEXManual;
    }

    public static String UPS(String Number) {
        /**
         * 18 Characters
         * UPS
         * Format: XX XXX XXX XX XXXX XXXX
         */
        ScanFromUPS = Number.substring(0, 2) + " " + Number.substring(2, 5) + " " + Number.substring(5, 8) + " " + Number.substring(8, 10) + " " + Number.substring(10, 14) + " " + Number.substring(14, 18);
        return ScanFromUPS;
    }

    public static String ManualUPS(String Number) {
        /**
         * 18 Characters
         * UPS Manual
         * Format: XX XXX XXX XX XXXX XXXX
         */
        ScanFromManualUPS = Number.substring(0, 2) + " " + Number.substring(3, 6) + " " + Number.substring(7, 10) + " " + Number.substring(11, 13) + " " + Number.substring(14, 18) + " " + Number.substring(19, 23);
        return ScanFromManualUPS;
    }


}
