package com.colorchains.colorchainsgl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

public class Chain {
    public String id;
    public List<Gem> elements = new ArrayList<>();
    public Integer count = 0;
    public List<Gem> chained = new ArrayList<>();
    public Boolean complete = false;
    public Integer checks = 0;
    public String entryPoint;
    public boolean loop;

    public Chain(String id, List<Gem> gemList, int i, boolean b) {
        this.id = id;
        this.elements = gemList;
        this.count = i;
        this.loop = b;
    }
}
