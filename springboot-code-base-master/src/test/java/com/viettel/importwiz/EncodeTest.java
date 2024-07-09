package com.viettel.importwiz;

import com.viettel.security.PassTranformer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.IsoFields;

public class EncodeTest {

    @Test
    public void encode() {
        String key = "thinhvd4";
        PassTranformer.setInputKey(key);
//        String dec = PassTranformer.decrypt("ec1f33d9f3668feb583a29ba462d469b1d5f12a21aef49346b14c1ebe7f7b1f1ab017d9b6569444305083fa1de32e9e3");
//
//        System.out.println(Paths.get("E:\\import-wiz\\upload" + File.separator + "thinh"));
        System.out.println(Integer.parseInt("2"));
    }

    @Test
    public void convertDateToWeek() {
        LocalDate date = LocalDate.parse("2023-11-06");
        int isoWeek = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int isoYear = date.get(IsoFields.WEEK_BASED_YEAR);
        int year = date.getYear();

        String rs = String.format("%d-%02d", isoYear, isoWeek);
        System.out.println(rs);
    }

}



