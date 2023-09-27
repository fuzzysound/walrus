package com.fuzzysound.walrus.common;

import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class NumberUtils {
    public static List<BigInteger> getRangeList(BigInteger from, BigInteger to) {
        List<BigInteger> rangeList = new ArrayList<>();
        for (BigInteger bi = from; bi.compareTo(to) < 0; bi = bi.add(BigInteger.ONE)) {
            rangeList.add(bi);
        }
        return rangeList;
    }

    public static String weiToEther(String wei) {
        return Convert.fromWei(wei, Convert.Unit.ETHER).toString();
    }
}
