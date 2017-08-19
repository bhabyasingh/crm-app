package com.saurasin.sbtentertainment.backend.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by saurasin on 5/31/17.
 */

public class Constants {

    public static SimpleDateFormat SIMPLE_DATA_FMT = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ssZ");
    public static Map<String, String> oneMonthIndexMap = new HashMap<>();
    public static Map<String, String> oneYearIndexMap = new HashMap<>();
    public static Map<String, String> twoMonthIndexMap = new HashMap<>();
    public static Map<String, String> twoYearIndexMap = new HashMap<>();
    
    static {
        oneMonthIndexMap.put("1", "202");
        oneMonthIndexMap.put("2", "204");
        oneMonthIndexMap.put("3", "206");
        oneMonthIndexMap.put("4", "208");
        oneMonthIndexMap.put("5", "210");
        oneMonthIndexMap.put("6", "212");
        oneMonthIndexMap.put("7", "214");
        oneMonthIndexMap.put("8", "216");
        oneMonthIndexMap.put("9", "218");
        oneMonthIndexMap.put("10", "220");
        oneMonthIndexMap.put("11", "222");
        oneMonthIndexMap.put("12", "224");
        
        twoMonthIndexMap.put("1", "354");
        twoMonthIndexMap.put("2", "356");
        twoMonthIndexMap.put("3", "358");
        twoMonthIndexMap.put("4", "360");
        twoMonthIndexMap.put("5", "362");
        twoMonthIndexMap.put("6", "364");
        twoMonthIndexMap.put("7", "366");
        twoMonthIndexMap.put("8", "368");
        twoMonthIndexMap.put("9", "370");
        twoMonthIndexMap.put("10", "372");
        twoMonthIndexMap.put("11", "374");
        twoMonthIndexMap.put("12", "376");

        oneYearIndexMap.put("2018", "416");
        oneYearIndexMap.put("2017", "418");
        oneYearIndexMap.put("2016", "226");
        oneYearIndexMap.put("2015", "228");
        oneYearIndexMap.put("2014", "230");
        oneYearIndexMap.put("2013", "232");
        oneYearIndexMap.put("2012", "234");
        oneYearIndexMap.put("2011", "236");
        oneYearIndexMap.put("2010", "238");
        oneYearIndexMap.put("2009", "240");
        oneYearIndexMap.put("2008", "242");
        oneYearIndexMap.put("2007", "244");
        oneYearIndexMap.put("2006", "248");
        oneYearIndexMap.put("2005", "246");
        
        twoYearIndexMap.put("2018", "414");
        twoYearIndexMap.put("2017", "412");
        twoYearIndexMap.put("2016", "410");
        twoYearIndexMap.put("2015", "408");
        twoYearIndexMap.put("2014", "406");
        twoYearIndexMap.put("2013", "404");
        twoYearIndexMap.put("2012", "402");
        twoYearIndexMap.put("2011", "400");
        twoYearIndexMap.put("2010", "398");
        twoYearIndexMap.put("2009", "396");
        twoYearIndexMap.put("2008", "394");
        twoYearIndexMap.put("2007", "392");
        twoYearIndexMap.put("2006", "390");
        twoYearIndexMap.put("2005", "388");
        twoYearIndexMap.put("2004", "386");
        twoYearIndexMap.put("2000", "378");
    }
    
}
