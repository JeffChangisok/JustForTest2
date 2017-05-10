package com.example.administrator.justfortest2.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/4/23.
 */

public class Province extends DataSupport {
    private int id;
    private String provinceName;
    private int provinceICode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceICode() {
        return provinceICode;
    }

    public void setProvinceICode(int provinceICode) {
        this.provinceICode = provinceICode;
    }
}
